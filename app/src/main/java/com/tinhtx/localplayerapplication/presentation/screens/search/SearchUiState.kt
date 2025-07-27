package com.tinhtx.localplayerapplication.presentation.screens.search

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Search Screen - Complete search state management
 */
data class SearchUiState(
    // =================================================================================
    // SEARCH QUERY & STATES
    // =================================================================================
    
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false,
    val searchStartTime: Long = 0L,
    val searchDuration: Long = 0L,
    val hasSearched: Boolean = false,
    
    // =================================================================================
    // SEARCH RESULTS
    // =================================================================================
    
    val searchResults: SearchResults = SearchResults(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Individual category loading states
    val songsLoading: Boolean = false,
    val albumsLoading: Boolean = false,
    val artistsLoading: Boolean = false,
    val playlistsLoading: Boolean = false,
    
    // Individual category errors
    val songsError: String? = null,
    val albumsError: String? = null,
    val artistsError: String? = null,
    val playlistsError: String? = null,
    
    // =================================================================================
    // SEARCH CATEGORIES & FILTERS
    // =================================================================================
    
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val availableCategories: List<SearchCategory> = SearchCategory.values().toList(),
    val sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE,
    val sortAscending: Boolean = false,
    
    // Search filters
    val filters: SearchFilters = SearchFilters(),
    val showFilters: Boolean = false,
    
    // =================================================================================
    // SEARCH SUGGESTIONS & HISTORY
    // =================================================================================
    
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val trendingSearches: List<String> = emptyList(),
    val suggestionsLoading: Boolean = false,
    
    // =================================================================================
    // VOICE SEARCH
    // =================================================================================
    
    val isVoiceSearchAvailable: Boolean = false,
    val isVoiceSearchActive: Boolean = false,
    val voiceSearchResults: String = "",
    val voiceSearchError: String? = null,
    
    // =================================================================================
    // QUICK ACTIONS
    // =================================================================================
    
    val quickActions: List<QuickAction> = QuickAction.getDefaultActions(),
    val showQuickActions: Boolean = true,
    
    // =================================================================================
    // SELECTION & PLAYBACK
    // =================================================================================
    
    val isSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    val selectedAlbums: Set<Long> = emptySet(),
    val selectedArtists: Set<Long> = emptySet(),
    val selectedPlaylists: Set<Long> = emptySet(),
    
    // Current playing state
    val currentPlayingSong: Song? = null,
    val isPlaying: Boolean = false,
    
    // Favorites
    val favoriteSongs: Set<Long> = emptySet(),
    
    // =================================================================================
    // NAVIGATION
    // =================================================================================
    
    val selectedSongForPlayback: Song? = null,
    val selectedAlbumForNavigation: Album? = null,
    val selectedArtistForNavigation: Artist? = null,
    val selectedPlaylistForNavigation: Playlist? = null,
    
    // =================================================================================
    // UI STATES
    // =================================================================================
    
    val showSearchFilters: Boolean = false,
    val showSearchHistory: Boolean = false,
    val showVoiceSearchDialog: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    
    // =================================================================================
    // ANALYTICS
    // =================================================================================
    
    val searchAnalytics: SearchAnalytics = SearchAnalytics()
) {
    
    // =================================================================================
    // COMPUTED PROPERTIES
    // =================================================================================
    
    val hasResults: Boolean
        get() = searchResults.hasResults
    
    val isEmpty: Boolean
        get() = hasSearched && !hasResults && !isLoading
    
    val hasError: Boolean
        get() = error != null || songsError != null || albumsError != null || 
                artistsError != null || playlistsError != null
    
    val currentError: String?
        get() = error ?: songsError ?: albumsError ?: artistsError ?: playlistsError
    
    val totalResultsCount: Int
        get() = searchResults.totalCount
    
    val filteredResults: SearchResults
        get() = when (selectedCategory) {
            SearchCategory.ALL -> searchResults
            SearchCategory.SONGS -> searchResults.copy(
                albums = emptyList(),
                artists = emptyList(),
                playlists = emptyList()
            )
            SearchCategory.ALBUMS -> searchResults.copy(
                songs = emptyList(),
                artists = emptyList(),
                playlists = emptyList()
            )
            SearchCategory.ARTISTS -> searchResults.copy(
                songs = emptyList(),
                albums = emptyList(),
                playlists = emptyList()
            )
            SearchCategory.PLAYLISTS -> searchResults.copy(
                songs = emptyList(),
                albums = emptyList(),
                artists = emptyList()
            )
        }
    
    val topResult: Any?
        get() = searchResults.topResult
    
    val hasTopResult: Boolean
        get() = topResult != null
    
    val selectedItemsCount: Int
        get() = when (selectedCategory) {
            SearchCategory.ALL -> selectedSongs.size + selectedAlbums.size + selectedArtists.size + selectedPlaylists.size
            SearchCategory.SONGS -> selectedSongs.size
            SearchCategory.ALBUMS -> selectedAlbums.size
            SearchCategory.ARTISTS -> selectedArtists.size
            SearchCategory.PLAYLISTS -> selectedPlaylists.size
        }
    
    val hasSelectedItems: Boolean
        get() = selectedItemsCount > 0
    
    val searchResultsText: String
        get() = when {
            isLoading -> "Searching..."
            isEmpty -> "No results for \"$searchQuery\""
            totalResultsCount == 1 -> "1 result for \"$searchQuery\""
            else -> "$totalResultsCount results for \"$searchQuery\""
        }
    
    val searchDurationText: String
        get() = if (searchDuration > 0) {
            "in ${searchDuration}ms"
        } else ""
    
    val canUseVoiceSearch: Boolean
        get() = isVoiceSearchAvailable && !isVoiceSearchActive
    
    val shouldShowSuggestions: Boolean
        get() = searchQuery.isBlank() && !isSearchActive
    
    val shouldShowResults: Boolean
        get() = searchQuery.isNotBlank() && hasSearched
    
    val shouldShowInitialState: Boolean
        get() = searchQuery.isBlank() && !hasSearched && !isSearchActive
    
    // Helper methods
    fun isSongSelected(songId: Long): Boolean = selectedSongs.contains(songId)
    fun isAlbumSelected(albumId: Long): Boolean = selectedAlbums.contains(albumId)
    fun isArtistSelected(artistId: Long): Boolean = selectedArtists.contains(artistId)
    fun isPlaylistSelected(playlistId: Long): Boolean = selectedPlaylists.contains(playlistId)
    fun isSongFavorite(song: Song): Boolean = favoriteSongs.contains(song.id)
    fun isCurrentlyPlaying(song: Song): Boolean = currentPlayingSong?.id == song.id && isPlaying
    
    fun getCategoryResultCount(category: SearchCategory): Int = when (category) {
        SearchCategory.ALL -> totalResultsCount
        SearchCategory.SONGS -> searchResults.songs.size
        SearchCategory.ALBUMS -> searchResults.albums.size
        SearchCategory.ARTISTS -> searchResults.artists.size
        SearchCategory.PLAYLISTS -> searchResults.playlists.size
    }
}

/**
 * Search results data class
 */
data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val topResult: Any? = null, // Can be Song, Album, Artist, or Playlist
    val query: String = "",
    val totalCount: Int = 0,
    val searchTime: Long = 0L
) {
    val hasResults: Boolean
        get() = songs.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty() || playlists.isNotEmpty()
    
    val isEmpty: Boolean
        get() = !hasResults
}

