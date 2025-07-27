package com.tinhtx.localplayerapplication.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.CoroutineWorker
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class AnalyticsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "AnalyticsWorker"
        const val WORK_NAME = "analytics_work"
        const val PERIODIC_WORK_NAME = "periodic_analytics_work"
        
        // Input data keys
        const val KEY_FORCE_UPLOAD = "force_upload"
        const val KEY_INCLUDE_USAGE_DATA = "include_usage_data"
        
        // Progress data keys
        const val KEY_PROGRESS = "progress"
        const val KEY_CURRENT_OPERATION = "current_operation"
        
        // Result data keys
        const val KEY_DATA_COLLECTED = "data_collected"
        const val KEY_UPLOAD_SUCCESS = "upload_success"
        const val KEY_ANALYTICS_DURATION = "analytics_duration"

        /**
         * Schedule one-time analytics collection
         */
        fun scheduleWork(
            context: Context,
            forceUpload: Boolean = false,
            includeUsageData: Boolean = true
        ): String {
            val inputData = Data.Builder()
                .putBoolean(KEY_FORCE_UPLOAD, forceUpload)
                .putBoolean(KEY_INCLUDE_USAGE_DATA, includeUsageData)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AnalyticsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)

            Timber.d("$TAG - One-time analytics work scheduled")
            return workRequest.id.toString()
        }

        /**
         * Schedule periodic analytics collection
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(false)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<AnalyticsWorker>(
                repeatInterval = 7, // 7 days
                repeatIntervalTimeUnit = TimeUnit.DAYS,
                flexTimeInterval = 1, // 1 day flex
                flexTimeIntervalUnit = TimeUnit.DAYS
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

            Timber.d("$TAG - Periodic analytics work scheduled")
        }

        /**
         * Cancel all analytics work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            Timber.d("$TAG - All analytics work cancelled")
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("$TAG - Starting analytics collection")
        
        val startTime = System.currentTimeMillis()
        var analyticsResult = AnalyticsResult()

        return try {
            // Set initial progress
            setProgress(createProgressData(0, "Preparing analytics..."))

            // Get analytics parameters
            val forceUpload = inputData.getBoolean(KEY_FORCE_UPLOAD, false)
            val includeUsageData = inputData.getBoolean(KEY_INCLUDE_USAGE_DATA, true)

            // Check user preferences
            val settings = settingsRepository.getAppSettings()
            val analyticsEnabled = settings.privacySettings?.analyticsEnabled ?: false

            if (!forceUpload && !analyticsEnabled) {
                Timber.d("$TAG - Analytics disabled by user, skipping")
                return Result.success()
            }

            // Collect analytics data
            analyticsResult = collectAnalyticsData(includeUsageData)

            // Upload data if enabled
            if (analyticsEnabled || forceUpload) {
                setProgress(createProgressData(80, "Uploading analytics..."))
                analyticsResult.uploadSuccess = uploadAnalyticsData(analyticsResult.data)
            }

            val analyticsDuration = System.currentTimeMillis() - startTime

            // Create result data
            val resultData = Data.Builder()
                .putBoolean(KEY_DATA_COLLECTED, analyticsResult.data.isNotEmpty())
                .putBoolean(KEY_UPLOAD_SUCCESS, analyticsResult.uploadSuccess)
                .putLong(KEY_ANALYTICS_DURATION, analyticsDuration)
                .build()

            Timber.i("$TAG - Analytics completed successfully in ${analyticsDuration}ms")
            Result.success(resultData)

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Analytics collection failed")
            
            Result.failure(createErrorData(exception.message ?: "Analytics failed"))
        }
    }

    private suspend fun collectAnalyticsData(includeUsageData: Boolean): AnalyticsResult {
        val result = AnalyticsResult()
        val analyticsData = mutableMapOf<String, Any>()

        try {
            // Basic app info
            setProgress(createProgressData(10, "Collecting app info..."))
            analyticsData.putAll(collectAppInfo())
            delay(100)

            // Library statistics
            setProgress(createProgressData(30, "Collecting library stats..."))
            analyticsData.putAll(collectLibraryStats())
            delay(100)

            // Usage statistics (if enabled)
            if (includeUsageData) {
                setProgress(createProgressData(50, "Collecting usage stats..."))
                analyticsData.putAll(collectUsageStats())
                delay(100)
            }

            // Performance metrics
            setProgress(createProgressData(70, "Collecting performance metrics..."))
            analyticsData.putAll(collectPerformanceMetrics())
            delay(100)

            // Error statistics
            setProgress(createProgressData(75, "Collecting error stats..."))
            analyticsData.putAll(collectErrorStats())

            result.data = analyticsData
            setProgress(createProgressData(80, "Analytics collection completed"))

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error collecting analytics data")
            throw exception
        }

        return result
    }

    private suspend fun collectAppInfo(): Map<String, Any> {
        return mapOf(
            "app_version" to getAppVersionName(),
            "app_version_code" to getAppVersionCode(),
            "android_version" to android.os.Build.VERSION.RELEASE,
            "device_model" to android.os.Build.MODEL,
            "device_manufacturer" to android.os.Build.MANUFACTURER,
            "timestamp" to System.currentTimeMillis(),
            "timezone" to java.util.TimeZone.getDefault().id
        )
    }

    private suspend fun collectLibraryStats(): Map<String, Any> {
        return try {
            mapOf(
                "total_songs" to musicRepository.getSongCount(),
                "total_albums" to musicRepository.getAlbumCount(),
                "total_artists" to musicRepository.getArtistCount(),
                "total_playlists" to musicRepository.getPlaylistCount(),
                "favorite_songs" to musicRepository.getFavoriteSongCount(),
                "total_playtime_hours" to musicRepository.getTotalPlaytimeHours(),
                "library_size_mb" to musicRepository.getTotalLibrarySizeMB()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting library stats")
            emptyMap()
        }
    }

    private suspend fun collectUsageStats(): Map<String, Any> {
        return try {
            val last30Days = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            
            mapOf(
                "sessions_last_30_days" to musicRepository.getSessionCount(last30Days),
                "playtime_last_30_days_hours" to musicRepository.getPlaytimeHours(last30Days),
                "most_played_songs" to musicRepository.getMostPlayedSongs(10).map { 
                    mapOf("title" to it.title, "artist" to it.artist, "play_count" to it.playCount)
                },
                "most_played_artists" to musicRepository.getMostPlayedArtists(10).map {
                    mapOf("name" to it.name, "play_count" to it.playCount)
                },
                "shuffle_usage_percentage" to musicRepository.getShuffleUsagePercentage(),
                "repeat_usage_percentage" to musicRepository.getRepeatUsagePercentage(),
                "average_session_duration_minutes" to musicRepository.getAverageSessionDurationMinutes(),
                "skip_rate_percentage" to musicRepository.getSkipRatePercentage()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting usage stats")
            emptyMap()
        }
    }

    private suspend fun collectPerformanceMetrics(): Map<String, Any> {
        return try {
            val runtime = Runtime.getRuntime()
            val memoryInfo = runtime.let {
                mapOf(
                    "max_memory_mb" to (it.maxMemory() / 1024 / 1024),
                    "total_memory_mb" to (it.totalMemory() / 1024 / 1024),
                    "free_memory_mb" to (it.freeMemory() / 1024 / 1024),
                    "used_memory_mb" to ((it.totalMemory() - it.freeMemory()) / 1024 / 1024)
                )
            }

            mapOf(
                "memory_info" to memoryInfo,
                "app_start_time_ms" to getAppStartTime(),
                "library_scan_count" to musicRepository.getLibraryScanCount(),
                "cache_size_mb" to getCacheSizeMB(),
                "database_size_mb" to getDatabaseSizeMB(),
                "crash_count_last_30_days" to getCrashCount()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting performance metrics")
            emptyMap()
        }
    }

    private suspend fun collectErrorStats(): Map<String, Any> {
        return try {
            mapOf(
                "playback_errors_last_30_days" to musicRepository.getPlaybackErrorCount(),
                "network_errors_last_30_days" to musicRepository.getNetworkErrorCount(),
                "permission_errors_last_30_days" to musicRepository.getPermissionErrorCount()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting error stats")
            emptyMap()
        }
    }

    private suspend fun uploadAnalyticsData(data: Map<String, Any>): Boolean {
        return try {
            setProgress(createProgressData(90, "Uploading to analytics service..."))
            
            // Here you would implement the actual upload to your analytics service
            // For example: Firebase Analytics, Google Analytics, custom backend, etc.
            
            // Simulate upload
            delay(1000)
            
            // Log anonymized data locally for debugging
            Timber.d("$TAG - Analytics data collected: ${data.keys.joinToString()}")
            
            true // Success
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error uploading analytics data")
            false // Failure
        }
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = applicationContext.packageManager.getPackageInfo(
                applicationContext.packageName, 0
            )
            packageInfo.versionName ?: "Unknown"
        } catch (exception: Exception) {
            "Unknown"
        }
    }

    private fun getAppVersionCode(): Long {
        return try {
            val packageInfo = applicationContext.packageManager.getPackageInfo(
                applicationContext.packageName, 0
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (exception: Exception) {
            0L
        }
    }

    private fun getAppStartTime(): Long {
        // This would be stored when app starts
        return System.currentTimeMillis() // Placeholder
    }

    private fun getCacheSizeMB(): Long {
        return try {
            val cacheDir = applicationContext.cacheDir
            (cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }) / 1024 / 1024
        } catch (exception: Exception) {
            0L
        }
    }

    private fun getDatabaseSizeMB(): Long {
        return try {
            val dbFile = applicationContext.getDatabasePath("music_database")
            (dbFile?.length() ?: 0L) / 1024 / 1024
        } catch (exception: Exception) {
            0L
        }
    }

    private fun getCrashCount(): Int {
        // This would be tracked by crash reporting service
        return 0 // Placeholder
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

    private data class AnalyticsResult(
        var data: Map<String, Any> = emptyMap(),
        var uploadSuccess: Boolean = false
    )
}
