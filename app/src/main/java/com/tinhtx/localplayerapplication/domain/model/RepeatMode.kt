package com.tinhtx.localplayerapplication.domain.model

/**
 * Enum representing repeat modes
 */
enum class RepeatMode {
    OFF,    // No repeat
    ONE,    // Repeat current song
    ALL;    // Repeat all songs in queue
    
    val displayName: String
        get() = when (this) {
            OFF -> "Off"
            ONE -> "Repeat One"
            ALL -> "Repeat All"
        }
    
    fun next(): RepeatMode {
        return when (this) {
            OFF -> ONE
            ONE -> ALL
            ALL -> OFF
        }
    }
}
