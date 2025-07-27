package com.tinhtx.localplayerapplication.presentation.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.queue.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

/**
 * ViewModel for Queue Screen - Complete integration with queue/playback use cases
 */
@HiltViewModel
class QueueViewModel @Inject constructor(
    // Queue Use Cases
    private val getQueueUseCase: GetQueueUseCase,
    private val addToQueueUseCase: AddToQueueUseCase,
    private val removeFromQueueUseCase: RemoveFromQueueUseCase,
    private val reorderQueueUseCase: ReorderQueueUseCase,
    private val clearQueueUseCase: ClearQueueUseCase,
    private val shuffleQueueUseCase: ShuffleQueueUseCase,
    private val saveQueueUseCase: SaveQueueUseCase,
    
    // Player Use Cases
    private val getPlaybackStateUseCase: GetPlaybackStateUseCase,
    private val playPauseUseCase: PlayPauseUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val setRepeatModeUseCase: SetRepeatModeUseCase,
    private val setShuffleModeUseCase: SetShuffleModeUseCase,
    private val playFromQueueUseCase: PlayFromQueueUseCase,
    
    // Music Use Cases
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    
    // Favorites Use Cases
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    
    // Playlist Use Cases
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var undoJob: Job? = null
    private val operationHistory = mutableListOf<QueueOperation>()
    private val redoHistory = mutableListOf<QueueOperation>()

    init {
        loadQueue()
        observePlaybackState()
        observeFavorites()
    }

    // =================================================================================
    // INITIALIZATION & DATA LOADING
    // =================================================================================

    /**
     * Load current queue
     */
    private fun loadQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                getQueueUseCase.execute().fold(
                    onSuccess = { queueData ->
                        _uiState.value = _uiState.value.copyWithQueue(
                            songs = queueData.songs,
                            currentIndex = queueData.currentIndex,
                            source = queueData.source
                        )
                        
                        // Update other queue properties
                        _uiState.value = _uiState.value.copy(
                            originalQueue = queueData.originalQueue,
                            queueHistory = queueData.history,
                            isShuffleEnabled = queueData.isShuffleEnabled,
                            repeatMode = queueData.repeatMode,
                            queueName = queueData.name,
                            queueDescription = queueData.description
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to load queue: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Queue error: ${e.message}")
            }
        }
    }

    /**
     * Observe playback state changes
     */
    private fun observePlaybackState() {
        viewModelScope.launch {
            getPlaybackStateUseCase.execute()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Playback error: ${e.message}")
                }
                .collect { playbackState ->
                    _uiState.value = _uiState.value.copyWithPlaybackState(
                        isPlaying = playbackState.isPlaying,
                        currentSong = playbackState.currentSong,
                        currentIndex = playbackState.currentIndex,
                        progress = playbackState.progress,
                        currentPosition = playbackState.position
                    ).copy(
                        isBuffering = playbackState.isBuffering,
                        repeatMode = playbackState.repeatMode,
                        isShuffleEnabled = playbackState.isShuffleEnabled
                    )
                }
        }
    }

    /**
     * Observe favorites changes
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase.getFavoriteSongs()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Failed to load favorites: ${e.message}")
                }
                .collect { favorites ->
                    val favoriteIds = favorites.map { it.id }.toSet()
                    _uiState.value = _uiState.value.copy(favoriteSongs = favoriteIds)
                }
        }
    }

    // =================================================================================
    // PLAYBACK CONTROLS
    // =================================================================================

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        viewModelScope.launch {
            try {
                playPauseUseCase.execute().fold(
                    onSuccess = { /* State will be updated via observePlaybackState */ },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Playback failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Playback error: ${e.message}")
            }
        }
    }

    /**
     * Skip to next song
     */
    fun skipToNext() {
        viewModelScope.launch {
            try {
                skipToNextUseCase.execute().fold(
                    onSuccess = { /* State will be updated via observePlaybackState */ },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Skip failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Skip error: ${e.message}")
            }
        }
    }

    /**
     * Skip to previous song
     */
    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                skipToPreviousUseCase.execute().fold(
                    onSuccess = { /* State will be updated via observePlaybackState */ },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Skip failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Skip error: ${e.message}")
            }
        }
    }

    /**
     * Seek to position
     */
    fun seekToPosition(position: Long) {
        viewModelScope.launch {
            try {
                seekToPositionUseCase.execute(position).fold(
                    onSuccess = { /* State will be updated via observePlaybackState */ },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Seek failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Seek error: ${e.message}")
            }
        }
    }

    /**
     * Play specific song from queue
     */
    fun playSongAtIndex(index: Int) {
        if (index < 0 || index >= _uiState.value.queueSongs.size) return
        
        viewModelScope.launch {
            try {
                playFromQueueUseCase.execute(index).fold(
                    onSuccess = { /* State will be updated via observePlaybackState */ },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to play song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Play error: ${e.message}")
            }
        }
    }

    /**
     * Play specific song
     */
    fun playSong(song: Song) {
        val index = _uiState.value.getSongIndex(song)
        if (index >= 0) {
            playSongAtIndex(index)
        }
    }

    // =================================================================================
    // REPEAT & SHUFFLE CONTROLS
    // =================================================================================

    /**
     * Toggle repeat mode
     */
    fun toggleRepeatMode() {
        val currentMode = _uiState.value.repeatMode
        val nextMode = currentMode.getNextMode()
        
        viewModelScope.launch {
            try {
                setRepeatModeUseCase.execute(nextMode).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to set repeat mode: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Repeat mode error: ${e.message}")
            }
        }
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffleMode() {
        val newShuffleState = !_uiState.value.isShuffleEnabled
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isShuffling = true)
                
                setShuffleModeUseCase.execute(newShuffleState).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isShuffleEnabled = newShuffleState,
                            isShuffling = false
                        )
                        
                        // If enabling shuffle, save original order
                        if (newShuffleState && _uiState.value.originalQueue.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                originalQueue = _uiState.value.queueSongs
                            )
                        }
                        
                        // Add to operation history
                        if (newShuffleState) {
                            addOperation(QueueOperation.ShuffleQueue(_uiState.value.queueSongs))
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isShuffling = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to shuffle: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isShuffling = false)
                _uiState.value = _uiState.value.copyWithError("Shuffle error: ${e.message}")
            }
        }
    }

    // =================================================================================
    // QUEUE MANAGEMENT
    // =================================================================================

    /**
     * Add song to queue
     */
    fun addSongToQueue(song: Song, index: Int = -1) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAddingSongs = true)
                
                val targetIndex = if (index < 0) _uiState.value.queueSongs.size else index
                
                addToQueueUseCase.execute(song, targetIndex).fold(
                    onSuccess = {
                        val updatedQueue = _uiState.value.queueSongs.toMutableList()
                        updatedQueue.add(targetIndex, song)
                        
                        _uiState.value = _uiState.value.copy(
                            queueSongs = updatedQueue,
                            queueStats = calculateQueueStats(updatedQueue),
                            isAddingSongs = false
                        )
                        
                        // Add to operation history
                        addOperation(QueueOperation.AddSong(song, targetIndex))
                        
                        // Show success message
                        showUndoSnackbar("Added \"${song.title}\" to queue")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isAddingSongs = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to add song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAddingSongs = false)
                _uiState.value = _uiState.value.copyWithError("Add song error: ${e.message}")
            }
        }
    }

    /**
     * Add multiple songs to queue
     */
    fun addSongsToQueue(songs: List<Song>, index: Int = -1) {
        if (songs.isEmpty()) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAddingSongs = true)
                
                val targetIndex = if (index < 0) _uiState.value.queueSongs.size else index
                
                songs.forEachIndexed { offset, song ->
                    addToQueueUseCase.execute(song, targetIndex + offset)
                }
                
                val updatedQueue = _uiState.value.queueSongs.toMutableList()
                updatedQueue.addAll(targetIndex, songs)
                
                _uiState.value = _uiState.value.copy(
                    queueSongs = updatedQueue,
                    queueStats = calculateQueueStats(updatedQueue),
                    isAddingSongs = false
                )
                
                // Add to operation history
                addOperation(QueueOperation.AddMultipleSongs(songs, targetIndex))
                
                // Show success message
                showUndoSnackbar("Added ${songs.size} songs to queue")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAddingSongs = false)
                _uiState.value = _uiState.value.copyWithError("Add songs error: ${e.message}")
            }
        }
    }

    /**
     * Remove song from queue
     */
    fun removeSongFromQueue(song: Song) {
        val index = _uiState.value.getSongIndex(song)
        if (index < 0) return
        
        removeSongAtIndex(index)
    }

    /**
     * Remove song at specific index
     */
    fun removeSongAtIndex(index: Int) {
        if (index < 0 || index >= _uiState.value.queueSongs.size) return
        
        viewModelScope.launch {
            try {
                val song = _uiState.value.queueSongs[index]
                
                removeFromQueueUseCase.execute(index).fold(
                    onSuccess = {
                        val updatedQueue = _uiState.value.queueSongs.toMutableList()
                        updatedQueue.removeAt(index)
                        
                        // Adjust current index if needed
                        val newCurrentIndex = when {
                            index < _uiState.value.currentIndex -> _uiState.value.currentIndex - 1
                            index == _uiState.value.currentIndex -> {
                                // If removing current song, determine next song
                                if (updatedQueue.isEmpty()) -1
                                else if (index >= updatedQueue.size) updatedQueue.size - 1
                                else index
                            }
                            else -> _uiState.value.currentIndex
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            queueSongs = updatedQueue,
                            currentIndex = newCurrentIndex,
                            queueStats = calculateQueueStats(updatedQueue)
                        )
                        
                        // Add to operation history
                        addOperation(QueueOperation.RemoveSong(song, index))
                        
                        // Show success message
                        showUndoSnackbar("Removed \"${song.title}\" from queue")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to remove song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Remove song error: ${e.message}")
            }
        }
    }

    /**
     * Remove multiple selected songs
     */
    fun removeSelectedSongs() {
        val selectedSongs = _uiState.value.selectedSongs
        if (selectedSongs.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val songsToRemove = _uiState.value.queueSongs
                    .mapIndexedNotNull { index, song ->
                        if (selectedSongs.contains(song.id)) song to index else null
                    }
                    .sortedByDescending { it.second } // Remove from end to maintain indices
                
                songsToRemove.forEach { (_, index) ->
                    removeFromQueueUseCase.execute(index)
                }
                
                val updatedQueue = _uiState.value.queueSongs.toMutableList()
                songsToRemove.forEach { (song, _) ->
                    updatedQueue.remove(song)
                }
                
                _uiState.value = _uiState.value.copy(
                    queueSongs = updatedQueue,
                    queueStats = calculateQueueStats(updatedQueue),
                    isSelectionMode = false,
                    selectedSongs = emptySet()
                )
                
                // Add to operation history
                addOperation(QueueOperation.RemoveMultipleSongs(songsToRemove))
                
                // Show success message
                showUndoSnackbar("Removed ${songsToRemove.size} songs from queue")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Remove songs error: ${e.message}")
            }
        }
    }

    /**
     * Reorder song in queue
     */
    fun reorderSong(fromIndex: Int, toIndex: Int) {
        if (!_uiState.value.canMoveSong(fromIndex, toIndex)) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isReordering = true,
                    isDragging = false,
                    draggedSongIndex = null,
                    dropTargetIndex = null
                )
                
                reorderQueueUseCase.execute(fromIndex, toIndex).fold(
                    onSuccess = {
                        val updatedQueue = _uiState.value.queueSongs.toMutableList()
                        val song = updatedQueue.removeAt(fromIndex)
                        updatedQueue.add(toIndex, song)
                        
                        // Adjust current index if needed
                        val newCurrentIndex = when {
                            _uiState.value.currentIndex == fromIndex -> toIndex
                            fromIndex < _uiState.value.currentIndex && toIndex >= _uiState.value.currentIndex -> 
                                _uiState.value.currentIndex - 1
                            fromIndex > _uiState.value.currentIndex && toIndex <= _uiState.value.currentIndex -> 
                                _uiState.value.currentIndex + 1
                            else -> _uiState.value.currentIndex
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            queueSongs = updatedQueue,
                            currentIndex = newCurrentIndex,
                            isReordering = false
                        )
                        
                        // Add to operation history
                        addOperation(QueueOperation.MoveSong(fromIndex, toIndex))
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isReordering = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to reorder: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isReordering = false)
                _uiState.value = _uiState.value.copyWithError("Reorder error: ${e.message}")
            }
        }
    }

    /**
     * Clear entire queue
     */
    fun clearQueue() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isClearingQueue = true)
                
                clearQueueUseCase.execute().fold(
                    onSuccess = {
                        val oldQueue = _uiState.value.queueSongs
                        
                        _uiState.value = _uiState.value.copy(
                            queueSongs = emptyList(),
                            currentSong = null,
                            currentIndex = -1,
                            queueStats = QueueStats(),
                            isClearingQueue = false,
                            showClearQueueDialog = false,
                            isPlaying = false,
                            isPaused = false
                        )
                        
                        // Add to operation history
                        addOperation(QueueOperation.ClearQueue)
                        
                        // Show success message
                        showUndoSnackbar("Cleared queue")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isClearingQueue = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to clear queue: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isClearingQueue = false)
                _uiState.value = _uiState.value.copyWithError("Clear queue error: ${e.message}")
            }
        }
    }

    // =================================================================================
    // SEARCH IN QUEUE
    // =================================================================================

    /**
     * Search songs in queue
     */
    fun searchInQueue(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearchActive = query.isNotBlank()
        )
        
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(filteredSongs = emptyList())
            return
        }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                delay(300) // Debounce search
                
                val filteredSongs = _uiState.value.queueSongs.filter { song ->
                    song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true) ||
                    song.album.contains(query, ignoreCase = true) ||
                    song.genre.contains(query, ignoreCase = true)
                }
                
                _uiState.value = _uiState.value.copy(filteredSongs = filteredSongs)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Search error: ${e.message}")
            }
        }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearchActive = false,
            filteredSongs = emptyList()
        )
    }

    // =================================================================================
    // FAVORITES MANAGEMENT
    // =================================================================================

    /**
     * Toggle song favorite status
     */
    fun toggleSongFavorite(song: Song) {
        viewModelScope.launch {
            try {
                val isFavorite = _uiState.value.isSongFavorite(song)
                
                if (isFavorite) {
                    removeFromFavoritesUseCase.execute(song.id)
                } else {
                    addToFavoritesUseCase.execute(song.id)
                }
                
                // Favorites will be updated via observeFavorites()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update favorites: ${e.message}")
            }
        }
    }

    // =================================================================================
    // QUEUE PERSISTENCE
    // =================================================================================

    /**
     * Save queue as playlist
     */
    fun saveQueueAsPlaylist(name: String) {
        if (name.isBlank() || _uiState.value.queueSongs.isEmpty()) return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSavingQueue = true)
                
                val playlist = Playlist(
                    id = 0, // Will be generated
                    name = name.trim(),
                    description = "Created from queue • ${_uiState.value.queueSize} songs",
                    songCount = _uiState.value.queueSize,
                    totalDuration = _uiState.value.totalDuration,
                    createdAt = System.currentTimeMillis()
                )
                
                createPlaylistUseCase.execute(playlist, _uiState.value.queueSongs).fold(
                    onSuccess = { createdPlaylist ->
                        _uiState.value = _uiState.value.copy(
                            isSavingQueue = false,
                            showSaveQueueDialog = false,
                            newPlaylistName = "",
                            lastSavedTime = System.currentTimeMillis()
                        )
                        
                        // Show success message
                        showMessage("Queue saved as \"${createdPlaylist.name}\"")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isSavingQueue = false)
                        _uiState.value = _uiState.value.copyWithError("Failed to save queue: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSavingQueue = false)
                _uiState.value = _uiState.value.copyWithError("Save queue error: ${e.message}")
            }
        }
    }

    // =================================================================================
    // SELECTION MODE
    // =================================================================================

    /**
     * Toggle selection mode
     */
    fun toggleSelectionMode() {
        val isSelectionMode = !_uiState.value.isSelectionMode
        _uiState.value = _uiState.value.copyWithSelection(
            isSelectionMode = isSelectionMode,
            selectedSongs = if (isSelectionMode) _uiState.value.selectedSongs else emptySet()
        )
    }

    /**
     * Toggle song selection
     */
    fun toggleSongSelection(songId: Long) {
        if (!_uiState.value.isSelectionMode) {
            // Enter selection mode and select the song
            _uiState.value = _uiState.value.copyWithSelection(
                isSelectionMode = true,
                selectedSongs = setOf(songId)
            )
            return
        }
        
        val currentSelection = _uiState.value.selectedSongs.toMutableSet()
        
        if (currentSelection.contains(songId)) {
            currentSelection.remove(songId)
        } else {
            currentSelection.add(songId)
        }
        
        _uiState.value = _uiState.value.copy(selectedSongs = currentSelection)
        
        // Exit selection mode if no songs selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false)
        }
    }

    /**
     * Select all songs
     */
    fun selectAllSongs() {
        val allSongIds = _uiState.value.displayedSongs.map { it.id }.toSet()
        _uiState.value = _uiState.value.copyWithSelection(
            isSelectionMode = true,
            selectedSongs = allSongIds
        )
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copyWithSelection(false)
    }

    // =================================================================================
    // DRAG & DROP
    // =================================================================================

    /**
     * Start dragging song
     */
    fun startDragging(songIndex: Int) {
        _uiState.value = _uiState.value.copy(
            isDragging = true,
            draggedSongIndex = songIndex,
            dropTargetIndex = null
        )
    }

    /**
     * Update drop target while dragging
     */
    fun updateDropTarget(targetIndex: Int?) {
        _uiState.value = _uiState.value.copy(dropTargetIndex = targetIndex)
    }

    /**
     * End dragging and perform reorder if needed
     */
    fun endDragging() {
        val draggedIndex = _uiState.value.draggedSongIndex
        val dropIndex = _uiState.value.dropTargetIndex
        
        _uiState.value = _uiState.value.copy(
            isDragging = false,
            draggedSongIndex = null,
            dropTargetIndex = null
        )
        
        if (draggedIndex != null && dropIndex != null && draggedIndex != dropIndex) {
            reorderSong(draggedIndex, dropIndex)
        }
    }

    // =================================================================================
    // ADD SONGS TO QUEUE
    // =================================================================================

    /**
     * Load available songs for adding to queue
     */
    fun loadAvailableSongs() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(availableSongsLoading = true)
                
                getAllSongsUseCase.getAllSongs().first().let { allSongs ->
                    // Filter out songs already in queue
                    val queueSongIds = _uiState.value.queueSongs.map { it.id }.toSet()
                    val availableSongs = allSongs.filter { !queueSongIds.contains(it.id) }
                    
                    _uiState.value = _uiState.value.copy(
                        availableSongs = availableSongs,
                        availableSongsLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    availableSongsLoading = false,
                    error = "Failed to load songs: ${e.message}"
                )
            }
        }
    }

    /**
     * Search available songs
     */
    fun searchAvailableSongs(query: String) {
        _uiState.value = _uiState.value.copy(addSongsSearchQuery = query)
        
        if (query.isBlank()) return
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                delay(300) // Debounce search
                
                searchSongsUseCase.execute(query).fold(
                    onSuccess = { searchResults ->
                        // Filter out songs already in queue
                        val queueSongIds = _uiState.value.queueSongs.map { it.id }.toSet()
                        val filteredResults = searchResults.filter { !queueSongIds.contains(it.id) }
                        
                        _uiState.value = _uiState.value.copy(availableSongs = filteredResults)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Search failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Search error: ${e.message}")
            }
        }
    }

    /**
     * Toggle song selection for adding
     */
    fun toggleSongSelectionForAdding(songId: Long) {
        val currentSelection = _uiState.value.selectedSongsToAdd.toMutableSet()
        
        if (currentSelection.contains(songId)) {
            currentSelection.remove(songId)
        } else {
            currentSelection.add(songId)
        }
        
        _uiState.value = _uiState.value.copy(selectedSongsToAdd = currentSelection)
    }

    /**
     * Add selected songs to queue
     */
    fun addSelectedSongsToQueue() {
        val selectedIds = _uiState.value.selectedSongsToAdd
        if (selectedIds.isEmpty()) return
        
        val songsToAdd = _uiState.value.availableSongs.filter { selectedIds.contains(it.id) }
        
        addSongsToQueue(songsToAdd)
        
        // Clear selection and close bottom sheet
        _uiState.value = _uiState.value.copy(
            selectedSongsToAdd = emptySet(),
            showAddSongsBottomSheet = false
        )
    }

    // =================================================================================
    // UNDO/REDO OPERATIONS
    // =================================================================================

    private fun addOperation(operation: QueueOperation) {
        operationHistory.add(operation)
        redoHistory.clear() // Clear redo history when new operation is added
        
        // Limit history size
        if (operationHistory.size > 20) {
            operationHistory.removeAt(0)
        }
        
        _uiState.value = _uiState.value.copy(
            canUndo = operationHistory.isNotEmpty(),
            canRedo = redoHistory.isNotEmpty()
        )
    }

    /**
     * Undo last operation
     */
    fun undoLastOperation() {
        if (operationHistory.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val lastOperation = operationHistory.removeLastOrNull() ?: return@launch
                
                when (lastOperation) {
                    is QueueOperation.AddSong -> {
                        removeSongAtIndex(lastOperation.index)
                    }
                    is QueueOperation.RemoveSong -> {
                        addSongToQueue(lastOperation.song, lastOperation.index)
                    }
                    is QueueOperation.MoveSong -> {
                        reorderSong(lastOperation.toIndex, lastOperation.fromIndex)
                    }
                    is QueueOperation.AddMultipleSongs -> {
                        // Remove songs in reverse order
                        for (i in lastOperation.songs.indices.reversed()) {
                            removeSongAtIndex(lastOperation.startIndex + i)
                        }
                    }
                    is QueueOperation.RemoveMultipleSongs -> {
                        // Add songs back in original order
                        lastOperation.songs.sortedBy { it.second }.forEach { (song, index) ->
                            addSongToQueue(song, index)
                        }
                    }
                    is QueueOperation.ClearQueue -> {
                        // TODO: Restore previous queue
                    }
                    is QueueOperation.ShuffleQueue -> {
                        // Restore original order
                        _uiState.value = _uiState.value.copy(
                            queueSongs = lastOperation.originalOrder,
                            isShuffleEnabled = false
                        )
                    }
                    is QueueOperation.ReplaceQueue -> {
                        _uiState.value = _uiState.value.copy(queueSongs = lastOperation.oldQueue)
                    }
                }
                
                redoHistory.add(lastOperation)
                
                _uiState.value = _uiState.value.copy(
                    canUndo = operationHistory.isNotEmpty(),
                    canRedo = redoHistory.isNotEmpty()
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Undo failed: ${e.message}")
            }
        }
    }

    /**
     * Redo last undone operation
     */
    fun redoLastOperation() {
        if (redoHistory.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val operation = redoHistory.removeLastOrNull() ?: return@launch
                
                // Re-execute the operation
                // Implementation would be similar to undoLastOperation but in reverse
                
                operationHistory.add(operation)
                
                _uiState.value = _uiState.value.copy(
                    canUndo = operationHistory.isNotEmpty(),
                    canRedo = redoHistory.isNotEmpty()
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Redo failed: ${e.message}")
            }
        }
    }

    // =================================================================================
    // UI STATE MANAGEMENT
    // =================================================================================

    /**
     * Toggle queue options menu
     */
    fun toggleQueueOptionsMenu() {
        _uiState.value = _uiState.value.copy(
            showQueueOptionsMenu = !_uiState.value.showQueueOptionsMenu
        )
    }

    /**
     * Toggle clear queue dialog
     */
    fun toggleClearQueueDialog() {
        _uiState.value = _uiState.value.copy(
            showClearQueueDialog = !_uiState.value.showClearQueueDialog
        )
    }

    /**
     * Toggle save queue dialog
     */
    fun toggleSaveQueueDialog() {
        _uiState.value = _uiState.value.copy(
            showSaveQueueDialog = !_uiState.value.showSaveQueueDialog
        )
    }

    /**
     * Toggle queue stats dialog
     */
    fun toggleQueueStatsDialog() {
        _uiState.value = _uiState.value.copy(
            showQueueStatsDialog = !_uiState.value.showQueueStatsDialog
        )
    }

    /**
     * Toggle add songs bottom sheet
     */
    fun toggleAddSongsBottomSheet() {
        val showBottomSheet = !_uiState.value.showAddSongsBottomSheet
        
        _uiState.value = _uiState.value.copy(
            showAddSongsBottomSheet = showBottomSheet
        )
        
        if (showBottomSheet) {
            loadAvailableSongs()
        } else {
            // Clear add songs state
            _uiState.value = _uiState.value.copy(
                selectedSongsToAdd = emptySet(),
                addSongsSearchQuery = "",
                availableSongs = emptyList()
            )
        }
    }

    /**
     * Update new playlist name
     */
    fun updateNewPlaylistName(name: String) {
        _uiState.value = _uiState.value.copy(newPlaylistName = name)
    }

    /**
     * Toggle compact view
     */
    fun toggleCompactView() {
        _uiState.value = _uiState.value.copy(compactView = !_uiState.value.compactView)
    }

    /**
     * Toggle queue stats visibility
     */
    fun toggleQueueStats() {
        _uiState.value = _uiState.value.copy(showQueueStats = !_uiState.value.showQueueStats)
    }

    /**
     * Navigate to now playing screen
     */
    fun navigateToNowPlaying() {
        _uiState.value = _uiState.value.copy(navigateToNowPlaying = true)
    }

    /**
     * Clear navigation states
     */
    fun clearNavigationStates() {
        _uiState.value = _uiState.value.copy(
            selectedSongForPlayback = null,
            selectedAlbumForNavigation = null,
            selectedArtistForNavigation = null,
            navigateToNowPlaying = false
        )
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refresh queue
     */
    fun refreshQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                loadQueue()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Refresh failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Show undo snackbar
     */
    private fun showUndoSnackbar(message: String) {
        _uiState.value = _uiState.value.copy(
            showUndoSnackbar = true,
            undoMessage = message
        )
        
        // Auto-hide after 5 seconds
        undoJob?.cancel()
        undoJob = viewModelScope.launch {
            delay(5000)
            _uiState.value = _uiState.value.copy(showUndoSnackbar = false)
        }
    }

    /**
     * Dismiss undo snackbar
     */
    fun dismissUndoSnackbar() {
        undoJob?.cancel()
        _uiState.value = _uiState.value.copy(showUndoSnackbar = false)
    }

    /**
     * Show message
     */
    private fun showMessage(message: String) {
        // TODO: Implement message display system
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        undoJob?.cancel()
    }
}

