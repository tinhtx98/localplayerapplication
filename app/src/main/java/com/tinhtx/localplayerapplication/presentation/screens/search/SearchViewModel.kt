package com.tinhtx.localplayerapplication.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import com.tinhtx.localplayerapplication.domain.usecase.search.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMusicUseCase: SearchMusicUseCase,
    private val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val playSongUseCase: PlaySongUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    
    init {
        observeSearchQuery()
        loadInitialData()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.value = _uiState.value.copy(searchQuery = query)
                    
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        clearSearchResults()
                    }
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load recent searches
                getRecentSearchesUseCase().collect { recentSearches ->
                    _uiState.value = _uiState.value.copy(
                        recentSearches = recentSearches
                    )
                }
            } catch (exception) {
                // Handle error silently for initial data
                android.util.Log.w("SearchViewModel", "Failed to load recent searches", exception)
            }

            try {
                // Load popular searches and suggestions
                val popularSearches = getPopularSearches()
                val suggestions = getSearchSuggestionsUseCase("")
                
                _uiState.value = _uiState.value.copy(
                    popularSearches = popularSearches,
                    searchSuggestions = suggestions
                )
            } catch (exception) {
                android.util.Log.w("SearchViewModel", "Failed to load initial data", exception)
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                error = null
            )

            val startTime = System.currentTimeMillis()

            try {
                searchMusicUseCase(query).collect { searchResults ->
                    val searchTime = System.currentTimeMillis() - startTime
                    
                    val totalResults = searchResults.songs.size + 
                                     searchResults.albums.size + 
                                     searchResults.artists.size + 
                                     searchResults.playlists.size

                    val topResult = determineTopResult(searchResults, query)
                    val quickActions = generateQuickActions(searchResults)

                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = searchResults,
                        totalResults = totalResults,
                        searchTime = searchTime,
                        topResult = topResult,
                        quickActions = quickActions,
                        error = null
                    )

                    // Save to recent searches
                    saveRecentSearchUseCase(query)
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = exception.message ?: "Search failed"
                )
            }
        }
    }

    private fun determineTopResult(searchResults: SearchResults, query: String): Any? {
        // Priority: Exact title match > Artist name match > Album name match
        
        // Check for exact song title match
        val exactSongMatch = searchResults.songs.find { 
            it.title.equals(query, ignoreCase = true) 
        }
        if (exactSongMatch != null) return exactSongMatch

        // Check for exact artist name match
        val exactArtistMatch = searchResults.artists.find { 
            it.displayName.equals(query, ignoreCase = true) 
        }
        if (exactArtistMatch != null) return exactArtistMatch

        // Check for exact album name match
        val exactAlbumMatch = searchResults.albums.find { 
            it.displayName.equals(query, ignoreCase = true) 
        }
        if (exactAlbumMatch != null) return exactAlbumMatch

        // Return most relevant result based on play count or popularity
        return searchResults.songs.maxByOrNull { it.playCount }
            ?: searchResults.artists.firstOrNull()
            ?: searchResults.albums.firstOrNull()
            ?: searchResults.playlists.firstOrNull()
    }

    private fun generateQuickActions(searchResults: SearchResults): List<SearchQuickAction> {
        val actions = mutableListOf<SearchQuickAction>()

        if (searchResults.songs.isNotEmpty()) {
            actions.add(
                SearchQuickAction(
                    type = SearchActionType.PLAY_ALL,
                    title = "Play all songs",
                    subtitle = "${searchResults.songs.size} songs",
                    icon = Icons.Default.PlayArrow
                )
            )

            if (searchResults.songs.size > 1) {
                actions.add(
                    SearchQuickAction(
                        type = SearchActionType.SHUFFLE_ALL,
                        title = "Shuffle songs",
                        subtitle = "Random order",
                        icon = Icons.Default.Shuffle
                    )
                )
            }
        }

        return actions
    }

    private fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = SearchResults(),
            totalResults = 0,
            searchTime = 0L,
            topResult = null,
            quickActions = emptyList(),
            isSearching = false,
            error = null
        )
    }

    private suspend fun getPopularSearches(): List<String> {
        // Mock popular searches - in real app this would come from analytics/backend
        return listOf(
            "rock", "pop", "jazz", "classical", "hip hop",
            "indie", "electronic", "country", "blues", "folk"
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun retrySearch() {
        val currentQuery = _uiState.value.searchQuery
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            try {
                clearRecentSearchesUseCase()
                _uiState.value = _uiState.value.copy(
                    recentSearches = emptyList()
                )
            } catch (exception) {
                android.util.Log.e("SearchViewModel", "Failed to clear recent searches", exception)
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song, "search_screen")
            } catch (exception) {
                android.util.Log.e("SearchViewModel", "Failed to play song", exception)
            }
        }
    }

    fun playAlbum(album: Album) {
        viewModelScope.launch {
            try {
                // Play first song from album
                android.util.Log.d("SearchViewModel", "Playing album: ${album.displayName}")
            } catch (exception) {
                android.util.Log.e("SearchViewModel", "Failed to play album", exception)
            }
        }
    }

    fun playArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                // Play shuffled songs from artist
                android.util.Log.d("SearchViewModel", "Playing artist: ${artist.displayName}")
            } catch (exception) {
                android.util.Log.e("SearchViewModel", "Failed to play artist", exception)
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
                android.util.Log.e("SearchViewModel", "Failed to toggle favorite", exception)
            }
        }
    }

    fun showAddToPlaylistDialog(song: Song) {
        android.util.Log.d("SearchViewModel", "Show add to playlist dialog for: ${song.title}")
    }
}

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class SearchQuickAction(
    val type: SearchActionType,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

enum class SearchActionType {
    PLAY_ALL,
    SHUFFLE_ALL,
    ADD_TO_QUEUE,
    CREATE_PLAYLIST
}
