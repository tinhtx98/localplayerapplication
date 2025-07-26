package com.tinhtx.localplayerapplication.domain.model

sealed class AudioFocusState {
    object Gained : AudioFocusState()
    object Lost : AudioFocusState()
    object LostTransient : AudioFocusState()
    object LostTransientCanDuck : AudioFocusState()
    
    val shouldPause: Boolean
        get() = when (this) {
            is Gained -> false
            is Lost -> true
            is LostTransient -> true
            is LostTransientCanDuck -> false
        }
    
    val shouldDuck: Boolean
        get() = this is LostTransientCanDuck
    
    val canResume: Boolean
        get() = this is Gained
    
    companion object {
        fun fromFocusChange(focusChange: Int): AudioFocusState {
            return when (focusChange) {
                android.media.AudioManager.AUDIOFOCUS_GAIN -> Gained
                android.media.AudioManager.AUDIOFOCUS_LOSS -> Lost
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> LostTransient
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> LostTransientCanDuck
                else -> Lost
            }
        }
    }
}
