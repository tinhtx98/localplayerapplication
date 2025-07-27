package com.tinhtx.localplayerapplication.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.playlist.GetPlaylistsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.favorites.*
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.settings.UpdateSettingsUseCase
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * ViewModel for Library Screen - Complete integration with all music use cases
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    // Music Use Cases
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val getSongsByAlbumUseCase: GetSongsByAlbumUseCase,
    private val getSongsByArtistUseCase: GetSongsByArtistUseCase,
    private val scanMediaLibraryUseCase: ScanMediaLibraryUseCase,
    
    // Playlist Use Cases
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    
    // Favorites Use Cases
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    
    // Settings Use Cases
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    
    // Repositories
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadAllData()
        observeSettings()
        observeFavorites()
    }

    // ====================================================================================
    // TAB MANAGEMENT
    // ====================================================================================

    /**
     * Switch to different library tab
     */
    fun selectTab(tab: LibraryTab) {
        if (_uiState.value.selectedTab == tab) return
        
        _uiState.value = _uiState.value.copyWithTab(tab)
        
        // Load data for selected tab if not loaded
        when (tab) {
            LibraryTab.SONGS -> if (_uiState.value.songs.isEmpty()) loadSongs()
            LibraryTab.ALBUMS -> if (_uiState.value.albums.isEmpty()) loadAlbums()
            LibraryTab.ARTISTS -> if (_uiState.value.artists.isEmpty()) loadArtists()
            LibraryTab.PLAYLISTS -> if (_uiState.value.playlists.isEmpty()) loadPlaylists()
            LibraryTab.GENRES -> if (_uiState.value.genres.isEmpty()) loadGenres()
        }
    }

    // ====================================================================================
    // DATA LOADING
    // ====================================================================================

    /**
     * Load all library data
     */
    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Load data concurrently
                val songsDeferred = async { loadSongs() }
                val albumsDeferred = async { loadAlbums() }
                val artistsDeferred = async { loadArtists() }
                val playlistsDeferred = async { loadPlaylists() }
                val genresDeferred = async { loadGenres() }
                val statsDeferred = async { loadLibraryStats() }

                awaitAll(songsDeferred, albumsDeferred, artistsDeferred, playlistsDeferred, genresDeferred, statsDeferred)
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load library: ${e.message}")
            }
        }
    }

    /**
     * Load songs data
     */
    private suspend fun loadSongs() {
        try {
            _uiState.value = _uiState.value.copy(songsLoading = true, songsError = null)
            
            getAllSongsUseCase.getAllSongs().first().let { songs ->
                _uiState.value = _uiState.value.copyWithSongs(songs)
                
                // Load recent activity
                loadRecentActivity()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithSongs(emptyList(), "Failed to load songs: ${e.message}")
        }
    }

    /**
     * Load albums data
     */
    private suspend fun loadAlbums() {
        try {
            _uiState.value = _uiState.value.copy(albumsLoading = true, albumsError = null)
            
            getAllAlbumsUseCase.getAllAlbums().first().let { albums ->
                _uiState.value = _uiState.value.copyWithAlbums(albums)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithAlbums(emptyList(), "Failed to load albums: ${e.message}")
        }
    }

    /**
     * Load artists data
     */
    private suspend fun loadArtists() {
        try {
            _uiState.value = _uiState.value.copy(artistsLoading = true, artistsError = null)
            
            getAllArtistsUseCase.getAllArtists().first().let { artists ->
                _uiState.value = _uiState.value.copyWithArtists(artists)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithArtists(emptyList(), "Failed to load artists: ${e.message}")
        }
    }

    /**
     * Load playlists data
     */
    private suspend fun loadPlaylists() {
        try {
            _uiState.value = _uiState.value.copy(playlistsLoading = true, playlistsError = null)
            
            getPlaylistsUseCase.getAllPlaylists().first().let { playlists ->
                _uiState.value = _uiState.value.copyWithPlaylists(playlists)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithPlaylists(emptyList(), "Failed to load playlists: ${e.message}")
        }
    }

    /**
     * Load genres data
     */
    private suspend fun loadGenres() {
        try {
            _uiState.value = _uiState.value.copy(genresLoading = true, genresError = null)
            
            val genres = musicRepository.getAllGenres()
            _uiState.value = _uiState.value.copy(
                genres = genres,
                genresLoading = false,
                genresError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                genres = emptyList(),
                genresLoading = false,
                genresError = "Failed to load genres: ${e.message}"
            )
        }
    }

    /**
     * Load library statistics
     */
    private suspend fun loadLibraryStats() {
        try {
            _uiState.value = _uiState.value.copy(statsLoading = true)
            
            val totalSongs = musicRepository.getSongCount()
            val totalAlbums = musicRepository.getAlbumCount()
            val totalArtists = musicRepository.getArtistCount()
            val totalDuration = musicRepository.getTotalDuration()
            val totalSize = musicRepository.getTotalSize()
            val genres = musicRepository.getAllGenres()
            val playlists = playlistRepository.getAllPlaylists().first()
            
            val stats = LibraryStatistics(
                totalSongs = totalSongs,
                totalAlbums = totalAlbums,
                totalArtists = totalArtists,
                totalPlaylists = playlists.size,
                totalGenres = genres.size,
                totalDuration = totalDuration,
                totalSize = totalSize,
                averageSongDuration = if (totalSongs > 0) totalDuration / totalSongs else 0L,
                favoritesCount = _uiState.value.favoriteSongs.size
            )
            
            _uiState.value = _uiState.value.copy(
                libraryStats = stats,
                statsLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(statsLoading = false)
        }
    }

    /**
     * Load recent activity
     */
    private suspend fun loadRecentActivity() {
        try {
            val recentlyAdded = musicRepository.getRecentlyAddedSongs(20)
            val recentlyPlayed = musicRepository.getRecentlyPlayedSongs(20)
            val mostPlayed = musicRepository.getMostPlayedSongs(20)
            
            _uiState.value = _uiState.value.copy(
                recentlyAdded = recentlyAdded,
                recentlyPlayed = recentlyPlayed,
                mostPlayed = mostPlayed
            )
        } catch (e: Exception) {
            // Silent failure for recent activity
        }
    }

    // ====================================================================================
    // SEARCH FUNCTIONALITY
    // ====================================================================================

    /**
     * Perform search with debouncing
     */
    fun searchLibrary(query: String) {
        searchJob?.cancel()
        
        _uiState.value = _uiState.value.copyWithSearch(query, query.isNotBlank())
        
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchLoading = false
            )
            return
        }
        
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Debounce delay
            
            try {
                _uiState.value = _uiState.value.copy(searchLoading = true)
                
                searchSongsUseCase.searchSongs(query).first().let { results ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        searchLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    searchLoading = false
                )
            }
        }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copyWithSearch("", false)
    }

    // ====================================================================================
    // SORTING AND FILTERING
    // ====================================================================================

    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SortOrder, ascending: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sortOrder = sortOrder,
                sortAscending = ascending
            )
            
            // Save to preferences
            updateSettingsUseCase.updateSortOrder(sortOrder)
        }
    }

    /**
     * Toggle sort direction
     */
    fun toggleSortDirection() {
        _uiState.value = _uiState.value.copy(
            sortAscending = !_uiState.value.sortAscending
        )
    }

    /**
     * Filter by favorites
     */
    fun filterByFavorites(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(filterByFavorites = enabled)
    }

    /**
     * Filter by genre
     */
    fun filterByGenre(genre: String?) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre)
    }

    /**
     * Filter by artist
     */
    fun filterByArtist(artist: String?) {
        _uiState.value = _uiState.value.copy(selectedArtist = artist)
    }

    /**
     * Filter by album
     */
    fun filterByAlbum(album: String?) {
        _uiState.value = _uiState.value.copy(selectedAlbum = album)
    }

    /**
     * Clear all filters
     */
    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            filterByFavorites = false,
            selectedGenre = null,
            selectedArtist = null,
            selectedAlbum = null
        )
    }

    // ====================================================================================
    // VIEW MODE MANAGEMENT
    // ====================================================================================

    /**
     * Update view mode
     */
    fun updateViewMode(viewMode: LibraryViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }

    /**
     * Update grid size
     */
    fun updateGridSize(gridSize: GridSize) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(gridSize = gridSize)
            updateSettingsUseCase.updateGridSize(gridSize)
        }
    }

    /**
     * Toggle album art display
     */
    fun toggleAlbumArt() {
        _uiState.value = _uiState.value.copy(showAlbumArt = !_uiState.value.showAlbumArt)
    }

    /**
     * Toggle details display
     */
    fun toggleDetails() {
        _uiState.value = _uiState.value.copy(showDetails = !_uiState.value.showDetails)
    }

    // ====================================================================================
    // SELECTION MODE
    // ====================================================================================

    /**
     * Toggle selection mode
     */
    fun toggleSelectionMode() {
        val isSelectionMode = !_uiState.value.isSelectionMode
        _uiState.value = _uiState.value.copyWithSelection(isSelectionMode)
    }

    /**
     * Select/deselect item
     */
    fun toggleItemSelection(itemId: Long) {
        val currentSelection = _uiState.value.selectedItems.toMutableSet()
        
        if (currentSelection.contains(itemId)) {
            currentSelection.remove(itemId)
        } else {
            currentSelection.add(itemId)
        }
        
        _uiState.value = _uiState.value.copy(selectedItems = currentSelection)
        
        // Exit selection mode if no items selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false)
        }
    }

    /**
     * Select all items in current tab
     */
    fun selectAllItems() {
        val allItemIds = when (_uiState.value.selectedTab) {
            LibraryTab.SONGS -> _uiState.value.filteredSongs.map { it.id }
            LibraryTab.ALBUMS -> _uiState.value.filteredAlbums.map { it.id }
            LibraryTab.ARTISTS -> _uiState.value.filteredArtists.map { it.id }
            LibraryTab.PLAYLISTS -> _uiState.value.filteredPlaylists.map { it.id }
            LibraryTab.GENRES -> emptyList() // Genres don't have IDs
        }
        
        _uiState.value = _uiState.value.copy(selectedItems = allItemIds.toSet())
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copyWithSelection(false)
    }

    // ====================================================================================
    // FAVORITES MANAGEMENT
    // ====================================================================================

    /**
     * Toggle favorite status for song
     */
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                val isFavorite = _uiState.value.isSongFavorite(song)
                
                if (isFavorite) {
                    removeFromFavoritesUseCase.execute(song.id).fold(
                        onSuccess = {
                            // Favorites will be updated via observeFavorites()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copyWithError("Failed to remove from favorites: ${error.message}")
                        }
                    )
                } else {
                    addToFavoritesUseCase.execute(song.id).fold(
                        onSuccess = {
                            // Favorites will be updated via observeFavorites()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copyWithError("Failed to add to favorites: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Add selected songs to favorites
     */
    fun addSelectedToFavorites() {
        if (_uiState.value.selectedTab != LibraryTab.SONGS) return
        
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedItems.toList()
                
                selectedSongIds.forEach { songId ->
                    addToFavoritesUseCase.execute(songId)
                }
                
                clearSelection()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to add to favorites: ${e.message}")
            }
        }
    }

    /**
     * Remove selected songs from favorites
     */
    fun removeSelectedFromFavorites() {
        if (_uiState.value.selectedTab != LibraryTab.SONGS) return
        
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedItems.toList()
                
                selectedSongIds.forEach { songId ->
                    removeFromFavoritesUseCase.execute(songId)
                }
                
                clearSelection()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to remove from favorites: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // LIBRARY SCANNING
    // ====================================================================================

    /**
     * Scan media library
     */
    fun scanLibrary() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isScanningLibrary = true,
                    scanProgress = 0f,
                    scanStatusMessage = "Starting scan..."
                )
                
                scanMediaLibraryUseCase.scanMediaLibrary().fold(
                    onSuccess = { result ->
                        _uiState.value = _uiState.value.copy(
                            isScanningLibrary = false,
                            scanProgress = 1f,
                            scanStatusMessage = "Scan completed",
                            lastScanTime = System.currentTimeMillis()
                        )
                        
                        // Reload data after scan
                        loadAllData()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isScanningLibrary = false,
                            scanProgress = 0f,
                            scanStatusMessage = "Scan failed"
                        )
                        _uiState.value = _uiState.value.copyWithError("Failed to scan library: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanningLibrary = false,
                    scanProgress = 0f,
                    scanStatusMessage = "Scan failed"
                )
                _uiState.value = _uiState.value.copyWithError("Unexpected error during scan: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // NAVIGATION ACTIONS
    // ====================================================================================

    /**
     * Play song
     */
    fun playSong(song: Song) {
        _uiState.value = _uiState.value.copy(selectedSongForPlayback = song)
    }

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
     * Clear navigation selections
     */
    fun clearNavigationSelections() {
        _uiState.value = _uiState.value.copy(
            selectedSongForPlayback = null,
            selectedAlbumForNavigation = null,
            selectedArtistForNavigation = null
        )
    }

    // ====================================================================================
    // REFRESH AND ERROR HANDLING
    // ====================================================================================

    /**
     * Refresh current tab data
     */
    fun refreshCurrentTab() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                when (_uiState.value.selectedTab) {
                    LibraryTab.SONGS -> loadSongs()
                    LibraryTab.ALBUMS -> loadAlbums()
                    LibraryTab.ARTISTS -> loadArtists()
                    LibraryTab.PLAYLISTS -> loadPlaylists()
                    LibraryTab.GENRES -> loadGenres()
                }
                
                loadLibraryStats()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to refresh: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refreshAll() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                loadAllData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to refresh library: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ====================================================================================
    // BATCH OPERATIONS
    // ====================================================================================

    /**
     * Delete selected songs
     */
    fun deleteSelectedSongs() {
        if (_uiState.value.selectedTab != LibraryTab.SONGS) return
        
        viewModelScope.launch {
            try {
                val selectedSongs = _uiState.value.selectedItems.mapNotNull { songId ->
                    _uiState.value.songs.find { it.id == songId }
                }
                
                musicRepository.deleteSongs(selectedSongs)
                
                // Reload songs after deletion
                loadSongs()
                clearSelection()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to delete songs: ${e.message}")
            }
        }
    }

    /**
     * Add selected songs to playlist
     */
    fun addSelectedToPlaylist(playlistId: Long) {
        if (_uiState.value.selectedTab != LibraryTab.SONGS) return
        
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedItems.toList()
                
                selectedSongIds.forEach { songId ->
                    playlistRepository.addSongToPlaylist(playlistId, songId)
                }
                
                clearSelection()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to add to playlist: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================================

    private fun observeSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase.getAppSettings()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Failed to load settings: ${e.message}")
                }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        sortOrder = settings.sortOrder,
                        gridSize = settings.gridSize
                    )
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
                    
                    // Update stats
                    val currentStats = _uiState.value.libraryStats
                    _uiState.value = _uiState.value.copy(
                        libraryStats = currentStats.copy(favoritesCount = favoriteIds.size)
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        refreshJob?.cancel()
    }

    // ====================================================================================
    // PUBLIC HELPER METHODS
    // ====================================================================================

    /**
     * Get songs by album
     */
    fun getSongsByAlbum(albumName: String): List<Song> {
        return _uiState.value.songs.filter { it.album == albumName }
    }

    /**
     * Get songs by artist
     */
    fun getSongsByArtist(artistName: String): List<Song> {
        return _uiState.value.songs.filter { it.artist == artistName }
    }

    /**
     * Get songs by genre
     */
    fun getSongsByGenre(genre: String): List<Song> {
        return _uiState.value.songs.filter { it.genre == genre }
    }

    /**
     * Get current tab data count
     */
    fun getCurrentTabDataCount(): Int {
        return _uiState.value.currentTabItemCount
    }

    /**
     * Check if can perform batch operations
     */
    fun canPerformBatchOperations(): Boolean {
        return _uiState.value.hasSelectedItems && _uiState.value.selectedTab == LibraryTab.SONGS
    }
}
