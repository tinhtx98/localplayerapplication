package com.tinhtx.localplayerapplication.data.service.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tinhtx.localplayerapplication.data.service.AudioFocusState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusManager @Inject constructor(
    private val context: Context
) {
    
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val handler = Handler(Looper.getMainLooper())
    
    private val _audioFocusState = MutableStateFlow(AudioFocusState.LOSS)
    val audioFocusState: StateFlow<AudioFocusState> = _audioFocusState.asStateFlow()
    
    private var audioFocusRequest: AudioFocusRequest? = null
    private var currentVolume = 1.0f
    private var isDucking = false
    private var focusChangeListener: AudioFocusChangeListener? = null
    
    // Delayed focus recovery
    private var delayedFocusRunnable: Runnable? = null
    private val focusRecoveryDelay = 2000L // 2 seconds
    
    interface AudioFocusChangeListener {
        fun onAudioFocusGained()
        fun onAudioFocusLost()
        fun onAudioFocusLostTransient()
        fun onAudioFocusLostTransientCanDuck()
    }
    
    fun setAudioFocusChangeListener(listener: AudioFocusChangeListener) {
        this.focusChangeListener = listener
    }
    
    private val onAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        handler.post {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    handleAudioFocusGain()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    handleAudioFocusLoss()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    handleAudioFocusLossTransient()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    handleAudioFocusLossTransientCanDuck()
                }
            }
        }
    }
    
    /**
     * Request audio focus for music playback
     */
    fun requestAudioFocus(): Boolean {
        if (!context.supportsAudioFocus()) {
            _audioFocusState.value = AudioFocusState.GAIN
            return true
        }
        
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusApi26()
        } else {
            requestAudioFocusLegacy()
        }
        
        return when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                _audioFocusState.value = AudioFocusState.GAIN
                cancelDelayedFocusRecovery()
                true
            }
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                // Handle delayed focus grant
                scheduleDelayedFocusRecovery()
                false
            }
            else -> {
                _audioFocusState.value = AudioFocusState.LOSS
                false
            }
        }
    }
    
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusApi26(): Int {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(onAudioFocusChangeListener, handler)
            .build()
        
        return audioManager.requestAudioFocus(audioFocusRequest!!)
    }
    
    @Suppress("DEPRECATION")
    private fun requestAudioFocusLegacy(): Int {
        return audioManager.requestAudioFocus(
            onAudioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }
    
    /**
     * Abandon audio focus
     */
    fun abandonAudioFocus() {
        cancelDelayedFocusRecovery()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }
        
        _audioFocusState.value = AudioFocusState.LOSS
        restoreVolumeIfDucking()
    }
    
    private fun handleAudioFocusGain() {
        _audioFocusState.value = AudioFocusState.GAIN
        restoreVolumeIfDucking()
        focusChangeListener?.onAudioFocusGained()
        
        ServiceUtils.logPlaybackEvent(
            "AudioFocus", 
            null, 
            "Focus gained"
        )
    }
    
    private fun handleAudioFocusLoss() {
        _audioFocusState.value = AudioFocusState.LOSS
        restoreVolumeIfDucking()
        focusChangeListener?.onAudioFocusLost()
        
        ServiceUtils.logPlaybackEvent(
            "AudioFocus", 
            null, 
            "Focus lost permanently"
        )
    }
    
    private fun handleAudioFocusLossTransient() {
        _audioFocusState.value = AudioFocusState.LOSS_TRANSIENT
        restoreVolumeIfDucking()
        focusChangeListener?.onAudioFocusLostTransient()
        
        ServiceUtils.logPlaybackEvent(
            "AudioFocus", 
            null, 
            "Focus lost transient"
        )
    }
    
    private fun handleAudioFocusLossTransientCanDuck() {
        _audioFocusState.value = AudioFocusState.LOSS_TRANSIENT_CAN_DUCK
        duckVolume()
        focusChangeListener?.onAudioFocusLostTransientCanDuck()
        
        ServiceUtils.logPlaybackEvent(
            "AudioFocus", 
            null, 
            "Focus lost transient - can duck"
        )
    }
    
    /**
     * Duck volume for transient focus loss
     */
    private fun duckVolume() {
        if (!isDucking) {
            isDucking = true
            // Duck to 20% of current volume
            // This will be handled by the ExoPlayerManager
        }
    }
    
    /**
     * Restore volume after ducking
     */
    private fun restoreVolumeIfDucking() {
        if (isDucking) {
            isDucking = false
            // Restore volume will be handled by ExoPlayerManager
        }
    }
    
    /**
     * Schedule delayed focus recovery
     */
    private fun scheduleDelayedFocusRecovery() {
        cancelDelayedFocusRecovery()
        
        delayedFocusRunnable = Runnable {
            if (_audioFocusState.value != AudioFocusState.GAIN) {
                val success = requestAudioFocus()
                ServiceUtils.logPlaybackEvent(
                    "AudioFocus", 
                    null, 
                    "Delayed focus recovery: $success"
                )
            }
        }
        
        handler.postDelayed(delayedFocusRunnable!!, focusRecoveryDelay)
    }
    
    /**
     * Cancel delayed focus recovery
     */
    private fun cancelDelayedFocusRecovery() {
        delayedFocusRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
            delayedFocusRunnable = null
        }
    }
    
    /**
     * Check if audio focus is granted
     */
    fun hasAudioFocus(): Boolean {
        return _audioFocusState.value == AudioFocusState.GAIN
    }
    
    /**
     * Check if should duck volume
     */
    fun shouldDuckVolume(): Boolean {
        return _audioFocusState.value == AudioFocusState.LOSS_TRANSIENT_CAN_DUCK
    }
    
    /**
     * Check if can play audio
     */
    fun canPlayAudio(): Boolean {
        return when (_audioFocusState.value) {
            AudioFocusState.GAIN -> true
            AudioFocusState.LOSS_TRANSIENT_CAN_DUCK -> true
            else -> false
        }
    }
    
    /**
     * Get current focus state
     */
    fun getCurrentFocusState(): AudioFocusState {
        return _audioFocusState.value
    }
    
    /**
     * Set volume level
     */
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }
    
    /**
     * Get effective volume (considering ducking)
     */
    fun getEffectiveVolume(): Float {
        return if (shouldDuckVolume()) {
            currentVolume * 0.2f // Duck to 20%
        } else {
            currentVolume
        }
    }
    
    /**
     * Handle becom  noisy (headphones disconnected)
     */
    fun handleBecomeNoisy() {
        // Pause playback when headphones are disconnected
        focusChangeListener?.onAudioFocusLostTransient()
        
        ServiceUtils.logPlaybackEvent(
            "AudioFocus", 
            null, 
            "Became noisy - headphones disconnected"
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        abandonAudioFocus()
        focusChangeListener = null
        cancelDelayedFocusRecovery()
    }
}
