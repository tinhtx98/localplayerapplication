package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.PlaybackState
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for seeking to position in current song
 */
class SeekToPositionUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Seek to specific position in milliseconds
     */
    suspend fun execute(positionMs: Long): Result<Unit> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            if (currentSong == null) {
                return Result.failure(Exception("No song currently loaded"))
            }
            
            val validPosition = positionMs.coerceIn(0, currentSong.duration)
            mediaPlayerService.seekTo(validPosition)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Seek to percentage of song duration (0.0 to 1.0)
     */
    suspend fun seekToPercentage(percentage: Float): Result<Unit> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            if (currentSong == null) {
                return Result.failure(Exception("No song currently loaded"))
            }
            
            val validPercentage = percentage.coerceIn(0f, 1f)
            val targetPosition = (currentSong.duration * validPercentage).toLong()
            
            execute(targetPosition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Seek forward by specific duration
     */
    suspend fun seekForward(durationMs: Long): Result<Long> {
        return try {
            val currentPosition = mediaPlayerService.playbackPosition.first()
            val newPosition = currentPosition + durationMs
            
            execute(newPosition)
            Result.success(newPosition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Seek backward by specific duration
     */
    suspend fun seekBackward(durationMs: Long): Result<Long> {
        return try {
            val currentPosition = mediaPlayerService.playbackPosition.first()
            val newPosition = (currentPosition - durationMs).coerceAtLeast(0)
            
            execute(newPosition)
            Result.success(newPosition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current playback position
     */
    fun getCurrentPosition(): Flow<Long> {
        return mediaPlayerService.playbackPosition
    }
    
    /**
     * Get current song info for seeking validation
     */
    fun getCurrentSong(): Flow<Song?> {
        return mediaPlayerService.currentSong
    }
    
    /**
     * Check if seeking is allowed in current state
     */
    suspend fun canSeek(): Result<Boolean> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            val playbackState = mediaPlayerService.playbackState.first()
            
            val canSeek = currentSong != null && 
                         (playbackState.isActive || playbackState == PlaybackState.READY)
            
            Result.success(canSeek)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Quick seek with predefined intervals
     */
    suspend fun quickSeek(direction: SeekDirection, intervalSeconds: Int = 15): Result<Long> {
        return try {
            val intervalMs = intervalSeconds * 1000L
            
            when (direction) {
                SeekDirection.FORWARD -> seekForward(intervalMs)
                SeekDirection.BACKWARD -> seekBackward(intervalMs)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Seek to specific time format (mm:ss)
     */
    suspend fun seekToTime(minutes: Int, seconds: Int): Result<Unit> {
        return try {
            val totalMs = (minutes * 60 + seconds) * 1000L
            execute(totalMs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get seek preview information
     */
    suspend fun getSeekPreview(positionMs: Long): Result<SeekPreview> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            if (currentSong == null) {
                return Result.failure(Exception("No song currently loaded"))
            }
            
            val validPosition = positionMs.coerceIn(0, currentSong.duration)
            val percentage = if (currentSong.duration > 0) {
                (validPosition.toFloat() / currentSong.duration) * 100f
            } else {
                0f
            }
            
            val preview = SeekPreview(
                position = validPosition,
                percentage = percentage,
                formattedTime = formatTime(validPosition),
                remainingTime = formatTime(currentSong.duration - validPosition)
            )
            
            Result.success(preview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Smooth seek with animation
     */
    suspend fun smoothSeek(targetPositionMs: Long, animationDurationMs: Long = 300): Result<Unit> {
        return try {
            // TODO: Implement smooth seeking with animation
            // This would require interpolation between current and target position
            execute(targetPositionMs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    private fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / (1000 * 60)) % 60
        val hours = (timeMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}

/**
 * Enum for seek directions
 */
enum class SeekDirection {
    FORWARD,
    BACKWARD
}

/**
 * Data class for seek preview information
 */
data class SeekPreview(
    val position: Long,
    val percentage: Float,
    val formattedTime: String,
    val remainingTime: String
)
