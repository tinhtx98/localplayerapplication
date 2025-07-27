package com.tinhtx.localplayerapplication.data.service

import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val playbackPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val error: PlayerError? = null,
    val audioSessionId: Int = 0,
    val isBuffering: Boolean = false,
    val bufferedPosition: Long = 0L
)

enum class RepeatMode {
    OFF, ALL, ONE;
    
    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}

sealed class PlayerError(val message: String, val cause: Throwable? = null) {
    class NetworkError(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class FileNotFound(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class UnsupportedFormat(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class AudioFocusLoss(message: String) : PlayerError(message)
    class Unknown(message: String, cause: Throwable? = null) : PlayerError(message, cause)
}

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
    ERROR
}

enum class AudioFocusState {
    GAIN,
    LOSS,
    LOSS_TRANSIENT,
    LOSS_TRANSIENT_CAN_DUCK
}

class PlayerStateManager {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    fun updateState(update: (PlayerState) -> PlayerState) {
        _playerState.value = update(_playerState.value)
    }
    
    fun getCurrentState(): PlayerState = _playerState.value
    
    // Convenience methods
    fun setCurrentSong(song: Song?) {
        updateState { it.copy(currentSong = song) }
    }
    
    fun setPlaying(isPlaying: Boolean) {
        updateState { it.copy(isPlaying = isPlaying) }
    }
    
    fun setLoading(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }
    
    fun setPosition(position: Long) {
        updateState { it.copy(playbackPosition = position) }
    }
    
    fun setDuration(duration: Long) {
        updateState { it.copy(duration = duration) }
    }
    
    fun setRepeatMode(mode: RepeatMode) {
        updateState { it.copy(repeatMode = mode) }
    }
    
    fun setShuffleMode(shuffle: Boolean) {
        updateState { it.copy(shuffleMode = shuffle) }
    }
    
    fun setQueue(queue: List<Song>, currentIndex: Int = 0) {
        updateState { 
            it.copy(
                queue = queue, 
                currentIndex = currentIndex,
                currentSong = queue.getOrNull(currentIndex)
            )
        }
    }
    
    fun setError(error: PlayerError?) {
        updateState { it.copy(error = error) }
    }
    
    fun setBuffering(isBuffering: Boolean) {
        updateState { it.copy(isBuffering = isBuffering) }
    }
    
    fun setBufferedPosition(position: Long) {
        updateState { it.copy(bufferedPosition = position) }
    }
    
    fun setAudioSessionId(sessionId: Int) {
        updateState { it.copy(audioSessionId = sessionId) }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        updateState { it.copy(playbackSpeed = speed) }
    }
    
    fun setVolume(volume: Float) {
        updateState { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }
    
    fun moveToNext(): Boolean {
        val state = getCurrentState()
        val nextIndex = when {
            state.repeatMode == RepeatMode.ONE -> state.currentIndex
            state.shuffleMode -> generateShuffleIndex(state.queue.size, state.currentIndex)
            else -> (state.currentIndex + 1) % state.queue.size
        }
        
        if (nextIndex < state.queue.size) {
            updateState { 
                it.copy(
                    currentIndex = nextIndex,
                    currentSong = state.queue.getOrNull(nextIndex)
                )
            }
            return true
        }
        return false
    }
    
    fun moveToPrevious(): Boolean {
        val state = getCurrentState()
        val prevIndex = when {
            state.repeatMode == RepeatMode.ONE -> state.currentIndex
            state.shuffleMode -> generateShuffleIndex(state.queue.size, state.currentIndex)
            else -> if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.size - 1
        }
        
        if (prevIndex >= 0 && prevIndex < state.queue.size) {
            updateState { 
                it.copy(
                    currentIndex = prevIndex,
                    currentSong = state.queue.getOrNull(prevIndex)
                )
            }
            return true
        }
        return false
    }
    
    private fun generateShuffleIndex(queueSize: Int, currentIndex: Int): Int {
        if (queueSize <= 1) return currentIndex
        var newIndex: Int
        do {
            newIndex = (0 until queueSize).random()
        } while (newIndex == currentIndex)
        return newIndex
    }
    
    fun hasNext(): Boolean {
        val state = getCurrentState()
        return when {
            state.queue.isEmpty() -> false
            state.repeatMode == RepeatMode.ALL -> true
            state.repeatMode == RepeatMode.ONE -> true
            else -> state.currentIndex < state.queue.size - 1
        }
    }
    
    fun hasPrevious(): Boolean {
        val state = getCurrentState()
        return when {
            state.queue.isEmpty() -> false
            state.repeatMode == RepeatMode.ALL -> true
            state.repeatMode == RepeatMode.ONE -> true
            else -> state.currentIndex > 0
        }
    }
    
    fun clear() {
        _playerState.value = PlayerState()
    }
}
