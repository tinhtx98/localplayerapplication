package com.tinhtx.localplayerapplication.core.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.database.entity.*
import com.tinhtx.localplayerapplication.data.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.model.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val musicRepository: MusicRepository
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = 1001

    companion object {
        const val TAG = "LibraryScanWorker"
        const val WORK_NAME = "library_scan_work"
        const val PERIODIC_WORK_NAME = "periodic_library_scan_work"
        
        // Input data keys
        const val KEY_FORCE_SCAN = "force_scan"
        const val KEY_SCAN_FOLDERS = "scan_folders"
        
        // Progress data keys
        const val KEY_PROGRESS = "progress"
        const val KEY_CURRENT_FILE = "current_file"
        const val KEY_TOTAL_FILES = "total_files"
        const val KEY_SCANNED_FILES = "scanned_files"
        
        // Result data keys
        const val KEY_SONGS_ADDED = "songs_added"
        const val KEY_SONGS_UPDATED = "songs_updated"
        const val KEY_SONGS_REMOVED = "songs_removed"
        const val KEY_ALBUMS_ADDED = "albums_added"
        const val KEY_ARTISTS_ADDED = "artists_added"
        const val KEY_SCAN_DURATION = "scan_duration"

        /**
         * Schedule one-time library scan
         */
        fun scheduleWork(
            context: Context,
            forceScan: Boolean = false,
            customFolders: List<String>? = null
        ): String {
            val inputData = Data.Builder()
                .putBoolean(KEY_FORCE_SCAN, forceScan)
                .apply {
                    customFolders?.let { folders ->
                        putStringArray(KEY_SCAN_FOLDERS, folders.toTypedArray())
                    }
                }
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LibraryScanWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)

            Timber.d("$TAG - One-time scan work scheduled, forceScan: $forceScan")
            return workRequest.id.toString()
        }

        /**
         * Schedule periodic library scan
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<LibraryScanWorker>(
                repeatInterval = 6, // 6 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 1, // 1 hour flex
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )

            Timber.d("$TAG - Periodic scan work scheduled")
        }

        /**
         * Cancel all scan work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Timber.d("$TAG - All scan work cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("$TAG - Starting library scan")
        
        val startTime = System.currentTimeMillis()
        var scanResult = ScanResult()

        return try {
            // Check permissions
            if (!hasRequiredPermissions()) {
                Timber.w("$TAG - Missing required permissions")
                return Result.failure(createErrorData("Missing storage permissions"))
            }

            // Show notification
            showScanStartNotification()

            // Set initial progress
            setProgress(createProgressData(0, "Preparing scan...", 0, 0))

            // Get scan parameters
            val forceScan = inputData.getBoolean(KEY_FORCE_SCAN, false)
            val customFolders = inputData.getStringArray(KEY_SCAN_FOLDERS)?.toList()

            // Perform scan
            scanResult = performLibraryScan(forceScan, customFolders)

            val scanDuration = System.currentTimeMillis() - startTime

            // Show completion notification
            showScanCompleteNotification(scanResult, scanDuration)

            // Create result data
            val resultData = Data.Builder()
                .putInt(KEY_SONGS_ADDED, scanResult.songsAdded)
                .putInt(KEY_SONGS_UPDATED, scanResult.songsUpdated)
                .putInt(KEY_SONGS_REMOVED, scanResult.songsRemoved)
                .putInt(KEY_ALBUMS_ADDED, scanResult.albumsAdded)
                .putInt(KEY_ARTISTS_ADDED, scanResult.artistsAdded)
                .putLong(KEY_SCAN_DURATION, scanDuration)
                .build()

            Timber.i("$TAG - Scan completed successfully in ${scanDuration}ms: $scanResult")
            Result.success(resultData)

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Scan failed")
            
            showScanErrorNotification(exception.message ?: "Unknown error")
            
            Result.failure(createErrorData(exception.message ?: "Scan failed"))
        } finally {
            // Clean up notification
            delay(5000) // Keep notification for 5 seconds
            notificationManager.cancel(notificationId)
        }
    }

    private suspend fun performLibraryScan(
        forceScan: Boolean,
        customFolders: List<String>?
    ): ScanResult {
        val scanResult = ScanResult()
        
        // Get current library state
        val existingSongs = if (forceScan) {
            emptyMap()
        } else {
            musicRepository.getAllSongs().associateBy { it.path }
        }

        // Scan audio files
        val audioFiles = scanAudioFiles(customFolders)
        val totalFiles = audioFiles.size

        setProgress(createProgressData(5, "Found $totalFiles audio files", 0, totalFiles))

        // Process files
        audioFiles.forEachIndexed { index, audioFile ->
            try {
                val progress = ((index + 1) * 90 / totalFiles) + 5 // 5-95%
                setProgress(createProgressData(
                    progress,
                    "Processing: ${audioFile.displayName}",
                    index + 1,
                    totalFiles
                ))

                processAudioFile(audioFile, existingSongs, scanResult)
                
                // Small delay to prevent overwhelming the system
                if (index % 50 == 0) {
                    delay(100)
                }

            } catch (exception: Exception) {
                Timber.w(exception, "$TAG - Error processing file: ${audioFile.data}")
            }
        }

        // Clean up removed files
        if (!forceScan) {
            setProgress(createProgressData(95, "Cleaning up removed files...", totalFiles, totalFiles))
            cleanupRemovedFiles(audioFiles, existingSongs, scanResult)
        }

        // Update statistics
        setProgress(createProgressData(98, "Updating statistics...", totalFiles, totalFiles))
        updateLibraryStatistics()

        setProgress(createProgressData(100, "Scan completed", totalFiles, totalFiles))

        return scanResult
    }

    private suspend fun scanAudioFiles(customFolders: List<String>?): List<AudioFileData> {
        val audioFiles = mutableListOf<AudioFileData>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.GENRE
        )

        val selection = StringBuilder().apply {
            append("${MediaStore.Audio.Media.IS_MUSIC} = 1")
            append(" AND ${MediaStore.Audio.Media.DURATION} > 10000") // > 10 seconds
            
            // Filter by custom folders if specified
            customFolders?.let { folders ->
                append(" AND (")
                folders.forEachIndexed { index, folder ->
                    if (index > 0) append(" OR ")
                    append("${MediaStore.Audio.Media.DATA} LIKE '$folder%'")
                }
                append(")")
            }
        }.toString()

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            audioFiles.addAll(processMediaStoreCursor(cursor))
        }

        return audioFiles
    }

    private fun processMediaStoreCursor(cursor: Cursor): List<AudioFileData> {
        val audioFiles = mutableListOf<AudioFileData>()
        
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
        val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
        val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

        while (cursor.moveToNext()) {
            val filePath = cursor.getString(dataColumn)
            
            // Verify file exists
            if (!File(filePath).exists()) continue

            val audioFile = AudioFileData(
                mediaStoreId = cursor.getLong(idColumn),
                path = filePath,
                title = cursor.getString(titleColumn) ?: File(filePath).nameWithoutExtension,
                artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                album = cursor.getString(albumColumn) ?: "Unknown Album",
                albumId = cursor.getLong(albumIdColumn),
                duration = cursor.getLong(durationColumn),
                size = cursor.getLong(sizeColumn),
                mimeType = cursor.getString(mimeTypeColumn) ?: "",
                dateAdded = cursor.getLong(dateAddedColumn) * 1000, // Convert to milliseconds
                dateModified = cursor.getLong(dateModifiedColumn) * 1000,
                year = cursor.getInt(yearColumn),
                trackNumber = cursor.getInt(trackColumn),
                displayName = File(filePath).name
            )
            
            audioFiles.add(audioFile)
        }

        return audioFiles
    }

    private suspend fun processAudioFile(
        audioFile: AudioFileData,
        existingSongs: Map<String, Song>,
        scanResult: ScanResult
    ) {
        val existingSong = existingSongs[audioFile.path]
        
        if (existingSong == null) {
            // New song - add to database
            val song = createSongFromAudioFile(audioFile)
            musicRepository.insertSong(song)
            scanResult.songsAdded++
            
            // Process artist and album
            processArtistAndAlbum(audioFile, scanResult)
            
        } else if (shouldUpdateSong(existingSong, audioFile)) {
            // Existing song - check if update needed
            val updatedSong = updateSongFromAudioFile(existingSong, audioFile)
            musicRepository.updateSong(updatedSong)
            scanResult.songsUpdated++
        }
    }

    private suspend fun processArtistAndAlbum(audioFile: AudioFileData, scanResult: ScanResult) {
        // Process artist
        val existingArtist = musicRepository.getArtistByName(audioFile.artist)
        if (existingArtist == null) {
            val artist = Artist(
                id = 0,
                name = audioFile.artist,
                albumCount = 0,
                songCount = 0
            )
            musicRepository.insertArtist(artist)
            scanResult.artistsAdded++
        }

        // Process album
        val existingAlbum = musicRepository.getAlbumByNameAndArtist(audioFile.album, audioFile.artist)
        if (existingAlbum == null) {
            val album = Album(
                id = 0,
                mediaStoreId = audioFile.albumId,
                name = audioFile.album,
                artist = audioFile.artist,
                artistId = 0, // Will be updated later
                year = audioFile.year,
                songCount = 0
            )
            musicRepository.insertAlbum(album)
            scanResult.albumsAdded++
        }
    }

    private suspend fun cleanupRemovedFiles(
        scannedFiles: List<AudioFileData>,
        existingSongs: Map<String, Song>,
        scanResult: ScanResult
    ) {
        val scannedPaths = scannedFiles.map { it.path }.toSet()
        
        existingSongs.keys.forEach { path ->
            if (path !in scannedPaths) {
                // File no longer exists - remove from database
                musicRepository.deleteSongByPath(path)
                scanResult.songsRemoved++
            }
        }
    }

    private suspend fun updateLibraryStatistics() {
        // Update artist song counts
        musicRepository.updateArtistStatistics()
        
        // Update album song counts
        musicRepository.updateAlbumStatistics()
        
        // Update genre statistics
        musicRepository.updateGenreStatistics()
    }

    private fun createSongFromAudioFile(audioFile: AudioFileData): Song {
        return Song(
            id = 0,
            mediaStoreId = audioFile.mediaStoreId,
            title = audioFile.title,
            artist = audioFile.artist,
            album = audioFile.album,
            albumId = audioFile.albumId,
            duration = audioFile.duration,
            path = audioFile.path,
            size = audioFile.size,
            mimeType = audioFile.mimeType,
            dateAdded = audioFile.dateAdded,
            dateModified = audioFile.dateModified,
            year = audioFile.year,
            trackNumber = audioFile.trackNumber,
            genre = null, // Will be populated later if available
            isFavorite = false,
            playCount = 0,
            lastPlayed = 0L
        )
    }

    private fun updateSongFromAudioFile(existingSong: Song, audioFile: AudioFileData): Song {
        return existingSong.copy(
            title = audioFile.title,
            artist = audioFile.artist,
            album = audioFile.album,
            albumId = audioFile.albumId,
            duration = audioFile.duration,
            size = audioFile.size,
            mimeType = audioFile.mimeType,
            dateModified = audioFile.dateModified,
            year = audioFile.year,
            trackNumber = audioFile.trackNumber
        )
    }

    private fun shouldUpdateSong(existingSong: Song, audioFile: AudioFileData): Boolean {
        return existingSong.dateModified < audioFile.dateModified ||
               existingSong.title != audioFile.title ||
               existingSong.artist != audioFile.artist ||
               existingSong.album != audioFile.album ||
               existingSong.duration != audioFile.duration
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showScanStartNotification() {
        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.SCAN_CHANNEL_ID)
            .setContentTitle("Scanning Music Library")
            .setContentText("Starting music library scan...")
            .setSmallIcon(R.drawable.ic_refresh)
            .setOngoing(true)
            .setProgress(100, 0, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showScanCompleteNotification(scanResult: ScanResult, duration: Long) {
        val durationText = "${duration / 1000}s"
        val resultText = buildString {
            if (scanResult.songsAdded > 0) append("${scanResult.songsAdded} added")
            if (scanResult.songsUpdated > 0) {
                if (isNotEmpty()) append(", ")
                append("${scanResult.songsUpdated} updated")
            }
            if (scanResult.songsRemoved > 0) {
                if (isNotEmpty()) append(", ")
                append("${scanResult.songsRemoved} removed")
            }
            if (isEmpty()) append("No changes")
        }

        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.SCAN_CHANNEL_ID)
            .setContentTitle("Library Scan Complete")
            .setContentText("$resultText in $durationText")
            .setSmallIcon(R.drawable.ic_check)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showScanErrorNotification(error: String) {
        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.ERROR_CHANNEL_ID)
            .setContentTitle("Library Scan Failed")
            .setContentText(error)
            .setSmallIcon(R.drawable.ic_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createProgressData(
        progress: Int,
        message: String,
        scannedFiles: Int,
        totalFiles: Int
    ): Data {
        return Data.Builder()
            .putInt(KEY_PROGRESS, progress)
            .putString(KEY_CURRENT_FILE, message)
            .putInt(KEY_SCANNED_FILES, scannedFiles)
            .putInt(KEY_TOTAL_FILES, totalFiles)
            .build()
    }

    private fun createErrorData(error: String): Data {
        return Data.Builder()
            .putString("error", error)
            .build()
    }

    private data class AudioFileData(
        val mediaStoreId: Long,
        val path: String,
        val title: String,
        val artist: String,
        val album: String,
        val albumId: Long,
        val duration: Long,
        val size: Long,
        val mimeType: String,
        val dateAdded: Long,
        val dateModified: Long,
        val year: Int,
        val trackNumber: Int,
        val displayName: String
    )

    private data class ScanResult(
        var songsAdded: Int = 0,
        var songsUpdated: Int = 0,
        var songsRemoved: Int = 0,
        var albumsAdded: Int = 0,
        var artistsAdded: Int = 0
    ) {
        override fun toString(): String {
            return "ScanResult(added=$songsAdded, updated=$songsUpdated, removed=$songsRemoved, " +
                   "albums=$albumsAdded, artists=$artistsAdded)"
        }
    }
}
