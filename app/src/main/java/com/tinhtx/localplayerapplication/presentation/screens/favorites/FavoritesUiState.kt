package com.tinhtx.localplayerapplication.presentation.screens.favorites

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Favorites Screen - Complete state management
 */
data class FavoritesUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Favorites data
    val favoriteSongs: List<Song> = emptyList(),
    val favoriteAlbums: List<Album> = emptyList(),
    val favoriteArtists: List<Artist> = emptyList(),
    val favoritePlaylists: List<Playlist> = emptyList(),
    
    // Loading states for each tab
    val songsLoading: Boolean = false,
    val albumsLoading: Boolean = false,
    val artistsLoading: Boolean = false,
    val playlistsLoading: Boolean = false,
    
    // Error states for each tab
    val songsError: String? = null,
    val albumsError: String? = null,
    val artistsError: String? = null,
    val playlistsError: String? = null,
    
    // Current tab
    val selectedTab: FavoritesTab = FavoritesTab.SONGS,
    
    // Playback state
    val currentPlayingSong: Song? = null,
    val isPlaying: Boolean = false,
    
    // Selection mode
    val isSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    val selectedAlbums: Set<Long> = emptySet(),
    val selectedArtists: Set<Long> = emptySet(),
    val selectedPlaylists: Set<Long> = emptySet(),
    
    // Search and filtering
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_ADDED,
    val sortAscending: Boolean = false, // Default descending for recently added
    val viewMode: FavoritesViewMode = FavoritesViewMode.LIST,
    
    // Statistics
    val favoritesStats: FavoritesStatistics = FavoritesStatistics(),
    
    // Navigation
    val selectedSongForPlayback: Song? = null,
    val selectedAlbumForNavigation: Album? = null,
    val selectedArtistForNavigation: Artist? = null,
    val selectedPlaylistForNavigation: Playlist? = null,
    
    // UI states
    val showSortDialog: Boolean = false,
    val showRemoveConfirmDialog: Boolean = false,
    val lastRemovedItem: Any? = null,
    val showUndoSnackbar: Boolean = false
) {
    
    // Computed properties
    val hasData: Boolean
        get() = favoriteSongs.isNotEmpty() || favoriteAlbums.isNotEmpty() || 
                favoriteArtists.isNotEmpty() || favoritePlaylists.isNotEmpty()
    
    val isEmpty: Boolean
        get() = !isLoading && !hasData
    
    val hasError: Boolean
        get() = error != null || songsError != null || albumsError != null || 
                artistsError != null || playlistsError != null
    
    val currentError: String?
        get() = error ?: songsError ?: albumsError ?: artistsError ?: playlistsError
    
    val isCurrentTabLoading: Boolean
        get() = when (selectedTab) {
            FavoritesTab.SONGS -> songsLoading
            FavoritesTab.ALBUMS -> albumsLoading
            FavoritesTab.ARTISTS -> artistsLoading
            FavoritesTab.PLAYLISTS -> playlistsLoading
        }
    
    val currentTabItemCount: Int
        get() = when (selectedTab) {
            FavoritesTab.SONGS -> filteredSongs.size
            FavoritesTab.ALBUMS -> filteredAlbums.size
            FavoritesTab.ARTISTS -> filteredArtists.size
            FavoritesTab.PLAYLISTS -> filteredPlaylists.size
        }
    
    val filteredSongs: List<Song>
        get() = favoriteSongs.filter { song ->
            if (searchQuery.isBlank()) true else {
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
                SortOrder.ARTIST -> if (sortAscending) filtered.sortedBy { it.artist } else filtered.sortedByDescending { it.artist }
                SortOrder.ALBUM -> if (sortAscending) filtered.sortedBy { it.album } else filtered.sortedByDescending { it.album }
                SortOrder.DURATION -> if (sortAscending) filtered.sortedBy { it.duration } else filtered.sortedByDescending { it.duration }
                SortOrder.DATE_ADDED -> if (sortAscending) filtered.sortedBy { it.dateAdded } else filtered.sortedByDescending { it.dateAdded }
                SortOrder.PLAY_COUNT -> if (sortAscending) filtered.sortedBy { it.playCount } else filtered.sortedByDescending { it.playCount }
                else -> filtered.sortedByDescending { it.dateAdded } // Default by date added desc
            }
        }
    
    val filteredAlbums: List<Album>
        get() = favoriteAlbums.filter { album ->
            if (searchQuery.isBlank()) true else {
                album.name.contains(searchQuery, ignoreCase = true) ||
                album.artist.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                SortOrder.ARTIST -> if (sortAscending) filtered.sortedBy { it.artist } else filtered.sortedByDescending { it.artist }
                SortOrder.YEAR -> if (sortAscending) filtered.sortedBy { it.year } else filtered.sortedByDescending { it.year }
                else -> filtered.sortedByDescending { it.year } // Default by year desc
            }
        }
    
    val filteredArtists: List<Artist>
        get() = favoriteArtists.filter { artist ->
            if (searchQuery.isBlank()) true else {
                artist.name.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                else -> filtered.sortedBy { it.name } // Default alphabetically
            }
        }
    
    val filteredPlaylists: List<Playlist>
        get() = favoritePlaylists.filter { playlist ->
            if (searchQuery.isBlank()) true else {
                playlist.name.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                SortOrder.DATE_ADDED -> if (sortAscending) filtered.sortedBy { it.createdAt } else filtered.sortedByDescending { it.createdAt }
                else -> filtered.sortedByDescending { it.createdAt } // Default by creation date desc
            }
        }
    
    val selectedItemsCount: Int
        get() = when (selectedTab) {
            FavoritesTab.SONGS -> selectedSongs.size
            FavoritesTab.ALBUMS -> selectedAlbums.size
            FavoritesTab.ARTISTS -> selectedArtists.size
            FavoritesTab.PLAYLISTS -> selectedPlaylists.size
        }
    
    val hasSelectedItems: Boolean
        get() = selectedItemsCount > 0
    
    val allItemsSelected: Boolean
        get() = when (selectedTab) {
            FavoritesTab.SONGS -> selectedSongs.containsAll(filteredSongs.map { it.id })
            FavoritesTab.ALBUMS -> selectedAlbums.containsAll(filteredAlbums.map { it.id })
            FavoritesTab.ARTISTS -> selectedArtists.containsAll(filteredArtists.map { it.id })
            FavoritesTab.PLAYLISTS -> selectedPlaylists.containsAll(filteredPlaylists.map { it.id })
        }
    
    val canSelectAll: Boolean
        get() = currentTabItemCount > 0 && !allItemsSelected
    
    val searchResultsCount: Int
        get() = when (selectedTab) {
            FavoritesTab.SONGS -> filteredSongs.size
            FavoritesTab.ALBUMS -> filteredAlbums.size
            FavoritesTab.ARTISTS -> filteredArtists.size
            FavoritesTab.PLAYLISTS -> filteredPlaylists.size
        }
    
    // Helper methods
    fun isSongSelected(songId: Long): Boolean = selectedSongs.contains(songId)
    fun isAlbumSelected(albumId: Long): Boolean = selectedAlbums.contains(albumId)
    fun isArtistSelected(artistId: Long): Boolean = selectedArtists.contains(artistId)
    fun isPlaylistSelected(playlistId: Long): Boolean = selectedPlaylists.contains(playlistId)
    
    fun isCurrentlyPlaying(song: Song): Boolean {
        return currentPlayingSong?.id == song.id && isPlaying
    }
}

