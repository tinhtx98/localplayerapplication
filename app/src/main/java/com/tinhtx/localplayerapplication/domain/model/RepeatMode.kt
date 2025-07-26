package com.tinhtx.localplayerapplication.domain.model

enum class RepeatMode(val value: Int) {
    OFF(0),
    ONE(1),
    ALL(2);
    
    fun next(): RepeatMode {
        return when (this) {
            OFF -> ONE
            ONE -> ALL
            ALL -> OFF
        }
    }
    
    val displayName: String
        get() = when (this) {
            OFF -> "Repeat Off"
            ONE -> "Repeat One"
            ALL -> "Repeat All"
        }
    
    val iconResource: String
        get() = when (this) {
            OFF -> "repeat"
            ONE -> "repeat_one"
            ALL -> "repeat"
        }
    
    companion object {
        fun fromValue(value: Int): RepeatMode {
            return values().find { it.value == value } ?: OFF
        }
    }
}
