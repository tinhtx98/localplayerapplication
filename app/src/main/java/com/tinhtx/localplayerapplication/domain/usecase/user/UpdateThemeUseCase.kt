package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.domain.model.AppTheme
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for updating app theme
 */
class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Update app theme
     */
    suspend fun execute(theme: AppTheme): Result<Unit> {
        return try {
            settingsRepository.updateTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current theme
     */
    suspend fun getCurrentTheme(): Result<AppTheme> {
        return try {
            val appSettings = settingsRepository.getAppSettings().first()
            Result.success(appSettings.theme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle between light and dark theme
     */
    suspend fun toggleTheme(): Result<AppTheme> {
        return try {
            val currentSettings = settingsRepository.getAppSettings().first()
            val newTheme = when (currentSettings.theme) {
                AppTheme.LIGHT -> AppTheme.DARK
                AppTheme.DARK -> AppTheme.LIGHT
                AppTheme.SYSTEM -> {
                    // If system theme, toggle to opposite of current system state
                    // For now, default to dark
                    AppTheme.DARK
                }
            }
            
            settingsRepository.updateTheme(newTheme)
            Result.success(newTheme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set theme to system default
     */
    suspend fun setSystemTheme(): Result<Unit> {
        return execute(AppTheme.SYSTEM)
    }
    
    /**
     * Set light theme
     */
    suspend fun setLightTheme(): Result<Unit> {
        return execute(AppTheme.LIGHT)
    }
    
    /**
     * Set dark theme
     */
    suspend fun setDarkTheme(): Result<Unit> {
        return execute(AppTheme.DARK)
    }
    
    /**
     * Check if current theme is dark
     */
    suspend fun isDarkTheme(): Result<Boolean> {
        return try {
            val currentTheme = getCurrentTheme().getOrNull()
            val isDark = when (currentTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> {
                    // Would need to check system theme - placeholder
                    false
                }
                null -> false
            }
            Result.success(isDark)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if using system theme
     */
    suspend fun isUsingSystemTheme(): Result<Boolean> {
        return try {
            val currentTheme = getCurrentTheme().getOrNull()
            Result.success(currentTheme == AppTheme.SYSTEM)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available themes
     */
    fun getAvailableThemes(): List<ThemeOption> {
        return listOf(
            ThemeOption(AppTheme.SYSTEM, "System", "Follow system setting"),
            ThemeOption(AppTheme.LIGHT, "Light", "Light theme"),
            ThemeOption(AppTheme.DARK, "Dark", "Dark theme")
        )
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
     * Check if dynamic color is enabled
     */
    suspend fun isDynamicColorEnabled(): Result<Boolean> {
        return try {
            val appSettings = settingsRepository.getAppSettings().first()
            Result.success(appSettings.dynamicColor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle dynamic color
     */
    suspend fun toggleDynamicColor(): Result<Boolean> {
        return try {
            val currentSettings = settingsRepository.getAppSettings().first()
            val newState = !currentSettings.dynamicColor
            settingsRepository.updateDynamicColor(newState)
            Result.success(newState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if dynamic color is supported
     */
    fun isDynamicColorSupported(): Boolean {
        // Dynamic color is supported on Android 12+ (API 31+)
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    }
    
    /**
     * Get theme configuration
     */
    suspend fun getThemeConfiguration(): Result<ThemeConfiguration> {
        return try {
            val appSettings = settingsRepository.getAppSettings().first()
            val configuration = ThemeConfiguration(
                currentTheme = appSettings.theme,
                dynamicColorEnabled = appSettings.dynamicColor,
                dynamicColorSupported = isDynamicColorSupported()
            )
            Result.success(configuration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Apply theme with dynamic color
     */
    suspend fun applyThemeConfiguration(
        theme: AppTheme,
        enableDynamicColor: Boolean
    ): Result<Unit> {
        return try {
            settingsRepository.updateTheme(theme)
            if (isDynamicColorSupported()) {
                settingsRepository.updateDynamicColor(enableDynamicColor)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for theme options
 */
data class ThemeOption(
    val theme: AppTheme,
    val displayName: String,
    val description: String
)

/**
 * Data class for theme configuration
 */
data class ThemeConfiguration(
    val currentTheme: AppTheme,
    val dynamicColorEnabled: Boolean,
    val dynamicColorSupported: Boolean
) {
    val effectiveTheme: AppTheme
        get() = when (currentTheme) {
            AppTheme.SYSTEM -> {
                // Would determine actual system theme - placeholder
                AppTheme.DARK
            }
            else -> currentTheme
        }
    
    val canUseDynamicColor: Boolean
        get() = dynamicColorSupported && dynamicColorEnabled
}
