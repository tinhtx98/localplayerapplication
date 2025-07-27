package com.tinhtx.localplayerapplication.domain.model

/**
 * Enum representing audio focus states
 */
enum class AudioFocusState {
    NONE,               // No audio focus
    GAIN,               // Full audio focus
    LOSS,               // Lost audio focus permanently
    LOSS_TRANSIENT,     // Lost audio focus temporarily
    LOSS_TRANSIENT_CAN_DUCK;  // Lost audio focus but can duck volume
    
    val displayName: String
        get() = when (this) {
            NONE -> "None"
            GAIN -> "Gained"
            LOSS -> "Lost"
            LOSS_TRANSIENT -> "Lost Temporarily"
            LOSS_TRANSIENT_CAN_DUCK -> "Ducked"
        }
    
    val canPlayback: Boolean
        get() = this == GAIN || this == LOSS_TRANSIENT_CAN_DUCK
    
    val shouldPause: Boolean
        get() = this == LOSS || this == LOSS_TRANSIENT
    
    val shouldDuck: Boolean
        get() = this == LOSS_TRANSIENT_CAN_DUCK
}
