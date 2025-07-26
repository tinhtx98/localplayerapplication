package com.tinhtx.localplayerapplication.presentation.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import com.tinhtx.localplayerapplication.domain.usecase.playlist.AddToPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val addToPlaylistUseCase: AddToPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(AppConstants.SortOrder.DATE_ADDED_DESC)
    private val _isSearching = MutableStateFlow(false)

    init {
        observeFavorites()
        observeSearchAndSort()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase().catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load favorites"
                )
            }.collect { favorites ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    favorites = favorites,
                    totalDuration = calculateTotalDuration(favorites),
                    averageRating = calculateAverageRating(favorites)
                )
            }
        }
    }

    private fun observeSearchAndSort() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _sortOrder,
                _uiState.map { it.favorites }
            ) { query, sortOrder, favorites ->
                processSearchAndSort(query, sortOrder, favorites)
            }.collect { (filteredSongs, processedState) ->
                _uiState.value = processedState.copy(
                    filteredFavorites = filteredSongs,
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
        favorites: List<Song>
    ): Pair<List<Song>, FavoritesUiState> {
        val currentState = _uiState.value

        // Apply search filter
        val searchFiltered = if (query.isBlank()) {
            favorites
        } else {
            favorites.filter { song ->
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
            AppConstants.SortOrder.DATE_ADDED_ASC -> searchFiltered.sortedBy { it.dateAdded }
            AppConstants.SortOrder.DATE_ADDED_DESC -> searchFiltered.sortedByDescending { it.dateAdded }
            AppConstants.SortOrder.DURATION_ASC -> searchFiltered.sortedBy { it.duration }
            AppConstants.SortOrder.DURATION_DESC -> searchFiltered.sortedByDescending { it.duration }
        }

        return sorted to currentState
    }

    fun loadFavorites() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    }

    fun retryLoadFavorites() {
        loadFavorites()
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song, "favorites_screen")
            } catch (exception) {
                // Handle error
                android.util.Log.e("FavoritesViewModel", "Failed to play song", exception)
            }
        }
    }

    fun playAllFavorites() {
        viewModelScope.launch {
            try {
                val favorites = _uiState.value.favorites
                if (favorites.isNotEmpty()) {
                    playSongUseCase(favorites.first(), "favorites_play_all")
                    // Set up queue with all favorites
                }
            } catch (exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to play all favorites", exception)
            }
        }
    }

    fun shuffleFavorites() {
        viewModelScope.launch {
            try {
                val favorites = _uiState.value.favorites.shuffled()
                if (favorites.isNotEmpty()) {
                    playSongUseCase(favorites.first(), "favorites_shuffle")
                    // Set up shuffled queue
                }
            } catch (exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to shuffle favorites", exception)
            }
        }
    }

    fun removeFromFavorites(song: Song) {
        viewModelScope.launch {
            try {
                removeFromFavoritesUseCase(song.id)
            } catch (exception) {
                // Handle error - could show snackbar
                android.util.Log.e("FavoritesViewModel", "Failed to remove from favorites", exception)
            }
        }
    }

    fun removeAllFavorites() {
        viewModelScope.launch {
            try {
                _uiState.value.favorites.forEach { song ->
                    removeFromFavoritesUseCase(song.id)
                }
            } catch (exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to remove all favorites", exception)
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

    fun showAddToPlaylistDialog(song: Song) {
        // This would typically trigger a dialog or bottom sheet
        android.util.Log.d("FavoritesViewModel", "Show add to playlist dialog for: ${song.title}")
    }

    fun exportFavorites() {
        viewModelScope.launch {
            try {
                val favorites = _uiState.value.favorites
                // Export favorites as M3U playlist or similar
                android.util.Log.d("FavoritesViewModel", "Exporting ${favorites.size} favorites")
            } catch (exception) {
                android.util.Log.e("FavoritesViewModel", "Failed to export favorites", exception)
            }
        }
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

    private fun calculateAverageRating(songs: List<Song>): Float {
        if (songs.isEmpty()) return 0f
        
        // Mock rating calculation based on play count
        val totalRating = songs.sumOf { song ->
            when {
                song.playCount > 50 -> 5f
                song.playCount > 25 -> 4f
                song.playCount > 10 -> 3f
                song.playCount > 5 -> 2f
                else -> 1f
            }
        }
        
        return totalRating / songs.size
    }
}