/**
 * Search categories
 */
enum class SearchCategory(val displayName: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists")
}

/**
 * Search sort orders
 */
enum class SearchSortOrder(val displayName: String) {
    RELEVANCE("Relevance"),
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Date Added"),
    PLAY_COUNT("Play Count"),
    RATING("Rating")
}

/**
 * Search filters
 */
data class SearchFilters(
    val minDuration: Long = 0L, // in milliseconds
    val maxDuration: Long = Long.MAX_VALUE,
    val genres: Set<String> = emptySet(),
    val artists: Set<String> = emptySet(),
    val albums: Set<String> = emptySet(),
    val years: IntRange? = null,
    val minRating: Float = 0f,
    val onlyFavorites: Boolean = false,
    val onlyDownloaded: Boolean = false,
    val fileFormats: Set<String> = emptySet()
) {
    val isActive: Boolean
        get() = minDuration > 0L || maxDuration < Long.MAX_VALUE || 
                genres.isNotEmpty() || artists.isNotEmpty() || albums.isNotEmpty() ||
                years != null || minRating > 0f || onlyFavorites || onlyDownloaded ||
                fileFormats.isNotEmpty()
    
    val activeFilterCount: Int
        get() = listOf(
            minDuration > 0L || maxDuration < Long.MAX_VALUE,
            genres.isNotEmpty(),
            artists.isNotEmpty(),
            albums.isNotEmpty(),
            years != null,
            minRating > 0f,
            onlyFavorites,
            onlyDownloaded,
            fileFormats.isNotEmpty()
        ).count { it }
}

/**
 * Search suggestions
 */
data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
    val icon: String? = null,
    val subtitle: String? = null,
    val resultCount: Int = 0
)

enum class SuggestionType {
    QUERY,
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST,
    GENRE
}

/**
 * Search history item
 */
data class SearchHistoryItem(
    val query: String,
    val timestamp: Long,
    val resultCount: Int,
    val category: SearchCategory = SearchCategory.ALL
)

/**
 * Quick actions for search
 */
data class QuickAction(
    val title: String,
    val icon: String,
    val action: QuickActionType
) {
    companion object {
        fun getDefaultActions(): List<QuickAction> = listOf(
            QuickAction("Shuffle All", "shuffle", QuickActionType.SHUFFLE_ALL),
            QuickAction("Recently Added", "new_releases", QuickActionType.RECENTLY_ADDED),
            QuickAction("Most Played", "trending_up", QuickActionType.MOST_PLAYED),
            QuickAction("Favorites", "favorite", QuickActionType.FAVORITES),
            QuickAction("Voice Search", "mic", QuickActionType.VOICE_SEARCH),
            QuickAction("Scan Library", "search", QuickActionType.SCAN_LIBRARY)
        )
    }
}

