package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings (SINGLE SOURCE)
 */
interface SettingsRepository {
    
    // Main settings flow
    fun getAppSettings(): Flow<AppSettings>
    suspend fun updateAppSettings(settings: AppSettings)
    
    // Theme & Appearance updates
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateGridSize(gridSize: GridSize)
    suspend fun updateSortOrder(sortOrder: SortOrder)
    suspend fun updateLanguage(language: String)
    
    // Playback settings updates
    suspend fun updateRepeatMode(mode: RepeatMode)
    suspend fun updateShuffleMode(mode: ShuffleMode)
    suspend fun updatePlaybackSpeed(speed: Float)
    suspend fun updateCrossfade(enabled: Boolean, duration: Int)
    suspend fun updateAutoPlay(enabled: Boolean)
    suspend fun updateGaplessPlayback(enabled: Boolean)
    suspend fun updateResumeOnHeadphones(enabled: Boolean)
    suspend fun updatePauseOnDisconnect(enabled: Boolean)
    
    // Audio settings updates
    suspend fun updateEqualizer(enabled: Boolean, preset: String, bands: List<Float>)
    suspend fun updateBassBoost(level: Int)
    suspend fun updateVirtualizer(level: Int)
    suspend fun updateLoudnessEnhancer(level: Int)
    suspend fun updateAudioFocus(enabled: Boolean, duckVolume: Boolean)
    
    // Library settings updates
    suspend fun updateAutoScan(enabled: Boolean, intervalHours: Int)
    suspend fun updateShortTrackSettings(ignore: Boolean, minDuration: Int)
    suspend fun updateIncludedFolders(folders: List<String>)
    suspend fun updateExcludedFolders(folders: List<String>)
    suspend fun updateLastScanTime(timestamp: Long)
    suspend fun updateLibrarySortOrder(sortOrder: SortOrder)
    
    // Privacy settings updates
    suspend fun updateAnalytics(enabled: Boolean)
    suspend fun updateCrashReporting(enabled: Boolean)
    suspend fun updateUsageStatistics(enabled: Boolean)
    suspend fun updateHistory(enabled: Boolean)
    suspend fun updateScrobble(enabled: Boolean)
    
    // Storage settings updates
    suspend fun updateCacheSettings(cacheSize: Long, maxCacheSize: Long, autoClear: Boolean)
    suspend fun updateDownloadSettings(location: String, quality: String, wifiOnly: Boolean)
    
    // App metadata updates
    suspend fun setFirstLaunch(isFirstLaunch: Boolean)
    suspend fun updateAppVersion(version: String)
    suspend fun updateLastUpdateCheck(timestamp: Long)
    suspend fun setTutorialCompleted(completed: Boolean)
    
    // Batch operations
    suspend fun updateThemeSettings(theme: AppTheme, dynamicColor: Boolean, gridSize: GridSize)
    suspend fun updatePlaybackSettings(
        repeatMode: RepeatMode,
        shuffleMode: ShuffleMode,
        playbackSpeed: Float,
        crossfadeEnabled: Boolean,
        crossfadeDuration: Int
    )
    suspend fun updateAudioSettings(
        equalizerEnabled: Boolean,
        equalizerPreset: String,
        equalizerBands: List<Float>,
        bassBoost: Int,
        virtualizer: Int,
        loudnessEnhancer: Int
    )
    suspend fun updateLibrarySettings(
        autoScan: Boolean,
        scanIntervalHours: Int,
        ignoreShortTracks: Boolean,
        minTrackDuration: Int,
        includedFolders: List<String>,
        excludedFolders: List<String>
    )
    suspend fun updatePrivacySettings(
        analyticsEnabled: Boolean,
        crashReporting: Boolean,
        usageStatistics: Boolean,
        historyEnabled: Boolean,
        scrobbleEnabled: Boolean
    )
    
    // Utility methods
    suspend fun resetToDefaults()
    suspend fun exportSettings(): String
    suspend fun importSettings(settingsJson: String): Boolean
    suspend fun clearAllSettings()
    suspend fun validateSettings(settings: AppSettings): List<String>
    suspend fun migrateSettings(fromVersion: String, toVersion: String): Boolean
    
    // Quick access methods
    suspend fun toggleDynamicColor(): Boolean
    suspend fun toggleEqualizer(): Boolean
    suspend fun toggleAutoScan(): Boolean
    suspend fun toggleAnalytics(): Boolean
    suspend fun cycleTheme(): AppTheme
    suspend fun cycleRepeatMode(): RepeatMode
    suspend fun toggleShuffle(): ShuffleMode
}
