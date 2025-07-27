package com.tinhtx.localplayerapplication.data.local.datastore

import androidx.datastore.preferences.core.*

/**
 * Keys for DataStore preferences
 */
object PreferencesKeys {
    
    // User Profile Keys
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email") 
    val USER_AVATAR_PATH = stringPreferencesKey("user_avatar_path")
    val USER_CREATED_AT = longPreferencesKey("user_created_at")
    val USER_LAST_LOGIN = longPreferencesKey("user_last_login")
    
    // App Settings Keys
    val APP_THEME = stringPreferencesKey("app_theme")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val GRID_SIZE = stringPreferencesKey("grid_size")
    val SORT_ORDER = stringPreferencesKey("sort_order")
    val LANGUAGE = stringPreferencesKey("language")
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    val APP_VERSION = stringPreferencesKey("app_version")
    
    // Playback Settings Keys
    val REPEAT_MODE = stringPreferencesKey("repeat_mode")
    val SHUFFLE_MODE = stringPreferencesKey("shuffle_mode")
    val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
    val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
    val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    val AUTO_PLAY = booleanPreferencesKey("auto_play")
    val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
    val RESUME_ON_HEADPHONES = booleanPreferencesKey("resume_on_headphones")
    val PAUSE_ON_DISCONNECT = booleanPreferencesKey("pause_on_disconnect")
    val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
    val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
    val LAST_QUEUE_SONGS = stringPreferencesKey("last_queue_songs")
    val LAST_QUEUE_POSITION = intPreferencesKey("last_queue_position")
    
    // Audio Settings Keys
    val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
    val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
    val EQUALIZER_BANDS = stringPreferencesKey("equalizer_bands")
    val BASS_BOOST = intPreferencesKey("bass_boost")
    val VIRTUALIZER = intPreferencesKey("virtualizer")
    val LOUDNESS_ENHANCER = intPreferencesKey("loudness_enhancer")
    val AUDIO_FOCUS_ENABLED = booleanPreferencesKey("audio_focus_enabled")
    val DUCK_VOLUME = booleanPreferencesKey("duck_volume")
    
    // Library Settings Keys
    val AUTO_SCAN = booleanPreferencesKey("auto_scan")
    val SCAN_INTERVAL_HOURS = intPreferencesKey("scan_interval_hours")
    val IGNORE_SHORT_TRACKS = booleanPreferencesKey("ignore_short_tracks")
    val MIN_TRACK_DURATION = intPreferencesKey("min_track_duration")
    val INCLUDED_FOLDERS = stringPreferencesKey("included_folders")
    val EXCLUDED_FOLDERS = stringPreferencesKey("excluded_folders")
    val LAST_SCAN_TIME = longPreferencesKey("last_scan_time")
    val LIBRARY_SORT_ORDER = stringPreferencesKey("library_sort_order")
    val SHOW_UNKNOWN_ARTIST = booleanPreferencesKey("show_unknown_artist")
    val GROUP_BY_ALBUM_ARTIST = booleanPreferencesKey("group_by_album_artist")
    
    // Storage Settings Keys
    val MAX_CACHE_SIZE_MB = longPreferencesKey("max_cache_size_mb")
    val AUTO_CLEAR_CACHE = booleanPreferencesKey("auto_clear_cache")
    val ARTWORK_CACHE_SIZE_MB = longPreferencesKey("artwork_cache_size_mb")
    val DOWNLOAD_OVER_WIFI_ONLY = booleanPreferencesKey("download_over_wifi_only")
    val LYRICS_CACHE_ENABLED = booleanPreferencesKey("lyrics_cache_enabled")
    
    // Privacy Settings Keys
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
    val USAGE_STATISTICS = booleanPreferencesKey("usage_statistics")
    val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
    val SCROBBLE_ENABLED = booleanPreferencesKey("scrobble_enabled")
    val SHARE_NOW_PLAYING = booleanPreferencesKey("share_now_playing")
    
    // Notification Settings Keys
    val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    val SHOW_ALBUM_ART = booleanPreferencesKey("show_album_art")
    val SHOW_PLAYBACK_POSITION = booleanPreferencesKey("show_playback_position")
    val NOTIFICATION_ACTIONS = stringPreferencesKey("notification_actions")
    val COLORED_NOTIFICATION = booleanPreferencesKey("colored_notification")
    val PERSISTENT_NOTIFICATION = booleanPreferencesKey("persistent_notification")
    
