package com.tinhtx.localplayerapplication.data.local.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages album artwork caching for efficient image loading
 */
@Singleton
class AlbumArtCache @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AlbumArtCache"
        private const val CACHE_DIR_NAME = AppConstants.ARTWORK_CACHE_DIR
        private const val THUMBNAIL_SIZE = 512
        private const val COMPRESSION_QUALITY = 85
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Get cached album art or extract and cache if not exists
     */
    suspend fun getAlbumArt(
        albumId: Long,
        songPath: String? = null,
        artist: String? = null,
        album: String? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Try to get from cache first
            val cachedBitmap = getCachedAlbumArt(albumId)
            if (cachedBitmap != null) {
                return@withContext cachedBitmap
            }
            
            // Extract from song file if path provided
            val extractedBitmap = songPath?.let { path ->
                MediaUtils.extractAlbumArt(path)
            }
            
            // Cache the extracted bitmap
            extractedBitmap?.let { bitmap ->
                cacheAlbumArt(albumId, bitmap, artist, album)
                return@withContext bitmap
            }
            
            // Try to get from MediaStore
            val mediaStoreBitmap = context.getAlbumArt(albumId)
            mediaStoreBitmap?.let { bitmap ->
                cacheAlbumArt(albumId, bitmap, artist, album)
                return@withContext bitmap
            }
            
            null
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting album art for album $albumId")
            null
        }
    }
    
    /**
     * Get cached album art by album ID
     */
    private suspend fun getCachedAlbumArt(albumId: Long): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(albumId)
            if (cacheFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                if (bitmap != null) {
                    Timber.d("$TAG - Found cached album art for album $albumId")
                    return@withContext bitmap
                } else {
                    // Delete corrupted cache file
                    cacheFile.delete()
                }
            }
            null
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error loading cached album art for album $albumId")
            null
        }
    }
    
    /**
     * Cache album art bitmap
     */
    private suspend fun cacheAlbumArt(
        albumId: Long,
        bitmap: Bitmap,
        artist: String? = null,
        album: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(albumId)
            
            // Create thumbnail for caching
            val thumbnail = createThumbnail(bitmap)
            
            FileOutputStream(cacheFile).use { outputStream ->
                thumbnail.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
                outputStream.flush()
            }
            
            // Recycle thumbnail if different from original
            if (thumbnail != bitmap) {
                thumbnail.recycle()
            }
            
            Timber.d("$TAG - Cached album art for album $albumId (${artist} - ${album})")
            true
        } catch (exception: IOException) {
            Timber.e(exception, "$TAG - Error caching album art for album $albumId")
            false
        }
    }
    
    /**
     * Create thumbnail from bitmap
     */
    private fun createThumbnail(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // If already smaller than thumbnail size, return original
        if (width <= THUMBNAIL_SIZE && height <= THUMBNAIL_SIZE) {
            return bitmap
        }
        
        // Calculate scaling factor
        val scaleFactor = THUMBNAIL_SIZE.toFloat() / maxOf(width, height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Get cache file for album ID
     */
    private fun getCacheFile(albumId: Long): File {
        return File(cacheDir, "album_${albumId}.jpg")
    }
    
    /**
     * Check if album art is cached
     */
    suspend fun isAlbumArtCached(albumId: Long): Boolean = withContext(Dispatchers.IO) {
        getCacheFile(albumId).exists()
    }
    
    /**
     * Remove cached album art
     */
    suspend fun removeAlbumArt(albumId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(albumId)
            val deleted = cacheFile.delete()
            if (deleted) {
                Timber.d("$TAG - Removed cached album art for album $albumId")
            }
            deleted
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error removing cached album art for album $albumId")
            false
        }
    }
    
    /**
     * Clear all cached album art
     */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        try {
            var freedBytes = 0L
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("album_") && file.name.endsWith(".jpg")) {
                    freedBytes += file.length()
                    file.delete()
                }
            }
            
            Timber.d("$TAG - Cleared album art cache, freed ${freedBytes / (1024 * 1024)}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing album art cache")
            0L
        }
    }
    
    /**
     * Clean up cache with size limit
     */
    suspend fun cleanupCache(maxSizeBytes: Long): Long = withContext(Dispatchers.IO) {
        try {
            val cacheFiles = cacheDir.listFiles { file ->
                file.isFile && file.name.startsWith("album_") && file.name.endsWith(".jpg")
            }?.sortedBy { it.lastModified() } ?: return@withContext 0L
            
            var totalSize = cacheFiles.sumOf { it.length() }
            var freedBytes = 0L
            
            // Remove oldest files until under size limit
            for (file in cacheFiles) {
                if (totalSize <= maxSizeBytes) break
                
                val fileSize = file.length()
                if (file.delete()) {
                    totalSize -= fileSize
                    freedBytes += fileSize
                }
            }
            
            if (freedBytes > 0) {
                Timber.d("$TAG - Cleaned up album art cache, freed ${freedBytes / (1024 * 1024)}MB")
            }
            
            freedBytes
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error cleaning up album art cache")
            0L
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        try {
            val cacheFiles = cacheDir.listFiles { file ->
                file.isFile && file.name.startsWith("album_") && file.name.endsWith(".jpg")
            } ?: emptyArray()
            
            val totalSize = cacheFiles.sumOf { it.length() }
            val fileCount = cacheFiles.size
            val oldestFile = cacheFiles.minByOrNull { it.lastModified() }
            val newestFile = cacheFiles.maxByOrNull { it.lastModified() }
            
            CacheStats(
                fileCount = fileCount,
                totalSizeBytes = totalSize,
                oldestFileTime = oldestFile?.lastModified() ?: 0L,
                newestFileTime = newestFile?.lastModified() ?: 0L
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting cache stats")
            CacheStats()
        }
    }
    
    /**
     * Preload album art for albums
     */
    suspend fun preloadAlbumArt(albums: List<AlbumInfo>) = withContext(Dispatchers.IO) {
        try {
            albums.forEach { albumInfo ->
                if (!isAlbumArtCached(albumInfo.id)) {
                    getAlbumArt(
                        albumId = albumInfo.id,
                        songPath = albumInfo.sampleSongPath,
                        artist = albumInfo.artist,
                        album = albumInfo.name
                    )
                }
            }
            Timber.d("$TAG - Preloaded album art for ${albums.size} albums")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error preloading album art")
        }
    }
    
    /**
     * Get cache directory size
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        try {
            cacheDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (exception: Exception) {
            0L
        }
    }
    
    /**
     * Check if cache directory exists and is writable
     */
    fun isCacheAvailable(): Boolean {
        return cacheDir.exists() && cacheDir.canWrite()
    }
}

/**
 * Album information for preloading
 */
data class AlbumInfo(
    val id: Long,
    val name: String,
    val artist: String,
    val sampleSongPath: String? = null
)

/**
 * Cache statistics
 */
data class CacheStats(
    val fileCount: Int = 0,
    val totalSizeBytes: Long = 0L,
    val oldestFileTime: Long = 0L,
    val newestFileTime: Long = 0L
) {
    val totalSizeMB: Double get() = totalSizeBytes / (1024.0 * 1024.0)
    val averageFileSizeBytes: Long get() = if (fileCount > 0) totalSizeBytes / fileCount else 0L
}

/**
 * Extension function for Context to get album art
 */
private fun Context.getAlbumArt(albumId: Long): Bitmap? {
    return MediaUtils.extractAlbumArt(this, MediaUtils.getAlbumArtUri(albumId))
}
