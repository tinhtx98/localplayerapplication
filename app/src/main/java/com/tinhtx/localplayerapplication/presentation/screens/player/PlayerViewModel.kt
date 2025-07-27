package com.tinhtx.localplayerapplication.presentation.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.history.UpdatePlayHistoryUseCase
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.settings.UpdateSettingsUseCase
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * ViewModel for Player Screen - Complete integration with all use cases
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    // Player Use Cases
    private val playSongUseCase: PlaySongUseCase,
    private val pauseSongUseCase: PauseSongUseCase,
    private val nextSongUseCase: NextSongUseCase,
    private val previousSongUseCase: PreviousSongUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val audioFocusUseCase: AudioFocusUseCase,
    private val sleepTimerUseCase: SleepTimerUseCase,
    
    // Favorites Use Cases
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    
    // History Use Case
    private val updatePlayHistoryUseCase: UpdatePlayHistoryUseCase,
    
    // Settings Use Cases
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    
    // Repository
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var sessionStartTime = 0L

    init {
        observeSettings()
        observeFavorites()
        restoreLastSession()
        startProgressTracking()
    }

    // ====================================================================================
    // PLAYBACK CONTROLS
    // ====================================================================================

    /**
     * Play song với complete integration
     */
    fun playSong(song: Song, queue: List<Song> = listOf(song), startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copyWithLoading(true)
                
                // Execute play use case
                playSongUseCase.execute(song).fold(
                    onSuccess = {
                        // Update UI state
                        _uiState.value = _uiState.value.copyWithSong(song, isPlaying = true)
                            .copyWithQueue(queue, startIndex)
                        
                        // Update session
                        sessionStartTime = System.currentTimeMillis()
                        _uiState.value = _uiState.value.copy(sessionStartTime = sessionStartTime)
                        
                        // Save to preferences
                        saveCurrentState()
                        
                        // Update play history
                        updatePlayHistoryUseCase.execute(song.id)
                        
                        // Request audio focus
                        requestAudioFocus()
                        
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to play song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Pause current song
     */
    fun pauseSong() {
        viewModelScope.launch {
            try {
                pauseSongUseCase.execute().fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copyWithPlaybackState(
                            isPlaying = false,
                            isPaused = true
                        )
                        saveCurrentState()
                        abandonAudioFocus()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to pause: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Resume playback
     */
    fun resumePlayback() {
        viewModelScope.launch {
            try {
                val currentSong = _uiState.value.currentSong ?: return@launch
                
                playSongUseCase.execute(currentSong).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copyWithPlaybackState(
                            isPlaying = true,
                            isPaused = false
                        )
                        saveCurrentState()
                        requestAudioFocus()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to resume: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pauseSong()
        } else {
            resumePlayback()
        }
    }

    /**
     * Skip to next song
     */
    fun skipToNext() {
        viewModelScope.launch {
            try {
                nextSongUseCase.execute().fold(
                    onSuccess = { nextSong ->
                        nextSong?.let { song ->
                            val currentState = _uiState.value
                            val newIndex = (currentState.currentIndex + 1) % currentState.queueSize
                            
                            _uiState.value = _uiState.value.copyWithSong(song, isPlaying = true)
                                .copy(currentIndex = newIndex)
                                .copy(
                                    hasNext = newIndex < currentState.queueSize - 1,
                                    hasPrevious = newIndex > 0
                                )
                            
                            saveCurrentState()
                            updatePlayHistoryUseCase.execute(song.id)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to skip: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Skip to previous song
     */
    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                // If more than 3 seconds played, restart current song
                if (_uiState.value.currentPosition > 3000) {
                    seekToPosition(0L)
                    return@launch
                }
                
                previousSongUseCase.execute().fold(
                    onSuccess = { previousSong ->
                        previousSong?.let { song ->
                            val currentState = _uiState.value
                            val newIndex = (currentState.currentIndex - 1).coerceAtLeast(0)
                            
                            _uiState.value = _uiState.value.copyWithSong(song, isPlaying = true)
                                .copy(currentIndex = newIndex)
                                .copy(
                                    hasNext = newIndex < currentState.queueSize - 1,
                                    hasPrevious = newIndex > 0
                                )
                            
                            saveCurrentState()
                            updatePlayHistoryUseCase.execute(song.id)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to go back: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Seek to specific position
     */
    fun seekToPosition(position: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSeekingByUser = true)
                
                seekToPositionUseCase.execute(position).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copyWithProgress(position)
                            .copy(isSeekingByUser = false)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isSeekingByUser = false)
                            .copyWithError("Failed to seek: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSeekingByUser = false)
                    .copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Seek by percentage (0.0 to 1.0)
     */
    fun seekByPercentage(percentage: Float) {
        val duration = _uiState.value.duration
        if (duration > 0) {
            val position = (duration * percentage.coerceIn(0f, 1f)).toLong()
            seekToPosition(position)
        }
    }

    // ====================================================================================
    // PLAYBACK MODES
    // ====================================================================================

    /**
     * Toggle repeat mode
     */
    fun toggleRepeatMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.repeatMode
            val nextMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            
            updateSettingsUseCase.updateRepeatMode(nextMode).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(repeatMode = nextMode)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copyWithError("Failed to update repeat mode: ${error.message}")
                }
            )
        }
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffleMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.shuffleMode
            val nextMode = when (currentMode) {
                ShuffleMode.OFF -> ShuffleMode.ON
                ShuffleMode.ON -> ShuffleMode.OFF
            }
            
            updateSettingsUseCase.updateShuffleMode(nextMode).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(shuffleMode = nextMode)
                    // TODO: Implement queue shuffle logic
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copyWithError("Failed to update shuffle mode: ${error.message}")
                }
            )
        }
    }

    /**
     * Update playback speed
     */
    fun updatePlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            val validSpeed = speed.coerceIn(0.25f, 4.0f)
            
            updateSettingsUseCase.updatePlaybackSpeed(validSpeed).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(playbackSpeed = validSpeed)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copyWithError("Failed to update speed: ${error.message}")
                }
            )
        }
    }

    // ====================================================================================
    // FAVORITES MANAGEMENT
    // ====================================================================================

    /**
     * Toggle favorite status
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentSong = _uiState.value.currentSong ?: return@launch
            val isFavorite = _uiState.value.isFavorite
            
            try {
                if (isFavorite) {
                    removeFromFavoritesUseCase.execute(currentSong.id).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isFavorite = false)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copyWithError("Failed to remove from favorites: ${error.message}")
                        }
                    )
                } else {
                    addToFavoritesUseCase.execute(currentSong.id).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isFavorite = true)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copyWithError("Failed to add to favorites: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // SLEEP TIMER
    // ====================================================================================

    /**
     * Start sleep timer
     */
    fun startSleepTimer(durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val durationMs = durationMinutes * 60 * 1000L
                
                sleepTimerUseCase.startTimer(durationMs).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            sleepTimerEnabled = true,
                            sleepTimerDuration = durationMs,
                            sleepTimerRemaining = durationMs
                        )
                        
                        startSleepTimerCountdown()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to start sleep timer: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Cancel sleep timer
     */
    fun cancelSleepTimer() {
        viewModelScope.launch {
            try {
                sleepTimerUseCase.cancelTimer().fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            sleepTimerEnabled = false,
                            sleepTimerRemaining = 0L,
                            sleepTimerDuration = 0L
                        )
                        
                        sleepTimerJob?.cancel()
                        sleepTimerJob = null
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to cancel sleep timer: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // UI STATE MANAGEMENT
    // ====================================================================================

    /**
     * Show/hide dialogs
     */
    fun showLyrics(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLyrics = show)
    }

    fun showEqualizer(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEqualizer = show)
    }

    fun showSpeedDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSpeedDialog = show)
    }

    fun showSleepTimerDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSleepTimer = show)
    }

    fun showQueue(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQueue = show)
    }

    fun toggleVisualization() {
        _uiState.value = _uiState.value.copy(showVisualization = !_uiState.value.showVisualization)
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ====================================================================================
    // QUEUE MANAGEMENT
    // ====================================================================================

    /**
     * Jump to specific song in queue
     */
    fun jumpToQueueIndex(index: Int) {
        val currentState = _uiState.value
        if (index >= 0 && index < currentState.queueSize) {
            val song = currentState.currentQueue[index]
            playSong(song, currentState.currentQueue, index)
        }
    }

    /**
     * Remove song from queue
     */
    fun removeFromQueue(index: Int) {
        val currentState = _uiState.value
        if (index >= 0 && index < currentState.queueSize) {
            val newQueue = currentState.currentQueue.toMutableList()
            newQueue.removeAt(index)
            
            val newIndex = if (index < currentState.currentIndex) {
                currentState.currentIndex - 1
            } else {
                currentState.currentIndex
            }
            
            _uiState.value = _uiState.value.copyWithQueue(newQueue, newIndex.coerceAtMost(newQueue.size - 1))
            saveCurrentState()
        }
    }

    /**
     * Add song to queue
     */
    fun addToQueue(song: Song) {
        val currentState = _uiState.value
        val newQueue = currentState.currentQueue + song
        
        _uiState.value = _uiState.value.copyWithQueue(newQueue, currentState.currentIndex)
        saveCurrentState()
    }

    // ====================================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================================

    private fun startProgressTracking() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                
                if (_uiState.value.isPlaying && !_uiState.value.isSeekingByUser) {
                    val currentPosition = _uiState.value.currentPosition + 1000
                    val duration = _uiState.value.duration
                    
                    if (currentPosition >= duration && duration > 0) {
                        // Song completed
                        onSongCompleted()
                    } else {
                        _uiState.value = _uiState.value.copyWithProgress(currentPosition)
                    }
                }
            }
        }
    }

    private fun startSleepTimerCountdown() {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            while (_uiState.value.sleepTimerEnabled && _uiState.value.sleepTimerRemaining > 0) {
                delay(1000)
                val remaining = _uiState.value.sleepTimerRemaining - 1000
                
                if (remaining <= 0) {
                    // Sleep timer expired
                    pauseSong()
                    _uiState.value = _uiState.value.copy(
                        sleepTimerEnabled = false,
                        sleepTimerRemaining = 0L
                    )
                } else {
                    _uiState.value = _uiState.value.copy(sleepTimerRemaining = remaining)
                }
            }
        }
    }

    private fun onSongCompleted() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            when (currentState.repeatMode) {
                RepeatMode.ONE -> {
                    // Restart current song
                    seekToPosition(0L)
                }
                RepeatMode.ALL -> {
                    if (currentState.hasNext) {
                        skipToNext()
                    } else {
                        // Restart queue from beginning
                        jumpToQueueIndex(0)
                    }
                }
                RepeatMode.OFF -> {
                    if (currentState.hasNext) {
                        skipToNext()
                    } else {
                        // Stop playback
                        pauseSong()
                    }
                }
            }
        }
    }

    private fun requestAudioFocus() {
        viewModelScope.launch {
            audioFocusUseCase.requestFocus().fold(
                onSuccess = { focusState ->
                    _uiState.value = _uiState.value.copy(audioFocusState = focusState)
                },
                onFailure = {
                    // Handle audio focus failure
                    _uiState.value = _uiState.value.copy(audioFocusState = AudioFocusState.LOST)
                }
            )
        }
    }

    private fun abandonAudioFocus() {
        viewModelScope.launch {
            audioFocusUseCase.abandonFocus()
            _uiState.value = _uiState.value.copy(audioFocusState = AudioFocusState.NONE)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase.getAppSettings()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Failed to load settings: ${e.message}")
                }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        repeatMode = settings.repeatMode,
                        shuffleMode = settings.shuffleMode,
                        playbackSpeed = settings.playbackSpeed,
                        equalizerEnabled = settings.equalizerEnabled,
                        equalizerPreset = settings.equalizerPreset,
                        equalizerBands = settings.equalizerBands,
                        volume = 1.0f // TODO: Get from audio manager
                    )
                }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase.getFavoriteSongs()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Failed to load favorites: ${e.message}")
                }
                .collect { favorites ->
                    val currentSong = _uiState.value.currentSong
                    if (currentSong != null) {
                        val isFavorite = favorites.any { it.id == currentSong.id }
                        _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
                    }
                }
        }
    }

    private fun restoreLastSession() {
        viewModelScope.launch {
            try {
                // TODO: Implement session restoration from UserPreferencesRepository
                // val lastQueue = userPreferencesRepository.getLastQueue().first()
                // val lastSong = userPreferencesRepository.getLastPlayedSong().first()
                // Restore last playing state
            } catch (e: Exception) {
                // Silent failure for session restoration
            }
        }
    }

    private fun saveCurrentState() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                // Save current queue
                if (currentState.isInQueueMode) {
                    val songIds = currentState.currentQueue.map { it.id }
                    userPreferencesRepository.saveLastQueue(songIds, currentState.currentIndex)
                }
                
                // Save current song
                currentState.currentSong?.let { song ->
                    userPreferencesRepository.saveLastPlayedSong(song.id, currentState.currentPosition)
                }
                
            } catch (e: Exception) {
                // Silent failure for state saving
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressUpdateJob?.cancel()
        sleepTimerJob?.cancel()
        
        // Save final session data
        viewModelScope.launch {
            if (sessionStartTime > 0) {
                val sessionDuration = System.currentTimeMillis() - sessionStartTime
                // TODO: Save session statistics
            }
        }
    }
}
