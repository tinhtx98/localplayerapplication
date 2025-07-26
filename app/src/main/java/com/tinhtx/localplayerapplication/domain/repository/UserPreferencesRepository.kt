package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.local.datastore.SettingsPreferences
import com.tinhtx.localplayerapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    
    // User Profile
    val userProfile: Flow<UserProfile>
    suspend fun updateUserName(name: String)
    suspend fun updateProfileImage(uri: String)
    suspend fun updateThemeMode(themeMode: AppConstants.ThemeMode)
    suspend fun setFirstLaunchCompleted()
    suspend fun updateLastScanTime(timestamp: Long)
    suspend fun setAutoScanEnabled(enabled: Boolean)
    
    // Settings
    val settingsPreferences: Flow<SettingsPreferences>
    suspend fun updateAudioQuality(quality: String)
    suspend fun updateCrossfadeSettings(enabled: Boolean, duration: Int)
    suspend fun updateGaplessPlayback(enabled: Boolean)
    suspend fun updateRepeatMode(mode: Int)
    suspend fun updateShuffleMode(mode: Int)
    suspend fun updateVolumeLevel(level: Float)
    suspend fun updateBassBoostSettings(enabled: Boolean, strength: Int)
    suspend fun updateVirtualizerSettings(enabled: Boolean, strength: Int)
    suspend fun updateSleepTimerDuration(duration: Int)
    suspend fun updateHeadphoneSettings(autoPlay: Boolean, autoPause: Boolean)
    suspend fun updateNotificationSettings(showNotification: Boolean, showLockScreen: Boolean)
    suspend fun updateLibraryTabOrder(tabOrder: List<String>)
    suspend fun updateGridSize(size: Int)
    suspend fun updateSortOrder(sortOrder: String)
    
    // Playback State Persistence
    suspend fun savePlaybackState(
        songId: Long,
        position: Long,
        playlistId: Long?,
        queueSongIds: List<Long>,
        queueIndex: Int
    )
    suspend fun getPlaybackState(): Flow<com.tinhtx.localplayerapplication.data.repository.UserPreferencesRepositoryImpl.PlaybackState?>
}
