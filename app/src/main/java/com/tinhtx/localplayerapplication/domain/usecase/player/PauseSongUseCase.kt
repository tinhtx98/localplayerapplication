package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.PlaybackState
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for pausing song playback
 */
class PauseSongUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Pause current song
     */
    suspend fun execute(): Result<Unit> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            
            if (currentState.canPause) {
                mediaPlayerService.pause()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot pause in current state: ${currentState.displayName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle pause/play
     */
    suspend fun togglePause(): Result<Boolean> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            
            when {
                currentState.isPlaying -> {
                    mediaPlayerService.pause()
                    Result.success(false) // Now paused
                }
                currentState.isPaused -> {
                    mediaPlayerService.play()
                    Result.success(true) // Now playing
                }
                currentState.canPlay -> {
                    mediaPlayerService.play()
                    Result.success(true) // Now playing
                }
                else -> {
                    Result.failure(Exception("Cannot toggle playback in current state: ${currentState.displayName}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if can pause current playback
     */
    suspend fun canPause(): Result<Boolean> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            Result.success(currentState.canPause)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current playback state
     */
    fun getPlaybackState(): Flow<PlaybackState> {
        return mediaPlayerService.playbackState
    }
    
    /**
     * Get currently playing song
     */
    fun getCurrentSong(): Flow<Song?> {
        return mediaPlayerService.currentSong
    }
    
    /**
     * Pause with fade out effect
     */
    suspend fun pauseWithFadeOut(fadeDurationMs: Long = 1000): Result<Unit> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            
            if (currentState.canPause) {
                // TODO: Implement fade out effect
                // This would require integration with audio engine
                mediaPlayerService.pause()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot pause in current state: ${currentState.displayName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Schedule pause after specific duration
     */
    suspend fun pauseAfter(delayMs: Long): Result<Unit> {
        return try {
            // TODO: Implement delayed pause
            // This would use coroutines delay
            kotlinx.coroutines.delay(delayMs)
            execute()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Pause and save current position
     */
    suspend fun pauseAndSavePosition(): Result<PauseState> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            val currentPosition = mediaPlayerService.playbackPosition.first()
            
            mediaPlayerService.pause()
            
            val pauseState = PauseState(
                song = currentSong,
                position = currentPosition,
                timestamp = System.currentTimeMillis()
            )
            
            Result.success(pauseState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if player is currently paused
     */
    suspend fun isPaused(): Result<Boolean> {
        return try {
            val state = mediaPlayerService.playbackState.first()
            Result.success(state.isPaused)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for pause state information
 */
data class PauseState(
    val song: Song?,
    val position: Long,
    val timestamp: Long
) {
    val formattedPosition: String
        get() {
            val seconds = (position / 1000) % 60
            val minutes = (position / (1000 * 60)) % 60
            val hours = (position / (1000 * 60 * 60))
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}
