package com.tinhtx.localplayerapplication.presentation.screens.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.music.*
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
import javax.inject.Inject

/**
 * ViewModel for Artist Screen - Complete integration with music use cases
 */
@HiltViewModel
class ArtistViewModel @Inject constructor(
    // Music Use Cases
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val getSongsByArtistUseCase: GetSongsByArtistUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    
    // Player Use Cases
    private val playSongUseCase: PlaySongUseCase,
    
    // Favorites Use Cases
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    
    // Settings Use Case
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    
    // Repository
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    private var currentArtistId: Long = -1
    private var searchJob: Job? = null

    init {
        observeSettings()
        observeFavorites()
    }

    // ====================================================================================
    // ARTIST LOADING
    // ====================================================================================

    /**
     * Load artist by ID
     */
    fun loadArtist(artistId: Long) {
        currentArtistId = artistId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Load artist, songs, and albums concurrently
                val artistDeferred = async { loadArtistData(artistId) }
                val songsDeferred = async { loadSongsByArtist(artistId) }
                val albumsDeferred = async { loadAlbumsByArtist(artistId) }
                
                awaitAll(artistDeferred, songsDeferred, albumsDeferred)
                
                // Calculate statistics and popular songs
                calculateArtistStats()
                calculatePopularSongs()
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load artist: ${e.message}")
            }
        }
    }

    /**
     * Load artist by name
     */
    fun loadArtist(artistName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Find artist by name
                val artists = getAllArtistsUseCase.getAllArtists().first()
                val artist = artists.find { 
                    it.name.equals(artistName, ignoreCase = true)
                }
                
                if (artist != null) {
                    currentArtistId = artist.id
                    loadArtist(artist.id)
                } else {
                    _uiState.value = _uiState.value.copyWithError("Artist not found")
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to find artist: ${e.message}")
            }
        }
    }

    private suspend fun loadArtistData(artistId: Long) {
        try {
            _uiState.value = _uiState.value.copy(artistLoading = true, artistError = null)
            
            val artists = getAllArtistsUseCase.getAllArtists().first()
            val artist = artists.find { it.id == artistId }
            
            _uiState.value = _uiState.value.copyWithArtist(artist)
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithArtist(null, "Failed to load artist: ${e.message}")
        }
    }

    private suspend fun loadSongsByArtist(artistId: Long) {
        try {
            _uiState.value = _uiState.value.copy(songsLoading = true, songsError = null)
            
            val artist = _uiState.value.artist
            if (artist != null) {
                getSongsByArtistUseCase.getSongsByArtist(artist.name).first().let { songs ->
                    _uiState.value = _uiState.value.copyWithSongs(songs)
                }
            }
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithSongs(emptyList(), "Failed to load songs: ${e.message}")
        }
    }

    private suspend fun loadAlbumsByArtist(artistId: Long) {
        try {
            _uiState.value = _uiState.value.copy(albumsLoading = true, albumsError = null)
            
            val artist = _uiState.value.artist
            if (artist != null) {
                val allAlbums = getAllAlbumsUseCase.getAllAlbums().first()
                val artistAlbums = allAlbums.filter { 
                    it.artist.equals(artist.name, ignoreCase = true) 
                }
                _uiState.value = _uiState.value.copyWithAlbums(artistAlbums)
            }
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithAlbums(emptyList(), "Failed to load albums: ${e.message}")
        }
    }

    // ====================================================================================
    // TAB MANAGEMENT
    // ====================================================================================

    /**
     * Switch to different artist tab
     */
    fun selectTab(tab: ArtistTab) {
        if (_uiState.value.currentTab == tab) return
        
        _uiState.value = _uiState.value.copyWithTab(tab)
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
                        _uiState.value = _uiState.value.copyWithPlayback(song, true)
                        _uiState.value = _uiState.value.copy(selectedSongForPlayback = song)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copyWithError("Failed to play song: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Play all artist songs
     */
    fun playAllSongs() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            playSong(songs.first())
        }
    }

    /**
     * Shuffle play all songs
     */
    fun shufflePlayAllSongs() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            _uiState.value = _uiState.value.copy(shuffleMode = true)
            playSong(shuffledSongs.first())
        }
    }

    /**
     * Play popular songs
     */
    fun playPopularSongs() {
        val popularSongs = _uiState.value.popularSongs
        if (popularSongs.isNotEmpty()) {
            playSong(popularSongs.first())
        }
    }

    /**
     * Play album
     */
    fun playAlbum(album: Album) {
        viewModelScope.launch {
            try {
                val albumSongs = getSongsByArtistUseCase.getSongsByArtist(_uiState.value.artist?.name ?: "").first()
                    .filter { it.album == album.name }
                    .sortedBy { it.trackNumber }
                
                if (albumSongs.isNotEmpty()) {
                    playSong(albumSongs.first())
                    _uiState.value = _uiState.value.copy(selectedAlbumForNavigation = album)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to play album: ${e.message}")
            }
        }
    }

    // ====================================================================================
    // FAVORITES MANAGEMENT  
    // ====================================================================================

    /**
     * Toggle favorite status for song
     */
    fun toggleSongFavorite(song: Song) {
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
     * Toggle artist favorite/follow status
     */
    fun toggleArtistFavorite() {
        viewModelScope.launch {
            try {
                val isArtistFavorite = _uiState.value.isArtistFavorite
                val songs = _uiState.value.songs
                
                if (isArtistFavorite) {
                    // Remove all artist songs from favorites
                    songs.forEach { song ->
                        if (_uiState.value.isSongFavorite(song)) {
                            removeFromFavoritesUseCase.execute(song.id)
                        }
                    }
                } else {
                    // Add popular songs to favorites
                    val popularSongs = _uiState.value.popularSongs.take(10) // Top 10
                    popularSongs.forEach { song ->
                        if (!_uiState.value.isSongFavorite(song)) {
                            addToFavoritesUseCase.execute(song.id)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update artist favorites: ${e.message}")
            }
        }
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
        if (currentSelection.isEmpty() && _uiState.value.selectedAlbums.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false)
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
        if (_uiState.value.selectedSongs.isEmpty() && currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false)
        }
    }

    /**
     * Select all items in current tab
     */
    fun selectAllItems() {
        when (_uiState.value.currentTab) {
            ArtistTab.SONGS -> {
                val allSongIds = _uiState.value.filteredSongs.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedSongs = allSongIds)
            }
            ArtistTab.ALBUMS -> {
                val allAlbumIds = _uiState.value.filteredAlbums.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedAlbums = allAlbumIds)
            }
            ArtistTab.POPULAR -> {
                val allPopularIds = _uiState.value.popularSongs.map { it.id }.toSet()
                _uiState.value = _uiState.value.copy(selectedSongs = allPopularIds)
            }
        }
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copyWithSelection(false)
    }

    // ====================================================================================
    // SEARCH FUNCTIONALITY
    // ====================================================================================

    /**
     * Search in artist content
     */
    fun searchContent(query: String) {
        searchJob?.cancel()
        
        _uiState.value = _uiState.value.copyWithSearch(query, query.isNotBlank())
        
        // No need for API call, filtering is done in computed properties
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copyWithSearch("", false)
    }

    // ====================================================================================
    // SORTING AND VIEW MODES
    // ====================================================================================

    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SortOrder, ascending: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            sortAscending = ascending
        )
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
     * Update view mode
     */
    fun updateViewMode(viewMode: ArtistViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }

    // ====================================================================================
    // BATCH OPERATIONS
    // ====================================================================================

    /**
     * Add selected songs to favorites
     */
    fun addSelectedToFavorites() {
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedSongs.toList()
                
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
     * Add selected songs to playlist
     */
    fun addSelectedToPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedSongs.toList()
                
                selectedSongIds.forEach { songId ->
                    playlistRepository.addSongToPlaylist(playlistId, songId)
                }
                
                clearSelection()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to add to playlist: ${e.message}")
            }
        }
    }

    /**
     * Play selected songs
     */
    fun playSelectedSongs() {
        val selectedSongs = _uiState.value.songs.filter { 
            _uiState.value.selectedSongs.contains(it.id) 
        }
        
        if (selectedSongs.isNotEmpty()) {
            playSong(selectedSongs.first())
            clearSelection()
        }
    }

    // ====================================================================================
    // UI ACTIONS
    // ====================================================================================

    /**
     * Navigate to album
     */
    fun navigateToAlbum(album: Album) {
        _uiState.value = _uiState.value.copy(selectedAlbumForNavigation = album)
    }

    /**
     * Clear navigation states
     */
    fun clearNavigationStates() {
        _uiState.value = _uiState.value.copy(
            selectedSongForPlayback = null,
            selectedAlbumForNavigation = null
        )
    }

    /**
     * Toggle more options dialog
     */
    fun toggleMoreOptions() {
        _uiState.value = _uiState.value.copy(showMoreOptions = !_uiState.value.showMoreOptions)
    }

    /**
     * Toggle sort options dialog
     */
    fun toggleSortOptions() {
        _uiState.value = _uiState.value.copy(showSortOptions = !_uiState.value.showSortOptions)
    }

    /**
     * Toggle add to playlist dialog
     */
    fun toggleAddToPlaylist() {
        _uiState.value = _uiState.value.copy(showAddToPlaylist = !_uiState.value.showAddToPlaylist)
    }

    /**
     * Toggle follow status
     */
    fun toggleFollowArtist() {
        _uiState.value = _uiState.value.copy(isFollowing = !_uiState.value.isFollowing)
    }

    // ====================================================================================
    // REFRESH AND ERROR HANDLING
    // ====================================================================================

    /**
     * Refresh artist data
     */
    fun refreshArtist() {
        if (currentArtistId != -1L) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                try {
                    loadArtist(currentArtistId)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copyWithError("Failed to refresh: ${e.message}")
                } finally {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            error = null, 
            artistError = null, 
            songsError = null, 
            albumsError = null
        )
    }

    // ====================================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================================

    private fun calculateArtistStats() {
        val songs = _uiState.value.songs
        val albums = _uiState.value.albums
        val artist = _uiState.value.artist
        
        if (songs.isNotEmpty() && artist != null) {
            val stats = ArtistStatistics(
                totalSongs = songs.size,
                totalAlbums = albums.size,
                totalDuration = songs.sumOf { it.duration },
                totalPlays = songs.sumOf { it.playCount },
                averageSongDuration = songs.map { it.duration }.average().toLong(),
                mostPlayedSong = songs.maxByOrNull { it.playCount },
                mostPopularAlbum = albums.maxByOrNull { album ->
                    songs.filter { it.album == album.name }.sumOf { it.playCount }
                },
                totalFavorites = songs.count { _uiState.value.isSongFavorite(it) },
                artistRating = songs.map { it.rating }.average().toFloat(),
                firstReleaseYear = songs.minOfOrNull { it.year } ?: 0,
                latestReleaseYear = songs.maxOfOrNull { it.year } ?: 0,
                genres = songs.mapNotNull { it.genre }.distinct().take(5),
                followers = 0, // TODO: Implement follower system
                monthlyListeners = 0 // TODO: Implement listener tracking
            )
            
            _uiState.value = _uiState.value.copy(artistStats = stats)
        }
    }

    private fun calculatePopularSongs() {
        val songs = _uiState.value.songs
        val popularSongs = songs.sortedByDescending { it.playCount }.take(20)
        
        _uiState.value = _uiState.value.copy(
            popularSongs = popularSongs,
            popularSongsLoading = false
        )
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase.getAppSettings()
                .catch { e ->
                    _uiState.value = _uiState.value.copyWithError("Failed to load settings: ${e.message}")
                }
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        sortOrder = settings.sortOrder,
                        sortAscending = true
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
                    
                    // Update artist favorite status
                    val songs = _uiState.value.songs
                    val popularSongs = _uiState.value.popularSongs
                    val isArtistFavorite = popularSongs.isNotEmpty() && 
                        popularSongs.take(5).any { favoriteIds.contains(it.id) }
                    
                    _uiState.value = _uiState.value.copy(isArtistFavorite = isArtistFavorite)
                    
                    // Recalculate stats
                    calculateArtistStats()
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

    // ====================================================================================
    // PUBLIC HELPER METHODS
    // ====================================================================================

    /**
     * Get current artist
     */
    fun getCurrentArtist(): Artist? {
        return _uiState.value.artist
    }

    /**
     * Get songs by current artist
     */
    fun getArtistSongs(): List<Song> {
        return _uiState.value.filteredSongs
    }

    /**
     * Get albums by current artist
     */
    fun getArtistAlbums(): List<Album> {
        return _uiState.value.filteredAlbums
    }

    /**
     * Check if song is currently playing
     */
    fun isSongPlaying(song: Song): Boolean {
        return _uiState.value.isCurrentlyPlaying(song)
    }
}
