package com.tinhtx.localplayerapplication.domain.model

/**
 * Enum representing shuffle modes
 */
enum class ShuffleMode {
    OFF,    // No shuffle
    ALL;    // Shuffle all songs
    
    val displayName: String
        get() = when (this) {
            OFF -> "Off"
            ALL -> "Shuffle"
        }
    
    fun toggle(): ShuffleMode {
        return when (this) {
            OFF -> ALL
            ALL -> OFF
        }
    }
    
    val isEnabled: Boolean
        get() = this == ALL
}
