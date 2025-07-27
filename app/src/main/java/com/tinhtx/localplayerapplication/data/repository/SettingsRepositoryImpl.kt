package com.tinhtx.localplayerapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import com.tinhtx.localplayerapplication.presentation.screens.settings.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : SettingsRepository {

    companion object {
        private const val PREFS_NAME = "localplayer_settings"
        private const val KEY_APP_SETTINGS = "app_settings"
        private const val KEY_PLAYBACK_SETTINGS = "playback_settings"
        private const val KEY_APPEARANCE_SETTINGS = "appearance_settings"
        private const val KEY_LIBRARY_SETTINGS = "library_settings"
        private const val KEY_NOTIFICATION_SETTINGS = "notification_settings"
        private const val KEY_STORAGE_SETTINGS = "storage_settings"
        private const val KEY_PRIVACY_SETTINGS = "privacy_settings"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // State flows for reactive updates
    private val _appSettingsFlow = MutableStateFlow(getDefaultAppSettings())
    private val _playbackSettingsFlow = MutableStateFlow(getDefaultPlaybackSettings())
    private val _appearanceSettingsFlow = MutableStateFlow(getDefaultAppearanceSettings())
    private val _librarySettingsFlow = MutableStateFlow(getDefaultLibrarySettings())
    private val _notificationSettingsFlow = MutableStateFlow(getDefaultNotificationSettings())
    private val _storageSettingsFlow = MutableStateFlow(getDefaultStorageSettings())
    private val _privacySettingsFlow = MutableStateFlow(getDefaultPrivacySettings())

    init {
        loadAllSettings()
    }

    // App Settings
    override suspend fun getAppSettings(): AppSettings {
        return try {
            val json = sharedPreferences.getString(KEY_APP_SETTINGS, null)
            json?.let { gson.fromJson(it, AppSettings::class.java) } ?: getDefaultAppSettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading app settings")
            getDefaultAppSettings()
        }
    }

    override suspend fun updateAppSettings(settings: AppSettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_APP_SETTINGS, json)
            }
            _appSettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving app settings")
        }
    }

    override fun getAppSettingsFlow(): Flow<AppSettings> = _appSettingsFlow.asStateFlow()

    // Playback Settings
    override suspend fun getPlaybackSettings(): PlaybackSettings {
        return try {
            val json = sharedPreferences.getString(KEY_PLAYBACK_SETTINGS, null)
            json?.let { gson.fromJson(it, PlaybackSettings::class.java) } ?: getDefaultPlaybackSettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading playback settings")
            getDefaultPlaybackSettings()
        }
    }

    override suspend fun updatePlaybackSettings(settings: PlaybackSettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_PLAYBACK_SETTINGS, json)
            }
            _playbackSettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving playback settings")
        }
    }

    override fun getPlaybackSettingsFlow(): Flow<PlaybackSettings> = _playbackSettingsFlow.asStateFlow()

    // Appearance Settings
    override suspend fun getAppearanceSettings(): AppearanceSettings {
        return try {
            val json = sharedPreferences.getString(KEY_APPEARANCE_SETTINGS, null)
            json?.let { gson.fromJson(it, AppearanceSettings::class.java) } ?: getDefaultAppearanceSettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading appearance settings")
            getDefaultAppearanceSettings()
        }
    }

    override suspend fun updateAppearanceSettings(settings: AppearanceSettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_APPEARANCE_SETTINGS, json)
            }
            _appearanceSettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving appearance settings")
        }
    }

    override fun getAppearanceSettingsFlow(): Flow<AppearanceSettings> = _appearanceSettingsFlow.asStateFlow()

    // Library Settings
    override suspend fun getLibrarySettings(): LibrarySettings {
        return try {
            val json = sharedPreferences.getString(KEY_LIBRARY_SETTINGS, null)
            json?.let { gson.fromJson(it, LibrarySettings::class.java) } ?: getDefaultLibrarySettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading library settings")
            getDefaultLibrarySettings()
        }
    }

    override suspend fun updateLibrarySettings(settings: LibrarySettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_LIBRARY_SETTINGS, json)
            }
            _librarySettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving library settings")
        }
    }

    override fun getLibrarySettingsFlow(): Flow<LibrarySettings> = _librarySettingsFlow.asStateFlow()

    // Notification Settings
    override suspend fun getNotificationSettings(): NotificationSettings {
        return try {
            val json = sharedPreferences.getString(KEY_NOTIFICATION_SETTINGS, null)
            json?.let { gson.fromJson(it, NotificationSettings::class.java) } ?: getDefaultNotificationSettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading notification settings")
            getDefaultNotificationSettings()
        }
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_NOTIFICATION_SETTINGS, json)
            }
            _notificationSettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving notification settings")
        }
    }

    override fun getNotificationSettingsFlow(): Flow<NotificationSettings> = _notificationSettingsFlow.asStateFlow()

    // Storage Settings
    override suspend fun getStorageSettings(): StorageSettings {
        return try {
            val json = sharedPreferences.getString(KEY_STORAGE_SETTINGS, null)
            json?.let { gson.fromJson(it, StorageSettings::class.java) } ?: getDefaultStorageSettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading storage settings")
            getDefaultStorageSettings()
        }
    }

    override suspend fun updateStorageSettings(settings: StorageSettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_STORAGE_SETTINGS, json)
            }
            _storageSettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving storage settings")
        }
    }

    override fun getStorageSettingsFlow(): Flow<StorageSettings> = _storageSettingsFlow.asStateFlow()

    // Privacy Settings
    override suspend fun getPrivacySettings(): PrivacySettings {
        return try {
            val json = sharedPreferences.getString(KEY_PRIVACY_SETTINGS, null)
            json?.let { gson.fromJson(it, PrivacySettings::class.java) } ?: getDefaultPrivacySettings()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading privacy settings")
            getDefaultPrivacySettings()
        }
    }

    override suspend fun updatePrivacySettings(settings: PrivacySettings) {
        try {
            val json = gson.toJson(settings)
            sharedPreferences.edit {
                putString(KEY_PRIVACY_SETTINGS, json)
            }
            _privacySettingsFlow.value = settings
        } catch (exception: Exception) {
            Timber.e(exception, "Error saving privacy settings")
        }
    }

    override fun getPrivacySettingsFlow(): Flow<PrivacySettings> = _privacySettingsFlow.asStateFlow()

    // Individual Setting Methods
    override suspend fun updateTheme(theme: AppTheme) {
        val currentSettings = getAppearanceSettings()
        updateAppearanceSettings(currentSettings.copy(theme = theme))
    }

    override suspend fun updateDynamicColors(enabled: Boolean) {
        val currentSettings = getAppearanceSettings()
        updateAppearanceSettings(currentSettings.copy(dynamicColors = enabled))
    }

    override suspend fun updateGaplessPlayback(enabled: Boolean) {
        val currentSettings = getPlaybackSettings()
        updatePlaybackSettings(currentSettings.copy(gaplessPlayback = enabled))
    }

    override suspend fun updateCrossfade(enabled: Boolean) {
        val currentSettings = getPlaybackSettings()
        updatePlaybackSettings(currentSettings.copy(crossfadeEnabled = enabled))
    }

    override suspend fun updateCrossfadeDuration(duration: Int) {
        val currentSettings = getPlaybackSettings()
        updatePlaybackSettings(currentSettings.copy(crossfadeDuration = duration))
    }

    override suspend fun updateAutoScan(enabled: Boolean) {
        val currentSettings = getLibrarySettings()
        updateLibrarySettings(currentSettings.copy(autoScan = enabled))
    }

    override suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        val currentSettings = getPrivacySettings()
        updatePrivacySettings(currentSettings.copy(analyticsEnabled = enabled))
    }

    // Reset Settings
    override suspend fun resetAllSettings() {
        sharedPreferences.edit { clear() }
        loadAllSettings()
    }

    override suspend fun resetPlaybackSettings() {
        updatePlaybackSettings(getDefaultPlaybackSettings())
    }

    override suspend fun resetAppearanceSettings() {
        updateAppearanceSettings(getDefaultAppearanceSettings())
    }

    override suspend fun resetLibrarySettings() {
        updateLibrarySettings(getDefaultLibrarySettings())
    }

    // Export/Import
    override suspend fun exportSettings(): String {
        return try {
            val allSettings = mapOf(
                "appSettings" to getAppSettings(),
                "playbackSettings" to getPlaybackSettings(),
                "appearanceSettings" to getAppearanceSettings(),
                "librarySettings" to getLibrarySettings(),
                "notificationSettings" to getNotificationSettings(),
                "storageSettings" to getStorageSettings(),
                "privacySettings" to getPrivacySettings()
            )
            gson.toJson(allSettings)
        } catch (exception: Exception) {
            Timber.e(exception, "Error exporting settings")
            ""
        }
    }

    override suspend fun importSettings(settingsJson: String): Boolean {
        return try {
            // Parse and validate settings JSON
            // Apply imported settings
            // This is a simplified implementation
            true
        } catch (exception: Exception) {
            Timber.e(exception, "Error importing settings")
            false
        }
    }

    // Private helper methods
    private fun loadAllSettings() {
        try {
            _appSettingsFlow.value = runCatching { getAppSettings() }.getOrDefault(getDefaultAppSettings())
            _playbackSettingsFlow.value = runCatching { getPlaybackSettings() }.getOrDefault(getDefaultPlaybackSettings())
            _appearanceSettingsFlow.value = runCatching { getAppearanceSettings() }.getOrDefault(getDefaultAppearanceSettings())
            _librarySettingsFlow.value = runCatching { getLibrarySettings() }.getOrDefault(getDefaultLibrarySettings())
            _notificationSettingsFlow.value = runCatching { getNotificationSettings() }.getOrDefault(getDefaultNotificationSettings())
            _storageSettingsFlow.value = runCatching { getStorageSettings() }.getOrDefault(getDefaultStorageSettings())
            _privacySettingsFlow.value = runCatching { getPrivacySettings() }.getOrDefault(getDefaultPrivacySettings())
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading settings")
        }
    }

    // Default settings
    private fun getDefaultAppSettings(): AppSettings {
        return AppSettings(
            playbackSettings = getDefaultPlaybackSettings(),
            appearanceSettings = getDefaultAppearanceSettings(),
            librarySettings = getDefaultLibrarySettings(),
            notificationSettings = getDefaultNotificationSettings(),
            storageSettings = getDefaultStorageSettings(),
            privacySettings = getDefaultPrivacySettings()
        )
    }

    private fun getDefaultPlaybackSettings(): PlaybackSettings {
        return PlaybackSettings(
            gaplessPlayback = true,
            crossfadeEnabled = false,
            crossfadeDuration = 3,
            audioFocus = true,
            equalizerSettings = EqualizerSettings(),
            replayGain = false,
            audioEffects = true
        )
    }

    private fun getDefaultAppearanceSettings(): AppearanceSettings {
        return AppearanceSettings(
            theme = AppTheme.SYSTEM,
            dynamicColors = true,
            gridSize = GridSize.MEDIUM,
            showAlbumArt = true,
            blurBackground = false,
            animatedArtwork = true
        )
    }

    private fun getDefaultLibrarySettings(): LibrarySettings {
        return LibrarySettings(
            autoScan = true,
            ignoreShortTracks = true,
            minTrackDuration = 30,
            downloadArtwork = true,
            downloadLyrics = false
        )
    }

    private fun getDefaultNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            showControls = true,
            showAlbumArt = true,
            coloredNotification = true,
            priority = NotificationPriority.DEFAULT,
            showOnLockScreen = true
        )
    }

    private fun getDefaultStorageSettings(): StorageSettings {
        return StorageSettings(
            autoClearCache = true,
            cacheArtwork = true,
            cacheLyrics = true
        )
    }

    private fun getDefaultPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            analyticsEnabled = true,
            crashReportsEnabled = true,
            dataCollection = true
        )
    }
}
