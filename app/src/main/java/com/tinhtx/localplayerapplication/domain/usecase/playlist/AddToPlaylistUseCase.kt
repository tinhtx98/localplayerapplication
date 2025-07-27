package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for managing sleep timer
 */
class SleepTimerUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Set sleep timer for specified minutes
     */
    suspend fun setSleepTimer(minutes: Int): Result<Unit> {
        return try {
            if (minutes <= 0) {
                return Result.failure(Exception("Sleep timer must be greater than 0 minutes"))
            }
            
            if (minutes > 480) { // 8 hours max
                return Result.failure(Exception("Sleep timer cannot exceed 8 hours"))
            }
            
            mediaPlayerService.setSleepTimer(minutes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel active sleep timer
     */
    suspend fun cancelSleepTimer(): Result<Unit> {
        return try {
            mediaPlayerService.cancelSleepTimer()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get remaining sleep timer time
     */
    fun getRemainingTime(): Flow<Long> {
        return mediaPlayerService.sleepTimerRemaining
    }
    
    /**
     * Check if sleep timer is active
     */
    suspend fun isActive(): Result<Boolean> {
        return try {
            val remaining = mediaPlayerService.sleepTimerRemaining.first()
            Result.success(remaining > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get sleep timer status
     */
    suspend fun getStatus(): Result<SleepTimerStatus> {
        return try {
            val remaining = mediaPlayerService.sleepTimerRemaining.first()
            val isActive = remaining > 0
            
            val status = SleepTimerStatus(
                isActive = isActive,
                remainingTimeMs = remaining,
                remainingMinutes = if (isActive) (remaining / 60000).toInt() else 0,
                formattedTime = formatRemainingTime(remaining)
            )
            
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set sleep timer with end of current song
     */
    suspend fun setSleepTimerEndOfSong(): Result<Unit> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            val currentPosition = mediaPlayerService.playbackPosition.first()
            
            if (currentSong == null) {
                return Result.failure(Exception("No song currently playing"))
            }
            
            val remainingMs = currentSong.duration - currentPosition
            val remainingMinutes = (remainingMs / 60000).toInt() + 1 // Add 1 minute buffer
            
            setSleepTimer(remainingMinutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set sleep timer with end of current album/playlist
     */
    suspend fun setSleepTimerEndOfQueue(): Result<Unit> {
        return try {
            val queue = mediaPlayerService.playbackQueue.first()
            val currentPosition = mediaPlayerService.playbackPosition.first()
            
            if (queue.songs.isEmpty()) {
                return Result.failure(Exception("No songs in queue"))
            }
            
            // Calculate remaining time for current song
            val currentSong = queue.currentSong
            val currentSongRemaining = if (currentSong != null) {
                currentSong.duration - currentPosition
            } else {
                0L
            }
            
            // Calculate total duration of remaining songs
            val remainingSongs = queue.songs.drop(queue.currentIndex + 1)
            val remainingSongsDuration = remainingSongs.sumOf { it.duration }
            
            val totalRemainingMs = currentSongRemaining + remainingSongsDuration
            val totalRemainingMinutes = (totalRemainingMs / 60000).toInt() + 2 // Add 2 minutes buffer
            
            setSleepTimer(totalRemainingMinutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extend current sleep timer
     */
    suspend fun extendSleepTimer(additionalMinutes: Int): Result<Unit> {
        return try {
            val currentRemaining = mediaPlayerService.sleepTimerRemaining.first()
            
            if (currentRemaining <= 0) {
                return Result.failure(Exception("No active sleep timer to extend"))
            }
            
            val currentMinutes = (currentRemaining / 60000).toInt()
            val newTotalMinutes = currentMinutes + additionalMinutes
            
            setSleepTimer(newTotalMinutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get common sleep timer presets
     */
    fun getPresets(): List<SleepTimerPreset> {
        return listOf(
            SleepTimerPreset("15 minutes", 15),
            SleepTimerPreset("30 minutes", 30),
            SleepTimerPreset("45 minutes", 45),
            SleepTimerPreset("1 hour", 60),
            SleepTimerPreset("1.5 hours", 90),
            SleepTimerPreset("2 hours", 120),
            SleepTimerPreset("End of song", -1), // Special case
            SleepTimerPreset("End of queue", -2)  // Special case
        )
    }
    
    /**
     * Set sleep timer using preset
     */
    suspend fun setSleepTimerPreset(preset: SleepTimerPreset): Result<Unit> {
        return try {
            when (preset.minutes) {
                -1 -> setSleepTimerEndOfSong()
                -2 -> setSleepTimerEndOfQueue()
                else -> setSleepTimer(preset.minutes)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    private fun formatRemainingTime(timeMs: Long): String {
        if (timeMs <= 0) return "00:00"
        
        val hours = timeMs / (1000 * 60 * 60)
        val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (timeMs % (1000 * 60)) / 1000
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

/**
 * Data class for sleep timer status
 */
data class SleepTimerStatus(
    val isActive: Boolean,
    val remainingTimeMs: Long,
    val remainingMinutes: Int,
    val formattedTime: String
)

/**
 * Data class for sleep timer presets
 */
data class SleepTimerPreset(
    val displayName: String,
    val minutes: Int // -1 for end of song, -2 for end of queue
)
