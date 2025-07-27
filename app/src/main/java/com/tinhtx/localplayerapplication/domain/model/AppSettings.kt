package com.tinhtx.localplayerapplication.domain.model

data class AppSettings(
    val playbackSettings: PlaybackSettings = PlaybackSettings(),
    val appearanceSettings: AppearanceSettings = AppearanceSettings(),
    val librarySettings: LibrarySettings = LibrarySettings(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val storageSettings: StorageSettings = StorageSettings(),
    val privacySettings: PrivacySettings? = null
)

data class PlaybackSettings(
    val gaplessPlayback: Boolean = true,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3, // seconds
    val audioFocus: Boolean = true,
    val equalizerSettings: EqualizerSettings = EqualizerSettings(),
    val replayGain: Boolean = false,
    val audioEffects: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val skipSilence: Boolean = false,
    val volumeBoost: Boolean = false,
    val bassBoost: Int = 0, // 0-100
    val virtualizer: Int = 0 // 0-100
)

data class AppearanceSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColors: Boolean = true,
    val gridSize: GridSize = GridSize.MEDIUM,
    val showAlbumArt: Boolean = true,
    val blurBackground: Boolean = false,
    val animatedArtwork: Boolean = true,
    val compactMode: Boolean = false,
    val showSongNumbers: Boolean = false,
    val colorfulPlayback: Boolean = true,
    val artworkCornerRadius: Int = 8 // dp
)

data class LibrarySettings(
    val autoScan: Boolean = true,
    val scanFolders: List<String> = emptyList(),
    val excludeFolders: List<String> = emptyList(),
    val ignoreShortTracks: Boolean = true,
    val minTrackDuration: Int = 30, // seconds
    val downloadArtwork: Boolean = true,
    val downloadLyrics: Boolean = false,
    val scanFrequency: ScanFrequency = ScanFrequency.DAILY,
    val sortOrder: SortOrder = SortOrder.TITLE,
    val groupByAlbumArtist: Boolean = false,
    val showDuplicates: Boolean = false
)

data class NotificationSettings(
    val showControls: Boolean = true,
    val showAlbumArt: Boolean = true,
    val coloredNotification: Boolean = true,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val showOnLockScreen: Boolean = true,
    val showProgress: Boolean = true,
    val compactActions: List<String> = listOf("previous", "play_pause", "next"),
    val showSeekButtons: Boolean = false,
    val vibrationEnabled: Boolean = true
)

data class StorageSettings(
    val autoClearCache: Boolean = true,
    val maxCacheSize: Long = 500L, // MB
    val cacheArtwork: Boolean = true,
    val cacheLyrics: Boolean = true,
    val cacheLocation: CacheLocation = CacheLocation.INTERNAL,
    val clearCacheFrequency: ClearFrequency = ClearFrequency.WEEKLY,
    val compressArtwork: Boolean = true,
    val artworkQuality: Int = 80 // 0-100
)

data class PrivacySettings(
    val analyticsEnabled: Boolean = true,
    val crashReportsEnabled: Boolean = true,
    val dataCollection: Boolean = true,
    val shareUsageData: Boolean = false,
    val personalizedAds: Boolean = false
)

data class EqualizerSettings(
    val enabled: Boolean = false,
    val preset: EqualizerPreset = EqualizerPreset.NORMAL,
    val customBands: List<Float> = List(10) { 0f }, // 10-band equalizer
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessEnhancer: Int = 0
)

// Enums
enum class AppTheme {
    LIGHT,
    DARK, 
    SYSTEM,
    AUTO // Based on time
}

enum class GridSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

enum class ScanFrequency {
    MANUAL,
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class SortOrder {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    DATE_ADDED,
    DATE_MODIFIED,
    YEAR,
    TRACK_NUMBER
}

enum class NotificationPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

enum class CacheLocation {
    INTERNAL,
    EXTERNAL,
    CUSTOM
}

enum class ClearFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER
}

enum class EqualizerPreset {
    NORMAL,
    ROCK,
    POP,
    JAZZ,
    CLASSICAL,
    ELECTRONIC,
    BASS_BOOST,
    TREBLE_BOOST,
    VOCAL_BOOST,
    CUSTOM
}
