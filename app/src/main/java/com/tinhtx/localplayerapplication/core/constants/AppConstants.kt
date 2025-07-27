package com.tinhtx.localplayerapplication.core.constants

/**
 * Application-wide constants
 */
object AppConstants {
    // Application Info
    const val TAG = "LocalPlayer"
    const val APP_NAME = "LocalPlayer"
    const val PACKAGE_NAME = "com.tinhtx.localplayerapplication"
    
    // Database
    const val DATABASE_NAME = "localplayer_database"
    const val DATABASE_VERSION = 1
    
    // SharedPreferences
    const val PREFS_NAME = "localplayer_preferences"
    const val SETTINGS_PREFS_NAME = "settings_preferences"
    
    // File Provider
    const val FILE_PROVIDER_AUTHORITY = "${PACKAGE_NAME}.fileprovider"
    
    // Cache & Storage
    const val MAX_CACHE_SIZE_MB = 500L
    const val CACHE_DIR_NAME = "music_cache"
    const val ARTWORK_CACHE_DIR = "artwork"
    const val LYRICS_CACHE_DIR = "lyrics"
    
    // Default Values
    const val DEFAULT_PLAYBACK_SPEED = 1.0f
    const val DEFAULT_VOLUME = 1.0f
    const val DEFAULT_CROSSFADE_DURATION = 3 // seconds
    const val MIN_TRACK_DURATION_SEC = 30
    
    // Limits & Pagination
    const val MAX_RECENT_SONGS = 50
    const val MAX_SEARCH_RESULTS = 100
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PLAYLIST_SONGS = 1000
    
    // Time Constants
    const val SCAN_INTERVAL_HOURS = 24
    const val ANALYTICS_REPORT_INTERVAL_HOURS = 6
    const val CACHE_CLEANUP_INTERVAL_DAYS = 7
    
    // Audio Focus
    const val AUDIO_FOCUS_GAIN_DELAY_MS = 200L
    const val AUDIO_FOCUS_LOSS_TRANSIENT_DELAY_MS = 1000L
    
    // Network
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val MAX_RETRY_ATTEMPTS = 3
    
    // UI Constants
    const val ANIMATION_DURATION_SHORT = 150
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500
    const val RIPPLE_ALPHA = 0.1f
    
    // Intent Actions
    const val ACTION_MEDIA_BUTTON = "action_media_button"
    const val ACTION_AUDIO_BECOMING_NOISY = "action_audio_becoming_noisy"
    
    // Work Manager
    const val LIBRARY_SCAN_WORK_NAME = "library_scan_work"
    const val CACHE_CLEANUP_WORK_NAME = "cache_cleanup_work"
    const val ANALYTICS_WORK_NAME = "analytics_work"
    
    // Deep Links
    const val DEEP_LINK_SCHEME = "localplayer"
    const val DEEP_LINK_HOST = "music"
    
    // Error Codes
    const val ERROR_CODE_PERMISSION_DENIED = 1001
    const val ERROR_CODE_FILE_NOT_FOUND = 1002
    const val ERROR_CODE_PLAYBACK_FAILED = 1003
    const val ERROR_CODE_NETWORK_ERROR = 1004
}
