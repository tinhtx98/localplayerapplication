package com.tinhtx.localplayerapplication.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.GetFavoritesUseCase
import com.tinhtx.localplayerapplication.domain.usecase.history.GetPlayHistoryUseCase
import com.tinhtx.localplayerapplication.domain.usecase.music.GetAllSongsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.playlist.GetPlaylistsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

/**
 * ViewModel for Home Screen - Dashboard with music overview
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getPlayHistoryUseCase: GetPlayHistoryUseCase,
    private val getAppSettingsUseCase: GetAppSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
        observeSettings()
    }

    /**
     * Load all home screen data
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copyWithLoading(true)
            
            try {
                // Load data concurrently for better performance
                val recentSongsDeferred = async { getRecentSongs() }
                val favoriteSongsDeferred = async { getFavoriteSongs() }
                val playlistsDeferred = async { getPlaylists() }
                val recentlyPlayedDeferred = async { getRecentlyPlayedSongs() }
                val libraryStatsDeferred = async { getLibraryStats() }

                // Await all results
                val (recentSongs, favoriteSongs, playlists, recentlyPlayed, libraryStats) = awaitAll(
                    recentSongsDeferred,
                    favoriteSongsDeferred,
                    playlistsDeferred,
                    recentlyPlayedDeferred,
                    libraryStatsDeferred
                )

                _uiState.value = _uiState.value.copyWithData(
                    recentSongs = recentSongs.getOrElse { emptyList() },
                    favoriteSongs = favoriteSongs.getOrElse { emptyList() },
                    playlists = playlists.getOrElse { emptyList() },
                    recentlyPlayedSongs = recentlyPlayed.getOrElse { emptyList() },
                    libraryStats = libraryStats.getOrElse { HomeLibraryStats() }
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to load home  ${e.message}")
            }
        }
    }

    /**
     * Refresh home data
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadHomeData()
            _isRefreshing.value = false
        }
    }

    /**
     * Play song from any section
     */
    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                // TODO: Integrate with MediaPlayerService
                // For now, just update recently played
                updateRecentlyPlayed(song)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to play song: ${e.message}")
            }
        }
    }

    /**
     * Play playlist
     */
    fun playPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                // TODO: Integrate with MediaPlayerService
                // Get songs in playlist and start playback
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to play playlist: ${e.message}")
            }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                // TODO: Integrate with UpdateFavoritesUseCase
                // For now, just update local state
                val updatedFavorites = _uiState.value.favoriteSongs.toMutableList()
                if (updatedFavorites.any { it.id == song.id }) {
                    updatedFavorites.removeAll { it.id == song.id }
                } else {
                    updatedFavorites.add(0, song)
                }
                
                _uiState.value = _uiState.value.copy(
                    favoriteSongs = updatedFavorites.take(10) // Keep only top 10 for home
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copyWithError("Failed to update favorite: ${e.message}")
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Search songs from home
     */
    fun searchSongs(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            try {
                // TODO: Integrate with SearchSongsUseCase
                val results = getAllSongsUseCase.getAllSongs().first()
                    .filter { 
                        it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
                    }
                    .take(5) // Limit for home screen

                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Search failed: ${e.message}",
                    isSearching = false
                )
            }
        }
    }

    /**
     * Handle section item click
     */
    fun onSectionItemClick(section: HomeSection, item: Any) {
        when (section) {
            HomeSection.RECENT_SONGS -> {
                if (item is Song) playSong(item)
            }
            HomeSection.FAVORITES -> {
                if (item is Song) playSong(item)
            }
            HomeSection.PLAYLISTS -> {
                if (item is Playlist) playPlaylist(item)
            }
            HomeSection.RECENTLY_PLAYED -> {
                if (item is Song) playSong(item)
            }
        }
    }

    /**
     * Navigate to section view all
     */
    fun navigateToSection(section: HomeSection) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
    }

    /**
     * Get section data for display
     */
    fun getSectionData(section: HomeSection): List<Any> {
        return when (section) {
            HomeSection.RECENT_SONGS -> _uiState.value.recentSongs
            HomeSection.FAVORITES -> _uiState.value.favoriteSongs
            HomeSection.PLAYLISTS -> _uiState.value.playlists
            HomeSection.RECENTLY_PLAYED -> _uiState.value.recentlyPlayedSongs
        }
    }

    // Private helper methods

    private suspend fun getRecentSongs(): Result<List<Song>> {
        return try {
            val songs = getAllSongsUseCase.getRecentlyAddedSongs(10).getOrElse { emptyList() }
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getFavoriteSongs(): Result<List<Song>> {
        return try {
            val favorites = getFavoritesUseCase.getFavoriteSongs().first().take(10)
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getPlaylists(): Result<List<Playlist>> {
        return try {
            val playlists = getPlaylistsUseCase.getAllPlaylists().first().take(6)
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getRecentlyPlayedSongs(): Result<List<Song>> {
        return try {
            val recentlyPlayed = getPlayHistoryUseCase.getRecentlyPlayedSongs(8).getOrElse { emptyList() }
            Result.success(recentlyPlayed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getLibraryStats(): Result<HomeLibraryStats> {
        return try {
            val allSongs = getAllSongsUseCase.getAllSongs().first()
            val favorites = getFavoritesUseCase.getFavoriteSongs().first()
            val playlists = getPlaylistsUseCase.getAllPlaylists().first()
            
            val stats = HomeLibraryStats(
                totalSongs = allSongs.size,
                totalFavorites = favorites.size,
                totalPlaylists = playlists.size,
                totalDuration = allSongs.sumOf { it.duration },
                totalArtists = allSongs.map { it.artist }.distinct().size,
                totalAlbums = allSongs.map { it.album }.distinct().size
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateRecentlyPlayed(song: Song) {
        // Add song to recently played list
        val currentRecentlyPlayed = _uiState.value.recentlyPlayedSongs.toMutableList()
        currentRecentlyPlayed.removeAll { it.id == song.id }
        currentRecentlyPlayed.add(0, song)
        
        _uiState.value = _uiState.value.copy(
            recentlyPlayedSongs = currentRecentlyPlayed.take(8)
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
                        currentTheme = settings.theme,
                        gridSize = settings.gridSize
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