/**
 * Favorites tabs enum
 */
enum class FavoritesTab(val displayName: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists")
}

/**
 * Favorites view modes
 */
enum class FavoritesViewMode {
    LIST,
    GRID,
    COMPACT;
    
    val displayName: String
        get() = when (this) {
            LIST -> "List"
            GRID -> "Grid"
            COMPACT -> "Compact"
        }
}

/**
 * Favorites statistics data class
 */
data class FavoritesStatistics(
    val totalFavoriteSongs: Int = 0,
    val totalFavoriteAlbums: Int = 0,
    val totalFavoriteArtists: Int = 0,
    val totalFavoritePlaylists: Int = 0,
    val favoritesTotalDuration: Long = 0L,
    val averageFavoriteDuration: Long = 0L,
    val mostPlayedFavorite: Song? = null,
    val recentlyAddedFavorites: List<Song> = emptyList(),
    val favoriteGenres: List<String> = emptyList(),
    val favoritesPlayCount: Int = 0
) {
    val totalFavorites: Int
        get() = totalFavoriteSongs + totalFavoriteAlbums + totalFavoriteArtists + totalFavoritePlaylists
    
    val formattedTotalDuration: String
        get() = formatDuration(favoritesTotalDuration)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageFavoriteDuration)
    
    val topGenre: String
        get() = favoriteGenres.firstOrNull() ?: "Mixed"
    
    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0m"
        
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
}
