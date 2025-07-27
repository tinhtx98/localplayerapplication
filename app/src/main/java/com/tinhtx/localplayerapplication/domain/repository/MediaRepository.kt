package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for media scanning and device operations (SIMPLIFIED)
 */
interface MediaRepository {
    
    // Basic scanning operations
    suspend fun scanAllMusic(): List<Song>
    suspend fun getAllSongsFromDevice(): List<Song>
    suspend fun scanSpecificDirectories(directories: List<String>): List<Song>
    suspend fun scanModifiedSongs(since: Long): List<Song>
    suspend fun getAllSongsFromMediaStore(): List<Song>
    suspend fun rescanLibrary(forceFullScan: Boolean = false): List<Song>
    suspend fun incrementalScan(): List<Song>
    suspend fun quickScan(): List<Song>
    
    // Scanning with progress updates
    fun scanAllMusicWithProgress(): Flow<ScanProgress>
    fun scanDirectoriesWithProgress(directories: List<String>): Flow<ScanProgress>
    suspend fun cancelScan(scanId: String): Boolean
    suspend fun pauseScan(scanId: String): Boolean
    suspend fun resumeScan(scanId: String): Boolean
    
    // File validation and metadata
    suspend fun validateAudioFile(path: String): Boolean
    suspend fun extractMetadata(filePath: String): Song?
    suspend fun validateFileIntegrity(filePath: String): Boolean
    suspend fun getFileInfo(filePath: String): FileInfo?
    suspend fun getSupportedFormats(): List<String>
    suspend fun isFormatSupported(format: String): Boolean
    
    // Directory management
    suspend fun getIncludedDirectories(): List<String>
    suspend fun getExcludedDirectories(): List<String>
    suspend fun setIncludedDirectories(directories: List<String>)
    suspend fun setExcludedDirectories(directories: List<String>)
    suspend fun addIncludedDirectory(directory: String)
    suspend fun removeIncludedDirectory(directory: String)
    suspend fun addExcludedDirectory(directory: String)
    suspend fun removeExcludedDirectory(directory: String)
    suspend fun isDirectoryAccessible(path: String): Boolean
    suspend fun getDirectoryContents(path: String): List<String>
    suspend fun getAudioFilesInDirectory(path: String): List<String>
    suspend fun getSubdirectories(path: String): List<String>
    
    // Cache and optimization
    suspend fun clearScanCache()
    suspend fun optimizeScanSettings()
    suspend fun preloadDirectoryStructure()
    suspend fun getEstimatedScanTime(): Long
    suspend fun getScanCacheSize(): Long
    suspend fun purgeScanCache(): Boolean
    
    // External storage
    suspend fun scanExternalStorage(): List<Song>
    suspend fun getExternalStorageDirectories(): List<String>
    suspend fun isExternalStorageAvailable(): Boolean
    suspend fun getStorageInfo(): StorageInfo
    suspend fun getAvailableStorage(): Long
    suspend fun getUsedStorage(): Long
    
    // Metadata extraction and caching
    suspend fun extractAlbumArt(filePath: String): ByteArray?
    suspend fun extractAllMetadata(filePath: String): SongMetadata?
    suspend fun updateMetadataCache(songs: List<Song>)
    suspend fun getMetadataFromCache(filePath: String): Song?
    suspend fun clearMetadataCache()
    suspend fun getMetadataCacheSize(): Long
    
    // Error handling and diagnostics
    suspend fun getScanErrors(): List<ScanError>
    suspend fun clearScanErrors()
    suspend fun reportScanIssue(issue: String, filePath: String)
    suspend fun getScanDiagnostics(): Map<String, Any>
    suspend fun runScanDiagnostics(): DiagnosticResult
    
    // Background operations
    suspend fun startBackgroundScan(): String // Returns scan ID
    suspend fun stopBackgroundScan(): Boolean
    suspend fun isBackgroundScanRunning(): Boolean
    suspend fun schedulePeriodicScan(intervalHours: Int): Boolean
    suspend fun cancelPeriodicScan(): Boolean
    
    // File system monitoring
    suspend fun startFileSystemWatcher(): Boolean
    suspend fun stopFileSystemWatcher(): Boolean
    suspend fun isFileSystemWatcherActive(): Boolean
    suspend fun getWatchedDirectories(): List<String>
    
    // Performance monitoring
    suspend fun getScanPerformanceMetrics(): ScanMetrics
    suspend fun resetPerformanceMetrics()
    suspend fun optimizeForDevice(): Boolean
    
    // Maintenance operations
    suspend fun validateScanConfiguration(): List<String>
    suspend fun repairScanConfiguration(): Boolean
    suspend fun resetScanConfiguration(): Boolean
    suspend fun migrateScanData(fromVersion: String): Boolean
}

/**
 * Data classes for media operations
 */
data class ScanProgress(
    val scanId: String,
    val phase: String,
    val currentFile: String? = null,
    val processedFiles: Int = 0,
    val totalFiles: Int = 0,
    val foundSongs: Int = 0,
    val errors: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val startTime: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (totalFiles > 0) (processedFiles.toFloat() / totalFiles) * 100f else 0f
}

data class FileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val format: String,
    val isAccessible: Boolean
)

data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long,
    val isWritable: Boolean,
    val isRemovable: Boolean
) {
    val freeSpacePercentage: Float
        get() = if (totalSpace > 0) (freeSpace.toFloat() / totalSpace) * 100f else 0f
}

data class SongMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val year: Int,
    val genre: String?,
    val trackNumber: Int,
    val albumArt: ByteArray? = null
)

data class ScanError(
    val filePath: String,
    val errorMessage: String,
    val errorType: String,
    val timestamp: Long
)

data class DiagnosticResult(
    val isHealthy: Boolean,
    val issues: List<String>,
    val recommendations: List<String>,
    val performance: Map<String, Any>
)

data class ScanMetrics(
    val totalScans: Int,
    val averageScanTime: Long,
    val filesPerSecond: Float,
    val errorRate: Float,
    val cacheHitRate: Float
)
