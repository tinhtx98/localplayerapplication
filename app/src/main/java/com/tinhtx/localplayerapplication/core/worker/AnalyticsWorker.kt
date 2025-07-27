package com.tinhtx.localplayerapplication.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker for collecting and reporting analytics data
 */
@HiltWorker
class AnalyticsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "AnalyticsWorker"
        private const val WORK_NAME = AppConstants.ANALYTICS_WORK_NAME
        
        /**
         * Schedule periodic analytics reporting
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<AnalyticsWorker>(
                AppConstants.ANALYTICS_REPORT_INTERVAL_HOURS.toLong(), TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval
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
         * Cancel analytics work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("$TAG - Starting analytics collection")

            // Check if analytics is enabled
            val privacySettings = settingsRepository.getPrivacySettings()
            if (!privacySettings.analyticsEnabled) {
                Timber.d("$TAG - Analytics disabled, skipping collection")
                return@withContext Result.success()
            }

            // Collect analytics data
            val analyticsData = collectAnalyticsData()
            
            // Process and send analytics (placeholder - would integrate with analytics service)
            processAnalyticsData(analyticsData)

            Timber.d("$TAG - Analytics collection completed successfully")
            Result.success()

        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error collecting analytics")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Collect comprehensive analytics data
     */
    private suspend fun collectAnalyticsData(): AnalyticsData {
        return AnalyticsData(
            timestamp = System.currentTimeMillis(),
            appUsage = collectAppUsageStats(),
            libraryStats = collectLibraryStats(),
            playbackStats = collectPlaybackStats(),
            userBehavior = collectUserBehaviorStats(),
            performanceStats = collectPerformanceStats(),
            errorStats = collectErrorStats()
        )
    }

    /**
     * Collect app usage statistics
     */
    private suspend fun collectAppUsageStats(): Map<String, Any> {
        return try {
            val currentTime = System.currentTimeMillis()
            val last24Hours = currentTime - (24 * 60 * 60 * 1000L)
            val lastWeek = currentTime - (7 * 24 * 60 * 60 * 1000L)

            mapOf(
                "session_count_24h" to musicRepository.getSessionCount(last24Hours),
                "session_count_week" to musicRepository.getSessionCount(lastWeek),
                "total_playtime_24h" to musicRepository.getPlaytimeHours(last24Hours),
                "total_playtime_week" to musicRepository.getPlaytimeHours(lastWeek),
                "average_session_duration" to musicRepository.getAverageSessionDurationMinutes(),
                "app_launches_24h" to getAppLaunchCount(last24Hours),
                "crashes_24h" to getCrashCount(last24Hours)
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting app usage stats")
            emptyMap()
        }
    }

    /**
     * Collect library statistics
     */
    private suspend fun collectLibraryStats(): Map<String, Any> {
        return try {
            mapOf(
                "total_songs" to musicRepository.getSongCount(),
                "total_albums" to musicRepository.getAlbumCount(),
                "total_artists" to musicRepository.getArtistCount(),
                "total_playlists" to musicRepository.getPlaylistCount(),
                "favorite_songs" to musicRepository.getFavoriteSongCount(),
                "total_playtime_hours" to musicRepository.getTotalPlaytimeHours(),
                "library_size_mb" to musicRepository.getTotalLibrarySizeMB(),
                "last_scan_count" to musicRepository.getLibraryScanCount(),
                "average_song_duration" to musicRepository.getAverageSongDuration()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting library stats")
            emptyMap()
        }
    }

    /**
     * Collect playback statistics
     */
    private suspend fun collectPlaybackStats(): Map<String, Any> {
        return try {
            val last24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            val mostPlayedSongs = musicRepository.getMostPlayedSongs(10)
            val mostPlayedArtists = musicRepository.getMostPlayedArtists(10)

            mapOf(
                "plays_24h" to musicRepository.getPlayCountSince(last24Hours),
                "completion_rate" to musicRepository.getAverageCompletionRate(last24Hours),
                "skip_rate" to musicRepository.getSkipRatePercentage(),
                "shuffle_usage" to musicRepository.getShuffleUsagePercentage(),
                "repeat_usage" to musicRepository.getRepeatUsagePercentage(),
                "most_played_songs_count" to mostPlayedSongs.size,
                "most_played_artists_count" to mostPlayedArtists.size,
                "crossfade_usage" to getCrossfadeUsage(),
                "equalizer_usage" to getEqualizerUsage()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting playback stats")
            emptyMap()
        }
    }

    /**
     * Collect user behavior statistics
     */
    private suspend fun collectUserBehaviorStats(): Map<String, Any> {
        return try {
            mapOf(
                "search_queries_24h" to getSearchQueryCount(),
                "playlist_creations_24h" to getPlaylistCreationCount(),
                "favorites_added_24h" to getFavoritesAddedCount(),
                "settings_changes_24h" to getSettingsChangeCount(),
                "theme_preference" to getCurrentTheme(),
                "grid_size_preference" to getCurrentGridSize(),
                "notification_interactions_24h" to getNotificationInteractionCount()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting user behavior stats")
            emptyMap()
        }
    }

    /**
     * Collect performance statistics
     */
    private suspend fun collectPerformanceStats(): Map<String, Any> {
        return try {
            mapOf(
                "app_start_time_ms" to getAverageAppStartTime(),
                "library_scan_time_ms" to getAverageLibraryScanTime(),
                "search_response_time_ms" to getAverageSearchResponseTime(),
                "memory_usage_mb" to getCurrentMemoryUsage(),
                "cache_size_mb" to getCacheSize(),
                "database_size_mb" to getDatabaseSize()
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting performance stats")
            emptyMap()
        }
    }

    /**
     * Collect error statistics
     */
    private suspend fun collectErrorStats(): Map<String, Any> {
        return try {
            val last24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            
            mapOf(
                "playback_errors_24h" to musicRepository.getPlaybackErrorCount(),
                "network_errors_24h" to musicRepository.getNetworkErrorCount(),
                "permission_errors_24h" to musicRepository.getPermissionErrorCount(),
                "crash_count_24h" to getCrashCount(last24Hours),
                "anr_count_24h" to getANRCount(last24Hours)
            )
        } catch (exception: Exception) {
            Timber.w(exception, "$TAG - Error collecting error stats")
            emptyMap()
        }
    }

    /**
     * Process and send analytics data
     */
    private suspend fun processAnalyticsData( AnalyticsData) {
        try {
            // Log analytics data for debugging
            Timber.d("$TAG - Analytics data collected: ${data.summary()}")
            
            // Here you would send data to your analytics service
            // For example: Firebase Analytics, Mixpanel, etc.
            // sendToAnalyticsService(data)
            
            // Store locally for later sync if needed
            storeAnalyticsLocally(data)
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error processing analytics data")
            throw exception
        }
    }

    /**
     * Store analytics data locally
     */
    private suspend fun storeAnalyticsLocally( AnalyticsData) {
        // Implementation would store in local database for later sync
        // This is a placeholder
    }

    // Placeholder methods for data collection - these would be implemented based on your analytics needs
    private fun getAppLaunchCount(since: Long): Int = 0
    private fun getCrashCount(since: Long): Int = 0
    private fun getCrossfadeUsage(): Float = 0f
    private fun getEqualizerUsage(): Float = 0f
    private fun getSearchQueryCount(): Int = 0
    private fun getPlaylistCreationCount(): Int = 0
    private fun getFavoritesAddedCount(): Int = 0
    private fun getSettingsChangeCount(): Int = 0
    private fun getCurrentTheme(): String = "system"
    private fun getCurrentGridSize(): String = "medium"
    private fun getNotificationInteractionCount(): Int = 0
    private fun getAverageAppStartTime(): Long = 0L
    private fun getAverageLibraryScanTime(): Long = 0L
    private fun getAverageSearchResponseTime(): Long = 0L
    private fun getCurrentMemoryUsage(): Long = 0L
    private fun getCacheSize(): Long = 0L
    private fun getDatabaseSize(): Long = 0L
    private fun getANRCount(since: Long): Int = 0

    /**
     * Analytics data structure
     */
    data class AnalyticsData(
        val timestamp: Long,
        val appUsage: Map<String, Any>,
        val libraryStats: Map<String, Any>,
        val playbackStats: Map<String, Any>,
        val userBehavior: Map<String, Any>,
        val performanceStats: Map<String, Any>,
        val errorStats: Map<String, Any>
    ) {
        fun summary(): String {
            return "Analytics[${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(timestamp))}] - " +
                    "Library: ${libraryStats["total_songs"]} songs, " +
                    "Usage: ${appUsage["session_count_24h"]} sessions, " +
                    "Playback: ${playbackStats["plays_24h"]} plays"
        }
    }
}
