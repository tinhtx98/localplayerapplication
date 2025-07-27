package com.tinhtx.localplayerapplication.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.search.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

/**
 * ViewModel for Search Screen - Complete integration with search use cases
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    // Search Use Cases
    private val searchMusicUseCase: SearchMusicUseCase,
    private val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchHistoryUseCase: SaveSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    
    // Music Use Cases
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    
    // Player Use Cases
    private val playSongUseCase: PlaySongUseCase,
    private val shufflePlayUseCase: ShufflePlayUseCase,
    
    // Favorites Use Cases
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    
    // Voice Search Use Cases
    private val voiceSearchUseCase: VoiceSearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null
    private var voiceSearchJob: Job? = null

    init {
        loadInitialData()
        observeFavorites()
    }

    // =================================================================================
    // INITIALIZATION
    // =================================================================================

    /**
     * Load initial search data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load search history and trending searches concurrently
                val historyDeferred = async { loadSearchHistory() }
                val trendingDeferred = async { loadTrendingSearches() }
                val voiceDeferred = async { checkVoiceSearchAvailability() }
                
                awaitAll(historyDeferred, trendingDeferred, voiceDeferred)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load search data: ${e.message}")
            }
        }
    }

    private suspend fun loadSearchHistory() {
        try {
            getSearchHistoryUseCase.execute().fold(
                onSuccess = { history ->
                    val recentSearches = history.take(10).map { it.query }
                    _uiState.value = _uiState.value.copy(
                        searchHistory = history,
                        recentSearches = recentSearches
                    )
                },
                onFailure = { error ->
                    // History loading failure is not critical
                }
            )
        } catch (e: Exception) {
            // History loading failure is not critical
        }
    }

    private suspend fun loadTrendingSearches() {
        try {
            // TODO: Implement trending searches from analytics
            val trending = listOf("rock", "pop", "jazz", "classical", "electronic")
            _uiState.value = _uiState.value.copy(trendingSearches = trending)
        } catch (e: Exception) {
            // Trending searches failure is not critical
        }
    }

    private suspend fun checkVoiceSearchAvailability() {
        try {
            val isAvailable = voiceSearchUseCase.isAvailable()
            _uiState.value = _uiState.value.copy(isVoiceSearchAvailable = isAvailable)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isVoiceSearchAvailable = false)
        }
    }

    // =================================================================================
    // SEARCH FUNCTIONALITY
    // =================================================================================

    /**
     * Perform search with query
     */
    fun search(query: String, saveToHistory: Boolean = true) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        // Cancel previous search
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                _uiState.value = _uiState.value.copy(
                    searchQuery = query.trim(),
                    isSearching = true,
                    isLoading = true,
                    searchStartTime = startTime,
                    error = null
                )

                // Perform search
                searchMusicUseCase.execute(
                    query = query.trim(),
                    category = _uiState.value.selectedCategory,
                    filters = _uiState.value.filters,
                    sortOrder = _uiState.value.sortOrder,
                    sortAscending = _uiState.value.sortAscending
                ).fold(
                    onSuccess = { results ->
                        val searchTime = System.currentTimeMillis() - startTime
                        val searchResults = results.copy(searchTime = searchTime)
                        
                        _uiState.value = _uiState.value.copy(
                            searchResults = searchResults,
                            isSearching = false,
                            isLoading = false,
                            hasSearched = true,
                            searchDuration = searchTime,
                            error = null
                        )
                        
                        // Save to search history
                        if (saveToHistory && results.hasResults) {
                            saveSearchToHistory(query.trim(), results.totalCount)
                        }
                        
                        // Update analytics
                        updateSearchAnalytics(query, results, searchTime)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            isLoading = false,
                            hasSearched = true,
                            error = "Search failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    isLoading = false,
                    hasSearched = true,
                    error = "Search error: ${e.message}"
                )
            }
        }
    }

    /**
     * Update search query (for real-time typing)
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copyWithQuery(query, query.isNotBlank())
        
        // Load suggestions for non-empty queries
        if (query.isNotBlank()) {
            loadSearchSuggestions(query)
        } else {
            _uiState.value = _uiState.value.copy(searchSuggestions = emptyList())
        }
    }

    /**
     * Trigger search with current query
     */
    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            search(query)
        }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        suggestionsJob?.cancel()
        
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false,
            isSearchActive = false,
            isLoading = false,
            hasSearched = false,
            searchResults = SearchResults(),
            searchSuggestions = emptyList(),
            error = null
        )
    }

    /**
     * Search from history item
     */
    fun searchFromHistory(historyItem: SearchHistoryItem) {
        updateSearchCategory(historyItem.category)
        search(historyItem.query)
    }

    /**
     * Search from suggestion
     */
    fun searchFromSuggestion(suggestion: SearchSuggestion) {
        when (suggestion.type) {
            SuggestionType.QUERY -> search(suggestion.text)
            SuggestionType.SONG -> {
                updateSearchCategory(SearchCategory.SONGS)
                search(suggestion.text)
            }
            SuggestionType.ARTIST -> {
                updateSearchCategory(SearchCategory.ARTISTS)
                search(suggestion.text)
            }
            SuggestionType.ALBUM -> {
                updateSearchCategory(SearchCategory.ALBUMS)
                search(suggestion.text)
            }
            SuggestionType.PLAYLIST -> {
                updateSearchCategory(SearchCategory.PLAYLISTS)
                search(suggestion.text)
            }
            SuggestionType.GENRE -> {
                // Update filters with selected genre
                val currentFilters = _uiState.value.filters
                val updatedFilters = currentFilters.copy(
                    genres = setOf(suggestion.text)
                )
                updateSearchFilters(updatedFilters)
                search(_uiState.value.searchQuery)
            }
        }
    }

    // =================================================================================
    // SEARCH SUGGESTIONS
    // =================================================================================

    /**
     * Load search suggestions
     */
    private fun loadSearchSuggestions(query: String) {
        suggestionsJob?.cancel()
        
        suggestionsJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(suggestionsLoading = true)
                
                // Add small delay to avoid too many requests
                delay(300)
                
                getSearchSuggestionsUseCase.execute(query).fold(
                    onSuccess = { suggestions ->
                        _uiState.value = _uiState.value.copy(
                            searchSuggestions = suggestions,
                            suggestionsLoading = false
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            searchSuggestions = emptyList(),
                            suggestionsLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    searchSuggestions = emptyList(),
                    suggestionsLoading = false
                )
            }
        }
    }

    // =================================================================================
    // VOICE SEARCH
    // =================================================================================

    /**
     * Start voice search
     */
    fun startVoiceSearch() {
        if (!_uiState.value.isVoiceSearchAvailable) return
        
        voiceSearchJob?.cancel()
        voiceSearchJob = viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isVoiceSearchActive = true,
                    showVoiceSearchDialog = true,
                    voiceSearchError = null
                )
                
                voiceSearchUseCase.startListening().fold(
                    onSuccess = { results ->
                        results.collect { result ->
                            when (result) {
                                is VoiceSearchResult.Listening -> {
                                    // Update UI to show listening state
                                }
                                is VoiceSearchResult.Speaking -> {
                                    _uiState.value = _uiState.value.copy(
                                        voiceSearchResults = result.partialText
                                    )
                                }
                                is VoiceSearchResult.Success -> {
                                    _uiState.value = _uiState.value.copy(
                                        isVoiceSearchActive = false,
                                        showVoiceSearchDialog = false,
                                        voiceSearchResults = result.text
                                    )
                                    
                                    // Perform search with voice result
                                    search(result.text)
                                }
                                is VoiceSearchResult.Error -> {
                                    _uiState.value = _uiState.value.copy(
                                        isVoiceSearchActive = false,
                                        showVoiceSearchDialog = false,
                                        voiceSearchError = result.message
                                    )
                                }
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isVoiceSearchActive = false,
                            showVoiceSearchDialog = false,
                            voiceSearchError = error.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVoiceSearchActive = false,
                    showVoiceSearchDialog = false,
                    voiceSearchError = e.message
                )
            }
        }
    }

    /**
     * Stop voice search
     */
    fun stopVoiceSearch() {
        voiceSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isVoiceSearchActive = false,
            showVoiceSearchDialog = false,
            voiceSearchResults = "",
            voiceSearchError = null
        )
    }

    // =================================================================================
    // SEARCH CATEGORIES & FILTERS
    // =================================================================================

    /**
     * Update search category
     */
    fun updateSearchCategory(category: SearchCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        
        // Re-search if we have results
        if (_uiState.value.hasSearched && _uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery, saveToHistory = false)
        }
    }

    /**
     * Update search filters
     */
    fun updateSearchFilters(filters: SearchFilters) {
        _uiState.value = _uiState.value.copy(filters = filters)
        
        // Re-search if we have results
        if (_uiState.value.hasSearched && _uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery, saveToHistory = false)
        }
    }

    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SearchSortOrder, ascending: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            sortAscending = ascending
        )
        
        // Re-search if we have results
        if (_uiState.value.hasSearched && _uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery, saveToHistory = false)
        }
    }

    /**
     * Toggle search filters visibility
     */
    fun toggleSearchFilters() {
        _uiState.value = _uiState.value.copy(
            showSearchFilters = !_uiState.value.showSearchFilters
        )
    }

    // =================================================================================
    // QUICK ACTIONS
    // =================================================================================

    /**
     * Execute quick action
     */
    fun executeQuickAction(action: QuickAction) {
        when (action.action) {
            QuickActionType.SHUFFLE_ALL -> shuffleAllSongs()
            QuickActionType.RECENTLY_ADDED -> searchRecentlyAdded()
            QuickActionType.MOST_PLAYED -> searchMostPlayed()
            QuickActionType.FAVORITES -> searchFavorites()
            QuickActionType.VOICE_SEARCH -> startVoiceSearch()
            QuickActionType.SCAN_LIBRARY -> triggerLibraryScan()
            QuickActionType.CREATE_PLAYLIST -> showCreatePlaylistDialog()
            QuickActionType.IMPORT_MUSIC -> importMusic()
        }
    }

    private fun shuffleAllSongs() {
        viewModelScope.launch {
            try {
                getAllSongsUseCase.getAllSongs().first().let { songs ->
                    if (songs.isNotEmpty()) {
                        shufflePlayUseCase.execute(songs)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to shuffle songs: ${e.message}")
            }
        }
    }

    private fun searchRecentlyAdded() {
        updateSearchCategory(SearchCategory.SONGS)
        updateSortOrder(SearchSortOrder.DATE_ADDED, ascending = false)
        search("", saveToHistory = false)
    }

    private fun searchMostPlayed() {
        updateSearchCategory(SearchCategory.SONGS)
        updateSortOrder(SearchSortOrder.PLAY_COUNT, ascending = false)
        search("", saveToHistory = false)
    }

    private fun searchFavorites() {
        val filters = _uiState.value.filters.copy(onlyFavorites = true)
        updateSearchFilters(filters)
        search("favorites", saveToHistory = false)
    }

    private fun triggerLibraryScan() {
        // TODO: Implement library scan trigger
    }

    private fun showCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = true)
    }

    private fun importMusic() {
        // TODO: Implement music import
    }

    // =================================================================================
    // PLAYBACK CONTROLS
    // =================================================================================

    /**
     * Play song
     */
    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase.execute(song).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            currentPlayingSong = song,
                            isPlaying = true,
                            selectedSongForPlayback = song
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to play song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Playback error: ${e.message}")
            }
        }
    }

    /**
     * Play search results
     */
    fun playSearchResults() {
        val songs = _uiState.value.filteredResults.songs
        if (songs.isNotEmpty()) {
            playSong(songs.first())
        }
    }

    /**
     * Shuffle play search results
     */
    fun shufflePlaySearchResults() {
        val songs = _uiState.value.filteredResults.songs
        if (songs.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    shufflePlayUseCase.execute(songs)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copyWithError("Failed to shuffle: ${e.message}")
                }
            }
        }
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
    // SEARCH HISTORY MANAGEMENT
    // =================================================================================

    private fun saveSearchToHistory(query: String, resultCount: Int) {
        viewModelScope.launch {
            try {
                val historyItem = SearchHistoryItem(
                    query = query,
                    timestamp = System.currentTimeMillis(),
                    resultCount = resultCount,
                    category = _uiState.value.selectedCategory
                )
                
                saveSearchHistoryUseCase.execute(historyItem)
                
                // Update current history
                loadSearchHistory()
            } catch (e: Exception) {
                // History saving failure is not critical
            }
        }
    }

    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                clearSearchHistoryUseCase.execute()
                _uiState.value = _uiState.value.copy(
                    searchHistory = emptyList(),
                    recentSearches = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to clear history: ${e.message}")
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
        _uiState.value = _uiState.value.copyWithSelection(isSelectionMode, clearSelections = !isSelectionMode)
    }

    /**
     * Toggle item selection
     */
    fun toggleSongSelection(songId: Long) {
        val currentSelection = _uiState.value.selectedSongs.toMutableSet()
        
        if (currentSelection.contains(songId)) {
            currentSelection.remove(songId)
        } else {
            currentSelection.add(songId)
        }
        
        _uiState.value = _uiState.value.copy(selectedSongs = currentSelection)
        
        // Exit selection mode if no items selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false, clearSelections = true)
        }
    }

    // =================================================================================
    // NAVIGATION
    // =================================================================================

    /**
     * Navigate to album
     */
    fun navigateToAlbum(album: Album) {
        _uiState.value = _uiState.value.copy(selectedAlbumForNavigation = album)
    }

    /**
     * Navigate to artist
     */
    fun navigateToArtist(artist: Artist) {
        _uiState.value = _uiState.value.copy(selectedArtistForNavigation = artist)
    }

    /**
     * Navigate to playlist
     */
    fun navigateToPlaylist(playlist: Playlist) {
        _uiState.value = _uiState.value.copy(selectedPlaylistForNavigation = playlist)
    }

    /**
     * Clear navigation states
     */
    fun clearNavigationStates() {
        _uiState.value = _uiState.value.copy(
            selectedSongForPlayback = null,
            selectedAlbumForNavigation = null,
            selectedArtistForNavigation = null,
            selectedPlaylistForNavigation = null
        )
    }

    // =================================================================================
    // UI STATE MANAGEMENT
    // =================================================================================

    /**
     * Toggle search history visibility
     */
    fun toggleSearchHistory() {
        _uiState.value = _uiState.value.copy(
            showSearchHistory = !_uiState.value.showSearchHistory
        )
    }

    /**
     * Toggle voice search dialog
     */
    fun toggleVoiceSearchDialog() {
        if (_uiState.value.showVoiceSearchDialog) {
            stopVoiceSearch()
        } else if (_uiState.value.canUseVoiceSearch) {
            startVoiceSearch()
        }
    }

    /**
     * Toggle create playlist dialog
     */
    fun toggleCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(
            showCreatePlaylistDialog = !_uiState.value.showCreatePlaylistDialog
        )
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null,
            songsError = null,
            albumsError = null,
            artistsError = null,
            playlistsError = null,
            voiceSearchError = null
        )
    }

    // =================================================================================
    // ANALYTICS
    // =================================================================================

    private fun updateSearchAnalytics(query: String, results: SearchResults, searchTime: Long) {
        viewModelScope.launch {
            try {
                val analytics = _uiState.value.searchAnalytics
                val isSuccessful = results.hasResults
                
                val updatedAnalytics = analytics.copy(
                    totalSearches = analytics.totalSearches + 1,
                    successfulSearches = if (isSuccessful) analytics.successfulSearches + 1 else analytics.successfulSearches,
                    emptySearches = if (!isSuccessful) analytics.emptySearches + 1 else analytics.emptySearches,
                    averageSearchTime = ((analytics.averageSearchTime * analytics.totalSearches) + searchTime) / (analytics.totalSearches + 1),
                    mostSearchedTerms = (analytics.mostSearchedTerms + query).groupingBy { it }.eachCount()
                        .toList().sortedByDescending { it.second }.take(10).map { it.first }
                )
                
                _uiState.value = _uiState.value.copy(searchAnalytics = updatedAnalytics)
            } catch (e: Exception) {
                // Analytics failure is not critical
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        suggestionsJob?.cancel()
        voiceSearchJob?.cancel()
    }
}

/**
 * Voice search result sealed class
 */
sealed class VoiceSearchResult {
    object Listening : VoiceSearchResult()
    data class Speaking(val partialText: String) : VoiceSearchResult()
    data class Success(val text: String) : VoiceSearchResult()
    data class Error(val message: String) : VoiceSearchResult()
}
