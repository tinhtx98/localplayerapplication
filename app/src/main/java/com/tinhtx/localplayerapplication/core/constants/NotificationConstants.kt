package com.tinhtx.localplayerapplication.core.constants

/**
 * Notification related constants
 */
object NotificationConstants {
    // Channel IDs
    const val MUSIC_CHANNEL_ID = "music_playback_channel"
    const val TIMER_CHANNEL_ID = "sleep_timer_channel"
    const val DOWNLOAD_CHANNEL_ID = "download_channel"
    const val ERROR_CHANNEL_ID = "error_channel"
    
    // Channel Names
    const val MUSIC_CHANNEL_NAME = "Music Playback"
    const val TIMER_CHANNEL_NAME = "Sleep Timer"
    const val DOWNLOAD_CHANNEL_NAME = "Downloads"
    const val ERROR_CHANNEL_NAME = "Errors"
    
    // Channel Descriptions
    const val MUSIC_CHANNEL_DESC = "Controls for music playback"
    const val TIMER_CHANNEL_DESC = "Sleep timer notifications"
    const val DOWNLOAD_CHANNEL_DESC = "Download progress notifications"
    const val ERROR_CHANNEL_DESC = "Error and warning notifications"
    
    // Notification IDs
    const val MUSIC_NOTIFICATION_ID = 1001
    const val TIMER_NOTIFICATION_ID = 1002
    const val DOWNLOAD_NOTIFICATION_ID = 1003
    const val ERROR_NOTIFICATION_ID = 1004
    
    // Request Codes for PendingIntents
    const val REQUEST_CODE_PLAY_PAUSE = 100
    const val REQUEST_CODE_SKIP_PREVIOUS = 101
    const val REQUEST_CODE_SKIP_NEXT = 102
    const val REQUEST_CODE_STOP = 103
    const val REQUEST_CODE_CONTENT = 104
    const val REQUEST_CODE_DELETE = 105
    
    // Actions
    const val ACTION_PLAY = "com.tinhtx.localplayerapplication.ACTION_PLAY"
    const val ACTION_PAUSE = "com.tinhtx.localplayerapplication.ACTION_PAUSE"
    const val ACTION_PLAY_PAUSE = "com.tinhtx.localplayerapplication.ACTION_PLAY_PAUSE"
    const val ACTION_SKIP_NEXT = "com.tinhtx.localplayerapplication.ACTION_SKIP_NEXT"
    const val ACTION_SKIP_PREVIOUS = "com.tinhtx.localplayerapplication.ACTION_SKIP_PREVIOUS"
    const val ACTION_STOP = "com.tinhtx.localplayerapplication.ACTION_STOP"
    const val ACTION_CANCEL_TIMER = "com.tinhtx.localplayerapplication.ACTION_CANCEL_TIMER"
    
    // Compact View Actions (indices in actions array)
    val SHOW_ACTIONS_IN_COMPACT_VIEW = intArrayOf(0, 1, 2) // Previous, Play/Pause, Next
    
    // Timer Actions
    val TIMER_COMPACT_ACTIONS = intArrayOf(0) // Cancel button only
    
    // Extra Keys
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_ACTION = "extra_action"
    const val EXTRA_SONG_ID = "extra_song_id"
    const val EXTRA_TIMER_DURATION = "extra_timer_duration"
    
    // Notification Priorities (for Android < O)
    const val PRIORITY_HIGH = 1
    const val PRIORITY_DEFAULT = 0
    const val PRIORITY_LOW = -1
    
    // Notification Importance (for Android >= O)
    const val IMPORTANCE_HIGH = 4
    const val IMPORTANCE_DEFAULT = 3
    const val IMPORTANCE_LOW = 2
    
    // Notification Visibility
    const val VISIBILITY_PUBLIC = 1
    const val VISIBILITY_PRIVATE = 0
    const val VISIBILITY_SECRET = -1
    
    // Colors
    const val NOTIFICATION_COLOR = 0xFF1E88E5.toInt() // Material Blue
    
    // Ongoing Notification Settings
    const val SHOW_WHEN = false
    const val ONGOING = true
    const val AUTO_CANCEL = false
    const val ONLY_ALERT_ONCE = true
    
    // Media Style Settings
    const val SHOW_CANCEL_BUTTON = true
    const val SHOW_ACTIONS_IN_COMPACT_VIEW_COUNT = 3
    
    // Big Text Style
    const val MAX_LINES_COLLAPSED = 1
    const val MAX_LINES_EXPANDED = 5
}
