package com.tinhtx.localplayerapplication.presentation.screens.settings

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Settings Screen - Complete settings state management
 */
data class SettingsUiState(
    // =================================================================================
    // LOADING & ERROR STATES
    // =================================================================================
    
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // =================================================================================
    // APP SETTINGS
    // =================================================================================
    
    val appSettings: AppSettings = AppSettings(),
    val settingsLoading: Boolean = false,
    val settingsError: String? = null,
    
    // =================================================================================
    // APPEARANCE SETTINGS
    // =================================================================================
    
    val theme: AppTheme = AppTheme.SYSTEM,
    val isDynamicColor: Boolean = true,
    val accentColor: AccentColor = AccentColor.BLUE,
    val fontSize: FontSize = FontSize.MEDIUM,
    val showNowPlayingBar: Boolean = true,
    val showVisualization: Boolean = true,
    val gridSize: GridSize = GridSize.MEDIUM,
    
    // Theme selection
    val showThemeSelectionDialog: Boolean = false,
    val availableThemes: List<AppTheme> = AppTheme.values().toList(),
    val availableAccentColors: List<AccentColor> = AccentColor.values().toList(),
    
    // =================================================================================
    // PLAYBACK SETTINGS
    // =================================================================================
    
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val crossfadeDuration: Int = 3, // seconds
    val replayGainMode: ReplayGainMode = ReplayGainMode.OFF,
    val equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT,
    val bassBoost: Int = 0, // 0-100
    val virtualizer: Int = 0, // 0-100
    val skipSilence: Boolean = false,
    val resumeOnHeadphoneConnect: Boolean = true,
    val pauseOnHeadphoneDisconnect: Boolean = true,
    val ducking: Boolean = true, // Lower volume for notifications
    
    // Equalizer
    val showEqualizerDialog: Boolean = false,
    val equalizerBands: List<EqualizerBand> = emptyList(),
    val isEqualizerEnabled: Boolean = false,
    
    // =================================================================================
    // LIBRARY SETTINGS
    // =================================================================================
    
    val libraryPath: String = "",
    val scanOnStartup: Boolean = true,
    val includeSubfolders: Boolean = true,
    val ignoredFolders: List<String> = emptyList(),
    val supportedFormats: List<String> = listOf("mp3", "flac", "wav", "aac", "ogg"),
    val lastScanTime: Long = 0L,
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalArtists: Int = 0,
    
    // Library scanning
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanCurrentFile: String = "",
    val showLibraryScanDialog: Boolean = false,
    
    // =================================================================================
    // NOTIFICATION SETTINGS
    // =================================================================================
    
    val showNotifications: Boolean = true,
    val showLockScreenControls: Boolean = true,
    val showAlbumArt: Boolean = true,
    val compactNotification: Boolean = false,
    val notificationActions: List<NotificationAction> = NotificationAction.getDefault(),
    
    // =================================================================================
    // STORAGE SETTINGS
    // =================================================================================
    
    val cacheSize: Long = 0L,
    val maxCacheSize: Long = 500L * 1024 * 1024, // 500MB
    val thumbnailCacheSize: Long = 0L,
    val maxThumbnailCacheSize: Long = 100L * 1024 * 1024, // 100MB
    val autoClearCache: Boolean = true,
    val cacheLocation: String = "",
    
    // Storage cleanup
    val showStorageCleanupDialog: Boolean = false,
    val isClearingCache: Boolean = false,
    
    // =================================================================================
    // ADVANCED SETTINGS
    // =================================================================================
    
    val enableAnalytics: Boolean = true,
    val enableCrashReporting: Boolean = true,
    val enableExperimentalFeatures: Boolean = false,
    val debugMode: Boolean = false,
    val logLevel: LogLevel = LogLevel.INFO,
    val maxLogFiles: Int = 5,
    
    // Developer options
    val showDeveloperOptions: Boolean = false,
    val enableDeveloperOptions: Boolean = false,
    
    // =================================================================================
    // SLEEP TIMER SETTINGS
    // =================================================================================
    
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerDuration: Int = 30, // minutes
    val sleepTimerAction: SleepTimerAction = SleepTimerAction.PAUSE,
    val sleepTimerRemainingTime: Long = 0L,
    val fadeOutDuration: Int = 10, // seconds
    
    // Sleep timer dialog
    val showSleepTimerDialog: Boolean = false,
    
    // =================================================================================
    // PROFILE SETTINGS
    // =================================================================================
    
    val userName: String = "",
    val userEmail: String = "",
    val userAvatar: String? = null,
    val loginMethod: LoginMethod = LoginMethod.GUEST,
    val isLoggedIn: Boolean = false,
    val lastBackupTime: Long = 0L,
    val autoBackup: Boolean = false,
    
    // Profile management
    val showProfileDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    
    // =================================================================================
    // ABOUT SETTINGS
    // =================================================================================
    
    val appVersion: String = "1.0.0",
    val buildNumber: String = "1",
    val buildDate: String = "",
    val appSize: Long = 0L,
    val privacyPolicyUrl: String = "",
    val termsOfServiceUrl: String = "",
    val supportEmail: String = "",
    val githubUrl: String = "",
    
    // About dialogs
    val showChangelogDialog: Boolean = false,
    val showLicensesDialog: Boolean = false,
    val changelog: List<ChangelogEntry> = emptyList(),
    val openSourceLicenses: List<License> = emptyList(),
    
    // =================================================================================
    // BACKUP & RESTORE
    // =================================================================================
    
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val backupProgress: Float = 0f,
    val restoreProgress: Float = 0f,
    val showBackupDialog: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val availableBackups: List<BackupFile> = emptyList(),
    
    // =================================================================================
    // RESET & FACTORY SETTINGS
    // =================================================================================
    
    val showResetDialog: Boolean = false,
    val showFactoryResetDialog: Boolean = false,
    val isResetting: Boolean = false,
    val resetProgress: Float = 0f
) {
    
    // =================================================================================
    // COMPUTED PROPERTIES
    // =================================================================================
    
    val hasError: Boolean
        get() = error != null || settingsError != null
    
    val currentError: String?
        get() = error ?: settingsError
    
    val formattedCacheSize: String
        get() = formatFileSize(cacheSize)
    
    val formattedMaxCacheSize: String
        get() = formatFileSize(maxCacheSize)
    
    val formattedThumbnailCacheSize: String
        get() = formatFileSize(thumbnailCacheSize)
    
    val formattedAppSize: String
        get() = formatFileSize(appSize)
    
    val cacheUsagePercentage: Float
        get() = if (maxCacheSize > 0) (cacheSize.toFloat() / maxCacheSize.toFloat()) * 100f else 0f
    
    val thumbnailCacheUsagePercentage: Float
        get() = if (maxThumbnailCacheSize > 0) (thumbnailCacheSize.toFloat() / maxThumbnailCacheSize.toFloat()) * 100f else 0f
    
    val isCacheNearLimit: Boolean
        get() = cacheUsagePercentage > 80f
    
    val isThumbnailCacheNearLimit: Boolean
        get() = thumbnailCacheUsagePercentage > 80f
    
    val formattedLastScanTime: String
        get() = if (lastScanTime > 0) {
            formatDateTime(lastScanTime)
        } else "Never"
    
    val formattedLastBackupTime: String
        get() = if (lastBackupTime > 0) {
            formatDateTime(lastBackupTime)
        } else "Never"
    
    val sleepTimerFormattedRemaining: String
        get() = if (sleepTimerRemainingTime > 0) {
            val minutes = (sleepTimerRemainingTime / 1000 / 60).toInt()
            val seconds = ((sleepTimerRemainingTime / 1000) % 60).toInt()
            String.format("%d:%02d", minutes, seconds)
        } else ""
    
    val libraryStats: String
        get() = "$totalSongs songs • $totalAlbums albums • $totalArtists artists"
    
    val scanProgressText: String
        get() = if (isScanning) {
            "Scanning... ${(scanProgress * 100).toInt()}%"
        } else "Scan Library"
    
    // Helper methods
    fun isDarkTheme(): Boolean = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> false // Will be determined by system
    }
    
    fun getThemeDisplayName(): String = when (theme) {
        AppTheme.LIGHT -> "Light"
        AppTheme.DARK -> "Dark"
        AppTheme.SYSTEM -> "Follow System"
    }
    
    fun getAudioQualityDisplayName(): String = when (audioQuality) {
        AudioQuality.LOW -> "Low (128 kbps)"
        AudioQuality.MEDIUM -> "Medium (256 kbps)"
        AudioQuality.HIGH -> "High (320 kbps)"
        AudioQuality.LOSSLESS -> "Lossless"
    }
    
    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        return String.format(
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
    
    private fun formatDateTime(timestamp: Long): String {
        return try {
            java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

/**
 * App theme options
 */
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Follow System")
}

/**
 * Accent color options
 */
enum class AccentColor(val displayName: String, val colorValue: Long) {
    BLUE("Blue", 0xFF2196F3),
    GREEN("Green", 0xFF4CAF50),
    PURPLE("Purple", 0xFF9C27B0),
    RED("Red", 0xFFF44336),
    ORANGE("Orange", 0xFFFF9800),
    TEAL("Teal", 0xFF009688),
    PINK("Pink", 0xFFE91E63),
    INDIGO("Indigo", 0xFF3F51B5)
}

/**
 * Font size options
 */
enum class FontSize(val displayName: String, val scale: Float) {
    SMALL("Small", 0.9f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.1f),
    EXTRA_LARGE("Extra Large", 1.2f)
}

/**
 * Audio quality options
 */
enum class AudioQuality(val displayName: String, val bitrate: Int) {
    LOW("Low", 128),
    MEDIUM("Medium", 256),
    HIGH("High", 320),
    LOSSLESS("Lossless", -1)
}

/**
 * Replay gain modes
 */
enum class ReplayGainMode(val displayName: String) {
    OFF("Off"),
    TRACK("Track"),
    ALBUM("Album")
}

/**
 * Equalizer presets
 */
enum class EqualizerPreset(val displayName: String) {
    FLAT("Flat"),
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSICAL("Classical"),
    ELECTRONIC("Electronic"),
    CUSTOM("Custom")
}

/**
 * Sleep timer actions
 */
enum class SleepTimerAction(val displayName: String) {
    PAUSE("Pause"),
    STOP("Stop"),
    EXIT("Exit App")
}

/**
 * Login methods
 */
enum class LoginMethod(val displayName: String) {
    GUEST("Guest"),
    GOOGLE("Google"),
    FACEBOOK("Facebook"),
    EMAIL("Email")
}

/**
 * Log levels
 */
enum class LogLevel(val displayName: String) {
    VERBOSE("Verbose"),
    DEBUG("Debug"),
    INFO("Info"),
    WARN("Warning"),
    ERROR("Error")
}

/**
 * Notification actions
 */
enum class NotificationAction(val displayName: String, val iconName: String) {
    PREVIOUS("Previous", "skip_previous"),
    PLAY_PAUSE("Play/Pause", "play_pause"),
    NEXT("Next", "skip_next"),
    FAVORITE("Favorite", "favorite"),
    REPEAT("Repeat", "repeat"),
    SHUFFLE("Shuffle", "shuffle");
    
    companion object {
        fun getDefault(): List<NotificationAction> = listOf(
            PREVIOUS, PLAY_PAUSE, NEXT
        )
    }
}

/**
 * Equalizer band data
 */
data class EqualizerBand(
    val frequency: Float, // Hz
    val gain: Float, // dB
    val displayName: String
)

/**
 * Changelog entry
 */
data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>
)

/**
 * Open source license
 */
data class License(
    val name: String,
    val url: String,
    val license: String
)

/**
 * Backup file info
 */
data class BackupFile(
    val name: String,
    val path: String,
    val size: Long,
    val createdAt: Long
)

/**
 * Extension functions for SettingsUiState
 */
fun SettingsUiState.copyWithLoading(isLoading: Boolean): SettingsUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun SettingsUiState.copyWithError(error: String?): SettingsUiState {
    return copy(error = error, isLoading = false)
}

fun SettingsUiState.copyWithSettings(settings: AppSettings): SettingsUiState {
    return copy(
        appSettings = settings,
        theme = settings.theme,
        isDynamicColor = settings.isDynamicColor,
        accentColor = settings.accentColor,
        fontSize = settings.fontSize,
        audioQuality = settings.audioQuality,
        crossfadeDuration = settings.crossfadeDuration,
        // ... map other settings
        settingsLoading = false,
        settingsError = null
    )
}

/**
 * Preview data for SettingsUiState
 */
object SettingsUiStatePreview {
    val loading = SettingsUiState(isLoading = true)
    
    val error = SettingsUiState(error = "Failed to load settings")
    
    val default = SettingsUiState(
        appVersion = "1.0.0",
        buildNumber = "1",
        totalSongs = 1250,
        totalAlbums = 125,
        totalArtists = 89,
        cacheSize = 150L * 1024 * 1024, // 150MB
        thumbnailCacheSize = 25L * 1024 * 1024, // 25MB
        lastScanTime = System.currentTimeMillis() - 3600000, // 1 hour ago
        lastBackupTime = System.currentTimeMillis() - 86400000 * 7, // 1 week ago
        userName = "John Doe",
        userEmail = "john.doe@example.com",
        isLoggedIn = true,
        loginMethod = LoginMethod.EMAIL
    )
    
    val withDialogs = default.copy(
        showThemeSelectionDialog = true,
        showSleepTimerDialog = true,
        showBackupDialog = true
    )
    
    val scanning = default.copy(
        isScanning = true,
        scanProgress = 0.65f,
        scanCurrentFile = "/storage/music/artist/album/song.mp3"
    )
    
    val sleepTimerActive = default.copy(
        sleepTimerEnabled = true,
        sleepTimerRemainingTime = 15 * 60 * 1000L // 15 minutes
    )
}
