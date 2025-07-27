package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences and profile data
 */
interface UserPreferencesRepository {
    
    // Basic profile management
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun updateUserName(name: String)
    suspend fun updateUserEmail(email: String)
    suspend fun updateUserAvatar(avatarPath: String?)
    suspend fun updateLastLogin()
    suspend fun clearUserProfile()
    
    // Listening statistics
    suspend fun getTotalListeningTime(): Flow<Long>
    suspend fun updateTotalListeningTime(timeMs: Long)
    suspend fun addToListeningTime(additionalTimeMs: Long)
    suspend fun resetListeningTime()
    
    suspend fun getSongsPlayedCount(): Flow<Long>
    suspend fun updateSongsPlayedCount(count: Int)
    suspend fun incrementSongsPlayedCount()
    suspend fun resetSongsPlayedCount()
    
    // Music preferences
    suspend fun getFavoriteGenre(): Flow<String>
    suspend fun updateFavoriteGenre(genre: String)
    suspend fun getTopGenres(count: Int): Flow<List<Pair<String, Int>>>
    
    suspend fun getFavoriteArtist(): Flow<String>
    suspend fun updateFavoriteArtist(artist: String)
    suspend fun getTopArtists(count: Int): Flow<List<Pair<String, Int>>>
    
    suspend fun getFavoriteAlbum(): Flow<String>
    suspend fun updateFavoriteAlbum(album: String)
    suspend fun getTopAlbums(count: Int): Flow<List<Pair<String, Int>>>
    
    // Session statistics
    suspend fun getLongestSession(): Flow<Long>
    suspend fun updateLongestSession(durationMs: Long)
    
    suspend fun getAverageSessionLength(): Flow<Long>
    suspend fun updateAverageSessionLength(durationMs: Long)
    suspend fun addSessionLength(durationMs: Long)
    
    suspend fun getTotalSessions(): Flow<Int>
    suspend fun incrementSessionCount()
    
    // Usage patterns
    suspend fun getMostActiveHour(): Flow<Int>
    suspend fun updateMostActiveHour(hour: Int)
    suspend fun getHourlyUsagePattern(): Flow<Map<Int, Int>>
    suspend fun updateHourlyUsage(hour: Int)
    
    suspend fun getMostActiveDayOfWeek(): Flow<Int>
    suspend fun updateDayOfWeekUsage(dayOfWeek: Int)
    suspend fun getDailyUsagePattern(): Flow<Map<Int, Int>>
    
    // App usage
    suspend fun getAppLaunchCount(): Flow<Int>
    suspend fun incrementAppLaunchCount()
    suspend fun getFirstLaunchDate(): Flow<Long>
    suspend fun setFirstLaunchDate(timestamp: Long)
    
    suspend fun getLastActiveDate(): Flow<Long>
    suspend fun updateLastActiveDate(timestamp: Long)
    
    // Listening goals
    suspend fun getWeeklyListeningGoal(): Flow<Long>
    suspend fun setWeeklyListeningGoal(minutes: Long)
    suspend fun getWeeklyProgress(): Flow<Long>
    suspend fun updateWeeklyProgress(minutes: Long)
    suspend fun resetWeeklyProgress()
    
    suspend fun getMonthlyListeningGoal(): Flow<Long>
    suspend fun setMonthlyListeningGoal(minutes: Long)
    suspend fun getMonthlyProgress(): Flow<Long>
    suspend fun updateMonthlyProgress(minutes: Long)
    suspend fun resetMonthlyProgress()
    
    suspend fun getDailyListeningGoal(): Flow<Long>
    suspend fun setDailyListeningGoal(minutes: Long)
    suspend fun getDailyProgress(): Flow<Long>
    suspend fun updateDailyProgress(minutes: Long)
    suspend fun resetDailyProgress()
    
    // Session and queue management
    suspend fun getLastQueue(): Flow<Pair<List<Long>, Int>>
    suspend fun saveLastQueue(songIds: List<Long>, position: Int)
    suspend fun clearLastQueue()
    
    suspend fun getLastPlayedSong(): Flow<Pair<Long, Long>>
    suspend fun saveLastPlayedSong(songId: Long, position: Long)
    suspend fun clearLastPlayedSong()
    
    suspend fun getLastPlayedPlaylist(): Flow<Long>
    suspend fun saveLastPlayedPlaylist(playlistId: Long)
    
    // Playback preferences
    suspend fun getLastVolume(): Flow<Float>
    suspend fun saveLastVolume(volume: Float)
    
    suspend fun getLastPlaybackSpeed(): Flow<Float>
    suspend fun saveLastPlaybackSpeed(speed: Float)
    
    suspend fun getPreferredAudioQuality(): Flow<String>
    suspend fun setPreferredAudioQuality(quality: String)
    
    // Achievement and milestones
    suspend fun getAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(achievementId: String)
    suspend fun getMilestones(): Flow<List<Milestone>>
    suspend fun addMilestone(milestone: Milestone)
    
    // Backup and restore
    suspend fun createBackup(): String
    suspend fun restoreFromBackup(backupData: String): Boolean
    suspend fun exportUserData(): String
    suspend fun importUserData(userData: String): Boolean
    suspend fun clearAllPreferences()
    
    // Session tracking
    suspend fun startSession(timestamp: Long)
    suspend fun endSession(timestamp: Long, songsPlayed: Int, songsSkipped: Int)
    suspend fun updateSessionActivity(activityType: String, timestamp: Long)
    suspend fun getCurrentSessionId(): Flow<String?>
    suspend fun getSessionHistory(): Flow<List<UserSession>>
    
    // Feature usage analytics
    suspend fun recordFeatureUsage(feature: String, timestamp: Long)
    suspend fun getFeatureUsageStats(): Map<String, Int>
    suspend fun getMostUsedFeatures(count: Int): List<Pair<String, Int>>
    suspend fun getUsagePatterns(): Map<String, Any>
    suspend fun resetFeatureUsageStats()
    
    // Recommendation preferences
    suspend fun getRecommendationPreferences(): Flow<RecommendationPreferences>
    suspend fun updateRecommendationPreferences(preferences: RecommendationPreferences)
    suspend fun getDislikedArtists(): Flow<List<String>>
    suspend fun addDislikedArtist(artist: String)
    suspend fun removeDislikedArtist(artist: String)
    
    // Privacy and data management
    suspend fun getDataRetentionSettings(): Flow<DataRetentionSettings>
    suspend fun updateDataRetentionSettings(settings: DataRetentionSettings)
    suspend fun clearOldData(olderThanDays: Int)
    suspend fun getDataUsageStats(): DataUsageStats
    
    // Custom user settings
    suspend fun setCustomSetting(key: String, value: String)
    suspend fun getCustomSetting(key: String): Flow<String?>
    suspend fun removeCustomSetting(key: String)
    suspend fun getAllCustomSettings(): Flow<Map<String, String>>
}
