package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.PlaybackState
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import com.tinhtx.localplayerapplication.domain.usecase.player.AudioFocusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for playing songs
 */
class PlaySongUseCase @Inject constructor(
    private val mediaPlayerService: MediaPlayerService,
    private val audioFocusUseCase: AudioFocusUseCase
) {
    
    /**
     * Play a specific song
     */
    suspend fun execute(song: Song): Result<Unit> {
        return try {
            // Request audio focus first
            val focusResult = audioFocusUseCase.canStartPlayback()
            if (focusResult.isFailure || focusResult.getOrNull() == false) {
                return Result.failure(Exception("Cannot obtain audio focus"))
            }
            
            mediaPlayerService.playSong(song)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resume current song if paused
     */
    suspend fun resume(): Result<Unit> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            
            if (currentState.canPlay) {
                // Request audio focus
                val focusGranted = audioFocusUseCase.requestAudioFocus()
                if (focusGranted.isFailure || focusGranted.getOrNull() == false) {
                    return Result.failure(Exception("Cannot obtain audio focus"))
                }
                
                mediaPlayerService.play()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot resume in current state: ${currentState.displayName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Play a list of songs as queue
     */
    suspend fun execute(songs: List<Song>, startIndex: Int = 0): Result<Unit> {
        return try {
            if (songs.isEmpty()) {
                return Result.failure(Exception("Song list is empty"))
            }
            
            if (startIndex < 0 || startIndex >= songs.size) {
                return Result.failure(Exception("Invalid start index: $startIndex"))
            }
            
            // Request audio focus
            val focusResult = audioFocusUseCase.canStartPlayback()
            if (focusResult.isFailure || focusResult.getOrNull() == false) {
                return Result.failure(Exception("Cannot obtain audio focus"))
            }
            
            mediaPlayerService.playQueue(songs, startIndex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Play song at specific position
     */
    suspend fun execute(song: Song, startPosition: Long): Result<Unit> {
        return try {
            // First start playing the song
            val playResult = execute(song)
            if (playResult.isFailure) {
                return playResult
            }
            
            // Then seek to the desired position
            mediaPlayerService.seekTo(startPosition)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if can start playback
     */
    suspend fun canPlay(): Result<Boolean> {
        return try {
            val currentState = mediaPlayerService.playbackState.first()
            val focusResult = audioFocusUseCase.canStartPlayback()
            
            val canPlay = currentState.canPlay && 
                         (focusResult.isSuccess && focusResult.getOrNull() == true)
            
            Result.success(canPlay)
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
     * Play song with fade in effect
     */
    suspend fun executeWithFadeIn(song: Song, fadeDurationMs: Long = 1000): Result<Unit> {
        return try {
            // Request audio focus
            val focusResult = audioFocusUseCase.canStartPlayback()
            if (focusResult.isFailure || focusResult.getOrNull() == false) {
                return Result.failure(Exception("Cannot obtain audio focus"))
            }
            
            // TODO: Implement fade in effect
            // This would require integration with audio engine
            mediaPlayerService.playSong(song)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Play song and add to recent history
     */
    suspend fun executeAndRecordHistory(song: Song): Result<Unit> {
        return try {
            val playResult = execute(song)
            if (playResult.isSuccess) {
                // TODO: Record to play history
                // This would integrate with history repository
            }
            playResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Smart play - resume if same song, play if different
     */
    suspend fun smartPlay(song: Song): Result<PlayAction> {
        return try {
            val currentSong = mediaPlayerService.currentSong.first()
            val currentState = mediaPlayerService.playbackState.first()
            
            when {
                currentSong?.id == song.id && currentState.isPaused -> {
                    // Same song, just resume
                    resume()
                    Result.success(PlayAction.RESUMED)
                }
                currentSong?.id == song.id && currentState.isPlaying -> {
                    // Same song already playing
                    Result.success(PlayAction.ALREADY_PLAYING)
                }
                else -> {
                    // Different song, play new one
                    execute(song)
                    Result.success(PlayAction.STARTED_NEW)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Play random song from provided list
     */
    suspend fun playRandom(songs: List<Song>): Result<Song> {
        return try {
            if (songs.isEmpty()) {
                return Result.failure(Exception("Song list is empty"))
            }
            
            val randomSong = songs.random()
            val playResult = execute(randomSong)
            
            if (playResult.isSuccess) {
                Result.success(randomSong)
            } else {
                playResult.exceptionOrNull()?.let { Result.failure(it) } 
                    ?: Result.failure(Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Enum for play action results
 */
enum class PlayAction {
    STARTED_NEW,      // Started playing new song
    RESUMED,          // Resumed paused song
    ALREADY_PLAYING   // Song was already playing
}
