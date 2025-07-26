package com.tinhtx.localplayerapplication.core.constants

object MediaConstants {
    // Audio Formats
    val SUPPORTED_AUDIO_FORMATS = arrayOf(
        "mp3", "m4a", "aac", "ogg", "wav", "flac", "wma", "3gp", "mp4"
    )
    
    val SUPPORTED_MIME_TYPES = arrayOf(
        "audio/mpeg",
        "audio/mp4",
        "audio/aac",
        "audio/ogg",
        "audio/wav",
        "audio/flac",
        "audio/x-ms-wma",
        "audio/3gpp",
        "video/mp4"
    )
    
    // MediaStore Projections
    val SONG_PROJECTION = arrayOf(
        "audio._id",
        "audio.title",
        "audio.artist",
        "audio.album",
        "audio.duration",
        "audio.data",
        "audio.date_added",
        "audio.album_id",
        "audio.artist_id",
        "audio.track",
        "audio.year",
        "audio.size"
    )
    
    val ALBUM_PROJECTION = arrayOf(
        "album._id",
        "album.album",
        "album.artist",
        "album.numsongs",
        "album.first_year",
        "album.last_year"
    )
    
    val ARTIST_PROJECTION = arrayOf(
        "artist._id",
        "artist.artist",
        "artist.number_of_albums",
        "artist.number_of_tracks"
    )
    
    // Selection Clauses
    const val SONG_SELECTION = "audio.is_music = 1 AND audio.duration >= ?"
    val SONG_SELECTION_ARGS = arrayOf("30000") // 30 seconds minimum
    
    const val ALBUM_SELECTION = "album.numsongs > 0"
    const val ARTIST_SELECTION = "artist.number_of_tracks > 0"
    
    // Sort Orders
    const val SONG_SORT_ORDER = "audio.title ASC"
    const val ALBUM_SORT_ORDER = "album.album ASC"
    const val ARTIST_SORT_ORDER = "artist.artist ASC"
    
    // Playback States
    const val STATE_NONE = 0
    const val STATE_STOPPED = 1
    const val STATE_PAUSED = 2
    const val STATE_PLAYING = 3
    const val STATE_BUFFERING = 4
    const val STATE_ERROR = 5
    
    // Repeat Modes
    const val REPEAT_MODE_OFF = 0
    const val REPEAT_MODE_ONE = 1
    const val REPEAT_MODE_ALL = 2
    
    // Shuffle Modes
    const val SHUFFLE_MODE_OFF = 0
    const val SHUFFLE_MODE_ON = 1
    
    // Audio Focus
    const val AUDIO_FOCUS_GAIN = 1
    const val AUDIO_FOCUS_LOSS = -1
    const val AUDIO_FOCUS_LOSS_TRANSIENT = -2
    const val AUDIO_FOCUS_LOSS_TRANSIENT_CAN_DUCK = -3
    
    // Seek Values
    const val SEEK_FORWARD_MS = 15000L
    const val SEEK_BACKWARD_MS = 15000L
    
    // Album Art
    const val ALBUM_ART_SIZE = 512
    const val ALBUM_ART_QUALITY = 85
    
    // Media Session
    const val MEDIA_SESSION_TAG = "LocalPlayerMediaSession"
    
    // Queue
    const val MAX_QUEUE_SIZE = 1000
    
    // Crossfade
    const val DEFAULT_CROSSFADE_DURATION = 3000L
    
    // Equalizer
    const val EQUALIZER_ENABLED = "equalizer_enabled"
    const val EQUALIZER_PRESET = "equalizer_preset"
    
    // Error Codes
    const val ERROR_CODE_UNSPECIFIED = 0
    const val ERROR_CODE_IO_UNSPECIFIED = 2000
    const val ERROR_CODE_IO_NETWORK_CONNECTION_FAILED = 2001
    const val ERROR_CODE_IO_FILE_NOT_FOUND = 2005
    const val ERROR_CODE_PARSING_CONTAINER_MALFORMED = 3001
    const val ERROR_CODE_PARSING_MANIFEST_MALFORMED = 3002
    const val ERROR_CODE_DECODER_INIT_FAILED = 4001
    const val ERROR_CODE_DECODER_QUERY_FAILED = 4002
}
