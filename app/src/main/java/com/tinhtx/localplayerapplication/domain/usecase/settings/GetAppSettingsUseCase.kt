package com.tinhtx.localplayerapplication.domain.usecase.settings

import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting application settings (SIMPLIFIED)
 */
class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Get all app settings as Flow
     */
    fun getAppSettings(): Flow<AppSettings> {
        return settingsRepository.getAppSettings()
    }
    
    /**
     * Get current app settings snapshot
     */
    suspend fun getCurrentSettings(): Result<AppSettings> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current theme
     */
    suspend fun getCurrentTheme(): Result<AppTheme> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.theme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get theme as Flow
     */
    fun getThemeFlow(): Flow<AppTheme> {
        return settingsRepository.getAppSettings().map { it.theme }
    }
    
    /**
     * Check if dynamic color is enabled
     */
    suspend fun isDynamicColorEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.dynamicColor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get dynamic color as Flow
     */
    fun getDynamicColorFlow(): Flow<Boolean> {
        return settingsRepository.getAppSettings().map { it.dynamicColor }
    }
    
    /**
     * Get current grid size
     */
    suspend fun getCurrentGridSize(): Result<GridSize> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.gridSize)
        } catch (e: Exception) {
        Result.failure(e)
        }
    }
    
    /**
     * Get current sort order
     */
    suspend fun getCurrentSortOrder(): Result<SortOrder> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.sortOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current language
     */
    suspend fun getCurrentLanguage(): Result<String> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.language)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current repeat mode
     */
    suspend fun getCurrentRepeatMode(): Result<RepeatMode> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.repeatMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get repeat mode as Flow
     */
    fun getRepeatModeFlow(): Flow<RepeatMode> {
        return settingsRepository.getAppSettings().map { it.repeatMode }
    }
    
    /**
     * Get current shuffle mode
     */
    suspend fun getCurrentShuffleMode(): Result<ShuffleMode> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.shuffleMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get shuffle mode as Flow
     */
    fun getShuffleModeFlow(): Flow<ShuffleMode> {
        return settingsRepository.getAppSettings().map { it.shuffleMode }
    }
    
    /**
     * Get current playback speed
     */
    suspend fun getPlaybackSpeed(): Result<Float> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.playbackSpeed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if crossfade is enabled
     */
    suspend fun isCrossfadeEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.crossfadeEnabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get crossfade duration
     */
    suspend fun getCrossfadeDuration(): Result<Int> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.crossfadeDuration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if auto play is enabled
     */
    suspend fun isAutoPlayEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.autoPlay)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if gapless playback is enabled
     */
    suspend fun isGaplessPlaybackEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.gaplessPlayback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if equalizer is enabled
     */
    suspend fun isEqualizerEnabled(): Result<Boolean> {  
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.equalizerEnabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get equalizer preset
     */
    suspend fun getEqualizerPreset(): Result<String> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.equalizerPreset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get equalizer bands
     */
    suspend fun getEqualizerBands(): Result<List<Float>> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.equalizerBands)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if auto scan is enabled
     */
    suspend fun isAutoScanEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.autoScan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get scan interval hours
     */
    suspend fun getScanIntervalHours(): Result<Int> {
        return try {  
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.scanIntervalHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get included folders
     */
    suspend fun getIncludedFolders(): Result<List<String>> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.includedFolders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get excluded folders
     */
    suspend fun getExcludedFolders(): Result<List<String>> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.excludedFolders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if this is first launch
     */
    suspend fun isFirstLaunch(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.firstLaunch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get app version
     */
    suspend fun getAppVersion(): Result<String> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.appVersion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if analytics is enabled
     */
    suspend fun isAnalyticsEnabled(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.analyticsEnabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get settings summary for display
     */
    suspend fun getSettingsSummary(): Result<SettingsSummary> {
        return try {
            val settings = settingsRepository.getAppSettings().first()  
            
            val summary = SettingsSummary(
                theme = settings.themeDisplayName,
                language = settings.language,
                playbackSpeed = settings.formattedPlaybackSpeed,
                equalizerEnabled = settings.equalizerEnabled,
                autoScanEnabled = settings.autoScan,
                analyticsEnabled = settings.analyticsEnabled,
                cacheSize = settings.formattedCacheSize,
                totalSettings = 25 // Approximate count of major settings
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate current settings
     */
    suspend fun validateCurrentSettings(): Result<List<String>> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            val validationErrors = settings.validate()
            Result.success(validationErrors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if settings are valid
     */
    suspend fun areSettingsValid(): Result<Boolean> {
        return try {
            val settings = settingsRepository.getAppSettings().first()
            Result.success(settings.isValid())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playback-related settings only
     */
    fun getPlaybackSettings(): Flow<PlaybackSettingsView> {
        return settingsRepository.getAppSettings().map { settings ->
            PlaybackSettingsView(
                repeatMode = settings.repeatMode,
                shuffleMode = settings.shuffleMode,
                playbackSpeed = settings.playbackSpeed,
                crossfadeEnabled = settings.crossfadeEnabled,
                crossfadeDuration = settings.crossfadeDuration,
                autoPlay = settings.autoPlay,
                gaplessPlayback = settings.gaplessPlayback
            )
        }
    }
    
    /**
     * Get audio-related settings only
     */
    fun getAudioSettings(): Flow<AudioSettingsView> {
        return settingsRepository.getAppSettings().map { settings ->
            AudioSettingsView(
                equalizerEnabled = settings.equalizerEnabled,
                equalizerPreset = settings.equalizerPreset,
                equalizerBands = settings.equalizerBands,
                bassBoost = settings.bassBoost,
                virtualizer = settings.virtualizer,
                loudnessEnhancer = settings.loudnessEnhancer,
                audioFocusEnabled = settings.audioFocusEnabled
            )
        }
    }
}

/**
 * Data class for settings summary
 */
data class SettingsSummary(
    val theme: String,
    val language: String,
    val playbackSpeed: String,
    val equalizerEnabled: Boolean,
    val autoScanEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val cacheSize: String,
    val totalSettings: Int
)

/**
 * View model for playback settings
 */
data class PlaybackSettingsView(
    val repeatMode: RepeatMode,
    val shuffleMode: ShuffleMode,
    val playbackSpeed: Float,
    val crossfadeEnabled: Boolean,
    val crossfadeDuration: Int,
    val autoPlay: Boolean,
    val gaplessPlayback: Boolean
)

/**
 * View model for audio settings
 */
data class AudioSettingsView(
    val equalizerEnabled: Boolean,
    val equalizerPreset: String,
    val equalizerBands: List<Float>,
    val bassBoost: Int,
    val virtualizer: Int,
    val loudnessEnhancer: Int,
    val audioFocusEnabled: Boolean
)
