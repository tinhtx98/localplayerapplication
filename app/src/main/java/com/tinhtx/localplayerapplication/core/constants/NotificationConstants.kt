package com.tinhtx.localplayerapplication.core.constants

object NotificationConstants {
    // Notification IDs
    const val MUSIC_NOTIFICATION_ID = 1001
    const val SLEEP_TIMER_NOTIFICATION_ID = 1002
    const val DOWNLOAD_NOTIFICATION_ID = 1003
    
    // Channel IDs
    const val MUSIC_CHANNEL_ID = "music_playback_channel"
    const val SLEEP_TIMER_CHANNEL_ID = "sleep_timer_channel"
    const val DOWNLOAD_CHANNEL_ID = "download_channel"
    
    // Channel Names
    const val MUSIC_CHANNEL_NAME = "Music Playback"
    const val SLEEP_TIMER_CHANNEL_NAME = "Sleep Timer"
    const val DOWNLOAD_CHANNEL_NAME = "Downloads"
    
    // Channel Descriptions
    const val MUSIC_CHANNEL_DESCRIPTION = "Controls for music playback"
    const val SLEEP_TIMER_CHANNEL_DESCRIPTION = "Sleep timer notifications"
    const val DOWNLOAD_CHANNEL_DESCRIPTION = "Download progress notifications"
    
    // Action Keys
    const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
    const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    const val ACTION_NEXT = "ACTION_NEXT"
    const val ACTION_STOP = "ACTION_STOP"
    const val ACTION_CLOSE = "ACTION_CLOSE"
    const val ACTION_CANCEL_SLEEP_TIMER = "ACTION_CANCEL_SLEEP_TIMER"
    
    // Request Codes
    const val REQUEST_CODE_PLAY_PAUSE = 100
    const val REQUEST_CODE_PREVIOUS = 101
    const val REQUEST_CODE_NEXT = 102
    const val REQUEST_CODE_STOP = 103
    const val REQUEST_CODE_CLOSE = 104
    const val REQUEST_CODE_OPEN_PLAYER = 105
    const val REQUEST_CODE_CANCEL_SLEEP_TIMER = 106
    
    // Notification Builder
    const val COMPACT_NOTIFICATION_ACTIONS = 3
    const val EXPANDED_NOTIFICATION_ACTIONS = 5
    
    // Media Style
    const val SHOW_ACTIONS_IN_COMPACT_VIEW = intArrayOf(0, 1, 2)
    
    // Auto Cancel
    const val AUTO_CANCEL_DELAY = 5000L
    
    // Priority
    const val NOTIFICATION_PRIORITY_HIGH = 1
    const val NOTIFICATION_PRIORITY_DEFAULT = 0
    const val NOTIFICATION_PRIORITY_LOW = -1
}
