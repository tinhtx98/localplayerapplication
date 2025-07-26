package com.tinhtx.localplayerapplication.domain.model

enum class ShuffleMode(val value: Int) {
    OFF(0),
    ON(1);
    
    fun toggle(): ShuffleMode {
        return when (this) {
            OFF -> ON
            ON -> OFF
        }
    }
    
    val displayName: String
        get() = when (this) {
            OFF -> "Shuffle Off"
            ON -> "Shuffle On"
        }
    
    val iconResource: String
        get() = "shuffle"
    
    val isEnabled: Boolean
        get() = this == ON
    
    companion object {
        fun fromValue(value: Int): ShuffleMode {
            return values().find { it.value == value } ?: OFF
        }
        
        fun fromBoolean(enabled: Boolean): ShuffleMode {
            return if (enabled) ON else OFF
        }
    }
}
