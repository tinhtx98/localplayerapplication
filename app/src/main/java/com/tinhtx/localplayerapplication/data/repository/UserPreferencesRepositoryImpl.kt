package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.data.local.datastore.PreferencesKeys
import com.tinhtx.localplayerapplication.data.local.datastore.UserPreferences
import com.tinhtx.localplayerapplication.domain.model.UserProfile
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserPreferencesRepository using DataStore
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : UserPreferencesRepository {
    
    // User Profile Operations - Mapped từ UserPreferences methods
    override fun getUserProfile(): Flow<UserProfile> {
        return userPreferences.userProfile
    }
    
    override suspend fun updateUserProfile(userProfile: UserProfile) {
        userPreferences.updateUserProfile(userProfile)
    }
    
    override suspend fun updateUserName(name: String) {
        userPreferences.setString(PreferencesKeys.USER_NAME, name)
    }
    
    override suspend fun updateUserEmail(email: String) {
        userPreferences.setString(PreferencesKeys.USER_EMAIL, email)
    }
    
    override suspend fun updateUserAvatar(avatarPath: String?) {
        avatarPath?.let {
            userPreferences.setString(PreferencesKeys.USER_AVATAR_PATH, it)
        }
    }
    
    override suspend fun updateLastLogin() {
        userPreferences.setLong(PreferencesKeys.USER_LAST_LOGIN, System.currentTimeMillis())
    }
    
    override suspend fun clearUserProfile() {
        userPreferences.remove(PreferencesKeys.USER_NAME)
        userPreferences.remove(PreferencesKeys.USER_EMAIL)
        userPreferences.remove(PreferencesKeys.USER_AVATAR_PATH)
    }
    
    // Simple Preference Operations - Mapped từ UserPreferences methods
    override suspend fun setString(key: String, value: String) {
        // Would need to map string key to PreferencesKey - simplified
        userPreferences.setString(PreferencesKeys.USER_NAME, value) // Placeholder
    }
    
    override suspend fun getString(key: String, defaultValue: String): Flow<String> {
        return userPreferences.getString(PreferencesKeys.USER_NAME, defaultValue) // Placeholder
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) {
        userPreferences.setBoolean(PreferencesKeys.FIRST_LAUNCH, value) // Placeholder
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        return userPreferences.getBoolean(PreferencesKeys.FIRST_LAUNCH, defaultValue) // Placeholder
    }
    
    override suspend fun setInt(key: String, value: Int) {
        userPreferences.setInt(PreferencesKeys.SCAN_INTERVAL_HOURS, value) // Placeholder
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Flow<Int> {
        return userPreferences.getInt(PreferencesKeys.SCAN_INTERVAL_HOURS, defaultValue) // Placeholder
    }
    
    override suspend fun setLong(key: String, value: Long) {
        userPreferences.setLong(PreferencesKeys.USER_LAST_LOGIN, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.USER_LAST_LOGIN, defaultValue)
    }
    
    override suspend fun setFloat(key: String, value: Float) {
        userPreferences.setFloat(PreferencesKeys.PLAYBACK_SPEED, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Flow<Float> {
        return userPreferences.getFloat(PreferencesKeys.PLAYBACK_SPEED, defaultValue)
    }
    
    // List Preferences - Basic implementation
    override suspend fun setStringList(key: String, value: List<String>) {
        // Would need proper key mapping and list serialization
    }
    
    override suspend fun getStringList(key: String, defaultValue: List<String>): Flow<List<String>> {
        return kotlinx.coroutines.flow.flowOf(defaultValue) // Placeholder
    }
    
    override suspend fun setLongList(key: String, value: List<Long>) {
        // Would need proper key mapping and list serialization
    }
    
    override suspend fun getLongList(key: String, defaultValue: List<Long>): Flow<List<Long>> {
        return kotlinx.coroutines.flow.flowOf(defaultValue) // Placeholder
    }
    
    // Session Management - Using available preference keys
    override suspend fun saveLastPlayedSong(songId: Long, position: Long) {
        userPreferences.setLong(PreferencesKeys.LAST_PLAYED_SONG_ID, songId)
        userPreferences.setLong(PreferencesKeys.LAST_PLAYED_POSITION, position)
    }
    
    override suspend fun getLastPlayedSong(): Flow<Pair<Long, Long>> {
        return kotlinx.coroutines.flow.combine(
            userPreferences.getLong(PreferencesKeys.LAST_PLAYED_SONG_ID, 0L),
            userPreferences.getLong(PreferencesKeys.LAST_PLAYED_POSITION, 0L)
        ) { songId, position ->
            Pair(songId, position)
        }
    }
    
    override suspend fun saveLastQueue(songIds: List<Long>, position: Int) {
        val serializedIds = songIds.joinToString(",")
        userPreferences.setString(PreferencesKeys.LAST_QUEUE_SONGS, serializedIds)
        userPreferences.setInt(PreferencesKeys.LAST_QUEUE_POSITION, position)
    }
    
    override suspend fun getLastQueue(): Flow<Pair<List<Long>, Int>> {
        return kotlinx.coroutines.flow.combine(
            userPreferences.getString(PreferencesKeys.LAST_QUEUE_SONGS, ""),
            userPreferences.getInt(PreferencesKeys.LAST_QUEUE_POSITION, 0)
        ) { serializedIds, position ->
            val songIds = if (serializedIds.isNotBlank()) {
                serializedIds.split(",").mapNotNull { it.toLongOrNull() }
            } else {
                emptyList()
            }
            Pair(songIds, position)
        }
    }
    
    // Statistics Preferences - Using available preference keys
    override suspend fun incrementAppLaunchCount() {
        // Would need a dedicated counter key - placeholder
    }
    
    override suspend fun getAppLaunchCount(): Flow<Int> {
        return kotlinx.coroutines.flow.flowOf(0) // Placeholder
    }
    
    override suspend fun updateTotalListeningTime(additionalTime: Long) {
        userPreferences.setLong(PreferencesKeys.TOTAL_LISTENING_TIME, additionalTime)
    }
    
    override suspend fun getTotalListeningTime(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.TOTAL_LISTENING_TIME, 0L)
    }
    
    override suspend fun updateSongsPlayedCount(count: Int) {
        userPreferences.setLong(PreferencesKeys.SONGS_PLAYED_COUNT, count.toLong())
    }
    
    override suspend fun getSongsPlayedCount(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.SONGS_PLAYED_COUNT, 0L)
    }
    
    override suspend fun updateFavoriteGenre(genre: String) {
        userPreferences.setString(PreferencesKeys.FAVORITE_GENRE, genre)
    }
    
    override suspend fun getFavoriteGenre(): Flow<String> {
        return userPreferences.getString(PreferencesKeys.FAVORITE_GENRE, "")
    }
    
    override suspend fun updateFavoriteArtist(artist: String) {
        userPreferences.setString(PreferencesKeys.FAVORITE_ARTIST, artist)
    }
    
    override suspend fun getFavoriteArtist(): Flow<String> {
        return userPreferences.getString(PreferencesKeys.FAVORITE_ARTIST, "")
    }
    
    override suspend fun updateLongestSession(duration: Long) {
        userPreferences.setLong(PreferencesKeys.LONGEST_SESSION, duration)
    }
    
    override suspend fun getLongestSession(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.LONGEST_SESSION, 0L)
    }
    
    override suspend fun updateAverageSessionLength(length: Long) {
        userPreferences.setLong(PreferencesKeys.AVERAGE_SESSION_LENGTH, length)
    }
    
    override suspend fun getAverageSessionLength(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.AVERAGE_SESSION_LENGTH, 0L)
    }
    
    override suspend fun updateMostActiveHour(hour: Int) {
        userPreferences.setInt(PreferencesKeys.MOST_ACTIVE_HOUR, hour)
    }
    
    override suspend fun getMostActiveHour(): Flow<Int> {
        return userPreferences.getInt(PreferencesKeys.MOST_ACTIVE_HOUR, 0)
    }
    
    // Goals and Achievements - Using available preference keys
    override suspend fun setWeeklyListeningGoal(goalMinutes: Long) {
        userPreferences.setLong(PreferencesKeys.WEEKLY_LISTENING_GOAL, goalMinutes)
    }
    
    override suspend fun getWeeklyListeningGoal(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.WEEKLY_LISTENING_GOAL, 0L)
    }
    
    override suspend fun setMonthlyListeningGoal(goalMinutes: Long) {
        userPreferences.setLong(PreferencesKeys.MONTHLY_LISTENING_GOAL, goalMinutes)
    }
    
    override suspend fun getMonthlyListeningGoal(): Flow<Long> {
        return userPreferences.getLong(PreferencesKeys.MONTHLY_LISTENING_GOAL, 0L)
    }
    
    override suspend fun updateWeeklyProgress(progressMinutes: Long) {
        // Would need dedicated progress keys - placeholder
    }
    
    override suspend fun getWeeklyProgress(): Flow<Long> {
        return kotlinx.coroutines.flow.flowOf(0L) // Placeholder
    }
    
    override suspend fun updateMonthlyProgress(progressMinutes: Long) {
        // Would need dedicated progress keys - placeholder
    }
    
    override suspend fun getMonthlyProgress(): Flow<Long> {
        return kotlinx.coroutines.flow.flowOf(0L) // Placeholder
    }
    
    // Backup and Restore - Using UserPreferences clear method
    override suspend fun createBackup(): String {
        // Would need to serialize all preferences - placeholder
        return ""
    }
    
    override suspend fun restoreFromBackup(backupData: String): Boolean {
        // Would need to deserialize and restore preferences - placeholder
        return false
    }
    
    override suspend fun clearAllPreferences() {
        userPreferences.clearAll()
    }
}