    // Sleep Timer Keys
    val SLEEP_TIMER_ENABLED = booleanPreferencesKey("sleep_timer_enabled")
    val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
    val FADE_OUT_ENABLED = booleanPreferencesKey("fade_out_enabled")
    val FADE_OUT_DURATION = intPreferencesKey("fade_out_duration")
    val STOP_AFTER_CURRENT = booleanPreferencesKey("stop_after_current")
    val SLEEP_TIMER_PRESETS = stringPreferencesKey("sleep_timer_presets")
    
    // UI Settings Keys
    val SHOW_MINI_PLAYER = booleanPreferencesKey("show_mini_player")
    val MINI_PLAYER_POSITION = stringPreferencesKey("mini_player_position")
    val LOCK_SCREEN_CONTROLS = booleanPreferencesKey("lock_screen_controls")
    val FULL_SCREEN_PLAYER = booleanPreferencesKey("full_screen_player")
    val PLAYER_BACKGROUND_TYPE = stringPreferencesKey("player_background_type")
    val BLUR_BACKGROUND = booleanPreferencesKey("blur_background")
    val SHOW_VISUALIZER = booleanPreferencesKey("show_visualizer")
    val VISUALIZER_TYPE = stringPreferencesKey("visualizer_type")
    val SHOW_LYRICS = booleanPreferencesKey("show_lyrics")
    val LYRICS_SYNC = booleanPreferencesKey("lyrics_sync")
    val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
    
    // Navigation Settings Keys
    val DEFAULT_TAB = stringPreferencesKey("default_tab")
    val SHOW_TAB_LABELS = booleanPreferencesKey("show_tab_labels")
    val TAB_ORDER = stringPreferencesKey("tab_order")
    val SWIPE_NAVIGATION = booleanPreferencesKey("swipe_navigation")
    
    // Search Settings Keys
    val SEARCH_HISTORY = stringPreferencesKey("search_history")
    val SEARCH_SUGGESTIONS = booleanPreferencesKey("search_suggestions")
    val SEARCH_ONLINE = booleanPreferencesKey("search_online")
    val MAX_SEARCH_RESULTS = intPreferencesKey("max_search_results")
    
    // Playlist Settings Keys
    val AUTO_ADD_TO_QUEUE = booleanPreferencesKey("auto_add_to_queue")
    val SMART_PLAYLISTS_ENABLED = booleanPreferencesKey("smart_playlists_enabled")
    val PLAYLIST_ARTWORK_GENERATION = booleanPreferencesKey("playlist_artwork_generation")
    val COLLABORATIVE_PLAYLISTS = booleanPreferencesKey("collaborative_playlists")
    
    // Backup Settings Keys
    val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
    val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
    val BACKUP_LOCATION = stringPreferencesKey("backup_location")
    val INCLUDE_PLAYLISTS = booleanPreferencesKey("include_playlists")
    val INCLUDE_SETTINGS = booleanPreferencesKey("include_settings")
    val INCLUDE_HISTORY = booleanPreferencesKey("include_history")
    val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
    
    // Cast Settings Keys
    val CAST_ENABLED = booleanPreferencesKey("cast_enabled")
    val AUTO_CAST_DISCOVERY = booleanPreferencesKey("auto_cast_discovery")
    val CAST_QUALITY = stringPreferencesKey("cast_quality")
    val CAST_VOLUME_SYNC = booleanPreferencesKey("cast_volume_sync")
    
    // Advanced Settings Keys
    val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
    val DEBUG_LOGGING = booleanPreferencesKey("debug_logging")
    val PERFORMANCE_MONITORING = booleanPreferencesKey("performance_monitoring")
    val MEMORY_OPTIMIZATION = booleanPreferencesKey("memory_optimization")
    val BATTERY_OPTIMIZATION = booleanPreferencesKey("battery_optimization")
    val NETWORK_CACHE_SIZE = intPreferencesKey("network_cache_size")
    val MAX_CONCURRENT_DOWNLOADS = intPreferencesKey("max_concurrent_downloads")
    
    // Statistics Keys
    val TOTAL_LISTENING_TIME = longPreferencesKey("total_listening_time")
    val SONGS_PLAYED_COUNT = longPreferencesKey("songs_played_count")
    val FAVORITE_GENRE = stringPreferencesKey("favorite_genre")
    val FAVORITE_ARTIST = stringPreferencesKey("favorite_artist")
    val LONGEST_SESSION = longPreferencesKey("longest_session")
    val AVERAGE_SESSION_LENGTH = longPreferencesKey("average_session_length")
    val MOST_ACTIVE_HOUR = intPreferencesKey("most_active_hour")
    val WEEKLY_LISTENING_GOAL = longPreferencesKey("weekly_listening_goal")
    val MONTHLY_LISTENING_GOAL = longPreferencesKey("monthly_listening_goal")
}
