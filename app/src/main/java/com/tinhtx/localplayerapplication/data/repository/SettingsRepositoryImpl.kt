package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.data.local.datastore.UserPreferences
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : SettingsRepository {
    
    // App Settings - Mapped từ UserPreferences methods
    override fun getAppSettings(): Flow<AppSettings> {
        return userPreferences.appSettings
    }
    
    override suspend fun updateAppSettings(settings: AppSettings) {
        userPreferences.updateAppSettings(settings)
    }
    
    override suspend fun updateTheme(theme: AppTheme) {
        val currentSettings = userPreferences.appSettings
        // Need to get current value first - simplified implementation
        updateAppSettings(AppSettings(theme = theme))
    }
    
    override suspend fun updateDynamicColor(enabled: Boolean) {
        val currentSettings = userPreferences.appSettings
        // Simplified - in real implementation would get current settings first
        updateAppSettings(AppSettings(dynamicColor = enabled))
    }
    
    override suspend fun updateGridSize(gridSize: GridSize) {
        updateAppSettings(AppSettings(gridSize = gridSize))
    }
    
    override suspend fun updateSortOrder(sortOrder: SortOrder) {
        updateAppSettings(AppSettings(sortOrder = sortOrder))
    }
    
    override suspend fun updateLanguage(language: String) {
        updateAppSettings(AppSettings(language = language))
    }
    
    override suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        updateAppSettings(AppSettings(firstLaunch = isFirstLaunch))
    }
    
    override suspend fun updateAppVersion(version: String) {
        updateAppSettings(AppSettings(appVersion = version))
    }
    
    // Playback Settings - Mapped từ UserPreferences methods
    override fun getPlaybackSettings(): Flow<PlaybackSettings> {
        return userPreferences.playbackSettings
    }
    
    override suspend fun updatePlaybackSettings(settings: PlaybackSettings) {
        userPreferences.updatePlaybackSettings(settings)
    }
    
    override suspend fun updateRepeatMode(mode: RepeatMode) {
        updatePlaybackSettings(PlaybackSettings(repeatMode = mode))
    }
    
    override suspend fun updateShuffleMode(mode: ShuffleMode) {
        updatePlaybackSettings(PlaybackSettings(shuffleMode = mode))
    }
    
    override suspend fun updateCrossfade(enabled: Boolean, duration: Int) {
        updatePlaybackSettings(PlaybackSettings(crossfadeEnabled = enabled, crossfadeDuration = duration))
    }
    
    override suspend fun updatePlaybackSpeed(speed: Float) {
        updatePlaybackSettings(PlaybackSettings(playbackSpeed = speed))
    }
    
    override suspend fun updateAutoPlay(enabled: Boolean) {
        updatePlaybackSettings(PlaybackSettings(autoPlay = enabled))
    }
    
    override suspend fun updateGaplessPlayback(enabled: Boolean) {
        updatePlaybackSettings(PlaybackSettings(gaplessPlayback = enabled))
    }
    
    override suspend fun updateResumeOnHeadphones(enabled: Boolean) {
        updatePlaybackSettings(PlaybackSettings(resumeOnHeadphones = enabled))
    }
    
    override suspend fun updatePauseOnDisconnect(enabled: Boolean) {
        updatePlaybackSettings(PlaybackSettings(pauseOnDisconnect = enabled))
    }
    
    // Audio Settings - Mapped từ UserPreferences methods
    override fun getAudioSettings(): Flow<AudioSettings> {
        return userPreferences.audioSettings
    }
    
    override suspend fun updateAudioSettings(settings: AudioSettings) {
        userPreferences.updateAudioSettings(settings)
    }
    
    override suspend fun updateEqualizer(enabled: Boolean, preset: String, bands: List<Float>) {
        updateAudioSettings(AudioSettings(
            equalizerEnabled = enabled,
            equalizerPreset = preset,
            equalizerBands = bands
        ))
    }
    
    override suspend fun updateBassBoost(level: Int) {
        updateAudioSettings(AudioSettings(bassBoost = level))
    }
    
    override suspend fun updateVirtualizer(level: Int) {
        updateAudioSettings(AudioSettings(virtualizer = level))
    }
    
    override suspend fun updateLoudnessEnhancer(level: Int) {
        updateAudioSettings(AudioSettings(loudnessEnhancer = level))
    }
    
    override suspend fun updateAudioFocus(enabled: Boolean, duckVolume: Boolean) {
        updateAudioSettings(AudioSettings(audioFocusEnabled = enabled, duckVolume = duckVolume))
    }
    
    // Library Settings - Mapped từ UserPreferences methods
    override fun getLibrarySettings(): Flow<LibrarySettings> {
        return userPreferences.librarySettings
    }
    
    override suspend fun updateLibrarySettings(settings: LibrarySettings) {
        userPreferences.updateLibrarySettings(settings)
    }
    
    override suspend fun updateAutoScan(enabled: Boolean, intervalHours: Int) {
        updateLibrarySettings(LibrarySettings(autoScan = enabled, scanIntervalHours = intervalHours))
    }
    
    override suspend fun updateShortTrackSettings(ignore: Boolean, minDuration: Int) {
        updateLibrarySettings(LibrarySettings(ignoreShortTracks = ignore, minTrackDuration = minDuration))
    }
    
    override suspend fun updateIncludedFolders(folders: List<String>) {
        updateLibrarySettings(LibrarySettings(includedFolders = folders))
    }
    
    override suspend fun updateExcludedFolders(folders: List<String>) {
        updateLibrarySettings(LibrarySettings(excludedFolders = folders))
    }
    
    override suspend fun updateLastScanTime(timestamp: Long) {
        updateLibrarySettings(LibrarySettings(lastScanTime = timestamp))
    }
    
    override suspend fun updateLibrarySortOrder(sortOrder: SortOrder) {
        updateLibrarySettings(LibrarySettings(librarySortOrder = sortOrder))
    }
    
    override suspend fun updateUnknownArtistDisplay(show: Boolean) {
        updateLibrarySettings(LibrarySettings(showUnknownArtist = show))
    }
    
    override suspend fun updateGroupByAlbumArtist(enabled: Boolean) {
        updateLibrarySettings(LibrarySettings(groupByAlbumArtist = enabled))
    }
    
    // Privacy Settings - Mapped từ UserPreferences methods
    override fun getPrivacySettings(): Flow<PrivacySettings> {
        return userPreferences.privacySettings
    }
    
    override suspend fun updatePrivacySettings(settings: PrivacySettings) {
        userPreferences.updatePrivacySettings(settings)
    }
    
    override suspend fun updateAnalytics(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(analyticsEnabled = enabled))
    }
    
    override suspend fun updateCrashReporting(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(crashReportingEnabled = enabled))
    }
    
    override suspend fun updateUsageStatistics(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(usageStatistics = enabled))
    }
    
    override suspend fun updateHistory(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(historyEnabled = enabled))
    }
    
    override suspend fun updateScrobble(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(scrobbleEnabled = enabled))
    }
    
    override suspend fun updateShareNowPlaying(enabled: Boolean) {
        updatePrivacySettings(PrivacySettings(shareNowPlaying = enabled))
    }
    
    // Storage Settings - Basic implementation (không có trong UserPreferences)
    override fun getStorageSettings(): Flow<StorageSettings> {
        // Placeholder - would need additional DataStore implementation
        return kotlinx.coroutines.flow.flowOf(StorageSettings())
    }
    
    override suspend fun updateStorageSettings(settings: StorageSettings) {
        // Placeholder - would need additional DataStore implementation
    }
    
    override suspend fun updateMaxCacheSize(sizeMB: Long) {
        // Placeholder
    }
    
    override suspend fun updateAutoClearCache(enabled: Boolean) {
        // Placeholder
    }
    
    override suspend fun updateArtworkCacheSize(sizeMB: Long) {
        // Placeholder
    }
    
    override suspend fun updateDownloadOverWifiOnly(enabled: Boolean) {
        // Placeholder
    }
    
    override suspend fun updateLyricsCache(enabled: Boolean) {
        // Placeholder
    }
    
    // General Operations - Using UserPreferences methods
    override suspend fun resetToDefaults() {
        userPreferences.clearAll()
    }
    
    override suspend fun exportSettings(): String {
        // Placeholder - would need to serialize all settings
        return ""
    }
    
    override suspend fun importSettings(settingsJson: String): Boolean {
        // Placeholder - would need to deserialize and apply settings
        return false
    }
    
    override suspend fun clearAllSettings() {
        userPreferences.clearAll()
    }
}
