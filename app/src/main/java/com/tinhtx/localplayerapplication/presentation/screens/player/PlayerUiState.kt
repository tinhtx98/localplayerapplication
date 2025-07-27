package com.tinhtx.localplayerapplication.presentation.screens.player

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Player Screen - Complete state management
 */
data class PlayerUiState(
    // Current playback state
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isBuffering: Boolean = false,
    
    // Playback progress
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isSeekingByUser: Boolean = false,
    val bufferedPosition: Long = 0L,
    
    // Queue management
    val currentQueue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val queueSize: Int = 0,
    
    // Playback modes
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val playbackSpeed: Float = 1.0f,
    
    // Audio settings
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val audioFocusState: AudioFocusState = AudioFocusState.NONE,
    val equalizerEnabled: Boolean = false,
    val equalizerPreset: String = "Normal",
    val equalizerBands: List<Float> = List(10) { 0f },
    
    // UI states
    val isExpanded: Boolean = true,
    val showLyrics: Boolean = false,
    val showQueue: Boolean = false,
    val showEqualizer: Boolean = false,
    val showSpeedDialog: Boolean = false,
    val showSleepTimer: Boolean = false,
    
    // Sleep timer
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerRemaining: Long = 0L,
    val sleepTimerDuration: Long = 0L,
    
    // Favorite and interaction
    val isFavorite: Boolean = false,
    val isSharing: Boolean = false,
    val canShare: Boolean = true,
    
    // Visual states
    val showVisualization: Boolean = false,
    val albumArtLoading: Boolean = false,
    val albumArtError: Boolean = false,
    
    // Network and storage
    val isOnline: Boolean = true,
    val storageSpace: Long = 0L,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    
    // History and analytics
    val playCount: Int = 0,
    val lastPlayedTime: Long = 0L,
    val sessionStartTime: Long = 0L,
    val totalSessionTime: Long = 0L
) {
    
    // Computed properties
    val progress: Float
        get() = if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
    
    val bufferedProgress: Float
        get() = if (duration > 0) (bufferedPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
    
    val hasCurrentSong: Boolean
        get() = currentSong != null
    
    val canPlay: Boolean
        get() = hasCurrentSong && !isBuffering && !isLoading
    
    val canPause: Boolean
        get() = isPlaying && !isBuffering
    
    val canSeek: Boolean
        get() = hasCurrentSong && duration > 0 && !isBuffering
    
    val isActive: Boolean
        get() = isPlaying || isPaused || isBuffering
    
    val isEmpty: Boolean
        get() = currentQueue.isEmpty() && currentSong == null
    
    val hasError: Boolean
        get() = error != null
    
    val isInQueueMode: Boolean
        get() = currentQueue.isNotEmpty()
    
    val queuePosition: String
        get() = if (isInQueueMode) "${currentIndex + 1} of $queueSize" else ""
    
    val formattedCurrentPosition: String
        get() = formatDuration(currentPosition)
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val formattedRemaining: String
        get() = formatDuration(duration - currentPosition)
    
    val formattedSleepTimer: String
        get() = formatDuration(sleepTimerRemaining)
    
    val playbackSpeedText: String
        get() = "${playbackSpeed}x"
    
    val repeatModeIcon: String
        get() = when (repeatMode) {
            RepeatMode.OFF -> "repeat_off"
            RepeatMode.ALL -> "repeat"
            RepeatMode.ONE -> "repeat_one"
        }
    
    val shuffleModeIcon: String
        get() = when (shuffleMode) {
            ShuffleMode.OFF -> "shuffle_off"
            ShuffleMode.ON -> "shuffle"
        }
    
    // Helper methods
    fun isCurrentSong(song: Song?): Boolean {
        return song != null && currentSong?.id == song.id
    }
    
    fun isInQueue(song: Song): Boolean {
        return currentQueue.any { it.id == song.id }
    }
    
    fun getQueueIndex(song: Song): Int {
        return currentQueue.indexOfFirst { it.id == song.id }
    }
    
    fun canSkipToNext(): Boolean {
        return when (repeatMode) {
            RepeatMode.ONE -> true
            RepeatMode.ALL -> true
            RepeatMode.OFF -> hasNext
        }
    }
    
    fun canSkipToPrevious(): Boolean {
        return when (repeatMode) {
            RepeatMode.ONE -> true
            RepeatMode.ALL -> true
            RepeatMode.OFF -> hasPrevious || currentPosition > 3000 // 3 seconds
        }
    }
    
    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"
        
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}

/**
 * Player dialog states
 */
enum class PlayerDialog {
    NONE,
    LYRICS,
    EQUALIZER,
    PLAYBACK_SPEED,
    SLEEP_TIMER,
    QUEUE,
    SHARE
}

/**
 * Player view modes
 */
enum class PlayerViewMode {
    FULL_SCREEN,
    MINI_PLAYER,
    NOTIFICATION_ONLY
}

/**
 * Extension functions for PlayerUiState
 */
fun PlayerUiState.copyWithLoading(isLoading: Boolean): PlayerUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun PlayerUiState.copyWithError(error: String?): PlayerUiState {
    return copy(error = error, isLoading = false)
}

fun PlayerUiState.copyWithSong(song: Song?, isPlaying: Boolean = false): PlayerUiState {
    return copy(
        currentSong = song,
        isPlaying = isPlaying,
        isPaused = !isPlaying,
        currentPosition = 0L,
        duration = song?.duration ?: 0L,
        isFavorite = false, // Will be updated by ViewModel
        playCount = song?.playCount ?: 0,
        error = null,
        isLoading = false
    )
}

fun PlayerUiState.copyWithPlaybackState(
    isPlaying: Boolean,
    isPaused: Boolean = !isPlaying,
    isBuffering: Boolean = false
): PlayerUiState {
    return copy(
        isPlaying = isPlaying,
        isPaused = isPaused,
        isBuffering = isBuffering
    )
}

fun PlayerUiState.copyWithProgress(
    currentPosition: Long,
    bufferedPosition: Long = this.bufferedPosition
): PlayerUiState {
    return copy(
        currentPosition = currentPosition,
        bufferedPosition = bufferedPosition
    )
}

fun PlayerUiState.copyWithQueue(
    queue: List<Song>,
    currentIndex: Int = 0
): PlayerUiState {
    return copy(
        currentQueue = queue,
        currentIndex = currentIndex,
        queueSize = queue.size,
        hasNext = currentIndex < queue.size - 1,
        hasPrevious = currentIndex > 0
    )
}

/**
 * Preview data for PlayerUiState
 */
object PlayerUiStatePreview {
    val loading = PlayerUiState(isLoading = true)
    
    val error = PlayerUiState(error = "Failed to load song")
    
    val empty = PlayerUiState()
    
    val playing = PlayerUiState(
        currentSong = Song(
            id = 1,
            title = "Sample Song",
            artist = "Sample Artist",
            album = "Sample Album",
            duration = 240000L, // 4 minutes
            path = "/sample/path",
            albumArtPath = null,
            playCount = 5,
            isFavorite = true,
            dateAdded = System.currentTimeMillis(),
            year = 2023,
            genre = "Pop"
        ),
        isPlaying = true,
        currentPosition = 60000L, // 1 minute
        duration = 240000L,
        repeatMode = RepeatMode.ALL,
        shuffleMode = ShuffleMode.ON,
        playbackSpeed = 1.0f,
        isFavorite = true,
        hasNext = true,
        hasPrevious = true
    )
    
    val paused = playing.copy(isPlaying = false, isPaused = true)
    
    val buffering = playing.copy(isBuffering = true)
    
    val withQueue = playing.copyWithQueue(
        queue = listOf(
            playing.currentSong!!,
            playing.currentSong!!.copy(id = 2, title = "Second Song"),
            playing.currentSong!!.copy(id = 3, title = "Third Song")
        ),
        currentIndex = 0
    )
}
