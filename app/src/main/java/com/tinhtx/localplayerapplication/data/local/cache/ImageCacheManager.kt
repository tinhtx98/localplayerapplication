package com.tinhtx.localplayerapplication.data.local.cache

import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages general image caching using Coil ImageLoader
 */
@Singleton
class ImageCacheManager @Inject constructor(
    private val context: Context,
    private val imageLoader: ImageLoader
) {
    
    companion object {
        private const val TAG = "ImageCacheManager"
        private const val CACHE_DIR_NAME = "image_cache"
        private const val MAX_CACHE_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Preload image into cache
     */
    suspend fun preloadImage(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            
            imageLoader.execute(request)
            
            Timber.d("$TAG - Preloaded image: $url")
            true
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error preloading image: $url")
            false
        }
    }
    
    /**
     * Check if image is cached in memory
     */
    fun isImageCachedInMemory(url: String): Boolean {
        return imageLoader.memoryCache?.get(MemoryCache.Key(url)) != null
    }
    
    /**
     * Check if image is cached on disk
     */
    suspend fun isImageCachedOnDisk(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val diskCache = imageLoader.diskCache
            val snapshot = diskCache?.openSnapshot(url)
            val isCached = snapshot != null
            snapshot?.close()
            isCached
        } catch (exception: Exception) {
            false
        }
    }
    
    /**
     * Remove image from all caches
     */
    suspend fun removeImageFromCache(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            var removed = false
            
            // Remove from memory cache
            imageLoader.memoryCache?.remove(MemoryCache.Key(url))
            removed = true
            
            // Remove from disk cache
            imageLoader.diskCache?.remove(url)
            
            Timber.d("$TAG - Removed image from cache: $url")
            removed
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error removing image from cache: $url")
            false
        }
    }
    
    /**
     * Clear memory cache
     */
    fun clearMemoryCache() {
        try {
            imageLoader.memoryCache?.clear()
            Timber.d("$TAG - Cleared memory cache")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing memory cache")
        }
    }
    
    /**
     * Clear disk cache
     */
    suspend fun clearDiskCache(): Long = withContext(Dispatchers.IO) {
        try {
            val diskCache = imageLoader.diskCache
            val initialSize = diskCache?.size ?: 0L
            
            diskCache?.clear()
            
            val freedBytes = initialSize
            Timber.d("$TAG - Cleared disk cache, freed ${freedBytes / (1024 * 1024)}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing disk cache")
            0L
        }
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAllCaches(): Long = withContext(Dispatchers.IO) {
        val freedBytes = clearDiskCache()
        clearMemoryCache()
        freedBytes
    }
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache(): Long = withContext(Dispatchers.IO) {
        try {
            var freedBytes = 0L
            val currentTime = System.currentTimeMillis()
            val cutoffTime = currentTime - MAX_CACHE_AGE_MS
            
            // Clear expired files from our custom cache directory
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        freedBytes += fileSize
                    }
                }
            }
            
            if (freedBytes > 0) {
                Timber.d("$TAG - Cleared expired cache, freed ${freedBytes / (1024 * 1024)}MB")
            }
            
            freedBytes
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing expired cache")
            0L
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): ImageCacheStats = withContext(Dispatchers.IO) {
        try {
            val memoryCache = imageLoader.memoryCache
            val diskCache = imageLoader.diskCache
            
            val memoryCacheSize = memoryCache?.size ?: 0L
            val memoryCacheMaxSize = memoryCache?.maxSize ?: 0L
            val diskCacheSize = diskCache?.size ?: 0L
            val diskCacheMaxSize = diskCache?.maxSize ?: 0L
            
            // Count custom cache files
            val customCacheFiles = cacheDir.listFiles { it.isFile } ?: emptyArray()
            val customCacheSize = customCacheFiles.sumOf { it.length() }
            val customCacheCount = customCacheFiles.size
            
            ImageCacheStats(
                memoryCacheSize = memoryCacheSize,
                memoryCacheMaxSize = memoryCacheMaxSize,
                diskCacheSize = diskCacheSize,
                diskCacheMaxSize = diskCacheMaxSize,
                customCacheSize = customCacheSize,
                customCacheCount = customCacheCount
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting cache stats")
            ImageCacheStats()
        }
    }
    
    /**
     * Optimize cache by removing least recently used items
     */
    suspend fun optimizeCache(targetSizeBytes: Long): Long = withContext(Dispatchers.IO) {
        try {
            var freedBytes = 0L
            
            // Get current cache stats
            val stats = getCacheStats()
            val totalSize = stats.diskCacheSize + stats.customCacheSize
            
            if (totalSize <= targetSizeBytes) {
                return@withContext 0L
            }
            
            // Clear custom cache files (oldest first)
            val customFiles = cacheDir.listFiles { it.isFile }?.sortedBy { it.lastModified() } ?: emptyArray()
            
            for (file in customFiles) {
                if (stats.diskCacheSize + stats.customCacheSize - freedBytes <= targetSizeBytes) {
                    break
                }
                
                val fileSize = file.length()
                if (file.delete()) {
                    freedBytes += fileSize
                }
            }
            
            Timber.d("$TAG - Optimized cache, freed ${freedBytes / (1024 * 1024)}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error optimizing cache")
            0L
        }
    }
    
    /**
     * Preload images in background
     */
    suspend fun preloadImages(urls: List<String>) = withContext(Dispatchers.IO) {
        try {
            urls.forEach { url ->
                preloadImage(url)
            }
            Timber.d("$TAG - Preloaded ${urls.size} images")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error preloading images")
        }
    }
    
    /**
     * Save bitmap to custom cache
     */
    suspend fun saveBitmapToCache(key: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(cacheDir, "${key.hashCode()}.jpg")
            cacheFile.outputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            }
            
            Timber.d("$TAG - Saved bitmap to cache: $key")
            true
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error saving bitmap to cache: $key")
            false
        }
    }
    
    /**
     * Load bitmap from custom cache
     */
    suspend fun loadBitmapFromCache(key: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(cacheDir, "${key.hashCode()}.jpg")
            if (cacheFile.exists()) {
                android.graphics.BitmapFactory.decodeFile(cacheFile.absolutePath)
            } else {
                null
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error loading bitmap from cache: $key")
            null
        }
    }
    
    /**
     * Get total cache size across all cache types
     */
    suspend fun getTotalCacheSize(): Long = withContext(Dispatchers.IO) {
        val stats = getCacheStats()
        stats.memoryCacheSize + stats.diskCacheSize + stats.customCacheSize
    }
    
    /**
     * Check if cache is healthy (not corrupted)
     */
    suspend fun isCacheHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if cache directories exist and are accessible
            val diskCache = imageLoader.diskCache
            val memoryCache = imageLoader.memoryCache
            
            val diskCacheHealthy = diskCache?.directory?.exists() ?: true
            val memoryCacheHealthy = memoryCache != null
            val customCacheHealthy = cacheDir.exists() && cacheDir.canRead() && cacheDir.canWrite()
            
            diskCacheHealthy && memoryCacheHealthy && customCacheHealthy
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error checking cache health")
            false
        }
    }
    
    /**
     * Repair cache if corrupted
     */
    suspend fun repairCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Recreate custom cache directory if needed
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            // Clear corrupted files
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() == 0L) {
                    file.delete()
                }
            }
            
            Timber.d("$TAG - Cache repaired successfully")
            true
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error repairing cache")
            false
        }
    }
}

/**
 * Image cache statistics
 */
data class ImageCacheStats(
    val memoryCacheSize: Long = 0L,
    val memoryCacheMaxSize: Long = 0L,
    val diskCacheSize: Long = 0L,
    val diskCacheMaxSize: Long = 0L,
    val customCacheSize: Long = 0L,
    val customCacheCount: Int = 0
) {
    val totalCacheSize: Long get() = memoryCacheSize + diskCacheSize + customCacheSize
    val memoryCacheUsagePercent: Float get() = if (memoryCacheMaxSize > 0) (memoryCacheSize.toFloat() / memoryCacheMaxSize) * 100f else 0f
    val diskCacheUsagePercent: Float get() = if (diskCacheMaxSize > 0) (diskCacheSize.toFloat() / diskCacheMaxSize) * 100f else 0f
    val totalCacheSizeMB: Double get() = totalCacheSize / (1024.0 * 1024.0)
}
