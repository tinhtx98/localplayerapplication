package com.tinhtx.localplayerapplication.presentation.screens.album

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
import javax.inject.Inject

/**
 * ViewModel for Album Screen - Complete integration with music use cases
 */
@HiltViewModel
class AlbumViewModel @Inject constructor(
    // Music Use Cases
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getSongsByAlbumUseCase: GetSongsByAlbumUseCase,
    
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

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    private var currentAlbumId: Long = -1

    init {
        observeSettings()
        observeFavorites()
    }

    // ====================================================================================
    // ALBUM LOADING
    // ====================================================================================

    /**
     * Load album by ID
     */
    fun loadAlbum(albumId: Long) {
        currentAlbumId = albumId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Load album and songs concurrently
                val albumDeferred = async { loadAlbumData(albumId) }
                val songsDeferred = async { loadSongsByAlbum(albumId) }
                
                awaitAll(albumDeferred, songsDeferred)
                
                // Calculate statistics
                calculateAlbumStats()
                
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load album: ${e.message}")
            }
        }
    }

    /**
     * Load album by name and artist
     */
    fun loadAlbum(albumName: String, artistName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Find album by name and artist
                val albums = getAllAlbumsUseCase.getAllAlbums().first()
                val album = albums.find { 
                    it.name.equals(albumName, ignoreCase = true) && 
                    it.artist.equals(artistName, ignoreCase = true) 
                }
                
                if (album != null) {
                    currentAlbumId = album.id
                    loadAlbum(album.id)
                } else {
                    _uiState.value = _uiState.value.copyWithError("Album not found")
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to find album: ${e.message}")
            }
        }
    }

    private suspend fun loadAlbumData(albumId: Long) {
        try {
            _uiState.value = _uiState.value.copy(albumLoading = true, albumError = null)
            
            val albums = getAllAlbumsUseCase.getAllAlbums().first()
            val album = albums.find { it.id == albumId }
            
            _uiState.value = _uiState.value.copyWithAlbum(album)
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithAlbum(null, "Failed to load album: ${e.message}")
        }
    }

    private suspend fun loadSongsByAlbum(albumId: Long) {
        try {
            _uiState.value = _uiState.value.copy(songsLoading = true, songsError = null)
            
            val album = _uiState.value.album
            if (album != null) {
                getSongsByAlbumUseCase.getSongsByAlbum(album.name).first().let { songs ->
                    _uiState.value = _uiState.value.copyWithSongs(songs)
                }
            }
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copyWithSongs(emptyList(), "Failed to load songs: ${e.message}")
        }
    }

    // ====================================================================================
    // PLAYBACK CONTROLS
    // ====================================================================================

    /**
     * Play specific song from album
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
     * Play entire album from beginning
     */
    fun playAlbum() {
        val songs = _uiState.value.sortedSongs
        if (songs.isNotEmpty()) {
            playSong(songs.first())
        }
    }

    /**
     * Shuffle play album
     */
    fun shufflePlayAlbum() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            _uiState.value = _uiState.value.copy(shuffleMode = true)
            playSong(shuffledSongs.first())
        }
    }

    /**
     * Play from specific track number
     */
    fun playFromTrackNumber(trackNumber: Int) {
        val songs = _uiState.value.sortedSongs
        val song = songs.find { it.trackNumber == trackNumber } ?: songs.getOrNull(trackNumber - 1)
        song?.let { playSong(it) }
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
     * Toggle album favorite status
     */
    fun toggleAlbumFavorite() {
        viewModelScope.launch {
            try {
                val isAlbumFavorite = _uiState.value.isAlbumFavorite
                val songs = _uiState.value.songs
                
                if (isAlbumFavorite) {
                    // Remove all songs from favorites
                    songs.forEach { song ->
                        if (_uiState.value.isSongFavorite(song)) {
                            removeFromFavoritesUseCase.execute(song.id)
                        }
                    }
                } else {
                    // Add all songs to favorites
                    songs.forEach { song ->
                        if (!_uiState.value.isSongFavorite(song)) {
                            addToFavoritesUseCase.execute(song.id)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update album favorites: ${e.message}")
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
        
        // Exit selection mode if no songs selected
        if (currentSelection.isEmpty()) {
            _uiState.value = _uiState.value.copyWithSelection(false)
        }
    }

    /**
     * Select all songs
     */
    fun selectAllSongs() {
        val allSongIds = _uiState.value.songs.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedSongs = allSongIds)
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copyWithSelection(false)
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
    fun updateViewMode(viewMode: AlbumViewMode) {
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
     * Remove selected songs from favorites
     */
    fun removeSelectedFromFavorites() {
        viewModelScope.launch {
            try {
                val selectedSongIds = _uiState.value.selectedSongs.toList()
                
                selectedSongIds.forEach { songId ->
                    removeFromFavoritesUseCase.execute(songId)
                }
                
                clearSelection()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to remove from favorites: ${e.message}")
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
     * Navigate to artist
     */
    fun navigateToArtist() {
        val artistName = _uiState.value.album?.artist
        _uiState.value = _uiState.value.copy(navigateToArtist = artistName)
    }

    /**
     * Clear navigation states
     */
    fun clearNavigationStates() {
        _uiState.value = _uiState.value.copy(
            selectedSongForPlayback = null,
            navigateToArtist = null
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

    // ====================================================================================
    // REFRESH AND ERROR HANDLING
    // ====================================================================================

    /**
     * Refresh album data
     */
    fun refreshAlbum() {
        if (currentAlbumId != -1L) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                try {
                    loadAlbum(currentAlbumId)
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
        _uiState.value = _uiState.value.copy(error = null, albumError = null, songsError = null)
    }

    // ====================================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================================

    private fun calculateAlbumStats() {
        val songs = _uiState.value.songs
        val album = _uiState.value.album
        
        if (songs.isNotEmpty() && album != null) {
            val stats = AlbumStatistics(
                totalSongs = songs.size,
                totalDuration = songs.sumOf { it.duration },
                totalPlays = songs.sumOf { it.playCount },
                averageSongDuration = songs.map { it.duration }.average().toLong(),
                mostPlayedSong = songs.maxByOrNull { it.playCount },
                leastPlayedSong = songs.minByOrNull { it.playCount },
                totalFavorites = songs.count { _uiState.value.isSongFavorite(it) },
                albumRating = songs.map { it.rating }.average().toFloat(),
                releaseYear = album.year,
                genre = songs.firstOrNull()?.genre ?: ""
            )
            
            _uiState.value = _uiState.value.copy(albumStats = stats)
        }
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
                    
                    // Update album favorite status
                    val songs = _uiState.value.songs
                    val isAlbumFavorite = songs.isNotEmpty() && songs.all { favoriteIds.contains(it.id) }
                    _uiState.value = _uiState.value.copy(isAlbumFavorite = isAlbumFavorite)
                    
                    // Recalculate stats
                    calculateAlbumStats()
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up if needed
    }

    // ====================================================================================
    // PUBLIC HELPER METHODS
    // ====================================================================================

    /**
     * Get current album
     */
    fun getCurrentAlbum(): Album? {
        return _uiState.value.album
    }

    /**
     * Get songs in current album
     */
    fun getAlbumSongs(): List<Song> {
        return _uiState.value.sortedSongs
    }

    /**
     * Check if song is currently playing
     */
    fun isSongPlaying(song: Song): Boolean {
        return _uiState.value.isCurrentlyPlaying(song)
    }
}
