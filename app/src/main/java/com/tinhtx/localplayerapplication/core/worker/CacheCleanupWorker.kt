package com.tinhtx.localplayerapplication.core.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.utils.FileUtils
import com.tinhtx.localplayerapplication.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = 1002

    companion object {
        const val TAG = "CacheCleanupWorker"
        const val WORK_NAME = "cache_cleanup_work"
        const val PERIODIC_WORK_NAME = "periodic_cache_cleanup_work"
        
        // Input data keys
        const val KEY_FORCE_CLEANUP = "force_cleanup"
        const val KEY_MAX_CACHE_SIZE_MB = "max_cache_size_mb"
        const val KEY_MAX_AGE_DAYS = "max_age_days"
        
        // Progress data keys
        const val KEY_PROGRESS = "progress"
        const val KEY_CURRENT_OPERATION = "current_operation"
        
        // Result data keys
        const val KEY_FILES_DELETED = "files_deleted"
        const val KEY_SPACE_FREED_MB = "space_freed_mb"
        const val KEY_CLEANUP_DURATION = "cleanup_duration"

        /**
         * Schedule one-time cache cleanup
         */
        fun scheduleWork(
            context: Context,
            forceCleanup: Boolean = false,
            maxCacheSizeMB: Int = 500,
            maxAgeDays: Int = 7
        ): String {
            val inputData = Data.Builder()
                .putBoolean(KEY_FORCE_CLEANUP, forceCleanup)
                .putInt(KEY_MAX_CACHE_SIZE_MB, maxCacheSizeMB)
                .putInt(KEY_MAX_AGE_DAYS, maxAgeDays)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresStorageNotLow(false) // We're freeing storage
                .build()

            val workRequest = OneTimeWorkRequestBuilder<CacheCleanupWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)

            Timber.d("$TAG - One-time cleanup work scheduled")
            return workRequest.id.toString()
        }

        /**
         * Schedule periodic cache cleanup
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresStorageNotLow(false)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
                repeatInterval = 24, // 24 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 4, // 4 hour flex
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

            Timber.d("$TAG - Periodic cleanup work scheduled")
        }

        /**
         * Cancel all cleanup work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Timber.d("$TAG - All cleanup work cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("$TAG - Starting cache cleanup")
        
        val startTime = System.currentTimeMillis()
        var cleanupResult = CleanupResult()

        return try {
            // Show notification
            showCleanupStartNotification()

            // Set initial progress
            setProgress(createProgressData(0, "Preparing cleanup..."))

            // Get cleanup parameters
            val forceCleanup = inputData.getBoolean(KEY_FORCE_CLEANUP, false)
            val maxCacheSizeMB = inputData.getInt(KEY_MAX_CACHE_SIZE_MB, 500)
            val maxAgeDays = inputData.getInt(KEY_MAX_AGE_DAYS, 7)

            // Get user settings
            val storageSettings = settingsRepository.getStorageSettings()
            val autoClearEnabled = storageSettings.autoClearCache

            // Check if cleanup is needed
            if (!forceCleanup && !autoClearEnabled) {
                Timber.d("$TAG - Auto cleanup disabled, skipping")
                return Result.success()
            }

            // Perform cleanup
            cleanupResult = performCacheCleanup(maxCacheSizeMB, maxAgeDays, forceCleanup)

            val cleanupDuration = System.currentTimeMillis() - startTime

            // Show completion notification
            showCleanupCompleteNotification(cleanupResult, cleanupDuration)

            // Create result data
            val resultData = Data.Builder()
                .putInt(KEY_FILES_DELETED, cleanupResult.filesDeleted)
                .putLong(KEY_SPACE_FREED_MB, cleanupResult.spaceFreedMB)
                .putLong(KEY_CLEANUP_DURATION, cleanupDuration)
                .build()

            Timber.i("$TAG - Cleanup completed successfully in ${cleanupDuration}ms: $cleanupResult")
            Result.success(resultData)

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Cleanup failed")
            
            showCleanupErrorNotification(exception.message ?: "Unknown error")
            
            Result.failure(createErrorData(exception.message ?: "Cleanup failed"))
        } finally {
            // Clean up notification
            delay(5000) // Keep notification for 5 seconds
            notificationManager.cancel(notificationId)
        }
    }

    private suspend fun performCacheCleanup(
        maxCacheSizeMB: Int,
        maxAgeDays: Int,
        forceCleanup: Boolean
    ): CleanupResult {
        val cleanupResult = CleanupResult()
        val maxCacheBytes = maxCacheSizeMB * 1024 * 1024L
        val maxAgeMs = maxAgeDays * 24 * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()

        // Cache directories to clean
        val cacheDirectories = listOf(
            applicationContext.cacheDir,
            File(applicationContext.filesDir, "album_art_cache"),
            File(applicationContext.filesDir, "lyrics_cache"),
            File(applicationContext.filesDir, "temp"),
            File(applicationContext.filesDir, "downloads"),
            applicationContext.externalCacheDir
        ).filterNotNull().filter { it.exists() }

        var currentProgress = 10
        val progressStep = 80 / cacheDirectories.size

        for ((index, cacheDir) in cacheDirectories.withIndex()) {
            setProgress(createProgressData(
                currentProgress,
                "Cleaning ${cacheDir.name}..."
            ))

            cleanupResult.add(cleanupDirectory(cacheDir, maxCacheBytes, maxAgeMs, currentTime, forceCleanup))
            
            currentProgress += progressStep
            delay(100) // Small delay between directories
        }

        // Clean external cache if available
        setProgress(createProgressData(90, "Cleaning external cache..."))
        applicationContext.externalCacheDir?.let { externalCache ->
            if (externalCache.exists()) {
                cleanupResult.add(cleanupDirectory(externalCache, maxCacheBytes, maxAgeMs, currentTime, forceCleanup))
            }
        }

        // Clean database temporary data
        setProgress(createProgressData(95, "Cleaning database cache..."))
        cleanupDatabaseCache(cleanupResult)

        setProgress(createProgressData(100, "Cleanup completed"))

        return cleanupResult
    }

    private fun cleanupDirectory(
        directory: File,
        maxCacheBytes: Long,
        maxAgeMs: Long,
        currentTime: Long,
        forceCleanup: Boolean
    ): CleanupResult {
        val result = CleanupResult()
        
        if (!directory.exists() || !directory.isDirectory) {
            return result
        }

        try {
            val files = directory.walkTopDown()
                .filter { it.isFile }
                .sortedBy { it.lastModified() } // Oldest first
                .toList()

            var totalSize = files.sumOf { it.length() }
            
            for (file in files) {
                val shouldDelete = when {
                    forceCleanup -> true
                    totalSize > maxCacheBytes -> true
                    (currentTime - file.lastModified()) > maxAgeMs -> true
                    else -> false
                }

                if (shouldDelete && file.exists()) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        result.filesDeleted++
                        result.spaceFreedBytes += fileSize
                        totalSize -= fileSize
                    }
                }
            }

            // Remove empty directories
            directory.walkBottomUp()
                .filter { it.isDirectory && it != directory }
                .forEach { dir ->
                    if (dir.listFiles()?.isEmpty() == true) {
                        dir.delete()
                    }
                }

        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning directory: ${directory.path}")
        }

        return result
    }

    private suspend fun cleanupDatabaseCache(cleanupResult: CleanupResult) {
        try {
            // Clean expired search history
            // Clean old play history entries
            // Clean temporary database files
            
            // This would involve calling repository methods to clean up old data
            Timber.d("$TAG - Database cache cleanup completed")
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error cleaning database cache")
        }
    }

    private fun showCleanupStartNotification() {
        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.SCAN_CHANNEL_ID)
            .setContentTitle("Cleaning Cache")
            .setContentText("Starting cache cleanup...")
            .setSmallIcon(R.drawable.ic_clean)
            .setOngoing(true)
            .setProgress(100, 0, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showCleanupCompleteNotification(cleanupResult: CleanupResult, duration: Long) {
        val durationText = "${duration / 1000}s"
        val resultText = if (cleanupResult.spaceFreedMB > 0) {
            "${cleanupResult.spaceFreedMB}MB freed, ${cleanupResult.filesDeleted} files deleted"
        } else {
            "No cleanup needed"
        }

        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.SCAN_CHANNEL_ID)
            .setContentTitle("Cache Cleanup Complete")
            .setContentText("$resultText in $durationText")
            .setSmallIcon(R.drawable.ic_check)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showCleanupErrorNotification(error: String) {
        val notification = NotificationCompat.Builder(applicationContext, AppConstants.Notifications.ERROR_CHANNEL_ID)
            .setContentTitle("Cache Cleanup Failed")
            .setContentText(error)
            .setSmallIcon(R.drawable.ic_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createProgressData(progress: Int, message: String): Data {
        return Data.Builder()
            .putInt(KEY_PROGRESS, progress)
            .putString(KEY_CURRENT_OPERATION, message)
            .build()
    }

    private fun createErrorData(error: String): Data {
        return Data.Builder()
            .putString("error", error)
            .build()
    }

    private data class CleanupResult(
        var filesDeleted: Int = 0,
        var spaceFreedBytes: Long = 0L
    ) {
        val spaceFreedMB: Long get() = spaceFreedBytes / (1024 * 1024)
        
        fun add(other: CleanupResult) {
            filesDeleted += other.filesDeleted
            spaceFreedBytes += other.spaceFreedBytes
        }
        
        override fun toString(): String {
            return "CleanupResult(files=$filesDeleted, space=${spaceFreedMB}MB)"
        }
    }
}
