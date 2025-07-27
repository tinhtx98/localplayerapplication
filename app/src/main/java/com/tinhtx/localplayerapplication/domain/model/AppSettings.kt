package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Consolidated app settings model (SINGLE SOURCE OF TRUTH)
 */
@Parcelize
data class AppSettings(
    // Theme & Appearance
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val gridSize: GridSize = GridSize.MEDIUM,
    val language: String = "en",
    
    // Playback Settings
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3000, // milliseconds
    val autoPlay: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val resumeOnHeadphones: Boolean = true,
    val pauseOnDisconnect: Boolean = true,
    
    // Audio Settings
    val equalizerEnabled: Boolean = false,
    val equalizerPreset: String = "Normal",
    val equalizerBands: List<Float> = List(10) { 0f },
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessEnhancer: Int = 0,
    val audioFocusEnabled: Boolean = true,
    val duckVolume: Boolean = true,
    
    // Library Settings
    val sortOrder: SortOrder = SortOrder.TITLE,
    val autoScan: Boolean = true,
    val scanIntervalHours: Int = 24,
    val ignoreShortTracks: Boolean = true,
    val minTrackDuration: Int = 30, // seconds
    val includedFolders: List<String> = emptyList(),
    val excludedFolders: List<String> = emptyList(),
    val lastScanTime: Long = 0L,
    val librarySortOrder: SortOrder = SortOrder.TITLE,
    
    // Privacy Settings
    val analyticsEnabled: Boolean = false,
    val crashReporting: Boolean = true,
    val usageStatistics: Boolean = false,
    val historyEnabled: Boolean = true,
    val scrobbleEnabled: Boolean = false,
    
    // Storage Settings
    val cacheSize: Long = 100 * 1024 * 1024, // 100MB
    val maxCacheSize: Long = 500 * 1024 * 1024, // 500MB
    val autoClearCache: Boolean = true,
    val downloadLocation: String = "",
    val downloadQuality: String = "high",
    val downloadOnlyWifi: Boolean = true,
    
    // App Metadata
    val firstLaunch: Boolean = true,
    val appVersion: String = "1.0.0",
    val lastUpdateCheck: Long = 0L,
    val tutorialCompleted: Boolean = false
) : Parcelable {
    
    companion object {
        fun getDefault() = AppSettings()
        
        // Validation constants
        const val MIN_PLAYBACK_SPEED = 0.25f
        const val MAX_PLAYBACK_SPEED = 4.0f
        const val MIN_CROSSFADE_DURATION = 0
        const val MAX_CROSSFADE_DURATION = 10000
        const val MIN_SCAN_INTERVAL = 1
        const val MAX_SCAN_INTERVAL = 168 // 1 week
        const val MIN_TRACK_DURATION = 0
        const val MAX_TRACK_DURATION = 300 // 5 minutes
        const val MIN_EQUALIZER_BAND = -15f
        const val MAX_EQUALIZER_BAND = 15f
        const val EQUALIZER_BAND_COUNT = 10
        const val MIN_BOOST_LEVEL = 0
        const val MAX_BOOST_LEVEL = 1000
    }
    
    // Validation helpers
    val isValidPlaybackSpeed: Boolean
        get() = playbackSpeed in MIN_PLAYBACK_SPEED..MAX_PLAYBACK_SPEED
    
    val isValidCrossfadeDuration: Boolean
        get() = crossfadeDuration in MIN_CROSSFADE_DURATION..MAX_CROSSFADE_DURATION
    
    val isValidScanInterval: Boolean
        get() = scanIntervalHours in MIN_SCAN_INTERVAL..MAX_SCAN_INTERVAL
    
    val isValidMinTrackDuration: Boolean
        get() = minTrackDuration in MIN_TRACK_DURATION..MAX_TRACK_DURATION
    
    val isValidEqualizerBands: Boolean
        get() = equalizerBands.size == EQUALIZER_BAND_COUNT && 
                equalizerBands.all { it in MIN_EQUALIZER_BAND..MAX_EQUALIZER_BAND }
    
    val isValidBassBoost: Boolean
        get() = bassBoost in MIN_BOOST_LEVEL..MAX_BOOST_LEVEL
    
    val isValidVirtualizer: Boolean
        get() = virtualizer in MIN_BOOST_LEVEL..MAX_BOOST_LEVEL
    
    val isValidLoudnessEnhancer: Boolean
        get() = loudnessEnhancer in MIN_BOOST_LEVEL..MAX_BOOST_LEVEL
    
    val isValidCacheSize: Boolean
        get() = cacheSize > 0 && cacheSize <= maxCacheSize
    
    val isValidMaxCacheSize: Boolean
        get() = maxCacheSize > 0
    
    // Formatting helpers
    val formattedCacheSize: String
        get() = "${cacheSize / (1024 * 1024)}MB"
    
    val formattedMaxCacheSize: String
        get() = "${maxCacheSize / (1024 * 1024)}MB"
    
    val formattedPlaybackSpeed: String
        get() = "${playbackSpeed}x"
    
    val formattedCrossfadeDuration: String
        get() = "${crossfadeDuration}ms"
    
    val formattedScanInterval: String
        get() = when {
            scanIntervalHours == 1 -> "1 hour"
            scanIntervalHours < 24 -> "$scanIntervalHours hours"
            scanIntervalHours == 24 -> "1 day"
            scanIntervalHours % 24 == 0 -> "${scanIntervalHours / 24} days"
            else -> "$scanIntervalHours hours"
        }
    
    val formattedMinTrackDuration: String
        get() = when {
            minTrackDuration == 0 -> "No limit"
            minTrackDuration < 60 -> "$minTrackDuration seconds"
            else -> "${minTrackDuration / 60}:${String.format("%02d", minTrackDuration % 60)}"
        }
    
    val themeDisplayName: String
        get() = when (theme) {
            AppTheme.SYSTEM -> "Follow system"
            AppTheme.LIGHT -> "Light"
            AppTheme.DARK -> "Dark"
        }
    
    val repeatModeDisplayName: String
        get() = when (repeatMode) {
            RepeatMode.OFF -> "Off"
            RepeatMode.ONE -> "Repeat one"
            RepeatMode.ALL -> "Repeat all"
        }
    
    val shuffleModeDisplayName: String
        get() = when (shuffleMode) {
            ShuffleMode.OFF -> "Off"
            ShuffleMode.ON -> "On"
        }
    
    // Validation method
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (!isValidPlaybackSpeed) {
            errors.add("Playback speed must be between ${MIN_PLAYBACK_SPEED}x and ${MAX_PLAYBACK_SPEED}x")
        }
        
        if (!isValidCrossfadeDuration) {
            errors.add("Crossfade duration must be between ${MIN_CROSSFADE_DURATION}ms and ${MAX_CROSSFADE_DURATION}ms")
        }
        
        if (!isValidScanInterval) {
            errors.add("Scan interval must be between $MIN_SCAN_INTERVAL and $MAX_SCAN_INTERVAL hours")
        }
        
        if (!isValidMinTrackDuration) {
            errors.add("Minimum track duration must be between $MIN_TRACK_DURATION and $MAX_TRACK_DURATION seconds")
        }
        
        if (!isValidEqualizerBands) {
            errors.add("Equalizer bands must have exactly $EQUALIZER_BAND_COUNT values between $MIN_EQUALIZER_BAND and $MAX_EQUALIZER_BAND")
        }
        
        if (!isValidBassBoost) {
            errors.add("Bass boost level must be between $MIN_BOOST_LEVEL and $MAX_BOOST_LEVEL")
        }
        
        if (!isValidVirtualizer) {
            errors.add("Virtualizer level must be between $MIN_BOOST_LEVEL and $MAX_BOOST_LEVEL")
        }
        
        if (!isValidLoudnessEnhancer) {
            errors.add("Loudness enhancer level must be between $MIN_BOOST_LEVEL and $MAX_BOOST_LEVEL")
        }
        
        if (!isValidCacheSize) {
            errors.add("Cache size must be positive and not exceed maximum cache size")
        }
        
        if (!isValidMaxCacheSize) {
            errors.add("Maximum cache size must be positive")
        }
        
        return errors
    }
    
    // Helper methods
    fun isValid(): Boolean = validate().isEmpty()
    
    fun withDefaults(): AppSettings {
        return copy(
            theme = if (theme in AppTheme.values()) theme else AppTheme.SYSTEM,
            playbackSpeed = playbackSpeed.coerceIn(MIN_PLAYBACK_SPEED, MAX_PLAYBACK_SPEED),
            crossfadeDuration = crossfadeDuration.coerceIn(MIN_CROSSFADE_DURATION, MAX_CROSSFADE_DURATION),
            scanIntervalHours = scanIntervalHours.coerceIn(MIN_SCAN_INTERVAL, MAX_SCAN_INTERVAL),
            minTrackDuration = minTrackDuration.coerceIn(MIN_TRACK_DURATION, MAX_TRACK_DURATION),
            bassBoost = bassBoost.coerceIn(MIN_BOOST_LEVEL, MAX_BOOST_LEVEL),
            virtualizer = virtualizer.coerceIn(MIN_BOOST_LEVEL, MAX_BOOST_LEVEL),
            loudnessEnhancer = loudnessEnhancer.coerceIn(MIN_BOOST_LEVEL, MAX_BOOST_LEVEL),
            equalizerBands = if (equalizerBands.size == EQUALIZER_BAND_COUNT) {
                equalizerBands.map { it.coerceIn(MIN_EQUALIZER_BAND, MAX_EQUALIZER_BAND) }
            } else {
                List(EQUALIZER_BAND_COUNT) { 0f }
            }
        )
    }
}

/**
 * Enum for grid size options
 */
enum class GridSize {
    SMALL, MEDIUM, LARGE;
    
    val displayName: String
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"  
            LARGE -> "Large"
        }
    
    val columnCount: Int
        get() = when (this) {
            SMALL -> 4
            MEDIUM -> 3
            LARGE -> 2
        }
}

/**
 * Enum for sort order options
 */
enum class SortOrder {
    TITLE, ARTIST, ALBUM, YEAR, DURATION, DATE_ADDED, DATE_MODIFIED, PLAY_COUNT, LAST_PLAYED;
    
    val displayName: String
        get() = when (this) {
            TITLE -> "Title"
            ARTIST -> "Artist"
            ALBUM -> "Album"
            YEAR -> "Year"
            DURATION -> "Duration"
            DATE_ADDED -> "Date Added"
            DATE_MODIFIED -> "Date Modified"
            PLAY_COUNT -> "Play Count"
            LAST_PLAYED -> "Last Played"
        }
}

/**
 * Enum for app theme options
 */
enum class AppTheme {
    SYSTEM, LIGHT, DARK;
    
    val displayName: String
        get() = when (this) {
            SYSTEM -> "Follow System"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}
