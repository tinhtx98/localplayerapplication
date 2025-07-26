package com.tinhtx.localplayerapplication.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import com.tinhtx.localplayerapplication.domain.usecase.playlist.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val scanMediaLibraryUseCase: ScanMediaLibraryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(AppConstants.SortOrder.TITLE_ASC)
    private val _viewMode = MutableStateFlow(AppConstants.ViewMode.LIST)
    private val _gridSize = MutableStateFlow(AppConstants.GridSize.MEDIUM)
    private val _activeFilters = MutableStateFlow<List<LibraryFilter>>(emptyList())
    private val _isSearching = MutableStateFlow(false)

    init {
        observeUserInputs()
    }

    private fun observeUserInputs() {
        viewModelScope.launch {
            combine(
                _selectedTabIndex,
                _searchQuery,
                _sortOrder,
                _viewMode,
                _gridSize,
                _activeFilters,
                _isSearching
            ) { tabIndex, query, sortOrder, viewMode, gridSize, filters, isSearching ->
                _uiState.value = _uiState.value.copy(
                    selectedTabIndex = tabIndex,
                    searchQuery = query,
                    sortOrder = sortOrder,
                    viewMode = viewMode,
                    gridSize = gridSize,
                    activeFilters = filters,
                    isSearching = isSearching
                )
                
                // Apply search and filters when they change
                applySearchAndFilters()
            }.collect()
        }
    }

    fun loadLibraryData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                combine(
                    getAllSongsUseCase(),
                    getAllAlbumsUseCase(),
                    getAllArtistsUseCase(),
                    getPlaylistsUseCase(),
                    getFavoritesUseCase()
                ) { songs, albums, artists, playlists, favorites ->
                    LibraryData(songs, albums, artists, playlists, favorites)
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load library"
                    )
                }.collect { libraryData ->
                    val stats = calculateLibraryStats(libraryData)
                    val tabCounts = calculateTabCounts(libraryData)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        songs = applySorting(libraryData.songs, LibraryTab.SONGS),
                        albums = applySorting(libraryData.albums, LibraryTab.ALBUMS),
                        artists = applySorting(libraryData.artists, LibraryTab.ARTISTS),
                        playlists = applySorting(libraryData.playlists, LibraryTab.PLAYLISTS),
                        favorites = libraryData.favorites,
                        libraryStats = stats,
                        tabCounts = tabCounts,
                        isEmpty = libraryData.songs.isEmpty() && 
                                 libraryData.albums.isEmpty() && 
                                 libraryData.artists.isEmpty()
                    )
                    
                    applySearchAndFilters()
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load library data"
                )
            }
        }
    }

    private fun applySearchAndFilters() {
        val currentState = _uiState.value
        val query = _searchQuery.value
        val filters = _activeFilters.value

        if (query.isBlank() && filters.isEmpty()) {
            _uiState.value = currentState.copy(
                filteredSongs = emptyList(),
                filteredAlbums = emptyList(),
                filteredArtists = emptyList(),
                filteredPlaylists = emptyList()
            )
            return
        }

        // Apply search
        val searchFilteredSongs = if (query.isBlank()) currentState.songs else {
            currentState.songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.displayArtist.contains(query, ignoreCase = true) ||
                song.displayAlbum.contains(query, ignoreCase = true)
            }
        }

        val searchFilteredAlbums = if (query.isBlank()) currentState.albums else {
            currentState.albums.filter { album ->
                album.displayName.contains(query, ignoreCase = true) ||
                album.displayArtist.contains(query, ignoreCase = true)
            }
        }

        val searchFilteredArtists = if (query.isBlank()) currentState.artists else {
            currentState.artists.filter { artist ->
                artist.displayName.contains(query, ignoreCase = true)
            }
        }

        val searchFilteredPlaylists = if (query.isBlank()) currentState.playlists else {
            currentState.playlists.filter { playlist ->
                playlist.name.contains(query, ignoreCase = true)
            }
        }

        // Apply filters (if any)
        val finalFilteredSongs = applyFilters(searchFilteredSongs, filters)
        val finalFilteredAlbums = applyFilters(searchFilteredAlbums, filters)
        val finalFilteredArtists = applyFilters(searchFilteredArtists, filters)
        val finalFilteredPlaylists = applyFilters(searchFilteredPlaylists, filters)

        _uiState.value = currentState.copy(
            filteredSongs = finalFilteredSongs,
            filteredAlbums = finalFilteredAlbums,
            filteredArtists = finalFilteredArtists,
            filteredPlaylists = finalFilteredPlaylists
        )
    }

    private fun <T> applyFilters(items: List<T>, filters: List<LibraryFilter>): List<T> {
        if (filters.isEmpty()) return items
        
        return items.filter { item ->
            filters.all { filter ->
                when (filter.type) {
                    LibraryFilterType.YEAR -> {
                        when (item) {
                            is Song -> item.year in filter.yearRange
                            is Album -> item.year in filter.yearRange
                            else -> true
                        }
                    }
                    LibraryFilterType.GENRE -> {
                        when (item) {
                            is Song -> item.genre.equals(filter.value, ignoreCase = true)
                            else -> true
                        }
                    }
                    LibraryFilterType.FAVORITES_ONLY -> {
                        when (item) {
                            is Song -> item.isFavorite
                            else -> true
                        }
                    }
                    LibraryFilterType.DURATION -> {
                        when (item) {
                            is Song -> item.duration in filter.durationRange
                            else -> true
                        }
                    }
                    else -> true
                }
            }
        }
    }

    private fun <T> applySorting(items: List<T>, tab: LibraryTab): List<T> {
        val sortOrder = _sortOrder.value
        
        return when (tab) {
            LibraryTab.SONGS -> {
                val songs = items as List<Song>
                when (sortOrder) {
                    AppConstants.SortOrder.TITLE_ASC -> songs.sortedBy { it.title.lowercase() }
                    AppConstants.SortOrder.TITLE_DESC -> songs.sortedByDescending { it.title.lowercase() }
                    AppConstants.SortOrder.ARTIST_ASC -> songs.sortedBy { it.displayArtist.lowercase() }
                    AppConstants.SortOrder.ARTIST_DESC -> songs.sortedByDescending { it.displayArtist.lowercase() }
                    AppConstants.SortOrder.ALBUM_ASC -> songs.sortedBy { it.displayAlbum.lowercase() }
                    AppConstants.SortOrder.ALBUM_DESC -> songs.sortedByDescending { it.displayAlbum.lowercase() }
                    AppConstants.SortOrder.DURATION_ASC -> songs.sortedBy { it.duration }
                    AppConstants.SortOrder.DURATION_DESC -> songs.sortedByDescending { it.duration }
                    AppConstants.SortOrder.DATE_ADDED_ASC -> songs.sortedBy { it.dateAdded }
                    AppConstants.SortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
                } as List<T>
            }
            LibraryTab.ALBUMS -> {
                val albums = items as List<Album>
                when (sortOrder) {
                    AppConstants.SortOrder.TITLE_ASC -> albums.sortedBy { it.displayName.lowercase() }
                    AppConstants.SortOrder.TITLE_DESC -> albums.sortedByDescending { it.displayName.lowercase() }
                    AppConstants.SortOrder.ARTIST_ASC -> albums.sortedBy { it.displayArtist.lowercase() }
                    AppConstants.SortOrder.ARTIST_DESC -> albums.sortedByDescending { it.displayArtist.lowercase() }
                    AppConstants.SortOrder.DATE_ADDED_ASC -> albums.sortedBy { it.year }
                    AppConstants.SortOrder.DATE_ADDED_DESC -> albums.sortedByDescending { it.year }
                    else -> albums.sortedBy { it.displayName.lowercase() }
                } as List<T>
            }
            LibraryTab.ARTISTS -> {
                val artists = items as List<Artist>
                when (sortOrder) {
                    AppConstants.SortOrder.TITLE_ASC -> artists.sortedBy { it.displayName.lowercase() }
                    AppConstants.SortOrder.TITLE_DESC -> artists.sortedByDescending { it.displayName.lowercase() }
                    else -> artists.sortedBy { it.displayName.lowercase() }
                } as List<T>
            }
            LibraryTab.PLAYLISTS -> {
                val playlists = items as List<Playlist>
                when (sortOrder) {
                    AppConstants.SortOrder.TITLE_ASC -> playlists.sortedBy { it.name.lowercase() }
                    AppConstants.SortOrder.TITLE_DESC -> playlists.sortedByDescending { it.name.lowercase() }
                    AppConstants.SortOrder.DATE_ADDED_ASC -> playlists.sortedBy { it.dateCreated }
                    AppConstants.SortOrder.DATE_ADDED_DESC -> playlists.sortedByDescending { it.dateCreated }
                    else -> playlists.sortedBy { it.name.lowercase() }
                } as List<T>
            }
        }
    }

    private fun calculateLibraryStats(libraryData: LibraryData): LibraryStats {
        val totalDuration = libraryData.songs.sumOf { it.duration }
        
        return LibraryStats(
            totalSongs = libraryData.songs.size,
            totalAlbums = libraryData.albums.size,
            totalArtists = libraryData.artists.size,
            totalPlaylists = libraryData.playlists.size,
            totalFavorites = libraryData.favorites.size,
            totalDuration = formatDuration(totalDuration)
        )
    }

    private fun calculateTabCounts(libraryData: LibraryData): Map<LibraryTab, Int> {
        return mapOf(
            LibraryTab.SONGS to libraryData.songs.size,
            LibraryTab.ALBUMS to libraryData.albums.size,
            LibraryTab.ARTISTS to libraryData.artists.size,
            LibraryTab.PLAYLISTS to libraryData.playlists.size
        )
    }

    fun selectTab(tabIndex: Int) {
        _selectedTabIndex.value = tabIndex
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
        // Re-apply sorting to current data
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            songs = applySorting(currentState.songs, LibraryTab.SONGS),
            albums = applySorting(currentState.albums, LibraryTab.ALBUMS),
            artists = applySorting(currentState.artists, LibraryTab.ARTISTS),
            playlists = applySorting(currentState.playlists, LibraryTab.PLAYLISTS)
        )
        applySearchAndFilters()
    }

    fun updateViewMode(viewMode: AppConstants.ViewMode) {
        _viewMode.value = viewMode
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == AppConstants.ViewMode.LIST) {
            AppConstants.ViewMode.GRID
        } else {
            AppConstants.ViewMode.LIST
        }
    }

    fun updateGridSize(gridSize: AppConstants.GridSize) {
        _gridSize.value = gridSize
    }

    fun updateFilters(filters: List<LibraryFilter>) {
        _activeFilters.value = filters
    }

    fun removeFilter(filter: LibraryFilter) {
        _activeFilters.value = _activeFilters.value - filter
    }

    fun clearAllFilters() {
        _activeFilters.value = emptyList()
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song, "library_screen")
            } catch (exception) {
                android.util.Log.e("LibraryViewModel", "Failed to play song", exception)
            }
        }
    }

    fun playAlbum(album: Album) {
        viewModelScope.launch {
            try {
                // Get songs from album and play first one
                android.util.Log.d("LibraryViewModel", "Playing album: ${album.displayName}")
            } catch (exception) {
                android.util.Log.e("LibraryViewModel", "Failed to play album", exception)
            }
        }
    }

    fun playArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                // Get songs from artist and play shuffled
                android.util.Log.d("LibraryViewModel", "Playing artist: ${artist.displayName}")
            } catch (exception) {
                android.util.Log.e("LibraryViewModel", "Failed to play artist", exception)
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
                android.util.Log.e("LibraryViewModel", "Failed to toggle favorite", exception)
            }
        }
    }

    fun showAddToPlaylistDialog(song: Song) {
        android.util.Log.d("LibraryViewModel", "Show add to playlist dialog for: ${song.title}")
    }

    fun showCreatePlaylistDialog() {
        android.util.Log.d("LibraryViewModel", "Show create playlist dialog")
    }

    fun retryLoadLibrary() {
        loadLibraryData()
    }

    fun startMediaScan() {
        viewModelScope.launch {
            try {
                scanMediaLibraryUseCase.performFullScan()
                loadLibraryData()
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to scan media library: ${exception.message}"
                )
            }
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val hours = durationMs / (1000 * 60 * 60)
        val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
        
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}

private data class LibraryData(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val playlists: List<Playlist>,
    val favorites: List<Song>
)

data class LibraryStats(
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalArtists: Int = 0,
    val totalPlaylists: Int = 0,
    val totalFavorites: Int = 0,
    val totalDuration: String = ""
)

data class LibraryFilter(
    val type: LibraryFilterType,
    val value: String = "",
    val yearRange: IntRange = IntRange.EMPTY,
    val durationRange: LongRange = LongRange.EMPTY
)

enum class LibraryFilterType {
    YEAR, GENRE, FAVORITES_ONLY, DURATION, ARTIST, ALBUM
}
