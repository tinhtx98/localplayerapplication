package com.tinhtx.localplayerapplication.data.repository

import android.content.Context
import com.tinhtx.localplayerapplication.data.local.media.AudioMetadataExtractor
import com.tinhtx.localplayerapplication.data.local.media.MediaScanner
import com.tinhtx.localplayerapplication.data.local.media.MediaStoreScanner
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MediaRepository using media scanning components
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val context: Context,
    private val mediaScanner: MediaScanner,
    private val mediaStoreScanner: MediaStoreScanner,
    private val audioMetadataExtractor: AudioMetadataExtractor
) : MediaRepository {
    
    // Media Scanning Operations - Mapped từ MediaScanner methods
    override suspend fun scanAllMusic(): List<Song> {
        return mediaScanner.scanCommonMusicDirectories()
    }
    
    override fun scanAllMusicWithProgress(): Flow<ScanProgress> {
        return mediaScanner.scanDirectoriesWithProgress(emptyList()).map { progress ->
            when (progress) {
                is com.tinhtx.localplayerapplication.data.local.media.ScanProgress.Started -> 
                    ScanProgress.Started
                is com.tinhtx.localplayerapplication.data.local.media.ScanProgress.Progress -> 
                    ScanProgress.Progress(
                        processed = progress.processedFiles,
                        total = progress.totalFiles,
                        currentFile = progress.currentSong.path
                    )
                is com.tinhtx.localplayerapplication.data.local.media.ScanProgress.Completed -> 
                    ScanProgress.Completed(
                        totalFound = progress.songs.size,
                        newSongs = progress.songs.size,
                        updatedSongs = 0
                    )
                is com.tinhtx.localplayerapplication.data.local.media.ScanProgress.Error -> 
                    ScanProgress.Error(
                        message = "Scan error",
                        exception = progress.exception
                    )
            }
        }
    }
    
    override suspend fun scanSpecificDirectories(directories: List<String>): List<Song> {
        return mediaScanner.scanDirectories(directories)
    }
    
    override suspend fun scanModifiedSongs(since: Long): List<Song> {
        return mediaStoreScanner.getModifiedSongs(since)
    }
    
    override suspend fun rescanLibrary(forceFullScan: Boolean) {
        // Would trigger library scan worker - placeholder
    }
    
    // MediaStore Operations - Mapped từ MediaStoreScanner methods
    override suspend fun getAllSongsFromMediaStore(): List<Song> {
        return mediaStoreScanner.getAllSongs()
    }
    
    override suspend fun getAllAlbumsFromMediaStore(): List<Album> {
        return mediaStoreScanner.getAllAlbums()
    }
    
    override suspend fun getAllArtistsFromMediaStore(): List<Artist> {
        return mediaStoreScanner.getAllArtists()
    }
    
    override suspend fun getSongFromMediaStore(mediaStoreId: Long): Song? {
        return mediaStoreScanner.getSongById(mediaStoreId)
    }
    
    override suspend fun searchSongsInMediaStore(query: String): List<Song> {
        return mediaStoreScanner.searchSongs(query)
    }
    
    override suspend fun getMediaStoreStatistics(): MediaStoreStatistics {
        val stats = mediaStoreScanner.getMediaStoreStatistics()
        return MediaStoreStatistics(
            totalSongs = stats.songCount,
            totalAlbums = stats.albumCount,
            totalArtists = stats.artistCount,
            totalDuration = stats.totalDuration,
            totalSize = stats.totalSize,
            averageBitrate = 0 // Not available in MediaStoreStatistics
        )
    }
    
    // Directory Management - Mapped từ MediaScanner methods
    override suspend fun getCommonMusicDirectories(): List<String> {
        return mediaScanner.findCommonMusicDirectories()
    }
    
    override suspend fun getAudioFilesInDirectory(directory: String): List<String> {
        val audioFormats = listOf("mp3", "wav", "flac", "m4a", "ogg")
        return mediaScanner.getAudioFilesInDirectory(directory, audioFormats).map { it.absolutePath }
    }
    
    override suspend fun validateAudioFile(filePath: String): Boolean {
        return audioMetadataExtractor.isValidAudioFile(filePath)
    }
    
    override suspend fun isDirectoryAccessible(directoryPath: String): Boolean {
        return try {
            val dir = java.io.File(directoryPath)
            dir.exists() && dir.canRead()
        } catch (e: Exception) {
            false
        }
    }
    
    // Metadata Operations - Mapped từ AudioMetadataExtractor methods
    override suspend fun extractMetadata(filePath: String): AudioMetadata? {
        val metadata = audioMetadataExtractor.extractMetadata(filePath)
        return metadata?.let {
            AudioMetadata(
                title = it.title,
                artist = it.artist,
                album = it.album,
                genre = it.genre,
                year = it.year,
                trackNumber = it.trackNumber,
                duration = it.duration,
                bitrate = it.bitrate,
                sampleRate = it.sampleRate,
                fileSize = 0L // Not available in extracted metadata
            )
        }
    }
    
    override suspend fun extractAlbumArt(filePath: String): ByteArray? {
        val bitmap = audioMetadataExtractor.extractAlbumArt(filePath)
        return bitmap?.let {
            val stream = java.io.ByteArrayOutputStream()
            it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
            stream.toByteArray()
        }
    }
    
    override suspend fun extractBatchMetadata(filePaths: List<String>): List<AudioMetadata> {
        val metadataList = audioMetadataExtractor.extractMetadataBatch(filePaths)
        return metadataList.map { metadata ->
            AudioMetadata(
                title = metadata.title,
                artist = metadata.artist,
                album = metadata.album,
                genre = metadata.genre,
                year = metadata.year,
                trackNumber = metadata.trackNumber,
                duration = metadata.duration,
                bitrate = metadata.bitrate,
                sampleRate = metadata.sampleRate,
                fileSize = 0L
            )
        }
    }
    
    override suspend fun updateSongMetadata(songId: Long, meta AudioMetadata) {
        // Would need to update song in database - placeholder
    }
    
    // Cache Operations - Basic implementations
    override suspend fun clearMetadataCache() {
        // Would clear metadata cache - placeholder
    }
    
    override suspend fun getCacheSize(): Long {
        return 0L // Placeholder
    }
    
    override suspend fun optimizeCache() {
        // Would optimize cache - placeholder
    }
    
    // Sync Operations - Basic implementations
    override suspend fun syncWithMediaStore(): SyncResult {
        return SyncResult(
            totalProcessed = 0,
            syncDurationMs = 0L
        )
    }
    
    override suspend fun detectDeletedFiles(): List<String> {
        return emptyList() // Placeholder
    }
    
    override suspend fun detectNewFiles(): List<String> {
        return emptyList() // Placeholder
    }
    
    override suspend fun resolveConflicts(conflicts: List<MediaConflict>): List<Song> {
        return emptyList() // Placeholder
    }
    
    // Statistics and Analytics - Basic implementations
    override suspend fun getLibraryStatistics(): LibraryStatistics {
        return LibraryStatistics(lastUpdated = System.currentTimeMillis())
    }
    
    override suspend fun getScanHistory(): List<ScanHistoryEntry> {
        return emptyList() // Placeholder
    }
    
    override suspend fun getLastScanTime(): Long {
        return 0L // Placeholder
    }
    
    override suspend fun updateLastScanTime(timestamp: Long) {
        // Would update scan time - placeholder
    }
}
