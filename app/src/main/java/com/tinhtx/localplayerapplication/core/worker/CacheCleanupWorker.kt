package com.tinhtx.localplayerapplication.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.local.cache.AlbumArtCache
import com.tinhtx.localplayerapplication.data.local.cache.ImageCacheManager
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Worker for cleaning up cache files and managing storage
 */
@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val albumArtCache: AlbumArtCache,
    private val imageCacheManager: ImageCacheManager,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "CacheCleanupWorker"
        private const val WORK_NAME = AppConstants.CACHE_CLEANUP_WORK_NAME
        
        /**
         * Schedule periodic cache cleanup
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
                AppConstants.CACHE_CLEANUP_INTERVAL_DAYS.toLong(), TimeUnit.DAYS,
                6, TimeUnit.HOURS // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
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
         * Schedule immediate cache cleanup
         */
        fun scheduleImmediateCleanup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val immediateRequest = OneTimeWorkRequestBuilder<CacheCleanupWorker>()
                .setConstraints(constraints)
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
         * Cancel cache cleanup work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("$TAG - Starting cache cleanup")

            // Get storage settings
            val storageSettings = settingsRepository.getStorageSettings()
            
            if (!storageSettings.autoClearCache) {
                Timber.d("$TAG - Auto cache cleanup disabled, skipping")
                return@withContext Result.success()
            }

            // Perform cleanup operations
            val cleanupResults = performCacheCleanup(storageSettings.maxCacheSize)
            
            // Log cleanup results
            logCleanupResults(cleanupResults)

            Timber.d("$TAG - Cache cleanup completed successfully")
            Result.success()

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error during cache cleanup")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Perform comprehensive cache cleanup
     */
    private suspend fun performCacheCleanup(maxCacheSizeMB: Long): CleanupResults {
        val results = CleanupResults()
        
        try {
            // 1. Clean album art cache
            val albumArtCleanup = cleanupAlbumArtCache(maxCacheSizeMB)
            results.albumArtFreed = albumArtCleanup
            
            // 2. Clean image cache
            val imageCleanup = cleanupImageCache()
            results.imageCacheFreed = imageCleanup
            
            // 3. Clean temporary files
            val tempCleanup = cleanupTempFiles()
            results.tempFilesFreed = tempCleanup
            
            // 4. Clean old log files
            val logCleanup = cleanupLogFiles()
            results.logFilesFreed = logCleanup
            
            // 5. Clean database cache
            val dbCleanup = cleanupDatabaseCache()
            results.databaseCacheFreed = dbCleanup
            
            // 6. Clean app cache
            val appCacheCleanup = cleanupAppCache()
            results.appCacheFreed = appCacheCleanup

            results.totalFreed = results.albumArtFreed + results.imageCacheFreed + 
                               results.tempFilesFreed + results.logFilesFreed + 
                               results.databaseCacheFreed + results.appCacheFreed
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in cache cleanup operations")
            throw exception
        }
        
        return results
    }

    /**
     * Clean album art cache
     */
    private suspend fun cleanupAlbumArtCache(maxCacheSizeMB: Long): Long {
        return try {
            val maxCacheSize = maxCacheSizeMB * 1024 * 1024 // Convert to bytes
            val freedBytes = albumArtCache.cleanupCache(maxCacheSize)
            Timber.d("$TAG - Album art cache cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning album art cache")
            0L
        }
    }

    /**
     * Clean image cache
     */
    private suspend fun cleanupImageCache(): Long {
        return try {
            val freedBytes = imageCacheManager.clearExpiredCache()
            Timber.d("$TAG - Image cache cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning image cache")
            0L
        }
    }

    /**
     * Clean temporary files
     */
    private suspend fun cleanupTempFiles(): Long {
        return try {
            var freedBytes = 0L
            val tempDir = File(context.cacheDir, "temp")
            
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile && isFileOld(file, 24 * 60 * 60 * 1000L)) { // 24 hours
                        val fileSize = file.length()
                        if (file.delete()) {
                            freedBytes += fileSize
                        }
                    }
                }
            }
            
            Timber.d("$TAG - Temp files cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning temp files")
            0L
        }
    }

    /**
     * Clean old log files
     */
    private suspend fun cleanupLogFiles(): Long {
        return try {
            var freedBytes = 0L
            val logDir = File(context.filesDir, "logs")
            
            if (logDir.exists()) {
                logDir.listFiles()?.forEach { file ->
                    if (file.isFile && isFileOld(file, 7 * 24 * 60 * 60 * 1000L)) { // 7 days
                        val fileSize = file.length()
                        if (file.delete()) {
                            freedBytes += fileSize
                        }
                    }
                }
            }
            
            Timber.d("$TAG - Log files cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning log files")
            0L
        }
    }

    /**
     * Clean database cache and optimize
     */
    private suspend fun cleanupDatabaseCache(): Long {
        return try {
            // This would typically involve database vacuum operations
            // Placeholder implementation
            val freedBytes = 0L
            
            Timber.d("$TAG - Database cache cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning database cache")
            0L
        }
    }

    /**
     * Clean general app cache
     */
    private suspend fun cleanupAppCache(): Long {
        return try {
            val cacheDir = context.cacheDir
            val initialSize = calculateDirectorySize(cacheDir)
            
            // Clean old files from cache directories
            cleanOldCacheFiles(cacheDir, 3 * 24 * 60 * 60 * 1000L) // 3 days
            
            val finalSize = calculateDirectorySize(cacheDir)
            val freedBytes = initialSize - finalSize
            
            Timber.d("$TAG - App cache cleanup freed ${freedBytes / 1024 / 1024}MB")
            freedBytes
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning app cache")
            0L
        }
    }

    /**
     * Check if file is older than specified age
     */
    private fun isFileOld(file: File, maxAgeMs: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val fileAge = currentTime - file.lastModified()
        return fileAge > maxAgeMs
    }

    /**
     * Calculate total size of directory
     */
    private fun calculateDirectorySize(directory: File): Long {
        return try {
            directory.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (exception: Exception) {
            0L
        }
    }

    /**
     * Clean old files from cache directory
     */
    private fun cleanOldCacheFiles(directory: File, maxAgeMs: Long) {
        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isFile && isFileOld(file, maxAgeMs) -> {
                        file.delete()
                    }
                    file.isDirectory -> {
                        cleanOldCacheFiles(file, maxAgeMs)
                        // Delete empty directories
                        if (file.listFiles()?.isEmpty() == true) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning old cache files")
        }
    }

    /**
     * Log cleanup results
     */
    private fun logCleanupResults(results: CleanupResults) {
        val totalMB = results.totalFreed / 1024 / 1024
        Timber.i("$TAG - Cache cleanup completed:")
        Timber.i("$TAG - Total freed: ${totalMB}MB")
        Timber.i("$TAG - Album art: ${results.albumArtFreed / 1024 / 1024}MB")
        Timber.i("$TAG - Image cache: ${results.imageCacheFreed / 1024 / 1024}MB")
        Timber.i("$TAG - Temp files: ${results.tempFilesFreed / 1024 / 1024}MB")
        Timber.i("$TAG - Log files: ${results.logFilesFreed / 1024 / 1024}MB")
        Timber.i("$TAG - Database cache: ${results.databaseCacheFreed / 1024 / 1024}MB")
        Timber.i("$TAG - App cache: ${results.appCacheFreed / 1024 / 1024}MB")
    }

    /**
     * Results of cache cleanup operation
     */
    data class CleanupResults(
        var albumArtFreed: Long = 0L,
        var imageCacheFreed: Long = 0L,
        var tempFilesFreed: Long = 0L,
        var logFilesFreed: Long = 0L,
        var databaseCacheFreed: Long = 0L,
        var appCacheFreed: Long = 0L,
        var totalFreed: Long = 0L
    )
}
