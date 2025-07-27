package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MediaRepository
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for scanning media library
 */
class ScanMediaLibraryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val musicRepository: MusicRepository
) {
    
    /**
     * Perform full library scan
     */
    suspend fun execute(): Result<ScanResult> {
        return try {
            val scannedSongs = mediaRepository.scanAllMusic()
            val result = processScanResults(scannedSongs)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Perform library scan with progress updates
     */
    fun executeWithProgress(): Flow<ScanProgressResult> {
        return mediaRepository.scanAllMusicWithProgress()
    }
    
    /**
     * Scan specific directories
     */
    suspend fun scanDirectories(directories: List<String>): Result<ScanResult> {
        return try {
            val scannedSongs = mediaRepository.scanSpecificDirectories(directories)
            val result = processScanResults(scannedSongs)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Incremental scan (only modified files)
     */
    suspend fun incrementalScan(since: Long): Result<ScanResult> {
        return try {
            val modifiedSongs = mediaRepository.scanModifiedSongs(since)
            val result = processScanResults(modifiedSongs, isIncremental = true)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Quick scan using MediaStore
     */
    suspend fun quickScan(): Result<ScanResult> {
        return try {
            val mediaStoreSongs = mediaRepository.getAllSongsFromMediaStore()
            val result = processScanResults(mediaStoreSongs, isQuickScan = true)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Rescan library (force full scan)
     */
    suspend fun rescanLibrary(): Result<ScanResult> {
        return try {
            // Clear existing data
            musicRepository.clearAllData()
            
            // Perform fresh scan
            mediaRepository.rescanLibrary(forceFullScan = true)
            val scannedSongs = mediaRepository.scanAllMusic()
            val result = processScanResults(scannedSongs, isFreshScan = true)
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get scan statistics
     */
    suspend fun getScanStatistics(): Result<LibraryScanStatistics> {
        return try {
            val libraryStats = mediaRepository.getLibraryStatistics()
            val mediaStoreStats = mediaRepository.getMediaStoreStatistics()
            val scanHistory = mediaRepository.getScanHistory()
            
            val statistics = LibraryScanStatistics(
                totalSongs = libraryStats.totalSongs,
                totalAlbums = libraryStats.totalAlbums,
                totalArtists = libraryStats.totalArtists,
                totalSize = libraryStats.totalSize,
                totalDuration = libraryStats.totalDuration,
                lastScanTime = mediaRepository.getLastScanTime(),
                mediaStoreSongs = mediaStoreStats.totalSongs,
                scanHistoryCount = scanHistory.size
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate scan results
     */
    suspend fun validateScanResults(songs: List<Song>): Result<ValidationResult> {
        return try {
            var validSongs = 0
            var invalidSongs = 0
            var duplicateSongs = 0
            val seenPaths = mutableSetOf<String>()
            val issues = mutableListOf<String>()
            
            songs.forEach { song ->
                // Check for duplicates
                if (seenPaths.contains(song.path)) {
                    duplicateSongs++
                    issues.add("Duplicate path: ${song.path}")
                } else {
                    seenPaths.add(song.path)
                }
                
                // Validate file
                if (mediaRepository.validateAudioFile(song.path)) {
                    validSongs++
                } else {
                    invalidSongs++
                    issues.add("Invalid file: ${song.path}")
                }
            }
            
            val validationResult = ValidationResult(
                totalScanned = songs.size,
                validSongs = validSongs,
                invalidSongs = invalidSongs,
                duplicateSongs = duplicateSongs,
                issues = issues
            )
            
            Result.success(validationResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    private suspend fun processScanResults(
        scannedSongs: List<Song>,
        isIncremental: Boolean = false,
        isQuickScan: Boolean = false,
        isFreshScan: Boolean = false
    ): ScanResult {
        var newSongs = 0
        var updatedSongs = 0
        var skippedSongs = 0
        
        scannedSongs.forEach { song ->
            val existingSong = musicRepository.getSongByPath(song.path)
            
            when {
                existingSong == null -> {
                    // New song
                    musicRepository.insertSong(song)
                    newSongs++
                }
                existingSong.dateModified < song.dateModified -> {
                    // Updated song
                    musicRepository.updateSong(song.copy(id = existingSong.id))
                    updatedSongs++
                }
                else -> {
                    // No changes
                    skippedSongs++
                }
            }
        }
        
        // Update statistics
        musicRepository.updateArtistStatistics()
        musicRepository.updateAlbumStatistics()
        
        return ScanResult(
            totalScanned = scannedSongs.size,
            newSongs = newSongs,
            updatedSongs = updatedSongs,
            skippedSongs = skippedSongs,
            scanType = when {
                isFreshScan -> ScanType.FRESH_SCAN
                isIncremental -> ScanType.INCREMENTAL_SCAN
                isQuickScan -> ScanType.QUICK_SCAN
                else -> ScanType.FULL_SCAN
            },
            scanDurationMs = 0L // Would be calculated in actual implementation
        )
    }
}

/**
 * Scan result data classes
 */
data class ScanResult(
    val totalScanned: Int,
    val newSongs: Int,
    val updatedSongs: Int,
    val skippedSongs: Int,
    val scanType: ScanType,
    val scanDurationMs: Long
) {
    val successRate: Float
        get() = if (totalScanned > 0) ((newSongs + updatedSongs).toFloat() / totalScanned) * 100f else 0f
}

data class ValidationResult(
    val totalScanned: Int,
    val validSongs: Int,
    val invalidSongs: Int,
    val duplicateSongs: Int,
    val issues: List<String>
) {
    val validationRate: Float
        get() = if (totalScanned > 0) (validSongs.toFloat() / totalScanned) * 100f else 0f
}

data class LibraryScanStatistics(
    val totalSongs: Int,
    val totalAlbums: Int,
    val totalArtists: Int,
    val totalSize: Long,
    val totalDuration: Long,
    val lastScanTime: Long,
    val mediaStoreSongs: Int,
    val scanHistoryCount: Int
) {
    val totalSizeMB: Double
        get() = totalSize / (1024.0 * 1024.0)
    
    val totalDurationHours: Double
        get() = totalDuration / (1000.0 * 60.0 * 60.0)
}

enum class ScanType {
    FULL_SCAN,
    INCREMENTAL_SCAN,
    QUICK_SCAN,
    FRESH_SCAN
}

typealias ScanProgressResult = com.tinhtx.localplayerapplication.domain.repository.ScanProgress
