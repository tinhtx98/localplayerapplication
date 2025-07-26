package com.tinhtx.localplayerapplication.data.local.datastore

import androidx.datastore.preferences.core.*

object PreferencesKeys {
    // User Preferences
    val USER_NAME = stringPreferencesKey("user_name")
    val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val LAST_SCAN_TIME = longPreferencesKey("last_scan_time")
    val AUTO_SCAN_ENABLED = booleanPreferencesKey("auto_scan_enabled")
    
    // Settings Preferences
    val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
    val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
    val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
    val REPEAT_MODE = intPreferencesKey("repeat_mode")
    val SHUFFLE_MODE = intPreferencesKey("shuffle_mode")
    val VOLUME_LEVEL = floatPreferencesKey("volume_level")
    val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
    val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
    val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
    val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
    val SLEEP_TIMER_DURATION = intPreferencesKey("sleep_timer_duration")
    val HEADPHONE_AUTO_PLAY = booleanPreferencesKey("headphone_auto_play")
    val HEADPHONE_AUTO_PAUSE = booleanPreferencesKey("headphone_auto_pause")
    val SHOW_NOTIFICATION = booleanPreferencesKey("show_notification")
    val SHOW_LOCK_SCREEN_CONTROLS = booleanPreferencesKey("show_lock_screen_controls")
    val LIBRARY_TAB_ORDER = stringPreferencesKey("library_tab_order")
    val GRID_SIZE = intPreferencesKey("grid_size")
    val SORT_ORDER = stringPreferencesKey("sort_order")
    
    // Playback State (for restoration)
    val CURRENT_SONG_ID = longPreferencesKey("current_song_id")
    val CURRENT_POSITION = longPreferencesKey("current_position")
    val CURRENT_PLAYLIST_ID = longPreferencesKey("current_playlist_id")
    val QUEUE_SONG_IDS = stringPreferencesKey("queue_song_ids") // JSON string
    val QUEUE_INDEX = intPreferencesKey("queue_index")
}
