package com.tinhtx.localplayerapplication.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.settings.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.library.*
import com.tinhtx.localplayerapplication.domain.usecase.backup.*
import com.tinhtx.localplayerapplication.domain.usecase.profile.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * ViewModel for Settings Screen - Complete integration with settings use cases
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // Settings Use Cases
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    
    // Library Use Cases
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
    
    // Backup Use Cases
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val getBackupsUseCase: GetBackupsUseCase,
    
    // Profile Use Cases
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    
    // Cache Use Cases
    private val getCacheSizeUseCase: GetCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var scanJob: Job? = null

    init {
        loadAllSettings()
        observeSettings()
        loadLibraryStats()
        loadCacheInfo()
        loadUserProfile()
    }

    // =================================================================================
    // SETTINGS LOADING
    // =================================================================================

    /**
     * Load all settings
     */
    private fun loadAllSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(settingsLoading = true)
            
            try {
                val settings = getAppSettingsUseCase.getAppSettings().first()
                _uiState.value = _uiState.value.copyWithSettings(settings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    settingsLoading = false,
                    settingsError = "Failed to load settings: ${e.message}"
                )
            }
        }
    }

    /**
     * Observe settings changes
     */
    private fun observeSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase.getAppSettings()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Settings error: ${e.message}")
                }
                .collect { settings ->
                    _uiState.value = _uiState.value.copyWithSettings(settings)
                }
        }
    }

    // =================================================================================
    // APPEARANCE SETTINGS
    // =================================================================================

    /**
     * Update theme
     */
    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(theme = theme)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(
                    theme = theme,
                    showThemeSelectionDialog = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update theme: ${e.message}")
            }
        }
    }

    /**
     * Update accent color
     */
    fun updateAccentColor(color: AccentColor) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(accentColor = color)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(accentColor = color)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update accent color: ${e.message}")
            }
        }
    }

    /**
     * Toggle dynamic color
     */
    fun toggleDynamicColor() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val newValue = !_uiState.value.isDynamicColor
                val updatedSettings = currentSettings.copy(isDynamicColor = newValue)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(isDynamicColor = newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to toggle dynamic color: ${e.message}")
            }
        }
    }

    /**
     * Update font size
     */
    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(fontSize = fontSize)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(fontSize = fontSize)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update font size: ${e.message}")
            }
        }
    }

    /**
     * Update grid size
     */
    fun updateGridSize(gridSize: GridSize) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(gridSize = gridSize)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(gridSize = gridSize)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update grid size: ${e.message}")
            }
        }
    }

    // =================================================================================
    // PLAYBACK SETTINGS
    // =================================================================================

    /**
     * Update audio quality
     */
    fun updateAudioQuality(quality: AudioQuality) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(audioQuality = quality)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(audioQuality = quality)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update audio quality: ${e.message}")
            }
        }
    }

    /**
     * Update crossfade duration
     */
    fun updateCrossfadeDuration(duration: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(crossfadeDuration = duration)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(crossfadeDuration = duration)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update crossfade: ${e.message}")
            }
        }
    }

    /**
     * Update replay gain mode
     */
    fun updateReplayGainMode(mode: ReplayGainMode) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.appSettings
                val updatedSettings = currentSettings.copy(replayGain = mode)
                updateAppSettingsUseCase.execute(updatedSettings)
                
                _uiState.value = _uiState.value.copy(replayGainMode = mode)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update replay gain: ${e.message}")
            }
        }
    }

    /**
     * Toggle skip silence
     */
    fun toggleSkipSilence() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.skipSilence
                // TODO: Update in settings
                _uiState.value = _uiState.value.copy(skipSilence = newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to toggle skip silence: ${e.message}")
            }
        }
    }

    /**
     * Toggle resume on headphone connect
     */
    fun toggleResumeOnHeadphoneConnect() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.resumeOnHeadphoneConnect
                // TODO: Update in settings
                _uiState.value = _uiState.value.copy(resumeOnHeadphoneConnect = newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to toggle headphone resume: ${e.message}")
            }
        }
    }

    // =================================================================================
    // LIBRARY SETTINGS
    // =================================================================================

    /**
     * Load library stats
     */
    private fun loadLibraryStats() {
        viewModelScope.launch {
            try {
                getLibraryStatsUseCase.execute().fold(
                    onSuccess = { stats ->
                        _uiState.value = _uiState.value.copy(
                            totalSongs = stats.totalSongs,
                            totalAlbums = stats.totalAlbums,
                            totalArtists = stats.totalArtists,
                            lastScanTime = stats.lastScanTime
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to load library stats: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load library stats: ${e.message}")
            }
        }
    }

    /**
     * Scan music library
     */
    fun scanLibrary() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanCurrentFile = ""
                )
                
                scanLibraryUseCase.execute().fold(
                    onSuccess = { scanFlow ->
                        scanFlow.collect { scanResult ->
                            _uiState.value = _uiState.value.copy(
                                scanProgress = scanResult.progress,
                                scanCurrentFile = scanResult.currentFile,
                                totalSongs = scanResult.totalSongs,
                                totalAlbums = scanResult.totalAlbums,
                                totalArtists = scanResult.totalArtists
                            )
                            
                            if (scanResult.isCompleted) {
                                _uiState.value = _uiState.value.copy(
                                    isScanning = false,
                                    lastScanTime = System.currentTimeMillis()
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isScanning = false,
                            scanProgress = 0f
                        )
                        _uiState.value = _uiState.value.copyWithError("Scan failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanProgress = 0f
                )
                _uiState.value = _uiState.value.copyWithError("Scan failed: ${e.message}")
            }
        }
    }

    /**
     * Cancel library scan
     */
    fun cancelLibraryScan() {
        scanJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            scanProgress = 0f,
            scanCurrentFile = ""
        )
    }

    /**
     * Toggle scan on startup
     */
    fun toggleScanOnStartup() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.scanOnStartup
                // TODO: Update in settings
                _uiState.value = _uiState.value.copy(scanOnStartup = newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to toggle scan on startup: ${e.message}")
            }
        }
    }

    // =================================================================================
    // STORAGE SETTINGS
    // =================================================================================

    /**
     * Load cache information
     */
    private fun loadCacheInfo() {
        viewModelScope.launch {
            try {
                getCacheSizeUseCase.execute().fold(
                    onSuccess = { cacheInfo ->
                        _uiState.value = _uiState.value.copy(
                            cacheSize = cacheInfo.totalCacheSize,
                            thumbnailCacheSize = cacheInfo.thumbnailCacheSize
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to load cache info: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load cache info: ${e.message}")
            }
        }
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isClearingCache = true)
                
                clearCacheUseCase.execute().fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            cacheSize = 0L,
                            thumbnailCacheSize = 0L,
                            isClearingCache = false,
                            showStorageCleanupDialog = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isClearingCache = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to clear cache: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isClearingCache = false)
                _uiState.value = _uiState.value.copyWithError("Failed to clear cache: ${e.message}")
            }
        }
    }

    /**
     * Update max cache size
     */
    fun updateMaxCacheSize(size: Long) {
        viewModelScope.launch {
            try {
                // TODO: Update in settings
                _uiState.value = _uiState.value.copy(maxCacheSize = size)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update cache size: ${e.message}")
            }
        }
    }

    // =================================================================================
    // SLEEP TIMER
    // =================================================================================

    /**
     * Start sleep timer
     */
    fun startSleepTimer(durationMinutes: Int) {
        stopSleepTimer() // Stop any existing timer
        
        sleepTimerJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sleepTimerEnabled = true,
                sleepTimerDuration = durationMinutes,
                sleepTimerRemainingTime = durationMinutes * 60 * 1000L,
                showSleepTimerDialog = false
            )
            
            // Count down every second
            while (_uiState.value.sleepTimerRemainingTime > 0) {
                delay(1000)
                val remaining = _uiState.value.sleepTimerRemainingTime - 1000
                _uiState.value = _uiState.value.copy(sleepTimerRemainingTime = remaining)
            }
            
            // Timer finished
            _uiState.value = _uiState.value.copy(
                sleepTimerEnabled = false,
                sleepTimerRemainingTime = 0L
            )
            
            // Execute sleep timer action
            executeSleepTimerAction()
        }
    }

    /**
     * Stop sleep timer
     */
    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            sleepTimerEnabled = false,
            sleepTimerRemainingTime = 0L
        )
    }

    /**
     * Execute sleep timer action
     */
    private fun executeSleepTimerAction() {
        when (_uiState.value.sleepTimerAction) {
            SleepTimerAction.PAUSE -> {
                // TODO: Pause playback
            }
            SleepTimerAction.STOP -> {
                // TODO: Stop playback
            }
            SleepTimerAction.EXIT -> {
                // TODO: Exit app
            }
        }
    }

    /**
     * Update sleep timer action
     */
    fun updateSleepTimerAction(action: SleepTimerAction) {
        _uiState.value = _uiState.value.copy(sleepTimerAction = action)
    }

    // =================================================================================
    // PROFILE SETTINGS
    // =================================================================================

    /**
     * Load user profile
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                getUserProfileUseCase.execute().fold(
                    onSuccess = { profile ->
                        _uiState.value = _uiState.value.copy(
                            userName = profile.name,
                            userEmail = profile.email,
                            userAvatar = profile.avatarUrl,
                            isLoggedIn = profile.isLoggedIn,
                            loginMethod = profile.loginMethod,
                            lastBackupTime = profile.lastBackupTime,
                            autoBackup = profile.autoBackup
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to load profile: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load profile: ${e.message}")
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateUserProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdatingProfile = true)
                
                val profile = UserProfile(
                    name = name,
                    email = email,
                    avatarUrl = _uiState.value.userAvatar,
                    isLoggedIn = _uiState.value.isLoggedIn,
                    loginMethod = _uiState.value.loginMethod,
                    lastBackupTime = _uiState.value.lastBackupTime,
                    autoBackup = _uiState.value.autoBackup
                )
                
                updateUserProfileUseCase.execute(profile).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            userName = name,
                            userEmail = email,
                            isUpdatingProfile = false,
                            showProfileDialog = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isUpdatingProfile = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to update profile: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdatingProfile = false)
                _uiState.value = _uiState.value.copyWithError("Failed to update profile: ${e.message}")
            }
        }
    }

    // =================================================================================
    // BACKUP & RESTORE
    // =================================================================================

    /**
     * Create backup
     */
    fun createBackup() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isBackingUp = true, backupProgress = 0f)
                
                createBackupUseCase.execute().fold(
                    onSuccess = { backupFlow ->
                        backupFlow.collect { progress ->
                            _uiState.value = _uiState.value.copy(backupProgress = progress)
                            
                            if (progress >= 1f) {
                                _uiState.value = _uiState.value.copy(
                                    isBackingUp = false,
                                    lastBackupTime = System.currentTimeMillis(),
                                    showBackupDialog = false
                                )
                                loadAvailableBackups()
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isBackingUp = false, backupProgress = 0f)
                        _uiState.value = _uiState.value.copyWithError("Backup failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isBackingUp = false, backupProgress = 0f)
                _uiState.value = _uiState.value.copyWithError("Backup failed: ${e.message}")
            }
        }
    }

    /**
     * Load available backups
     */
    private fun loadAvailableBackups() {
        viewModelScope.launch {
            try {
                getBackupsUseCase.execute().fold(
                    onSuccess = { backups ->
                        _uiState.value = _uiState.value.copy(availableBackups = backups)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to load backups: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load backups: ${e.message}")
            }
        }
    }

    /**
     * Restore from backup
     */
    fun restoreFromBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRestoring = true, restoreProgress = 0f)
                
                restoreBackupUseCase.execute(backupFile.path).fold(
                    onSuccess = { restoreFlow ->
                        restoreFlow.collect { progress ->
                            _uiState.value = _uiState.value.copy(restoreProgress = progress)
                            
                            if (progress >= 1f) {
                                _uiState.value = _uiState.value.copy(
                                    isRestoring = false,
                                    showRestoreDialog = false
                                )
                                // Reload all settings after restore
                                loadAllSettings()
                                loadLibraryStats()
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isRestoring = false, restoreProgress = 0f)
                        _uiState.value = _uiState.value.copyWithError("Restore failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRestoring = false, restoreProgress = 0f)
                _uiState.value = _uiState.value.copyWithError("Restore failed: ${e.message}")
            }
        }
    }

    // =================================================================================
    // RESET SETTINGS
    // =================================================================================

    /**
     * Reset all settings to default
     */
    fun resetSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isResetting = true, resetProgress = 0f)
                
                resetSettingsUseCase.execute().fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isResetting = false,
                            resetProgress = 1f,
                            showResetDialog = false
                        )
                        // Reload settings
                        loadAllSettings()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isResetting = false, resetProgress = 0f)
                        _uiState.value = _uiState.value.copyWithError("Reset failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isResetting = false, resetProgress = 0f)
                _uiState.value = _uiState.value.copyWithError("Reset failed: ${e.message}")
            }
        }
    }

    /**
     * Factory reset
     */
    fun factoryReset() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isResetting = true, resetProgress = 0f)
                
                // TODO: Implement factory reset
                // This should clear all data including library, playlists, etc.
                
                delay(3000) // Simulate reset process
                
                _uiState.value = _uiState.value.copy(
                    isResetting = false,
                    resetProgress = 1f,
                    showFactoryResetDialog = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isResetting = false, resetProgress = 0f)
                _uiState.value = _uiState.value.copyWithError("Factory reset failed: ${e.message}")
            }
        }
    }

    // =================================================================================
    // UI STATE MANAGEMENT
    // =================================================================================

    /**
     * Toggle theme selection dialog
     */
    fun toggleThemeSelectionDialog() {
        _uiState.value = _uiState.value.copy(
            showThemeSelectionDialog = !_uiState.value.showThemeSelectionDialog
        )
    }

    /**
     * Toggle sleep timer dialog
     */
    fun toggleSleepTimerDialog() {
        _uiState.value = _uiState.value.copy(
            showSleepTimerDialog = !_uiState.value.showSleepTimerDialog
        )
    }

    /**
     * Toggle storage cleanup dialog
     */
    fun toggleStorageCleanupDialog() {
        _uiState.value = _uiState.value.copy(
            showStorageCleanupDialog = !_uiState.value.showStorageCleanupDialog
        )
    }

    /**
     * Toggle profile dialog
     */
    fun toggleProfileDialog() {
        _uiState.value = _uiState.value.copy(
            showProfileDialog = !_uiState.value.showProfileDialog
        )
    }

    /**
     * Toggle backup dialog
     */
    fun toggleBackupDialog() {
        _uiState.value = _uiState.value.copy(
            showBackupDialog = !_uiState.value.showBackupDialog
        )
        
        if (_uiState.value.showBackupDialog) {
            loadAvailableBackups()
        }
    }

    /**
     * Toggle restore dialog
     */
    fun toggleRestoreDialog() {
        _uiState.value = _uiState.value.copy(
            showRestoreDialog = !_uiState.value.showRestoreDialog
        )
        
        if (_uiState.value.showRestoreDialog) {
            loadAvailableBackups()
        }
    }

    /**
     * Toggle reset dialog
     */
    fun toggleResetDialog() {
        _uiState.value = _uiState.value.copy(
            showResetDialog = !_uiState.value.showResetDialog
        )
    }

    /**
     * Toggle factory reset dialog
     */
    fun toggleFactoryResetDialog() {
        _uiState.value = _uiState.value.copy(
            showFactoryResetDialog = !_uiState.value.showFactoryResetDialog
        )
    }

    /**
     * Toggle developer options
     */
    fun toggleDeveloperOptions() {
        _uiState.value = _uiState.value.copy(
            showDeveloperOptions = !_uiState.value.showDeveloperOptions
        )
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null,
            settingsError = null
        )
    }

    /**
     * Refresh all settings
     */
    fun refreshSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                loadAllSettings()
                loadLibraryStats()
                loadCacheInfo()
                loadUserProfile()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to refresh: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        scanJob?.cancel()
    }
}
