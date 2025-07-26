package com.tinhtx.localplayerapplication.domain.usecase.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.tinhtx.localplayerapplication.domain.model.AudioFocusState
import javax.inject.Inject

class AudioFocusUseCase @Inject constructor(
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    fun requestAudioFocus(onFocusChangeListener: AudioManager.OnAudioFocusChangeListener): Result<AudioFocusState> {
        return try {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(onFocusChangeListener)
                    .build()
                
                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    onFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
            
            when (result) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> Result.success(AudioFocusState.Gained)
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> Result.failure(Exception("Audio focus request failed"))
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> Result.success(AudioFocusState.LostTransient)
                else -> Result.failure(Exception("Unknown audio focus result: $result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun abandonAudioFocus(): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager.abandonAudioFocusRequest(request)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
