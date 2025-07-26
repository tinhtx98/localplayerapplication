package com.tinhtx.localplayerapplication.presentation.service.media

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MediaSessionCallback @Inject constructor(
    private val playPauseUseCase: PlayPauseUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val setRepeatModeUseCase: SetRepeatModeUseCase,
    private val setShuffleModeUseCase: SetShuffleModeUseCase,
    private val serviceScope: CoroutineScope
) : MediaSessionCompat.Callback() {
    
    override fun onPlay() {
        serviceScope.launch {
            try {
                playPauseUseCase()
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onPlay", exception)
            }
        }
    }
    
    override fun onPause() {
        serviceScope.launch {
            try {
                playPauseUseCase()
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onPause", exception)
            }
        }
    }
    
    override fun onSkipToNext() {
        serviceScope.launch {
            try {
                skipToNextUseCase()
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onSkipToNext", exception)
            }
        }
    }
    
    override fun onSkipToPrevious() {
        serviceScope.launch {
            try {
                skipToPreviousUseCase()
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onSkipToPrevious", exception)
            }
        }
    }
    
    override fun onSeekTo(pos: Long) {
        serviceScope.launch {
            try {
                seekToUseCase(pos)
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onSeekTo", exception)
            }
        }
    }
    
    override fun onSetRepeatMode(repeatMode: Int) {
        serviceScope.launch {
            try {
                val mode = when (repeatMode) {
                    android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE -> 
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF
                    android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE -> 
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE
                    android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL -> 
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL
                    else -> com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF
                }
                setRepeatModeUseCase(mode)
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onSetRepeatMode", exception)
            }
        }
    }
    
    override fun onSetShuffleMode(shuffleMode: Int) {
        serviceScope.launch {
            try {
                val mode = when (shuffleMode) {
                    android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE -> 
                        com.tinhtx.localplayerapplication.domain.model.ShuffleMode.OFF
                    android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL -> 
                        com.tinhtx.localplayerapplication.domain.model.ShuffleMode.ON
                    else -> com.tinhtx.localplayerapplication.domain.model.ShuffleMode.OFF
                }
                setShuffleModeUseCase(mode)
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onSetShuffleMode", exception)
            }
        }
    }
    
    override fun onStop() {
        serviceScope.launch {
            try {
                // Stop playback and service
                android.util.Log.d("MediaSessionCallback", "onStop called")
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onStop", exception)
            }
        }
    }
    
    override fun onCustomAction(action: String?, extras: Bundle?) {
        serviceScope.launch {
            try {
                when (action) {
                    "TOGGLE_FAVORITE" -> {
                        // Handle favorite toggle
                    }
                    "ADD_TO_QUEUE" -> {
                        // Handle add to queue
                    }
                    // Add more custom actions as needed
                }
            } catch (exception: Exception) {
                android.util.Log.e("MediaSessionCallback", "Error in onCustomAction", exception)
            }
        }
    }
}
