package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.PlaybackQueue
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for skipping to next song
 */
class NextSongUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService
) {
    
    /**
     * Skip to next song in queue
     */
    suspend fun execute(): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            
            if (currentQueue.hasNext) {
                mediaPlayerService.skipToNext()
                val nextSong = mediaPlayerService.currentSong.first()
                Result.success(nextSong)
            } else {
                Result.failure(Exception("No next song available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if next song is available
     */
    suspend fun hasNext(): Result<Boolean> {
        return try {
            val queue = mediaPlayerService.playbackQueue.first()
            Result.success(queue.hasNext)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get next song without playing it
     */
    suspend fun getNextSong(): Result<Song?> {
        return try {
            val queue = mediaPlayerService.playbackQueue.first()
            Result.success(queue.nextSong)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Skip to next song with auto-play control
     */
    suspend fun execute(autoPlay: Boolean): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            
            if (currentQueue.hasNext) {
                if (autoPlay) {
                    mediaPlayerService.skipToNext()
                } else {
                    // Just move to next without playing
                    val nextIndex = when (currentQueue.repeatMode) {
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE -> currentQueue.currentIndex
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL -> {
                            if (currentQueue.currentIndex >= currentQueue.songs.size - 1) 0 
                            else currentQueue.currentIndex + 1
                        }
                        com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF -> currentQueue.currentIndex + 1
                    }
                    mediaPlayerService.skipToQueueItem(nextIndex)
                }
                
                val nextSong = mediaPlayerService.currentSong.first()
                Result.success(nextSong)
            } else {
                Result.failure(Exception("No next song available"))
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
     * Skip multiple songs forward
     */
    suspend fun skipForward(count: Int): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            val targetIndex = currentQueue.currentIndex + count
            
            if (targetIndex < currentQueue.songs.size) {
                mediaPlayerService.skipToQueueItem(targetIndex)
                val targetSong = mediaPlayerService.currentSong.first()
                Result.success(targetSong)
            } else {
                Result.failure(Exception("Cannot skip $count songs forward"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Auto-skip to next when current song ends
     */
    suspend fun autoSkipToNext(): Result<Song?> {
        return try {
            val currentQueue = mediaPlayerService.playbackQueue.first()
            
            when (currentQueue.repeatMode) {
                com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE -> {
                    // Repeat current song
                    mediaPlayerService.seekTo(0)
                    mediaPlayerService.play()
                    val currentSong = mediaPlayerService.currentSong.first()
                    Result.success(currentSong)
                }
                com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL -> {
                    // Go to next song or loop to first
                    mediaPlayerService.skipToNext()
                    val nextSong = mediaPlayerService.currentSong.first()
                    Result.success(nextSong)
                }
                com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF -> {
                    // Go to next song or stop
                    if (currentQueue.hasNext) {
                        mediaPlayerService.skipToNext()
                        val nextSong = mediaPlayerService.currentSong.first()
                        Result.success(nextSong)
                    } else {
                        mediaPlayerService.stop()
                        Result.success(null)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