/**
 * Helper function to calculate queue stats
 */
private fun calculateQueueStats(songs: List<Song>): QueueStats {
    if (songs.isEmpty()) return QueueStats()
    
    val totalDuration = songs.sumOf { it.duration }
    val averageDuration = totalDuration / songs.size
    val uniqueArtists = songs.map { it.artist }.distinct().size
    val uniqueAlbums = songs.map { it.album }.distinct().size
    val uniqueGenres = songs.map { it.genre }.filter { it.isNotEmpty() }.distinct().size
    val totalPlayCount = songs.sumOf { it.playCount }
    val averageRating = songs.filter { it.rating > 0 }.map { it.rating }.average().toFloat()
    val favoriteSongs = songs.filter { it.isFavorite }
    
    return QueueStats(
        totalSongs = songs.size,
        totalDuration = totalDuration,
        averageSongDuration = averageDuration,
        uniqueArtists = uniqueArtists,
        uniqueAlbums = uniqueAlbums,
        uniqueGenres = uniqueGenres,
        totalPlayCount = totalPlayCount,
        averageRating = if (averageRating.isNaN()) 0f else averageRating,
        favoriteSongsCount = favoriteSongs.size,
        mostPlayedSong = songs.maxByOrNull { it.playCount },
        longestSong = songs.maxByOrNull { it.duration },
        shortestSong = songs.minByOrNull { it.duration },
        newestSong = songs.maxByOrNull { it.dateAdded },
        oldestSong = songs.minByOrNull { it.dateAdded }
    )
}
