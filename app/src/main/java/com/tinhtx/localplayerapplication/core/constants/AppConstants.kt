package com.tinhtx.localplayerapplication.core.constants

object AppConstants {
    // App Info
    const val APP_NAME = "Retro Music"
    const val PACKAGE_NAME = "com.tinhtx.localplayerapplication"
    
    // Database
    const val DATABASE_NAME = "local_player_database"
    const val DATABASE_VERSION = 1
    
    // DataStore
    const val USER_PREFERENCES_NAME = "user_preferences"
    const val SETTINGS_PREFERENCES_NAME = "settings_preferences"
    
    // Default Values
    const val DEFAULT_USER_NAME = "Music Lover"
    const val DEFAULT_PROFILE_IMAGE = ""
    
    // Permissions
    const val PERMISSION_REQUEST_CODE = 1001
    const val MEDIA_PERMISSION_REQUEST_CODE = 1002
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
    
    // Intent Actions
    const val ACTION_PLAY = "ACTION_PLAY"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val ACTION_NEXT = "ACTION_NEXT"
    const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    const val ACTION_STOP = "ACTION_STOP"
    
    // Animation Duration
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // UI Constants
    const val MINI_PLAYER_HEIGHT = 72
    const val BOTTOM_NAV_HEIGHT = 80
    const val TOP_APP_BAR_HEIGHT = 64
    
    // Search
    const val SEARCH_DEBOUNCE_DELAY = 300L
    const val MAX_SEARCH_HISTORY = 10
    
    // Cast
    const val CAST_APPLICATION_ID = "CC1AD845"
    
    // File Provider
    const val FILE_PROVIDER_AUTHORITY = "$PACKAGE_NAME.fileprovider"
    
    // Theme
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    // Sort Orders
    enum class SortOrder {
        TITLE_ASC, TITLE_DESC,
        ARTIST_ASC, ARTIST_DESC,
        ALBUM_ASC, ALBUM_DESC,
        DURATION_ASC, DURATION_DESC,
        DATE_ADDED_ASC, DATE_ADDED_DESC
    }
}
