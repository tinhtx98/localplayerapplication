package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.PlaybackQueue
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for skipping to previous song
 */
class PreviousSongUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Skip to previous song in queue
     */
    suspend fun execute(): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            val currentPosition = mediaPlayerService.playbackPosition.first()
            
            // If we're more than 3 seconds into the song, restart it instead of going to previous
            if (currentPosition > 3000) {
                mediaPlayerService.seekTo(0)
                val currentSong = mediaPlayerService.currentSong.first()
                Result.success(currentSong)
            } else if (currentQueue.hasPrevious) {
                mediaPlayerService.skipToPrevious()
                val previousSong = mediaPlayerService.currentSong.first()
                Result.success(previousSong)
            } else {
                Result.failure(Exception("No previous song available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Force skip to previous song (ignore current position)
     */
    suspend fun forcePrevious(): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            
            if (currentQueue.hasPrevious) {
                mediaPlayerService.skipToPrevious()
                val previousSong = mediaPlayerService.currentSong.first()
                Result.success(previousSong)
            } else {
                Result.failure(Exception("No previous song available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if previous song is available
     */
    suspend fun hasPrevious(): Result<Boolean> {
        return try {
            val queue = mediaPlayerService.playbackQueue.first()
            Result.success(queue.hasPrevious)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get previous song without playing it
     */
    suspend fun getPreviousSong(): Result<Song?> {
        return try {
            val queue = mediaPlayerService.playbackQueue.first()
            Result.success(queue.previousSong)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Skip to previous song with auto-play control
     */
    suspend fun execute(autoPlay: Boolean): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            val currentPosition = mediaPlayerService.playbackPosition.first()
            
            when {
                currentPosition > 3000 && autoPlay -> {
                    // Restart current song
                    mediaPlayerService.seekTo(0)
                    val currentSong = mediaPlayerService.currentSong.first()
                    Result.success(currentSong)
                }
                currentQueue.hasPrevious -> {
                    if (autoPlay) {
                        mediaPlayerService.skipToPrevious()
                    } else {
                        // Just move to previous without playing
                        val prevIndex = when (currentQueue.repeatMode) {
                            com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE -> currentQueue.currentIndex
                            com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL -> {
                                if (currentQueue.currentIndex <= 0) currentQueue.songs.size - 1 
                                else currentQueue.currentIndex - 1
                            }
                            com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF -> currentQueue.currentIndex - 1
                        }
                        mediaPlayerService.skipToQueueItem(prevIndex)
                    }
                    
                    val previousSong = mediaPlayerService.currentSong.first()
                    Result.success(previousSong)
                }
                else -> {
                    Result.failure(Exception("No previous song available"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playback queue
     */
    fun getPlaybackQueue(): Flow<PlaybackQueue> {
        return mediaPlayerService.playbackQueue
    }
    
    /**
     * Skip multiple songs backward
     */
    suspend fun skipBackward(count: Int): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            val targetIndex = currentQueue.currentIndex - count
            
            if (targetIndex >= 0) {
                mediaPlayerService.skipToQueueItem(targetIndex)
                val targetSong = mediaPlayerService.currentSong.first()
                Result.success(targetSong)
            } else {
                Result.failure(Exception("Cannot skip $count songs backward"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restart current song
     */
    suspend fun restartCurrentSong(): Result<Song?> {
        return try {
            mediaPlayerService.seekTo(0)
            val currentSong = mediaPlayerService.currentSong.first()
            Result.success(currentSong)
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
     * Smart previous - restart if > 3 seconds, previous if <= 3 seconds
     */
    suspend fun smartPrevious(): Result<PreviousAction> {
        return try {
            val currentPosition = mediaPlayerService.playbackPosition.first()
            val currentQueue = mediaPlayerService.playbackQueue.first()
            
            if (currentPosition > 3000) {
                // Restart current song
                mediaPlayerService.seekTo(0)
                Result.success(PreviousAction.RESTARTED_CURRENT)
            } else if (currentQueue.hasPrevious) {
                // Go to previous song
                mediaPlayerService.skipToPrevious()
                Result.success(PreviousAction.SKIPPED_TO_PREVIOUS)
            } else {
                // No previous available, restart current
                mediaPlayerService.seekTo(0)
                Result.success(PreviousAction.RESTARTED_CURRENT)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Enum for previous action results
 */
enum class PreviousAction {
    SKIPPED_TO_PREVIOUS,  // Moved to previous song
    RESTARTED_CURRENT     // Restarted current song
}
