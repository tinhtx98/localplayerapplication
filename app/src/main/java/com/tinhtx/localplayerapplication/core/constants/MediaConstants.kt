package com.tinhtx.localplayerapplication.core.constants

/**
 * Media playback related constants
 */
object MediaConstants {
    // Media Session
    const val MEDIA_SESSION_TAG = "LocalPlayerMediaSession"
    const val MEDIA_SESSION_ID = "local_player_session"
    
    // Playback Actions
    const val ACTION_PLAY = "action_play"
    const val ACTION_PAUSE = "action_pause"
    const val ACTION_STOP = "action_stop"
    const val ACTION_SKIP_NEXT = "action_skip_next"
    const val ACTION_SKIP_PREVIOUS = "action_skip_previous"
    const val ACTION_PLAY_PAUSE = "action_play_pause"
    const val ACTION_SEEK_TO = "action_seek_to"
    const val ACTION_SET_REPEAT_MODE = "action_set_repeat_mode"
    const val ACTION_SET_SHUFFLE_MODE = "action_set_shuffle_mode"
    
    // Custom Actions
    const val ACTION_TOGGLE_FAVORITE = "action_toggle_favorite"
    const val ACTION_ADD_TO_QUEUE = "action_add_to_queue"
    const val ACTION_REMOVE_FROM_QUEUE = "action_remove_from_queue"
    const val ACTION_CLEAR_QUEUE = "action_clear_queue"
    
    // Media Metadata Keys
    const val METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI"
    const val METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID"
    const val METADATA_KEY_MEDIA_URI = "android.media.metadata.MEDIA_URI"
    
    // Playback States
    const val PLAYBACK_STATE_IDLE = 0
    const val PLAYBACK_STATE_PLAYING = 1
    const val PLAYBACK_STATE_PAUSED = 2
    const val PLAYBACK_STATE_STOPPED = 3
    const val PLAYBACK_STATE_BUFFERING = 4
    const val PLAYBACK_STATE_ERROR = 5
    
    // Repeat Modes
    const val REPEAT_MODE_OFF = 0
    const val REPEAT_MODE_ONE = 1
    const val REPEAT_MODE_ALL = 2
    
    // Shuffle Modes
    const val SHUFFLE_MODE_OFF = 0
    const val SHUFFLE_MODE_ALL = 1
    
    // Audio Format Support
    val SUPPORTED_AUDIO_FORMATS = arrayOf(
        "mp3", "flac", "wav", "aac", "ogg", "m4a", "wma", "opus"
    )
    
    val SUPPORTED_MIME_TYPES = arrayOf(
        "audio/mpeg",
        "audio/flac", 
        "audio/wav",
        "audio/aac",
        "audio/ogg",
        "audio/mp4",
        "audio/x-ms-wma",
        "audio/opus"
    )
    
    // Playback Speed
    const val MIN_PLAYBACK_SPEED = 0.25f
    const val MAX_PLAYBACK_SPEED = 3.0f
    const val PLAYBACK_SPEED_NORMAL = 1.0f
    
    // Volume
    const val MIN_VOLUME = 0.0f
    const val MAX_VOLUME = 1.0f
    
    // Seeking
    const val SEEK_STEP_MS = 10000L // 10 seconds
    const val FAST_SEEK_STEP_MS = 30000L // 30 seconds
    
    // Cross Fade
    const val MIN_CROSSFADE_DURATION = 0
    const val MAX_CROSSFADE_DURATION = 10 // seconds
    
    // Queue
    const val MAX_QUEUE_SIZE = 500
    const val SHUFFLE_BUFFER_SIZE = 10
    
    // Audio Effects
    const val EQUALIZER_BAND_COUNT = 10
    const val BASS_BOOST_MAX = 1000
    const val VIRTUALIZER_MAX = 1000
    
    // Media Store Projections
    val SONG_PROJECTION = arrayOf(
        android.provider.MediaStore.Audio.Media._ID,
        android.provider.MediaStore.Audio.Media.TITLE,
        android.provider.MediaStore.Audio.Media.ARTIST,
        android.provider.MediaStore.Audio.Media.ALBUM,
        android.provider.MediaStore.Audio.Media.ALBUM_ID,
        android.provider.MediaStore.Audio.Media.DURATION,
        android.provider.MediaStore.Audio.Media.DATA,
        android.provider.MediaStore.Audio.Media.SIZE,
        android.provider.MediaStore.Audio.Media.MIME_TYPE,
        android.provider.MediaStore.Audio.Media.DATE_ADDED,
        android.provider.MediaStore.Audio.Media.DATE_MODIFIED,
        android.provider.MediaStore.Audio.Media.YEAR,
        android.provider.MediaStore.Audio.Media.TRACK
    )
    
    val ALBUM_PROJECTION = arrayOf(
        android.provider.MediaStore.Audio.Albums._ID,
        android.provider.MediaStore.Audio.Albums.ALBUM,
        android.provider.MediaStore.Audio.Albums.ARTIST,
        android.provider.MediaStore.Audio.Albums.FIRST_YEAR,
        android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS
    )
    
    val ARTIST_PROJECTION = arrayOf(
        android.provider.MediaStore.Audio.Artists._ID,
        android.provider.MediaStore.Audio.Artists.ARTIST,
        android.provider.MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        android.provider.MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    )
}
