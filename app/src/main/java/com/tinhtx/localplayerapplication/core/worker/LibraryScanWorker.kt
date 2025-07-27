package com.tinhtx.localplayerapplication.core.worker

import android.content.Context
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.core.utils.PermissionUtils
import com.tinhtx.localplayerapplication.data.local.media.MediaStoreScanner
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker for scanning music library and updating database
 */
@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaStoreScanner: MediaStoreScanner
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "LibraryScanWorker"
        private const val WORK_NAME = AppConstants.LIBRARY_SCAN_WORK_NAME
        
        // Input data keys
        const val KEY_FORCE_FULL_SCAN = "force_full_scan"
        const val KEY_SCAN_SPECIFIC_FOLDERS = "scan_specific_folders"
        
        /**
         * Schedule periodic library scan
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<LibraryScanWorker>(
                AppConstants.SCAN_INTERVAL_HOURS.toLong(), TimeUnit.HOURS,
                30, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
                )
        }

        /**
         * Schedule immediate library scan
         */
        fun scheduleImmediateScan(context: Context, forceFullScan: Boolean = false) {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val inputData = Data.Builder()
                .putBoolean(KEY_FORCE_FULL_SCAN, forceFullScan)
                .build()

            val immediateRequest = OneTimeWorkRequestBuilder<LibraryScanWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_immediate",
                    ExistingWorkPolicy.REPLACE,
                    immediateRequest
                )
        }

        /**
         * Schedule scan for specific folders
         */
        fun scheduleFolderScan(context: Context, folders: List<String>) {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val inputData = Data.Builder()
                .putStringArray(KEY_SCAN_SPECIFIC_FOLDERS, folders.toTypedArray())
                .build()

            val folderScanRequest = OneTimeWorkRequestBuilder<LibraryScanWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_folder_scan",
                    ExistingWorkPolicy.REPLACE,
                    folderScanRequest
                )
        }

        /**
         * Cancel library scan work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("$TAG - Starting library scan")

            // Check permissions
            if (!hasRequiredPermissions()) {
                Timber.w("$TAG - Missing required permissions for library scan")
                return@withContext Result.failure()
            }

            // Check if auto scan is enabled
            val librarySettings = settingsRepository.getLibrarySettings()
            if (!librarySettings.autoScan && !isManualScan()) {
                Timber.d("$TAG - Auto scan disabled, skipping")
                return@withContext Result.success()
            }

            // Perform library scan
            val scanResults = performLibraryScan()
            
            // Update repository statistics
            updateRepositoryStatistics()
            
            // Log scan results
            logScanResults(scanResults)

            Timber.d("$TAG - Library scan completed successfully")
            Result.success()

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error during library scan")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Check if required permissions are granted
     */
    private fun hasRequiredPermissions(): Boolean {
        return PermissionUtils.hasStoragePermissions(context)
    }

    /**
     * Check if this is a manual scan (immediate or forced)
     */
    private fun isManualScan(): Boolean {
        return inputData.getBoolean(KEY_FORCE_FULL_SCAN, false) ||
               inputData.getStringArray(KEY_SCAN_SPECIFIC_FOLDERS) != null
    }

    /**
     * Perform comprehensive library scan
     */
    private suspend fun performLibraryScan(): ScanResults {
        val results = ScanResults()
        val startTime = System.currentTimeMillis()
        
        try {
            // Determine scan type
            val forceFullScan = inputData.getBoolean(KEY_FORCE_FULL_SCAN, false)
            val specificFolders = inputData.getStringArray(KEY_SCAN_SPECIFIC_FOLDERS)
            
            when {
                specificFolders != null -> {
                    Timber.d("$TAG - Performing folder-specific scan")
                    results.merge(scanSpecificFolders(specificFolders.toList()))
                }
                forceFullScan -> {
                    Timber.d("$TAG - Performing full library scan")
                    results.merge(performFullScan())
                }
                else -> {
                    Timber.d("$TAG - Performing incremental scan")
                    results.merge(performIncrementalScan())
                }
            }
            
            results.scanDurationMs = System.currentTimeMillis() - startTime
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in library scan operations")
            throw exception
        }
        
        return results
    }

    /**
     * Perform full library scan
     */
    private suspend fun performFullScan(): ScanResults {
        val results = ScanResults()
        
        try {
            // Get all songs from MediaStore
            val mediaStoreSongs = mediaStoreScanner.getAllSongs()
            Timber.d("$TAG - Found ${mediaStoreSongs.size} songs in MediaStore")
            
            // Process songs, albums, and artists
            val processedData = processScanData(mediaStoreSongs)
            
            // Update database
            updateDatabase(processedData, isFullScan = true)
            
            results.songsFound = mediaStoreSongs.size
            results.songsAdded = processedData.newSongs.size
            results.albumsAdded = processedData.newAlbums.size
            results.artistsAdded = processedData.newArtists.size
            results.songsRemoved = processedData.removedSongs.size
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in full scan")
            throw exception
        }
        
        return results
    }

    /**
     * Perform incremental scan (only changed files)
     */
    private suspend fun performIncrementalScan(): ScanResults {
        val results = ScanResults()
        
        try {
            // Get last scan time
            val lastScanTime = getLastScanTime()
            
            // Get only modified songs from MediaStore
            val modifiedSongs = mediaStoreScanner.getModifiedSongs(lastScanTime)
            Timber.d("$TAG - Found ${modifiedSongs.size} modified songs since last scan")
            
            if (modifiedSongs.isNotEmpty()) {
                // Process only modified data
                val processedData = processScanData(modifiedSongs)
                
                // Update database incrementally
                updateDatabase(processedData, isFullScan = false)
                
                results.songsFound = modifiedSongs.size
                results.songsAdded = processedData.newSongs.size
                results.songsUpdated = processedData.updatedSongs.size
                results.albumsAdded = processedData.newAlbums.size
                results.artistsAdded = processedData.newArtists.size
            }
            
            // Clean up deleted files
            val cleanupResults = cleanupDeletedFiles()
            results.songsRemoved = cleanupResults
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in incremental scan")
            throw exception
        }
        
        return results
    }

    /**
     * Scan specific folders
     */
    private suspend fun scanSpecificFolders(folders: List<String>): ScanResults {
        val results = ScanResults()
        
        try {
            val allSongs = mutableListOf<Song>()
            
            folders.forEach { folderPath ->
                Timber.d("$TAG - Scanning folder: $folderPath")
                val folderSongs = mediaStoreScanner.getSongsInFolder(folderPath)
                allSongs.addAll(folderSongs)
            }
            
            Timber.d("$TAG - Found ${allSongs.size} songs in specified folders")
            
            if (allSongs.isNotEmpty()) {
                val processedData = processScanData(allSongs)
                updateDatabase(processedData, isFullScan = false)
                
                results.songsFound = allSongs.size
                results.songsAdded = processedData.newSongs.size
                results.albumsAdded = processedData.newAlbums.size
                results.artistsAdded = processedData.newArtists.size
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error scanning specific folders")
            throw exception
        }
        
        return results
    }

    /**
     * Process scan data and organize into songs, albums, artists
     */
    private suspend fun processScanData(songs: List<Song>): ProcessedScanData {
        val data = ProcessedScanData()
        
        try {
            val librarySettings = settingsRepository.getLibrarySettings()
            
            // Filter songs based on settings
            val filteredSongs = songs.filter { song ->
                // Check minimum duration
                if (librarySettings.ignoreShortTracks && 
                    song.duration < librarySettings.minTrackDuration * 1000) {
                    return@filter false
                }
                
                // Check if file exists and is valid
                if (!MediaUtils.fileExists(song.path) || 
                    !MediaUtils.validateAudioFile(song.path)) {
                    return@filter false
                }
                
                true
            }
            
            Timber.d("$TAG - Filtered ${songs.size} to ${filteredSongs.size} valid songs")
            
            // Separate new and existing songs
            for (song in filteredSongs) {
                val existingSong = musicRepository.getSongByPath(song.path)
                if (existingSong == null) {
                    data.newSongs.add(song)
                } else if (hasContentChanged(existingSong, song)) {
                    data.updatedSongs.add(song)
                }
            }
            
            // Process albums and artists from all songs
            processAlbumsAndArtists(filteredSongs, data)
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error processing scan data")
            throw exception
        }
        
        return data
    }

    /**
     * Process albums and artists from songs
     */
    private suspend fun processAlbumsAndArtists(songs: List<Song>,  ProcessedScanData) {
        val albumMap = mutableMapOf<String, MutableList<Song>>()
        val artistMap = mutableMapOf<String, MutableList<Song>>()
        
        // Group songs by album and artist
        songs.forEach { song ->
            val albumKey = "${song.album}_${song.artist}"
            albumMap.getOrPut(albumKey) { mutableListOf() }.add(song)
            
            artistMap.getOrPut(song.artist) { mutableListOf() }.add(song)
        }
        
        // Process albums
        albumMap.forEach { (_, albumSongs) ->
            val firstSong = albumSongs.first()
            val existingAlbum = musicRepository.getAlbumByNameAndArtist(firstSong.album, firstSong.artist)
            
            if (existingAlbum == null) {
                val newAlbum = Album(
                    id = 0,
                    mediaStoreId = firstSong.albumId,
                    name = firstSong.album,
                    artist = firstSong.artist,
                    artistId = 0,
                    year = firstSong.year,
                    songCount = albumSongs.size,
                    artworkPath = null
                )
                data.newAlbums.add(newAlbum)
            }
        }
        
        // Process artists
        artistMap.forEach { (artistName, artistSongs) ->
            val existingArtist = musicRepository.getArtistByName(artistName)
            
            if (existingArtist == null) {
                val albums = artistSongs.map { it.album }.distinct()
                val newArtist = Artist(
                    id = 0,
                    name = artistName,
                    albumCount = albums.size,
                    songCount = artistSongs.size,
                    artworkPath = null
                )
                data.newArtists.add(newArtist)
            }
        }
    }

    /**
     * Check if song content has changed
     */
    private fun hasContentChanged(existing: Song, new: Song): Boolean {
        return existing.dateModified != new.dateModified ||
               existing.title != new.title ||
               existing.artist != new.artist ||
               existing.album != new.album ||
               existing.duration != new.duration
    }

    /**
     * Update database with processed data
     */
    private suspend fun updateDatabase( ProcessedScanData, isFullScan: Boolean) {
        try {
            // Insert new artists first
            data.newArtists.forEach { artist ->
                musicRepository.insertArtist(artist)
            }
            
            // Insert new albums
            data.newAlbums.forEach { album ->
                musicRepository.insertAlbum(album)
            }
            
            // Insert new songs
            data.newSongs.forEach { song ->
                musicRepository.insertSong(song)
            }
            
            // Update existing songs
            data.updatedSongs.forEach { song ->
                musicRepository.updateSong(song)
            }
            
            // For full scan, remove songs that no longer exist
            if (isFullScan) {
                data.removedSongs.forEach { songPath ->
                    musicRepository.deleteSongByPath(songPath)
                }
            }
            
            // Update last scan time
            updateLastScanTime()
            
            Timber.d("$TAG - Database update completed")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating database")
            throw exception
        }
    }

    /**
     * Clean up songs for files that no longer exist
     */
    private suspend fun cleanupDeletedFiles(): Int {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val deletedPaths = mutableListOf<String>()
            
            allSongs.forEach { song ->
                if (!MediaUtils.fileExists(song.path)) {
                    deletedPaths.add(song.path)
                }
            }
            
            deletedPaths.forEach { path ->
                musicRepository.deleteSongByPath(path)
            }
            
            Timber.d("$TAG - Cleaned up ${deletedPaths.size} deleted files")
            deletedPaths.size
            
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning up deleted files")
            0
        }
    }

    /**
     * Update repository statistics after scan
     */
    private suspend fun updateRepositoryStatistics() {
        try {
            musicRepository.updateArtistStatistics()
            musicRepository.updateAlbumStatistics()
            musicRepository.updateGenreStatistics()
            
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error updating repository statistics")
        }
    }

    /**
     * Get last scan time from settings
     */
    private suspend fun getLastScanTime(): Long {
        return try {
            val settings = settingsRepository.getLibrarySettings()
            // This would be stored in settings - placeholder
            0L
        } catch (exception: Exception) {
            0L
        }
    }

    /**
     * Update last scan time in settings
     */
    private suspend fun updateLastScanTime() {
        try {
            // This would update the last scan time in settings - placeholder
            Timber.d("$TAG - Updated last scan time")
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error updating last scan time")
        }
    }

    /**
     * Log scan results
     */
    private fun logScanResults(results: ScanResults) {
        val durationSec = results.scanDurationMs / 1000.0
        Timber.i("$TAG - Library scan completed in ${durationSec}s:")
        Timber.i("$TAG - Songs found: ${results.songsFound}")
        Timber.i("$TAG - Songs added: ${results.songsAdded}")
        Timber.i("$TAG - Songs updated: ${results.songsUpdated}")
        Timber.i("$TAG - Songs removed: ${results.songsRemoved}")
        Timber.i("$TAG - Albums added: ${results.albumsAdded}")
        Timber.i("$TAG - Artists added: ${results.artistsAdded}")
    }

    /**
     * Results of library scan operation
     */
    data class ScanResults(
        var songsFound: Int = 0,
        var songsAdded: Int = 0,
        var songsUpdated: Int = 0,
        var songsRemoved: Int = 0,
        var albumsAdded: Int = 0,
        var artistsAdded: Int = 0,
        var scanDurationMs: Long = 0L
    ) {
        fun merge(other: ScanResults) {
            songsFound += other.songsFound
            songsAdded += other.songsAdded
            songsUpdated += other.songsUpdated
            songsRemoved += other.songsRemoved
            albumsAdded += other.albumsAdded
            artistsAdded += other.artistsAdded
        }
    }

    /**
     * Processed scan data structure
     */
    data class ProcessedScanData(
        val newSongs: MutableList<Song> = mutableListOf(),
        val updatedSongs: MutableList<Song> = mutableListOf(),
        val removedSongs: MutableList<String> = mutableListOf(),
        val newAlbums: MutableList<Album> = mutableListOf(),
        val newArtists: MutableList<Artist> = mutableListOf()
    )
}
