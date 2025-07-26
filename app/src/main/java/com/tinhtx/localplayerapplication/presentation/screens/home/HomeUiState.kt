package com.tinhtx.localplayerapplication.presentation.screens.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile = UserProfile.default(),
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
    val hasNotifications: Boolean = false,
    val isEmpty: Boolean = false
)