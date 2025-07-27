package com.tinhtx.localplayerapplication.domain.usecase.settings

import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for updating application settings (SIMPLIFIED)
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Update complete app settings
     */
    suspend fun updateAppSettings(settings: AppSettings): Result<Unit> {
        return try {
            val validationErrors = settings.validate()
            if (validationErrors.isNotEmpty()) {
                return Result.failure(Exception("Settings validation failed: ${validationErrors.joinToString(", ")}"))
            }
            
            settingsRepository.updateAppSettings(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update theme
     */
    suspend fun updateTheme(theme: AppTheme): Result<Unit> {
        return try {
            settingsRepository.updateTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update dynamic color setting
     */
    suspend fun updateDynamicColor(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateDynamicColor(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update grid size
     */
    suspend fun updateGridSize(gridSize: GridSize): Result<Unit> {
        return try {
            settingsRepository.updateGridSize(gridSize)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update sort order
     */
    suspend fun updateSortOrder(sortOrder: SortOrder): Result<Unit> {
        return try {
            settingsRepository.updateSortOrder(sortOrder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update language
     */
    suspend fun updateLanguage(language: String): Result<Unit> {
        return try {
            if (language.isBlank()) {
                return Result.failure(Exception("Language cannot be empty"))
            }
            settingsRepository.updateLanguage(language)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update repeat mode
     */
    suspend fun updateRepeatMode(mode: RepeatMode): Result<Unit> {
        return try {
            settingsRepository.updateRepeatMode(mode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update shuffle mode
     */
    suspend fun updateShuffleMode(mode: ShuffleMode): Result<Unit> {
        return try {
            settingsRepository.updateShuffleMode(mode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update playback speed
     */
    suspend fun updatePlaybackSpeed(speed: Float): Result<Unit> {
        return try {
            if (speed !in AppSettings.MIN_PLAYBACK_SPEED..AppSettings.MAX_PLAYBACK_SPEED) {
                return Result.failure(Exception("Playback speed must be between ${AppSettings.MIN_PLAYBACK_SPEED}x and ${AppSettings.MAX_PLAYBACK_SPEED}x"))
            }
            settingsRepository.updatePlaybackSpeed(speed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update crossfade settings
     */
    suspend fun updateCrossfade(enabled: Boolean, duration: Int): Result<Unit> {
        return try {
            if (duration !in AppSettings.MIN_CROSSFADE_DURATION..AppSettings.MAX_CROSSFADE_DURATION) {
                return Result.failure(Exception("Crossfade duration must be between ${AppSettings.MIN_CROSSFADE_DURATION}ms and ${AppSettings.MAX_CROSSFADE_DURATION}ms"))
            }
            settingsRepository.updateCrossfade(enabled, duration)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update auto play setting
     */
    suspend fun updateAutoPlay(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateAutoPlay(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update gapless playback setting
     */
    suspend fun updateGaplessPlayback(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateGaplessPlayback(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update resume on headphones setting
     */
    suspend fun updateResumeOnHeadphones(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateResumeOnHeadphones(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update pause on disconnect setting
     */
    suspend fun updatePauseOnDisconnect(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updatePauseOnDisconnect(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update equalizer settings
     */
    suspend fun updateEqualizer(enabled: Boolean, preset: String, bands: List<Float>): Result<Unit> {
        return try {
            if (bands.size != AppSettings.EQUALIZER_BAND_COUNT) {
                return Result.failure(Exception("Equalizer must have exactly ${AppSettings.EQUALIZER_BAND_COUNT} bands"))
            }
            
            if (bands.any { it !in AppSettings.MIN_EQUALIZER_BAND..AppSettings.MAX_EQUALIZER_BAND }) {
                return Result.failure(Exception("Equalizer band values must be between ${AppSettings.MIN_EQUALIZER_BAND} and ${AppSettings.MAX_EQUALIZER_BAND}"))
            }
            
            settingsRepository.updateEqualizer(enabled, preset, bands)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update bass boost level
     */
    suspend fun updateBassBoost(level: Int): Result<Unit> {
        return try {
            if (level !in AppSettings.MIN_BOOST_LEVEL..AppSettings.MAX_BOOST_LEVEL) {
                return Result.failure(Exception("Bass boost level must be between ${AppSettings.MIN_BOOST_LEVEL} and ${AppSettings.MAX_BOOST_LEVEL}"))
            }
            settingsRepository.updateBassBoost(level)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update virtualizer level
     */
    suspend fun updateVirtualizer(level: Int): Result<Unit> {
        return try {
            if (level !in AppSettings.MIN_BOOST_LEVEL..AppSettings.MAX_BOOST_LEVEL) {
                return Result.failure(Exception("Virtualizer level must be between ${AppSettings.MIN_BOOST_LEVEL} and ${AppSettings.MAX_BOOST_LEVEL}"))
            }
            settingsRepository.updateVirtualizer(level)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update loudness enhancer level
     */ 
    suspend fun updateLoudnessEnhancer(level: Int): Result<Unit> {
        return try {
            if (level !in AppSettings.MIN_BOOST_LEVEL..AppSettings.MAX_BOOST_LEVEL) {
                return Result.failure(Exception("Loudness enhancer level must be between ${AppSettings.MIN_BOOST_LEVEL} and ${AppSettings.MAX_BOOST_LEVEL}"))
            }
            settingsRepository.updateLoudnessEnhancer(level)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update audio focus settings
     */
    suspend fun updateAudioFocus(enabled: Boolean, duckVolume: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateAudioFocus(enabled, duckVolume)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update auto scan settings
     */
    suspend fun updateAutoScan(enabled: Boolean, intervalHours: Int): Result<Unit> {
        return try {
            if (intervalHours !in AppSettings.MIN_SCAN_INTERVAL..AppSettings.MAX_SCAN_INTERVAL) {
                return Result.failure(Exception("Scan interval must be between ${AppSettings.MIN_SCAN_INTERVAL} and ${AppSettings.MAX_SCAN_INTERVAL} hours"))
            }
            settingsRepository.updateAutoScan(enabled, intervalHours)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update short track settings
     */
    suspend fun updateShortTrackSettings(ignore: Boolean, minDuration: Int): Result<Unit> {
        return try {
            if (minDuration !in AppSettings.MIN_TRACK_DURATION..AppSettings.MAX_TRACK_DURATION) {
                return Result.failure(Exception("Minimum track duration must be between ${AppSettings.MIN_TRACK_DURATION} and ${AppSettings.MAX_TRACK_DURATION} seconds"))
            }
            settingsRepository.updateShortTrackSettings(ignore, minDuration)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update included folders
     */
    suspend fun updateIncludedFolders(folders: List<String>): Result<Unit> {
        return try {
            val validFolders = folders.filter { it.isNotBlank() }.distinct()
            settingsRepository.updateIncludedFolders(validFolders)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update excluded folders
     */
    suspend fun updateExcludedFolders(folders: List<String>): Result<Unit> {
        return try {
            val validFolders = folders.filter { it.isNotBlank() }.distinct()
            settingsRepository.updateExcludedFolders(validFolders)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update analytics setting
     */
    suspend fun updateAnalytics(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateAnalytics(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update crash reporting setting
     */
    suspend fun updateCrashReporting(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateCrashReporting(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update usage statistics setting
     */
    suspend fun updateUsageStatistics(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateUsageStatistics(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update history setting
     */
    suspend fun updateHistory(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateHistory(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update scrobble setting
     */
    suspend fun updateScrobble(enabled: Boolean): Result<Unit> {
        return try {
            settingsRepository.updateScrobble(enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update cache settings
     */
    suspend fun updateCacheSettings(cacheSize: Long, maxCacheSize: Long, autoClear: Boolean): Result<Unit> {
        return try {
            if (cacheSize <= 0 || maxCacheSize <= 0) {
                return Result.failure(Exception("Cache sizes must be positive"))
            }
            if (cacheSize > maxCacheSize) {
                return Result.failure(Exception("Cache size cannot exceed maximum cache size"))
            }
            settingsRepository.updateCacheSettings(cacheSize, maxCacheSize, autoClear)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set first launch flag
     */
    suspend fun setFirstLaunch(isFirstLaunch: Boolean): Result<Unit> {
        return try {
            settingsRepository.setFirstLaunch(isFirstLaunch)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update app version
     */
    suspend fun updateAppVersion(version: String): Result<Unit> {
        return try {
            if (version.isBlank()) {
                return Result.failure(Exception("App version cannot be empty"))
            }
            settingsRepository.updateAppVersion(version)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle theme between light/dark
     */
    suspend fun toggleTheme(): Result<AppTheme> {
        return try {
            val currentTheme = settingsRepository.getAppSettings().first().theme
            val newTheme = when (currentTheme) {
                AppTheme.LIGHT -> AppTheme.DARK
                AppTheme.DARK -> AppTheme.LIGHT
                AppTheme.SYSTEM -> AppTheme.DARK // Default to dark when toggling from system
            }
            settingsRepository.updateTheme(newTheme)
            Result.success(newTheme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cycle through repeat modes
     */
    suspend fun cycleRepeatMode(): Result<RepeatMode> {
        return try {
            val currentMode = settingsRepository.getAppSettings().first().repeatMode
            val newMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            settingsRepository.updateRepeatMode(newMode)
            Result.success(newMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle shuffle mode
     */
    suspend fun toggleShuffle(): Result<ShuffleMode> {
        return try {
            val currentMode = settingsRepository.getAppSettings().first().shuffleMode
            val newMode = when (currentMode) {
                ShuffleMode.OFF -> ShuffleMode.ON
                ShuffleMode.ON -> ShuffleMode.OFF
            }
            settingsRepository.updateShuffleMode(newMode)
            Result.success(newMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Batch update theme settings
     */
    suspend fun updateThemeSettings(theme: AppTheme, dynamicColor: Boolean, gridSize: GridSize): Result<Unit> {
        return try {
            settingsRepository.updateThemeSettings(theme, dynamicColor, gridSize)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Batch update playback settings
     */
    suspend fun updatePlaybackSettings(
        repeatMode: RepeatMode,
        shuffleMode: ShuffleMode,
        playbackSpeed: Float,
        crossfadeEnabled: Boolean,
        crossfadeDuration: Int
    ): Result<Unit> {
        return try {
            if (playbackSpeed !in AppSettings.MIN_PLAYBACK_SPEED..AppSettings.MAX_PLAYBACK_SPEED) {
                return Result.failure(Exception("Invalid playback speed"))
            }
            if (crossfadeDuration !in AppSettings.MIN_CROSSFADE_DURATION..AppSettings.MAX_CROSSFADE_DURATION) {
                return Result.failure(Exception("Invalid crossfade duration"))
            }
            
            settingsRepository.updatePlaybackSettings(repeatMode, shuffleMode, playbackSpeed, crossfadeEnabled, crossfadeDuration)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset all settings to defaults
     */
    suspend fun resetToDefaults(): Result<Unit> {
        return try {
            settingsRepository.resetToDefaults()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export settings to JSON string
     */     
    suspend fun exportSettings(): Result<String> {
        return try {
            val settingsJson = settingsRepository.exportSettings()
            Result.success(settingsJson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import settings from JSON string
     */
    suspend fun importSettings(settingsJson: String): Result<Unit> {
        return try {
            if (settingsJson.isBlank()) {
                return Result.failure(Exception("Settings JSON cannot be empty"))
            }
            
            val success = settingsRepository.importSettings(settingsJson)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to import settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear all settings
     */
    suspend fun clearAllSettings(): Result<Unit> {
        return try {
            settingsRepository.clearAllSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate and fix settings
     */
    suspend fun validateAndFixSettings(): Result<List<String>> {
        return try {
            val currentSettings = settingsRepository.getAppSettings().first()
            val validationErrors = currentSettings.validate()
            
            if (validationErrors.isNotEmpty()) {
                // Apply defaults for invalid settings
                val fixedSettings = currentSettings.withDefaults()
                settingsRepository.updateAppSettings(fixedSettings)
            }
            
            Result.success(validationErrors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
