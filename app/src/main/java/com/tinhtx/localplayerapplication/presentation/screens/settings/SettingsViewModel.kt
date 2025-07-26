// TODO: Implement settings view model
package com.tinhtx.localplayerapplication.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updatePlaybackSettingUseCase: UpdatePlaybackSettingUseCase,
    private val updateLibrarySettingUseCase: UpdateLibrarySettingUseCase,
    private val updateAppearanceSettingUseCase: UpdateAppearanceSettingUseCase,
    private val updateNotificationSettingUseCase: UpdateNotificationSettingUseCase,
    private val updateStorageSettingUseCase: UpdateStorageSettingUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val importSettingsUseCase: ImportSettingsUseCase,
    private val getStorageInfoUseCase: GetStorageInfoUseCase,
    private val getAppInfoUseCase: GetAppInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                combine(
                    getSettingsUseCase(),
                    getStorageInfoUseCase(),
                    getAppInfoUseCase()
                ) { settings, storageInfo, appInfo ->
                    SettingsData(settings, storageInfo, appInfo)
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load settings"
                    )
                }.collect { settingsData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        playbackSettings = settingsData.settings.playbackSettings,
                        librarySettings = settingsData.settings.librarySettings,
                        appearanceSettings = settingsData.settings.appearanceSettings,
                        notificationSettings = settingsData.settings.notificationSettings,
                        storageSettings = settingsData.settings.storageSettings,
                        storageInfo = settingsData.storageInfo,
                        appInfo = settingsData.appInfo
                    )
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load settings"
                )
            }
        }
    }

    fun retryLoadSettings() {
        loadSettings()
    }

    fun updatePlaybackSetting(settingType: PlaybackSettingType, value: Any) {
        viewModelScope.launch {
            try {
                updatePlaybackSettingUseCase(settingType, value)
                
                // Update local state immediately for better UX
                val currentSettings = _uiState.value.playbackSettings
                val updatedSettings = when (settingType) {
                    PlaybackSettingType.GAPLESS_PLAYBACK -> currentSettings.copy(gaplessPlayback = value as Boolean)
                    PlaybackSettingType.CROSSFADE -> currentSettings.copy(crossfadeEnabled = value as Boolean)
                    PlaybackSettingType.CROSSFADE_DURATION -> currentSettings.copy(crossfadeDuration = value as Int)
                    PlaybackSettingType.AUDIO_FOCUS -> currentSettings.copy(audioFocus = value as Boolean)
                    PlaybackSettingType.EQUALIZER -> currentSettings.copy(equalizerSettings = value as EqualizerSettings)
                    PlaybackSettingType.REPLAY_GAIN -> currentSettings.copy(replayGain = value as Boolean)
                    PlaybackSettingType.AUDIO_EFFECTS -> currentSettings.copy(audioEffects = value as Boolean)
                }
                
                _uiState.value = _uiState.value.copy(playbackSettings = updatedSettings)
            } catch (exception) {
                handleError(exception, "Failed to update playback setting")
            }
        }
    }

    fun updateLibrarySetting(settingType: LibrarySettingType, value: Any) {
        viewModelScope.launch {
            try {
                updateLibrarySettingUseCase(settingType, value)
                
                val currentSettings = _uiState.value.librarySettings
                val updatedSettings = when (settingType) {
                    LibrarySettingType.AUTO_SCAN -> currentSettings.copy(autoScan = value as Boolean)
                    LibrarySettingType.SCAN_FOLDERS -> currentSettings.copy(scanFolders = value as List<String>)
                    LibrarySettingType.EXCLUDE_FOLDERS -> currentSettings.copy(excludeFolders = value as List<String>)
                    LibrarySettingType.IGNORE_SHORT_TRACKS -> currentSettings.copy(ignoreShortTracks = value as Boolean)
                    LibrarySettingType.MIN_TRACK_DURATION -> currentSettings.copy(minTrackDuration = value as Int)
                    LibrarySettingType.DOWNLOAD_ARTWORK -> currentSettings.copy(downloadArtwork = value as Boolean)
                    LibrarySettingType.DOWNLOAD_LYRICS -> currentSettings.copy(downloadLyrics = value as Boolean)
                }
                
                _uiState.value = _uiState.value.copy(librarySettings = updatedSettings)
            } catch (exception) {
                handleError(exception, "Failed to update library setting")
            }
        }
    }

    fun updateAppearanceSetting(settingType: AppearanceSettingType, value: Any) {
        viewModelScope.launch {
            try {
                updateAppearanceSettingUseCase(settingType, value)
                
                val currentSettings = _uiState.value.appearanceSettings
                val updatedSettings = when (settingType) {
                    AppearanceSettingType.THEME -> currentSettings.copy(theme = value as AppTheme)
                    AppearanceSettingType.DYNAMIC_COLORS -> currentSettings.copy(dynamicColors = value as Boolean)
                    AppearanceSettingType.ACCENT_COLOR -> currentSettings.copy(accentColor = value as String)
                    AppearanceSettingType.GRID_SIZE -> currentSettings.copy(gridSize = value as GridSize)
                    AppearanceSettingType.SHOW_ALBUM_ART -> currentSettings.copy(showAlbumArt = value as Boolean)
                    AppearanceSettingType.BLUR_BACKGROUND -> currentSettings.copy(blurBackground = value as Boolean)
                    AppearanceSettingType.ANIMATED_ARTWORK -> currentSettings.copy(animatedArtwork = value as Boolean)
                }
                
                _uiState.value = _uiState.value.copy(appearanceSettings = updatedSettings)
            } catch (exception) {
                handleError(exception, "Failed to update appearance setting")
            }
        }
    }

    fun updateNotificationSetting(settingType: NotificationSettingType, value: Any) {
        viewModelScope.launch {
            try {
                updateNotificationSettingUseCase(settingType, value)
                
                val currentSettings = _uiState.value.notificationSettings
                val updatedSettings = when (settingType) {
                    NotificationSettingType.SHOW_CONTROLS -> currentSettings.copy(showControls = value as Boolean)
                    NotificationSettingType.SHOW_ALBUM_ART -> currentSettings.copy(showAlbumArt = value as Boolean)
                    NotificationSettingType.COLORED_NOTIFICATION -> currentSettings.copy(coloredNotification = value as Boolean)
                    NotificationSettingType.PRIORITY -> currentSettings.copy(priority = value as NotificationPriority)
                    NotificationSettingType.SHOW_ON_LOCK_SCREEN -> currentSettings.copy(showOnLockScreen = value as Boolean)
                }
                
                _uiState.value = _uiState.value.copy(notificationSettings = updatedSettings)
            } catch (exception) {
                handleError(exception, "Failed to update notification setting")
            }
        }
    }

    fun updateStorageSetting(settingType: StorageSettingType, value: Any) {
        viewModelScope.launch {
            try {
                updateStorageSettingUseCase(settingType, value)
                
                val currentSettings = _uiState.value.storageSettings
                val updatedSettings = when (settingType) {
                    StorageSettingType.CACHE_SIZE -> currentSettings.copy(cacheSize = value as Long)
                    StorageSettingType.AUTO_CLEAR_CACHE -> currentSettings.copy(autoClearCache = value as Boolean)
                    StorageSettingType.CACHE_ARTWORK -> currentSettings.copy(cacheArtwork = value as Boolean)
                    StorageSettingType.CACHE_LYRICS -> currentSettings.copy(cacheLyrics = value as Boolean)
                }
                
                _uiState.value = _uiState.value.copy(storageSettings = updatedSettings)
            } catch (exception) {
                handleError(exception, "Failed to update storage setting")
            }
        }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            try {
                scanLibraryUseCase()
                android.util.Log.d("SettingsViewModel", "Library scan initiated")
            } catch (exception) {
                handleError(exception, "Failed to scan library")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                clearCacheUseCase()
                // Refresh storage info after clearing cache
                val storageInfo = getStorageInfoUseCase()
                _uiState.value = _uiState.value.copy(storageInfo = storageInfo)
                android.util.Log.d("SettingsViewModel", "Cache cleared successfully")
            } catch (exception) {
                handleError(exception, "Failed to clear cache")
            }
        }
    }

    fun exportSettings() {
        viewModelScope.launch {
            try {
                exportSettingsUseCase()
                android.util.Log.d("SettingsViewModel", "Settings exported successfully")
            } catch (exception) {
                handleError(exception, "Failed to export settings")
            }
        }
    }

    fun importSettings() {
        viewModelScope.launch {
            try {
                importSettingsUseCase()
                // Reload settings after import
                loadSettings()
                android.util.Log.d("SettingsViewModel", "Settings imported successfully")
            } catch (exception) {
                handleError(exception, "Failed to import settings")
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                resetSettingsUseCase()
                // Reload settings after reset
                loadSettings()
                _uiState.value = _uiState.value.copy(showResetDialog = false)
                android.util.Log.d("SettingsViewModel", "Settings reset to defaults")
            } catch (exception) {
                handleError(exception, "Failed to reset settings")
            }
        }
    }

    fun showResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = true)
    }

    fun hideResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = false)
    }

    private fun handleError(exception: Throwable, message: String) {
        android.util.Log.e("SettingsViewModel", message, exception)
        _uiState.value = _uiState.value.copy(
            error = exception.message ?: message
        )
    }
}

private data class SettingsData(
    val settings: AppSettings,
    val storageInfo: StorageInfo,
    val appInfo: AppInfo
)

data class AppSettings(
    val playbackSettings: PlaybackSettings,
    val librarySettings: LibrarySettings,
    val appearanceSettings: AppearanceSettings,
    val notificationSettings: NotificationSettings,
    val storageSettings: StorageSettings
)