enum class QuickActionType {
    SHUFFLE_ALL,
    RECENTLY_ADDED,
    MOST_PLAYED,
    FAVORITES,
    VOICE_SEARCH,
    SCAN_LIBRARY,
    CREATE_PLAYLIST,
    IMPORT_MUSIC
}

/**
 * Search analytics
 */
data class SearchAnalytics(
    val totalSearches: Int = 0,
    val successfulSearches: Int = 0,
    val emptySearches: Int = 0,
    val averageSearchTime: Long = 0L,
    val mostSearchedTerms: List<String> = emptyList(),
    val popularCategories: Map<SearchCategory, Int> = emptyMap(),
    val searchPatterns: Map<String, Int> = emptyMap()
) {
    val successRate: Float
        get() = if (totalSearches > 0) successfulSearches.toFloat() / totalSearches else 0f
    
    val emptySearchRate: Float
        get() = if (totalSearches > 0) emptySearches.toFloat() / totalSearches else 0f
}

/**
 * Extension functions for SearchUiState
 */
fun SearchUiState.copyWithLoading(isLoading: Boolean): SearchUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun SearchUiState.copyWithError(error: String?): SearchUiState {
    return copy(error = error, isLoading = false)
}

fun SearchUiState.copyWithResults(results: SearchResults): SearchUiState {
    return copy(
        searchResults = results,
        isLoading = false,
        error = null,
        hasSearched = true,
        searchDuration = results.searchTime
    )
}

fun SearchUiState.copyWithQuery(query: String, isActive: Boolean = true): SearchUiState {
    return copy(
        searchQuery = query,
        isSearchActive = isActive,
        searchStartTime = if (isActive) System.currentTimeMillis() else 0L
    )
}

fun SearchUiState.copyWithSelection(
    isSelectionMode: Boolean,
    clearSelections: Boolean = false
): SearchUiState {
    return copy(
        isSelectionMode = isSelectionMode,
        selectedSongs = if (clearSelections) emptySet() else selectedSongs,
        selectedAlbums = if (clearSelections) emptySet() else selectedAlbums,
        selectedArtists = if (clearSelections) emptySet() else selectedArtists,
        selectedPlaylists = if (clearSelections) emptySet() else selectedPlaylists
    )
}

/**
 * Preview data for SearchUiState
 */
object SearchUiStatePreview {
    val initial = SearchUiState(
        searchHistory = listOf(
            SearchHistoryItem("rock", System.currentTimeMillis() - 3600000, 25),
            SearchHistoryItem("jazz", System.currentTimeMillis() - 7200000, 12),
            SearchHistoryItem("classical", System.currentTimeMillis() - 10800000, 8)
        ),
        trendingSearches = listOf("pop", "rock", "electronic", "jazz", "classical"),
        recentSearches = listOf("rock music", "jazz piano", "electronic beats")
    )
    
    val searching = SearchUiState(
        searchQuery = "beatles",
        isSearching = true,
        isLoading = true,
        isSearchActive = true
    )
    
    val withResults = SearchUiState(
        searchQuery = "beatles",
        hasSearched = true,
        searchResults = SearchResults(
            songs = listOf(
                Song(
                    id = 1, title = "Hey Jude", artist = "The Beatles",
                    album = "The Beatles 1967-1970", duration = 431000L, playCount = 156,
                    rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                    year = 1968, genre = "Rock"
                ),
                Song(
                    id = 2, title = "Yesterday", artist = "The Beatles",
                    album = "Help!", duration = 125000L, playCount = 89,
                    rating = 4.5f, isFavorite = false, dateAdded = System.currentTimeMillis() - 86400000,
                    year = 1965, genre = "Rock"
                )
            ),
            albums = listOf(
                Album(
                    id = 1, name = "Abbey Road", artist = "The Beatles",
                    songCount = 17, year = 1969, totalDuration = 2874000L
                )
            ),
            artists = listOf(
                Artist(
                    id = 1, name = "The Beatles", songCount = 213,
                    albumCount = 13, totalDuration = 12548000L
                )
            ),
            topResult = Song(
                id = 1, title = "Hey Jude", artist = "The Beatles",
                album = "The Beatles 1967-1970", duration = 431000L, playCount = 156,
                rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 1968, genre = "Rock"
            ),
            query = "beatles",
            totalCount = 25,
            searchTime = 142L
        ),
        searchDuration = 142L
    )
    
    val empty = SearchUiState(
        searchQuery = "xyzabc",
        hasSearched = true,
        searchResults = SearchResults(query = "xyzabc", totalCount = 0)
    )
    
    val error = SearchUiState(
        searchQuery = "test",
        hasSearched = true,
        error = "Search service temporarily unavailable"
    )
    
    val voiceSearch = SearchUiState(
        isVoiceSearchActive = true,
        isVoiceSearchAvailable = true,
        showVoiceSearchDialog = true
    )
    
    val withFilters = withResults.copy(
        showFilters = true,
        filters = SearchFilters(
            genres = setOf("Rock", "Pop"),
            years = 1960..1980,
            onlyFavorites = true,
            minRating = 4f
        )
    )
}
