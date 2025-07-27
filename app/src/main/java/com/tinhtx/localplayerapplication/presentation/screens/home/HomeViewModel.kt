package com.tinhtx.localplayerapplication.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.favorites.GetFavoritesUseCase
import com.tinhtx.localplayerapplication.domain.usecase.favorites.AddToFavoritesUseCase
import com.tinhtx.localplayerapplication.domain.usecase.favorites.RemoveFromFavoritesUseCase
import com.tinhtx.localplayerapplication.domain.usecase.music.*
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import com.tinhtx.localplayerapplication.domain.usecase.playlist.GetPlaylistsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val scanMediaLibraryUseCase: ScanMediaLibraryUseCase,
    private val playMusicUseCase: PlaySongUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load all data concurrently
                val allSongsFlow = getAllSongsUseCase()
                val allAlbumsFlow = getAllAlbumsUseCase()
                val allArtistsFlow = getAllArtistsUseCase()
                val playlistsFlow = getPlaylistsUseCase()
                val favoritesFlow = getFavoritesUseCase()

                combine(
                    allSongsFlow,
                    allAlbumsFlow,
                    allArtistsFlow,
                    playlistsFlow,
                    favoritesFlow
                ) { songs, albums, artists, playlists, favorites ->
                    processHomeData(songs, albums, artists, playlists, favorites)
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }.collect { processedData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        recentlyPlayed = processedData.recentlyPlayed,
                        mostPlayed = processedData.mostPlayed,
                        recentAlbums = processedData.recentAlbums,
                        featuredArtists = processedData.featuredArtists,
                        quickAccessPlaylists = processedData.quickAccessPlaylists,
                        recommendedSongs = processedData.recommendedSongs,
                        totalSongs = processedData.totalSongs,
                        totalAlbums = processedData.totalAlbums,
                        totalArtists = processedData.totalArtists,
                        totalDuration = processedData.totalDuration,
                        isEmpty = processedData.isEmpty
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load music data"
                )
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    userProfile = profile
                )
            }
        }
    }

    private fun processHomeData(
        songs: List<Song>,
        albums: List<Album>,
        artists: List<Artist>,
        playlists: List<Playlist>,
        favorites: List<Song>
    ): ProcessedHomeData {
        val isEmpty = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()
        
        if (isEmpty) {
            return ProcessedHomeData(isEmpty = true)
        }

        // Recently played songs (mock data based on lastPlayed)
        val recentlyPlayed = songs
            .filter { it.lastPlayed != null }
            .sortedByDescending { it.lastPlayed ?: 0L }
            .take(10)

        // Most played songs
        val mostPlayed = songs
            .filter { it.playCount > 0 }
            .sortedByDescending { it.playCount }
            .take(10)

        // Recent albums (based on dateAdded)
        val recentAlbums = albums
            .sortedByDescending { album ->
                songs.filter { it.albumId == album.mediaStoreId }
                    .maxOfOrNull { it.dateAdded } ?: 0L
            }
            .take(8)

        // Featured artists (based on play count)
        val featuredArtists = artists
            .sortedByDescending { artist ->
                songs.filter { it.artistId == artist.mediaStoreId }
                    .sumOf { it.playCount }
            }
            .take(6)

        // Quick access playlists
        val quickAccessPlaylists = playlists.take(6)

        // Recommended songs (simple algorithm based on favorites and play count)
        val recommendedSongs = generateRecommendations(songs, favorites)

        val totalDuration = songs.sumOf { it.duration }

        return ProcessedHomeData(
            recentlyPlayed = recentlyPlayed,
            mostPlayed = mostPlayed,
            recentAlbums = recentAlbums,
            featuredArtists = featuredArtists,
            quickAccessPlaylists = quickAccessPlaylists,
            recommendedSongs = recommendedSongs,
            totalSongs = songs.size,
            totalAlbums = albums.size,
            totalArtists = artists.size,
            totalDuration = totalDuration,
            isEmpty = false
        )
    }

    private fun generateRecommendations(songs: List<Song>, favorites: List<Song>): List<Song> {
        if (favorites.isEmpty()) return emptyList()

        // Get artists from favorite songs
        val favoriteArtists = favorites.map { it.artistId }.distinct()
        
        // Find songs from same artists that are not in favorites
        val recommendations = songs
            .filter { song ->
                favoriteArtists.contains(song.artistId) && 
                !favorites.any { fav -> fav.id == song.id }
            }
            .sortedByDescending { it.playCount }
            .take(8)

        return recommendations
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playMusicUseCase(song, "home_screen")
            } catch (exception: Exception) {
                // Handle play error
            }
        }
    }

    fun playAlbum(album: Album) {
        viewModelScope.launch {
            try {
                // Get songs from album and play first one
                // This would typically involve getting songs by album ID
                // For now, we'll just log the action
                android.util.Log.d("HomeViewModel", "Playing album: ${album.displayName}")
            } catch (exception: Exception) {
                // Handle play error
            }
        }
    }

    fun playArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                // Get songs from artist and play shuffled
                android.util.Log.d("HomeViewModel", "Playing artist: ${artist.displayName}")
            } catch (exception: Exception) {
                // Handle play error
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
            } catch (exception: Exception) {
                // Handle error
            }
        }
    }

    fun showAddToPlaylistDialog(song: Song) {
        // This would typically show a dialog to select playlist
        android.util.Log.d("HomeViewModel", "Show add to playlist dialog for: ${song.title}")
    }

    fun onProfileClick() {
        // Navigate to profile or show profile menu
        android.util.Log.d("HomeViewModel", "Profile clicked")
    }

    fun onNotificationClick() {
        // Show notifications or navigate to notifications screen
        android.util.Log.d("HomeViewModel", "Notifications clicked")
    }

    fun retryLoadData() {
        loadHomeData()
    }

    fun refreshData() {
        loadHomeData()
    }

    fun startMediaScan() {
        viewModelScope.launch {
            try {
                scanMediaLibraryUseCase.performFullScan()
                loadHomeData()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to scan media library: ${exception.message}"
                )
            }
        }
    }
}

private data class ProcessedHomeData(
    val recentlyPlayed: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val recentAlbums: List<Album> = emptyList(),
    val featuredArtists: List<Artist> = emptyList(),
    val quickAccessPlaylists: List<Playlist> = emptyList(),
    val recommendedSongs: List<Song> = emptyList(),
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalArtists: Int = 0,
    val totalDuration: Long = 0L,
    val isEmpty: Boolean = false
)
