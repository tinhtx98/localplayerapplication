package com.tinhtx.localplayerapplication.presentation.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.playlist.CreatePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val getQueueUseCase: GetQueueUseCase,
    private val skipToQueueItemUseCase: SkipToQueueItemUseCase,
    private val removeFromQueueUseCase: RemoveFromQueueUseCase,
    private val moveQueueItemUseCase: MoveQueueItemUseCase,
    private val clearQueueUseCase: ClearQueueUseCase,
    private val shuffleQueueUseCase: ShuffleQueueUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val getPlayerStateUseCase: GetPlayerStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _scrollToCurrentSong = MutableSharedFlow<Unit>()
    val scrollToCurrentSong = _scrollToCurrentSong.asSharedFlow()

    init {
        observeQueue()
        observePlayerState()
        observeSearch()
    }

    private fun observeQueue() {
        viewModelScope.launch {
            getQueueUseCase().catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load queue"
                )
            }.collect { queueData ->
                val totalDuration = calculateTotalDuration(queueData.queue)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    queue = queueData.queue,
                    currentSongIndex = queueData.currentIndex,
                    totalDuration = totalDuration
                )
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            getPlayerStateUseCase().collect { playerState ->
                _uiState.value = _uiState.value.copy(
                    currentSong = playerState.currentSong,
                    isPlaying = playerState.isPlaying,
                    repeatMode = playerState.repeatMode,
                    shuffleMode = playerState.shuffleMode
                )
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _uiState.map { it.queue }
            ) { query, queue ->
                if (query.isBlank()) {
                    emptyList()
                } else {
                    queue.filter { song ->
                        song.title.contains(query, ignoreCase = true) ||
                        song.displayArtist.contains(query, ignoreCase = true) ||
                        song.displayAlbum.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredQueue ->
                _uiState.value = _uiState.value.copy(
                    filteredQueue = filteredQueue,
                    searchQuery = _searchQuery.value
                )
            }
        }
    }

    fun loadQueue() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Queue data will be loaded through observeQueue()
    }

    fun retryLoadQueue() {
        loadQueue()
    }

    fun skipToSong(index: Int) {
        viewModelScope.launch {
            try {
                skipToQueueItemUseCase(index)
            } catch (exception) {
                handleError(exception, "Failed to skip to song")
            }
        }
    }

    fun removeFromQueue(index: Int) {
        viewModelScope.launch {
            try {
                removeFromQueueUseCase(index)
            } catch (exception) {
                handleError(exception, "Failed to remove song from queue")
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                moveQueueItemUseCase(fromIndex, toIndex)
            } catch (exception) {
                handleError(exception, "Failed to move queue item")
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            try {
                clearQueueUseCase()
            } catch (exception) {
                handleError(exception, "Failed to clear queue")
            }
        }
    }

    fun shuffleQueue() {
        viewModelScope.launch {
            try {
                shuffleQueueUseCase()
            } catch (exception) {
                handleError(exception, "Failed to shuffle queue")
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                if (song.isFavorite) {
                    removeFromFavoritesUseCase(song.id)
                } else {
                    addToFavoritesUseCase(song.id)
                }
            } catch (exception) {
                handleError(exception, "Failed to toggle favorite")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun scrollToCurrentSong() {
        viewModelScope.launch {
            _scrollToCurrentSong.emit(Unit)
        }
    }

    fun saveQueueAsPlaylist() {
        viewModelScope.launch {
            try {
                val queue = _uiState.value.queue
                if (queue.isNotEmpty()) {
                    val playlistName = "Queue - ${formatCurrentDate()}"
                    createPlaylistUseCase(
                        name = playlistName,
                        description = "Created from queue with ${queue.size} songs",
                        songIds = queue.map { it.id }
                    )
                    
                    android.util.Log.d("QueueViewModel", "Queue saved as playlist: $playlistName")
                }
            } catch (exception) {
                handleError(exception, "Failed to save queue as playlist")
            }
        }
    }

    fun shareQueue() {
        val queue = _uiState.value.queue
        val currentSong = _uiState.value.currentSong
        
        android.util.Log.d("QueueViewModel", "Sharing queue with ${queue.size} songs, current: ${currentSong?.title}")
    }

    private fun calculateTotalDuration(songs: List<Song>): String {
        val totalMs = songs.sumOf { it.duration }
        val hours = totalMs / (1000 * 60 * 60)
        val minutes = (totalMs % (1000 * 60 * 60)) / (1000 * 60)

        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    private fun formatCurrentDate(): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private fun handleError(exception: Throwable, message: String) {
        android.util.Log.e("QueueViewModel", message, exception)
        _uiState.value = _uiState.value.copy(
            error = exception.message ?: message
        )
    }
}

data class QueueData(
    val queue: List<Song>,
    val currentIndex: Int
)
