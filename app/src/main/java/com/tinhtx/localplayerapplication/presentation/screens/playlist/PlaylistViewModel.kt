package com.tinhtx.localplayerapplication.presentation.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import com.tinhtx.localplayerapplication.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistUseCase: GetPlaylistUseCase,
    private val getPlaylistSongsUseCase: GetPlaylistSongsUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val addSongsToPlaylistUseCase: AddSongsToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val playSongUseCase: PlaySongUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(AppConstants.SortOrder.CUSTOM)
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
    ): Pair<List<Song>, PlaylistUiState> {
        val currentState = _uiState.value

        // Apply search filter
        val searchFiltered = if (query.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.displayArtist.contains(query, ignoreCase = true) ||
                song.displayAlbum.contains(query, ignoreCase = true)
            }
        }

        // Apply sorting
        val sorted = when (sortOrder) {
            AppConstants.SortOrder.TITLE_ASC -> searchFiltered.sortedBy { it.title.lowercase() }
            AppConstants.SortOrder.TITLE_DESC -> searchFiltered.sortedByDescending { it.title.lowercase() }
            AppConstants.SortOrder.ARTIST_ASC -> searchFiltered.sortedBy { it.displayArtist.lowercase() }
            AppConstants.SortOrder.ARTIST_DESC -> searchFiltered.sortedByDescending { it.displayArtist.lowercase() }
            AppConstants.SortOrder.ALBUM_ASC -> searchFiltered.sortedBy { it.displayAlbum.lowercase() }
            AppConstants.SortOrder.ALBUM_DESC -> searchFiltered.sortedByDescending { it.displayAlbum.lowercase() }
            AppConstants.SortOrder.DURATION_ASC -> searchFiltered.sortedBy { it.duration }
            AppConstants.SortOrder.DURATION_DESC -> searchFiltered.sortedByDescending { it.duration }
            AppConstants.SortOrder.DATE_ADDED_ASC -> searchFiltered.sortedBy { it.dateAdded }
            AppConstants.SortOrder.DATE_ADDED_DESC -> searchFiltered.sortedByDescending { it.dateAdded }
            AppConstants.SortOrder.CUSTOM -> searchFiltered // Keep original playlist order
        }

        return sorted to currentState
    }

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load playlist and songs concurrently
                combine(
                    getPlaylistUseCase(playlistId),
                    getPlaylistSongsUseCase(playlistId)
                ) { playlist, songs ->
                    PlaylistData(playlist, songs)
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load playlist"
                    )
                }.collect { playlistData ->
                    val totalDuration = calculateTotalDuration(playlistData.songs)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        playlist = playlistData.playlist,
                        songs = playlistData.songs,
                        totalDuration = totalDuration
                    )
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load playlist"
                )
            }
        }
    }

    fun retryLoadPlaylist() {
        val currentPlaylist = _uiState.value.playlist
        currentPlaylist?.let { loadPlaylist(it.id) }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song, "playlist_screen")
            } catch (exception) {
                handleError(exception, "Failed to play song")
            }
        }
    }

    fun playAllSongs() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs
                if (songs.isNotEmpty()) {
                    playSongUseCase(songs.first(), "playlist_play_all")
                    // Set up queue with all playlist songs
                }
            } catch (exception) {
                handleError(exception, "Failed to play all songs")
            }
        }
    }

    fun shufflePlaylist() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.songs.shuffled()
                if (songs.isNotEmpty()) {
                    playSongUseCase(songs.first(), "playlist_shuffle")
                    // Set up shuffled queue
                }
            } catch (exception) {
                handleError(exception, "Failed to shuffle playlist")
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

    fun removeSongFromPlaylist(song: Song) {
        viewModelScope.launch {
            try {
                val playlist = _uiState.value.playlist ?: return@launch
                removeSongFromPlaylistUseCase(playlist.id, song.id)
                
                // Update local state immediately for better UX
                val updatedSongs = _uiState.value.songs.filter { it.id != song.id }
                val updatedPlaylist = playlist.copy(songCount = updatedSongs.size)
                val newTotalDuration = calculateTotalDuration(updatedSongs)
                
                _uiState.value = _uiState.value.copy(
                    playlist = updatedPlaylist,
                    songs = updatedSongs,
                    totalDuration = newTotalDuration
                )
            } catch (exception) {
                handleError(exception, "Failed to remove song from playlist")
            }
        }
    }

    fun addSongsToPlaylist(songs: List<Song>) {
        viewModelScope.launch {
            try {
                val playlist = _uiState.value.playlist ?: return@launch
                addSongsToPlaylistUseCase(playlist.id, songs.map { it.id })
                
                // Update local state
                val currentSongs = _uiState.value.songs
                val newSongs = songs.filter { newSong ->
                    currentSongs.none { it.id == newSong.id }
                }
                val updatedSongs = currentSongs + newSongs
                val updatedPlaylist = playlist.copy(songCount = updatedSongs.size)
                val newTotalDuration = calculateTotalDuration(updatedSongs)
                
                _uiState.value = _uiState.value.copy(
                    playlist = updatedPlaylist,
                    songs = updatedSongs,
                    totalDuration = newTotalDuration
                )
            } catch (exception) {
                handleError(exception, "Failed to add songs to playlist")
            }
        }
    }

    fun updatePlaylist(name: String, description: String?, coverUri: String?) {
        viewModelScope.launch {
            try {
                val playlist = _uiState.value.playlist ?: return@launch
                val updatedPlaylist = playlist.copy(
                    name = name,
                    description = description,
                    coverArtPath = coverUri
                )
                
                updatePlaylistUseCase(updatedPlaylist)
                _uiState.value = _uiState.value.copy(playlist = updatedPlaylist)
            } catch (exception) {
                handleError(exception, "Failed to update playlist")
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            try {
                val playlist = _uiState.value.playlist ?: return@launch
                deletePlaylistUseCase(playlist.id)
            } catch (exception) {
                handleError(exception, "Failed to delete playlist")
            }
        }
    }

    fun sharePlaylist() {
        val playlist = _uiState.value.playlist ?: return
        val songs = _uiState.value.songs
        
        android.util.Log.d("PlaylistViewModel", "Sharing playlist: ${playlist.name} with ${songs.size} songs")
    }

    fun exportPlaylist() {
        viewModelScope.launch {
            try {
                val playlist = _uiState.value.playlist ?: return@launch
                val songs = _uiState.value.songs
                
                // Export as M3U playlist format
                android.util.Log.d("PlaylistViewModel", "Exporting playlist: ${playlist.name}")
            } catch (exception) {
                handleError(exception, "Failed to export playlist")
            }
        }
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
        android.util.Log.e("PlaylistViewModel", message, exception)
        _uiState.value = _uiState.value.copy(
            error = exception.message ?: message
        )
    }
}

private data class PlaylistData(
    val playlist: Playlist,
    val songs: List<Song>
)
