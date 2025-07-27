package com.tinhtx.localplayerapplication.presentation.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for Favorites Screen - Complete integration with favorites use cases
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    // Favorites Use Cases
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    
    // Player Use Cases
    private val playSongUseCase: PlaySongUseCase,
    
    // Settings Use Case
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    
    // Repository
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var undoJob: Job? = null

    init {
        loadAllFavorites()
        observeSettings()
    }

    // ====================================================================================
    // FAVORITES LOADING
    // ====================================================================================

    /**
     * Load all favorites data
     */
    fun loadAllFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load all favorites concurrently
                val songsDeferred = async { loadFavoriteSongs() }
                val albumsDeferred = async { loadFavoriteAlbums() }
                val artistsDeferred = async { loadFavoriteArtists() }
                val playlistsDeferred = async { loadFavoritePlaylists() }
                val statsDeferred = async { calculateFavoritesStats() }
                
                awaitAll(songsDeferred, albumsDeferred, artistsDeferred, playlistsDeferred, statsDeferred)
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load favorites: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadFavoriteSongs() {
        try {
            _uiState.value = _uiState.value.copy(songsLoading = true, songsError = null)
            
            getFavoritesUseCase.getFavoriteSongs().first().let { songs ->
                _uiState.value = _uiState.value.copy(
                    favoriteSongs = songs,
                    songsLoading = false,
                    songsError = null
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                favoriteSongs = emptyList(),
                songsLoading = false,
                songsError = "Failed to load favorite songs: ${e.message}"
            )
        }
    }

    private suspend fun loadFavoriteAlbums() {
        try {
            _uiState.value = _uiState.value.copy(albumsLoading = true, albumsError = null)
            
            // Derive from favorite songs (simplified)
            val favoriteSongs = getFavoritesUseCase.getFavoriteSongs().first()
            val favoriteAlbums = favoriteSongs
                .groupBy { it.album }
                .map { (albumName, songs) ->
                    Album(
                        id = albumName.hashCode().toLong(),
                        name = albumName,
                        artist = songs.first().artist,
                        songCount = songs.size,
                        year = songs.maxOfOrNull { it.year } ?: 0,
                        totalDuration = songs.sumOf { it.duration }
                    )
                }
            
            _uiState.value = _uiState.value.copy(
                favoriteAlbums = favoriteAlbums,
                albumsLoading = false,
                albumsError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                favoriteAlbums = emptyList(),
                albumsLoading = false,
                albumsError = "Failed to load favorite albums: ${e.message}"
            )
        }
    }

    private suspend fun loadFavoriteArtists() {
        try {
            _uiState.value = _uiState.value.copy(artistsLoading = true, artistsError = null)
            
            // Derive from favorite songs (simplified)
            val favoriteSongs = getFavoritesUseCase.getFavoriteSongs().first()
            val favoriteArtists = favoriteSongs
                .groupBy { it.artist }
                .map { (artistName, songs) ->
                    Artist(
                        id = artistName.hashCode().toLong(),
                        name = artistName,
                        songCount = songs.size,
                        albumCount = songs.distinctBy { it.album }.size,
                        totalDuration = songs.sumOf { it.duration }
                    )
                }
            
            _uiState.value = _uiState.value.copy(
                favoriteArtists = favoriteArtists,
                artistsLoading = false,
                artistsError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                favoriteArtists = emptyList(),
                artistsLoading = false,
                artistsError = "Failed to load favorite artists: ${e.message}"
            )
        }
    }

    private suspend fun loadFavoritePlaylists() {
        try {
            _uiState.value = _uiState.value.copy(playlistsLoading = true, playlistsError = null)
            
            // Get all playlists (could be filtered by favorite status)
            val playlists = playlistRepository.getAllPlaylists().first()
            
            _uiState.value = _uiState.value.copy(
                favoritePlaylists = playlists,
                playlistsLoading = false,
                playlistsError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                favoritePlaylists = emptyList(),
                playlistsLoading = false,
                playlistsError = "Failed to load favorite playlists: ${e.message}"
            )
        }
    }

    // ====================================================================================
    // TAB MANAGEMENT
    // ====================================================================================

    /**
     * Switch to different favorites tab
     */
    fun selectTab(tab: FavoritesTab) {
        if (_uiState.value.selectedTab == tab) return
        
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            isSelectionMode = false,
            selectedSongs = emptySet(),
            selectedAlbums = emptySet(),
            selectedArtists = emptySet(),
            selectedPlaylists = emptySet()
        )
    }

    // ====================================================================================
    // PLAYBACK CONTROLS
    // ====================================================================================

    /**
     * Play specific song
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
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to play song: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Play all favorite songs
     */
    fun playAllFavorites() {
        val songs = _uiState.value.filteredSongs
        if (songs.isNotEmpty()) {
            playSong(songs.first())
        }
    }

    /**
     * Shuffle play favorites
     */
    fun shufflePlayFavorites() {
        val songs = _uiState.value.favoriteSongs
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            playSong(shuffledSongs.first())
        }
    }

    /**
     * Play selected songs
     */
    fun playSelectedSongs() {
        val selectedSongs = _uiState.value.favoriteSongs.filter { 
            _uiState.value.selectedSongs.contains(it.id) 
        }
        
        if (selectedSongs.isNotEmpty()) {
            playSong(selectedSongs.first())
            clearSelection()
        }
    }

    // ====================================================================================
    // FAVORITES MANAGEMENT
    // ====================================================================================

    /**
     * Remove song from favorites
     */
    fun removeSongFromFavorites(song: Song) {
        viewModelScope.launch {
            try {
                removeFromFavoritesUseCase.execute(song.id).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            lastRemovedItem = song,
                            showUndoSnackbar = true
                        )
                        
                        // Auto-hide undo snackbar after 5 seconds
                        undoJob?.cancel()
                        undoJob = launch {
                            delay(5000)
                            _uiState.value = _uiState.value.copy(showUndoSnackbar = false)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to remove from favorites: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Remove selected items from favorites
     */
    fun removeSelectedFromFavorites() {
        viewModelScope.launch {
            try {
                when (_uiState.value.selectedTab) {
                    FavoritesTab.SONGS -> {
                        val selectedSongIds = _uiState.value.selectedSongs.toList()
                        selectedSongIds.forEach { songId ->
                            removeFromFavoritesUseCase.execute(songId)
                        }
                    }
                    FavoritesTab.ALBUMS -> {
                        // Remove all songs from selected albums
                        val selectedAlbumIds = _uiState.value.selectedAlbums
                        val selectedAlbums = _uiState.value.favoriteAlbums.filter { 
                            selectedAlbumIds.contains(it.id) 
                        }
                        
                        selectedAlbums.forEach { album ->
                            val albumSongs = _uiState.value.favoriteSongs.filter { it.album == album.name }
                            albumSongs.forEach { song ->
                                removeFromFavoritesUseCase.execute(song.id)
                            }
                        }
                    }
                    FavoritesTab.ARTISTS -> {
                        // Remove all songs from selected artists
                        val selectedArtistIds = _uiState.value.selectedArtists
                        val selectedArtists = _uiState.value.favoriteArtists.filter { 
                            selectedArtistIds.contains(it.id) 
                        }
                        
                        selectedArtists.forEach { artist ->
                            val artistSongs = _uiState.value.favoriteSongs.filter { it.artist == artist.name }
                            artistSongs.forEach { song ->
                                removeFromFavoritesUseCase.execute(song.id)
                            }
                        }
                    }
                    FavoritesTab.PLAYLISTS -> {
                        // TODO: Implement playlist unfavoriting
                    }
                }
                
                clearSelection()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove from favorites: ${e.message}"
                )
            }
        }
    }

    /**
     * Undo last remove action
     */
    fun undoRemoveFromFavorites() {
        val lastRemoved = _uiState.value.lastRemovedItem
        if (lastRemoved is Song) {
            viewModelScope.launch {
                try {
                    addToFavoritesUseCase.execute(lastRemoved.id).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                lastRemovedItem = null,
                                showUndoSnackbar = false
                            )
                            undoJob?.cancel()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to undo: ${error.message}"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Dismiss undo snackbar
     */
    fun dismissUndoSnackbar() {
        undoJob?.cancel()
        _uiState.value = _uiState.value.copy(
            showUndoSnackbar = false,
            lastRemovedItem = null
        )
    }

    // ====================================================================================
    // SELECTION MODE
    // ====================================================================================

    /**
     * Toggle selection mode
     */
    fun toggleSelectionMode() {
        val isSelectionMode = !_uiState.value.isSelectionMode
        _uiState.value = _uiState.value.copy(
            isSelectionMode = isSelectionMode,
            selectedSongs = if (isSelectionMode) _uiState.value.selectedSongs else emptySet(),
            selectedAlbums = if (isSelectionMode) _uiState.value.selectedAlbums else emptySet(),
            selectedArtists = if (isSelectionMode) _uiState.value.selectedArtists else emptySet(),
            selectedPlaylists = if (isSelectionMode) _uiState.value.selectedPlaylists else emptySet()
        )
    }

    /**
     * Toggle song selection
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
            _uiState.value = _uiState.value.copy(isSelectionMode = false)
        }
    }

    /**
     * Toggle album selection
     */
    fun toggleAlbumSelection(albumId: Long) {
        val currentSelection = _uiState.value.selectedAlbums.toMutableSet()
        
        if (currentSelection.contains(albumId)) {
            currentSelection.remove(albumId)
        } else {
            currentSelection.add(albumId)
        }
        
        _uiState.value = _uiState.value.copy(selectedAlbums = currentSelection)
        
        // Exit selection mode if no items selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copy(isSelectionMode = false)
        }
    }

    /**
     * Toggle artist selection
     */
    fun toggleArtistSelection(artistId: Long) {
        val currentSelection = _uiState.value.selectedArtists.toMutableSet()
        
        if (currentSelection.contains(artistId)) {
            currentSelection.remove(artistId)
        } else {
            currentSelection.add(artistId)
        }
        
        _uiState.value = _uiState.value.copy(selectedArtists = currentSelection)
        
        // Exit selection mode if no items selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copy(isSelectionMode = false)
        }
    }

    /**
     * Toggle playlist selection
     */
    fun togglePlaylistSelection(playlistId: Long) {
        val currentSelection = _uiState.value.selectedPlaylists.toMutableSet()
        
        if (currentSelection.contains(playlistId)) {
            currentSelection.remove(playlistId)
        } else {
            currentSelection.add(playlistId)
        }
        
        _uiState.value = _uiState.value.copy(selectedPlaylists = currentSelection)
        
        // Exit selection mode if no items selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copy(isSelectionMode = false)
        }
    }

    /**
     * Select all items in current tab
     */
    fun selectAllItems() {
        when (_uiState.value.selectedTab) {
            FavoritesTab.SONGS -> {
                val allSongIds = _uiState.value.filteredSongs.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedSongs = allSongIds)
            }
            FavoritesTab.ALBUMS -> {
                val allAlbumIds = _uiState.value.filteredAlbums.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedAlbums = allAlbumIds)
            }
            FavoritesTab.ARTISTS -> {
                val allArtistIds = _uiState.value.filteredArtists.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedArtists = allArtistIds)
            }
            FavoritesTab.PLAYLISTS -> {
                val allPlaylistIds = _uiState.value.filteredPlaylists.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedPlaylists = allPlaylistIds)
            }
        }
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            selectedSongs = emptySet(),
            selectedAlbums = emptySet(),
            selectedArtists = emptySet(),
            selectedPlaylists = emptySet()
        )
    }

    // ====================================================================================
    // SEARCH FUNCTIONALITY
    // ====================================================================================

    /**
     * Search favorites
     */
    fun searchFavorites(query: String) {
        searchJob?.cancel()
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearchActive = query.isNotBlank()
        )
        
        // Real-time filtering is handled in computed properties
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearchActive = false
        )
    }

    // ====================================================================================
    // SORTING AND VIEW MODES
    // ====================================================================================

    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SortOrder, ascending: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            sortAscending = ascending
        )
    }

    /**
     * Update view mode
     */
    fun updateViewMode(viewMode: FavoritesViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }

    // ====================================================================================
    // NAVIGATION ACTIONS
    // ====================================================================================

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

    // ====================================================================================
    // REFRESH AND ERROR HANDLING
    // ====================================================================================

    /**
     * Refresh favorites
     */
    fun refreshFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                loadAllFavorites()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to refresh favorites: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
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
            playlistsError = null
        )
    }

    // ====================================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================================

    private suspend fun calculateFavoritesStats() {
        val songs = _uiState.value.favoriteSongs
        val albums = _uiState.value.favoriteAlbums
        val artists = _uiState.value.favoriteArtists
        val playlists = _uiState.value.favoritePlaylists
        
        val stats = FavoritesStatistics(
            totalFavoriteSongs = songs.size,
            totalFavoriteAlbums = albums.size,
            totalFavoriteArtists = artists.size,
            totalFavoritePlaylists = playlists.size,
            favoritesTotalDuration = songs.sumOf { it.duration },
            averageFavoriteDuration = if (songs.isNotEmpty()) songs.map { it.duration }.average().toLong() else 0L,
            mostPlayedFavorite = songs.maxByOrNull { it.playCount },
            recentlyAddedFavorites = songs.sortedByDescending { it.dateAdded }.take(10),
            favoriteGenres = songs.mapNotNull { it.genre }.groupingBy { it }.eachCount()
                .toList().sortedByDescending { it.second }.take(5).map { it.first },
            favoritesPlayCount = songs.sumOf { it.playCount }
        )
        
        _uiState.value = _uiState.value.copy(favoritesStats = stats)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase.getAppSettings()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load settings: ${e.message}"
                    )
                }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        sortOrder = settings.sortOrder,
                        viewMode = when (settings.gridSize) {
                            GridSize.SMALL -> FavoritesViewMode.COMPACT
                            GridSize.MEDIUM -> FavoritesViewMode.LIST
                            GridSize.LARGE -> FavoritesViewMode.GRID
                        }
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        undoJob?.cancel()
    }
}
