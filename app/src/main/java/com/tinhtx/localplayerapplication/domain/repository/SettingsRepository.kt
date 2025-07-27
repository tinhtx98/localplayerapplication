package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    // App Settings
    suspend fun getAppSettings(): AppSettings
    suspend fun updateAppSettings(settings: AppSettings)
    fun getAppSettingsFlow(): Flow<AppSettings>

    // Playback Settings
    suspend fun getPlaybackSettings(): PlaybackSettings
    suspend fun updatePlaybackSettings(settings: PlaybackSettings)
    fun getPlaybackSettingsFlow(): Flow<PlaybackSettings>

    // Appearance Settings
    suspend fun getAppearanceSettings(): AppearanceSettings
    suspend fun updateAppearanceSettings(settings: AppearanceSettings)
    fun getAppearanceSettingsFlow(): Flow<AppearanceSettings>

    // Library Settings
    suspend fun getLibrarySettings(): LibrarySettings
    suspend fun updateLibrarySettings(settings: LibrarySettings)
    fun getLibrarySettingsFlow(): Flow<LibrarySettings>

    // Notification Settings
    suspend fun getNotificationSettings(): NotificationSettings
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    fun getNotificationSettingsFlow(): Flow<NotificationSettings>

    // Storage Settings
    suspend fun getStorageSettings(): StorageSettings
    suspend fun updateStorageSettings(settings: StorageSettings)
    fun getStorageSettingsFlow(): Flow<StorageSettings>

    // Privacy Settings
    suspend fun getPrivacySettings(): PrivacySettings
    suspend fun updatePrivacySettings(settings: PrivacySettings)
    fun getPrivacySettingsFlow(): Flow<PrivacySettings>

    // Individual Setting Methods
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateDynamicColors(enabled: Boolean)
    suspend fun updateGaplessPlayback(enabled: Boolean)
    suspend fun updateCrossfade(enabled: Boolean)
    suspend fun updateCrossfadeDuration(duration: Int)
    suspend fun updateAutoScan(enabled: Boolean)
    suspend fun updateAnalyticsEnabled(enabled: Boolean)

    // Reset Settings
    suspend fun resetAllSettings()
    suspend fun resetPlaybackSettings()
    suspend fun resetAppearanceSettings()
    suspend fun resetLibrarySettings()

    // Export/Import
    suspend fun exportSettings(): String
    suspend fun importSettings(settingsJson: String): Boolean
}
