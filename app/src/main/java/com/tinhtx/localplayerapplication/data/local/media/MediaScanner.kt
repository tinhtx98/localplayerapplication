package com.tinhtx.localplayerapplication.data.local.media

import android.content.Context
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans local storage for media files
 */
@Singleton
class MediaScanner @Inject constructor(
    private val context: Context,
    private val audioMetadataExtractor: AudioMetadataExtractor
) {
    
    companion object {
        private const val TAG = "MediaScanner"
        
        // Common music directories
        private val MUSIC_DIRECTORIES = listOf(
            "Music",
            "Download", 
            "Downloads",
            "music",
            "Audio",
            "Songs"
        )
        
        // Directories to exclude from scanning
        private val EXCLUDED_DIRECTORIES = listOf(
            "Android",
            "android_secure",
            "cache",
            "temp",
            "tmp",
            ".thumbnails",
            ".android_secure",
            "LOST.DIR",
            "System Volume Information",
            "$RECYCLE.BIN"
        )
    }
    
    /**
     * Scan for audio files in specified directories
     */
    suspend fun scanDirectories(directories: List<String>): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        directories.forEach { directory ->
            try {
                val dirFile = File(directory)
                if (dirFile.exists() && dirFile.canRead()) {
                    val dirSongs = scanDirectory(dirFile)
                    songs.addAll(dirSongs)
                    Timber.d("$TAG - Found ${dirSongs.size} songs in $directory")
                }
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error scanning directory: $directory")
            }
        }
        
        Timber.i("$TAG - Total songs found: ${songs.size}")
        songs
    }
    
    /**
     * Scan single directory for audio files
     */
    suspend fun scanDirectory(directory: File): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            if (!directory.exists() || !directory.canRead()) {
                return@withContext songs
            }
            
            if (isExcludedDirectory(directory)) {
                return@withContext songs
            }
            
            directory.listFiles()?.forEach { file ->
                when {
                    file.isDirectory -> {
                        // Recursively scan subdirectories
                        val subDirSongs = scanDirectory(file)
                        songs.addAll(subDirSongs)
                    }
                    file.isFile && isAudioFile(file) -> {
                        // Process audio file
                        extractSongFromFile(file)?.let { song ->
                            songs.add(song)
                        }
                    }
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error scanning directory: ${directory.absolutePath}")
        }
        
        songs
    }
    
    /**
     * Scan with progress flow
     */
    fun scanDirectoriesWithProgress(directories: List<String>): Flow<ScanProgress> = flow {
        var totalFiles = 0
        var processedFiles = 0
        val songs = mutableListOf<Song>()
        
        // First pass: count total files
        directories.forEach { directory ->
            val dirFile = File(directory)
            if (dirFile.exists() && dirFile.canRead()) {
                totalFiles += countAudioFiles(dirFile)
            }
        }
        
        emit(ScanProgress.Started(totalFiles))
        
        // Second pass: process files
        directories.forEach { directory ->
            val dirFile = File(directory)
            if (dirFile.exists() && dirFile.canRead()) {
                scanDirectoryWithProgress(dirFile) { song ->
                    songs.add(song)
                    processedFiles++
                    emit(ScanProgress.Progress(processedFiles, totalFiles, song))
                }
            }
        }
        
        emit(ScanProgress.Completed(songs, processedFiles))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Scan common music directories
     */
    suspend fun scanCommonMusicDirectories(): List<Song> = withContext(Dispatchers.IO) {
        val musicDirs = findCommonMusicDirectories()
        scanDirectories(musicDirs)
    }
    
    /**
     * Find common music directories on device
     */
    suspend fun findCommonMusicDirectories(): List<String> = withContext(Dispatchers.IO) {
        val directories = mutableListOf<String>()
        
        // Primary external storage
        val primaryStorage = android.os.Environment.getExternalStorageDirectory()
        MUSIC_DIRECTORIES.forEach { dirName ->
            val musicDir = File(primaryStorage, dirName)
            if (musicDir.exists() && musicDir.canRead()) {
                directories.add(musicDir.absolutePath)
            }
        }
        
        // Secondary storage (SD cards, etc.)
        try {
            val secondaryStorages = getSecondaryStorageDirectories()
            secondaryStorages.forEach { storage ->
                MUSIC_DIRECTORIES.forEach { dirName ->
                    val musicDir = File(storage, dirName)
                    if (musicDir.exists() && musicDir.canRead()) {
                        directories.add(musicDir.absolutePath)
                    }
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error finding secondary storage")
        }
        
        Timber.d("$TAG - Found music directories: $directories")
        directories
    }
    
    /**
     * Get specific file types in directory
     */
    suspend fun getAudioFilesInDirectory(directory: String, fileTypes: List<String>): List<File> = withContext(Dispatchers.IO) {
        val files = mutableListOf<File>()
        val dirFile = File(directory)
        
        if (dirFile.exists() && dirFile.canRead()) {
            dirFile.walkTopDown()
                .filter { file ->
                    file.isFile && 
                    fileTypes.any { type -> 
                        file.extension.lowercase() == type.lowercase() 
                    }
                }
                .forEach { file ->
                    files.add(file)
                }
        }
        
        files
    }
    
    /**
     * Validate and clean scan results
     */
    suspend fun validateScanResults(songs: List<Song>): List<Song> = withContext(Dispatchers.IO) {
        val validSongs = mutableListOf<Song>()
        
        songs.forEach { song ->
            try {
                // Check if file exists
                if (!File(song.path).exists()) {
                    return@forEach
                }
                
                // Validate audio file
                if (!audioMetadataExtractor.isValidAudioFile(song.path)) {
                    return@forEach
                }
                
                // Check minimum duration if specified
                if (song.duration < 1000) { // Less than 1 second
                    return@forEach
                }
                
                validSongs.add(song)
            } catch (exception: Exception) {
                Timber.w(exception, "$TAG - Error validating song: ${song.path}")
            }
        }
        
        Timber.d("$TAG - Validated ${validSongs.size}/${songs.size} songs")
        validSongs
    }
    
    // Private helper methods
    private suspend fun scanDirectoryWithProgress(
        directory: File,
        onSongFound: suspend (Song) -> Unit
    ) {
        try {
            if (!directory.exists() || !directory.canRead() || isExcludedDirectory(directory)) {
                return
            }
            
            directory.listFiles()?.forEach { file ->
                when {
                    file.isDirectory -> {
                        scanDirectoryWithProgress(file, onSongFound)
                    }
                    file.isFile && isAudioFile(file) -> {
                        extractSongFromFile(file)?.let { song ->
                            onSongFound(song)
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in progress scan: ${directory.absolutePath}")
        }
    }
    
    private suspend fun countAudioFiles(directory: File): Int {
        return try {
            if (!directory.exists() || !directory.canRead() || isExcludedDirectory(directory)) {
                0
            } else {
                directory.walkTopDown()
                    .filter { file -> file.isFile && isAudioFile(file) }
                    .count()
            }
        } catch (exception: Exception) {
            0
        }
    }
    
    private suspend fun extractSongFromFile(file: File): Song? {
        return try {
            val metadata = audioMetadataExtractor.extractMetadata(file.absolutePath)
            if (metadata != null) {
                audioMetadataExtractor.audioMetadataToSong(
                    metadata = metadata,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    dateModified = file.lastModified()
                )
            } else {
                null
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error extracting song from file: ${file.absolutePath}")
            null
        }
    }
    
    private fun isAudioFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return MediaConstants.SUPPORTED_AUDIO_FORMATS.contains(extension) && 
               MediaUtils.isSupportedAudioFile(file.absolutePath)
    }
    
    private fun isExcludedDirectory(directory: File): Boolean {
        val dirName = directory.name.lowercase()
        return EXCLUDED_DIRECTORIES.any { excluded ->
            dirName.contains(excluded.lowercase())
        } || directory.name.startsWith(".")
    }
    
    private fun getSecondaryStorageDirectories(): List<File> {
        val directories = mutableListOf<File>()
        
        try {
            // Try to find removable storage
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? android.os.storage.StorageManager
            storageManager?.let { manager ->
                try {
                    val method = manager.javaClass.getMethod("getVolumeList")
                    val volumes = method.invoke(manager) as Array<*>
                    
                    volumes.forEach { volume ->
                        try {
                            val getPath = volume.javaClass.getMethod("getPath")
                            val isRemovable = volume.javaClass.getMethod("isRemovable")
                            
                            val path = getPath.invoke(volume) as String
                            val removable = isRemovable.invoke(volume) as Boolean
                            
                            if (removable) {
                                val dir = File(path)
                                if (dir.exists() && dir.canRead()) {
                                    directories.add(dir)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore individual volume errors
                        }
                    }
                } catch (e: Exception) {
                    // Fallback method failed
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting secondary storage directories")
        }
        
        return directories
    }
}

/**
 * Scan progress sealed class
 */
sealed class ScanProgress {
    data class Started(val totalFiles: Int) : ScanProgress()
    data class Progress(val processedFiles: Int, val totalFiles: Int, val currentSong: Song) : ScanProgress()
    data class Completed(val songs: List<Song>, val totalProcessed: Int) : ScanProgress()
    data class Error(val exception: Throwable) : ScanProgress()
    
    val progressPercentage: Float
        get() = when (this) {
            is Progress -> if (totalFiles > 0) (processedFiles.toFloat() / totalFiles) * 100f else 0f
            is Completed -> 100f
            else -> 0f
        }
}

/**
 * Scan statistics
 */
data class ScanStatistics(
    val totalFilesScanned: Int = 0,
    val validSongsFound: Int = 0,
    val invalidFilesSkipped: Int = 0,
    val directoriesScanned: Int = 0,
    val scanDurationMs: Long = 0L,
    val averageFileProcessTimeMs: Long = 0L
) {
    val successRate: Float
        get() = if (totalFilesScanned > 0) (validSongsFound.toFloat() / totalFilesScanned) * 100f else 0f
    
    val scanDurationSeconds: Float
        get() = scanDurationMs / 1000f
}
