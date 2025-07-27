package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.AudioFocusState
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing audio focus
 */
class AudioFocusUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Request audio focus for playback
     */
    fun requestAudioFocus(): Result<Boolean> {
        return try {
            val granted = mediaPlayerService.requestAudioFocus()
            Result.success(granted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Abandon audio focus
     */
    fun abandonAudioFocus(): Result<Unit> {
        return try {
            mediaPlayerService.abandonAudioFocus()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current audio focus state
     */
    fun getAudioFocusState(): Flow<AudioFocusState> {
        return mediaPlayerService.audioFocusState
    }
    
    /**
     * Handle audio focus change
     */
    fun handleAudioFocusChange(focusState: AudioFocusState): Result<Unit> {
        return try {
            when (focusState) {
                AudioFocusState.GAIN -> {
                    // Resume playback if it was paused due to focus loss
                    mediaPlayerService.play()
                }
                AudioFocusState.LOSS -> {
                    // Stop playback permanently
                    mediaPlayerService.stop()
                }
                AudioFocusState.LOSS_TRANSIENT -> {
                    // Pause playback temporarily
                    mediaPlayerService.pause()
                }
                AudioFocusState.LOSS_TRANSIENT_CAN_DUCK -> {
                    // Lower volume but continue playing
                    // This would be handled by the audio focus manager
                }
                AudioFocusState.NONE -> {
                    // No action needed
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if audio focus is required for playback
     */
    fun isAudioFocusRequired(): Boolean {
        // Audio focus is always required for music playback
        return true
    }
    
    /**
     * Check if can start playback based on audio focus
     */
    suspend fun canStartPlayback(): Result<Boolean> {
        return try {
            val currentState = mediaPlayerService.audioFocusState
            var canPlay = false
            
            currentState.collect { state ->
                canPlay = state.canPlayback
                return@collect
            }
            
            if (!canPlay) {
                val focusGranted = mediaPlayerService.requestAudioFocus()
                canPlay = focusGranted
            }
            
            Result.success(canPlay)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
