package com.tinhtx.localplayerapplication.domain.model

/**
 * Enum representing playback states
 */
enum class PlaybackState {
    IDLE,       // Player is idle
    LOADING,    // Loading media
    READY,      // Ready to play
    PLAYING,    // Currently playing
    PAUSED,     // Paused
    STOPPED,    // Stopped
    BUFFERING,  // Buffering
    ERROR;      // Error state
    
    val displayName: String
        get() = when (this) {
            IDLE -> "Idle"
            LOADING -> "Loading"
            READY -> "Ready"
            PLAYING -> "Playing"
            PAUSED -> "Paused"
            STOPPED -> "Stopped"
            BUFFERING -> "Buffering"
            ERROR -> "Error"
        }
    
    val isPlaying: Boolean
        get() = this == PLAYING
    
    val isPaused: Boolean
        get() = this == PAUSED
    
    val canPlay: Boolean
        get() = this == READY || this == PAUSED || this == STOPPED
    
    val canPause: Boolean
        get() = this == PLAYING || this == BUFFERING
    
    val isActive: Boolean
        get() = this == PLAYING || this == PAUSED || this == BUFFERING
}
