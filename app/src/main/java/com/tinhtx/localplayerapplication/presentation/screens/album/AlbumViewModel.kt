package com.tinhtx.localplayerapplication.presentation.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.album.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val getAlbumUseCase: GetAlbumUseCase,
    private val getAlbumSongsUseCase: GetAlbumSongsUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val addToQueueUseCase: AddToQueueUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(AppConstants.SortOrder.TRACK_NUMBER)
    private val _isSearching = MutableStateFlow(false)

    init {
        observeSearchAndSort()
    }

    private fun observeSearchAndSort() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _sortOrder,
                _uiState.map { it.songs }
            ) { query, sortOrder, songs ->
                processSearchAndSort(query, sortOrder, songs)
            }.collect { (filteredSongs, processedState) ->
                _uiState.value = processedState.copy(
                    filteredSongs = filteredSongs,
                    searchQuery = _searchQuery.value,
                    sortOrder = _sortOrder.value,
                    isSearching = _isSearching.value
                )
            }
        }
    }

    private fun processSearchAndSort(
        query: String,
        sortOrder: AppConstants.SortOrder,
        songs: List<Song>
    ): Pair<List<Song>, AlbumUiState> {
        val currentState = _uiState.value

        // Apply search filter
        val searchFiltered = if (query.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.displayArtist.contains(query, ignoreCase = true)
            }
        }

        // Apply sorting
        val sorted = when (sortOrder) {
            AppConstants.SortOrder.TRACK_NUMBER -> searchFiltered.sortedBy { it.trackNumber ?: Int.MAX_VALUE }
            AppConstants.SortOrder.TITLE_ASC -> searchFiltered.sortedBy { it.title.lowercase() }
            AppConstants.SortOrder.TITLE_DESC -> searchFiltered.sortedByDescending { it.title.lowercase() }
            AppConstants.SortOrder.DURATION_ASC -> searchFiltered.sortedBy { it.duration }
            AppConstants.SortOrder.DURATION_DESC -> searchFiltered.sortedByDescending { it.duration }
            else -> searchFiltered.sortedBy { it.trackNumber ?: Int.MAX_VALUE }
        }

        return sorted to currentState
    }

    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load album and songs concurrently
                combine(
                    getAlbumUseCase(albumId),
                    getAlbumSongsUseCase(albumId)
                ) { album, songs ->
                    AlbumData(album, songs)
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load album"
                    )
                }.collect { albumData ->
                    val totalDuration = calculateTotalDuration(albumData.songs)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        album = albumData.album,
                        songs = albumData.songs,
                        totalDuration = totalDuration
                    )
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load album"
                )
            }
        }
    }

    fun retryLoadAlbum() {
        val currentAlbum = _uiState.value.album
        currentAlbum?.let { loadAlbum(it.mediaStoreId) }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song, "album_screen")
            } catch (exception) {
                handleError(exception, "Failed to play song")
            }
        }
    }

    fun playAlbum() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs
                if (songs.isNotEmpty()) {
                    playSongUseCase(songs.first(), "album_play_all")
                    // Set up queue with all album songs
                }
            } catch (exception) {
                handleError(exception, "Failed to play album")
            }
        }
    }

    fun shuffleAlbum() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs.shuffled()
                if (songs.isNotEmpty()) {
                    playSongUseCase(songs.first(), "album_shuffle")
                    // Set up shuffled queue
                }
            } catch (exception) {
                handleError(exception, "Failed to shuffle album")
            }
        }
    }

    fun addAlbumToQueue() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs
                songs.forEach { song ->
                    addToQueueUseCase(song)
                }
                android.util.Log.d("AlbumViewModel", "Added ${songs.size} songs to queue")
            } catch (exception) {
                handleError(exception, "Failed to add album to queue")
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

    fun addAllToFavorites() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs
                songs.forEach { song ->
                    if (!song.isFavorite) {
                        addToFavoritesUseCase(song.id)
                    }
                }
                android.util.Log.d("AlbumViewModel", "Added all album songs to favorites")
            } catch (exception) {
                handleError(exception, "Failed to add all songs to favorites")
            }
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: Long) {
        viewModelScope.launch {
            try {
                addSongToPlaylistUseCase(playlistId, listOf(song.id))
            } catch (exception) {
                handleError(exception, "Failed to add song to playlist")
            }
        }
    }

    fun createPlaylistWithSong(song: Song, playlistName: String) {
        viewModelScope.launch {
            try {
                createPlaylistUseCase(
                    name = playlistName,
                    description = "Created from album: ${_uiState.value.album?.displayName}",
                    songIds = listOf(song.id)
                )
                android.util.Log.d("AlbumViewModel", "Created playlist: $playlistName")
            } catch (exception) {
                handleError(exception, "Failed to create playlist")
            }
        }
    }

    fun shareAlbum() {
        val album = _uiState.value.album ?: return
        val songs = _uiState.value.songs
        
        android.util.Log.d("AlbumViewModel", "Sharing album: ${album.displayName} with ${songs.size} songs")
    }

    fun toggleSearchMode() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(sortOrder: AppConstants.SortOrder) {
        _sortOrder.value = sortOrder
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

    private fun handleError(exception: Throwable, message: String) {
        android.util.Log.e("AlbumViewModel", message, exception)
        _uiState.value = _uiState.value.copy(
            error = exception.message ?: message
        )
    }
}

data class AlbumUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: AppConstants.SortOrder = AppConstants.SortOrder.TRACK_NUMBER,
    val isSearching: Boolean = false,
    val totalDuration: String = ""
)

private data class AlbumData(
    val album: Album,
    val songs: List<Song>
)
