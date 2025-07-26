package com.tinhtx.localplayerapplication.presentation.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getPlayerStateUseCase: GetPlayerStateUseCase,
    private val playPauseUseCase: PlayPauseUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val setShuffleModeUseCase: SetShuffleModeUseCase,
    private val setRepeatModeUseCase: SetRepeatModeUseCase,
    private val setVolumeUseCase: SetVolumeUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val setEqualizerPresetUseCase: SetEqualizerPresetUseCase,
    private val setSleepTimerUseCase: SetSleepTimerUseCase,
    private val setPlaybackSpeedUseCase: SetPlaybackSpeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            getPlayerStateUseCase().collect { playerState ->
                _uiState.value = _uiState.value.copy(
                    currentSong = playerState.currentSong,
                    isPlaying = playerState.isPlaying,
                    progress = playerState.progress,
                    currentTimeString = formatTime(playerState.currentPosition),
                    totalTimeString = formatTime(playerState.duration),
                    shuffleMode = playerState.shuffleMode,
                    repeatMode = playerState.repeatMode,
                    volume = playerState.volume,
                    isMuted = playerState.isMuted,
                    hasNext = playerState.hasNext,
                    hasPrevious = playerState.hasPrevious,
                    playbackSpeed = playerState.playbackSpeed,
                    equalizerPreset = playerState.equalizerPreset,
                    audioData = playerState.audioData
                )
            }
        }
    }

    fun loadPlayerState() {
        // Player state is automatically loaded through observePlayerState()
        // This function can be used for additional initialization if needed
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            try {
                playPauseUseCase()
            } catch (exception) {
                handleError(exception, "Failed to toggle play/pause")
            }
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            try {
                skipToNextUseCase()
            } catch (exception) {
                handleError(exception, "Failed to skip to next")
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                skipToPreviousUseCase()
            } catch (exception) {
                handleError(exception, "Failed to skip to previous")
            }
        }
    }

    fun seekTo(position: Float) {
        viewModelScope.launch {
            try {
                val currentSong = _uiState.value.currentSong ?: return@launch
                val seekPosition = (position * currentSong.duration).toLong()
                seekToUseCase(seekPosition)
            } catch (exception) {
                handleError(exception, "Failed to seek")
            }
        }
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            try {
                val currentMode = _uiState.value.shuffleMode
                val newMode = when (currentMode) {
                    ShuffleMode.OFF -> ShuffleMode.ON
                    ShuffleMode.ON -> ShuffleMode.OFF
                }
                setShuffleModeUseCase(newMode)
            } catch (exception) {
                handleError(exception, "Failed to toggle shuffle")
            }
        }
    }

    fun toggleRepeat() {
        viewModelScope.launch {
            try {
                val currentMode = _uiState.value.repeatMode
                val newMode = when (currentMode) {
                    RepeatMode.OFF -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.OFF
                }
                setRepeatModeUseCase(newMode)
            } catch (exception) {
                handleError(exception, "Failed to toggle repeat")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val currentSong = _uiState.value.currentSong ?: return@launch
                if (currentSong.isFavorite) {
                    removeFromFavoritesUseCase(currentSong.id)
                } else {
                    addToFavoritesUseCase(currentSong.id)
                }
            } catch (exception) {
                handleError(exception, "Failed to toggle favorite")
            }
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            try {
                setVolumeUseCase(volume)
            } catch (exception) {
                handleError(exception, "Failed to set volume")
            }
        }
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            try {
                setEqualizerPresetUseCase(preset)
                _uiState.value = _uiState.value.copy(equalizerPreset = preset)
            } catch (exception) {
                handleError(exception, "Failed to set equalizer preset")
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        viewModelScope.launch {
            try {
                setSleepTimerUseCase(minutes)
                _uiState.value = _uiState.value.copy(sleepTimer = minutes)
            } catch (exception) {
                handleError(exception, "Failed to set sleep timer")
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            try {
                setPlaybackSpeedUseCase(speed)
                _uiState.value = _uiState.value.copy(playbackSpeed = speed)
            } catch (exception) {
                handleError(exception, "Failed to set playback speed")
            }
        }
    }

    fun showAddToPlaylistDialog() {
        val currentSong = _uiState.value.currentSong ?: return
        android.util.Log.d("PlayerViewModel", "Show add to playlist dialog for: ${currentSong.title}")
    }

    fun shareSong() {
        val currentSong = _uiState.value.currentSong ?: return
        android.util.Log.d("PlayerViewModel", "Share song: ${currentSong.title}")
    }

    private fun handleError(exception: Throwable, message: String) {
        android.util.Log.e("PlayerViewModel", message, exception)
        _uiState.value = _uiState.value.copy(
            error = exception.message ?: message
        )
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
