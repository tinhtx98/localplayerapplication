package com.tinhtx.localplayerapplication.presentation.screens.library

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Library Screen - Complete state management
 */
data class LibraryUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Current tab
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val tabLoadingStates: Map<LibraryTab, Boolean> = emptyMap(),
    
    // Songs data
    val songs: List<Song> = emptyList(),
    val songsLoading: Boolean = false,
    val songsError: String? = null,
    
    // Albums data
    val albums: List<Album> = emptyList(),
    val albumsLoading: Boolean = false,
    val albumsError: String? = null,
    
    // Artists data
    val artists: List<Artist> = emptyList(),
    val artistsLoading: Boolean = false,
    val artistsError: String? = null,
    
    // Playlists data
    val playlists: List<Playlist> = emptyList(),
    val playlistsLoading: Boolean = false,
    val playlistsError: String? = null,
    
    // Genres data
    val genres: List<String> = emptyList(),
    val genresLoading: Boolean = false,
    val genresError: String? = null,
    
    // Search functionality
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: List<Song> = emptyList(),
    val searchLoading: Boolean = false,
    
    // Filtering and sorting
    val sortOrder: SortOrder = SortOrder.TITLE,
    val sortAscending: Boolean = true,
    val filterByFavorites: Boolean = false,
    val selectedGenre: String? = null,
    val selectedArtist: String? = null,
    val selectedAlbum: String? = null,
    
    // View preferences
    val viewMode: LibraryViewMode = LibraryViewMode.LIST,
    val gridSize: GridSize = GridSize.MEDIUM,
    val showAlbumArt: Boolean = true,
    val showDetails: Boolean = true,
    
    // Selection mode
    val isSelectionMode: Boolean = false,
    val selectedItems: Set<Long> = emptySet(),
    val selectedItemsType: LibraryTab? = null,
    
    // Statistics
    val libraryStats: LibraryStatistics = LibraryStatistics(),
    val statsLoading: Boolean = false,
    
    // Scanning
    val isScanningLibrary: Boolean = false,
    val scanProgress: Float = 0f,
    val scanStatusMessage: String = "",
    val lastScanTime: Long = 0L,
    
    // Navigation
    val selectedSongForPlayback: Song? = null,
    val selectedAlbumForNavigation: Album? = null,
    val selectedArtistForNavigation: Artist? = null,
    
    // Favorites
    val favoriteSongs: Set<Long> = emptySet(),
    
    // Recent activity
    val recentlyAdded: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList()
) {
    
    // Computed properties
    val hasData: Boolean
        get() = when (selectedTab) {
            LibraryTab.SONGS -> songs.isNotEmpty()
            LibraryTab.ALBUMS -> albums.isNotEmpty()
            LibraryTab.ARTISTS -> artists.isNotEmpty()
            LibraryTab.PLAYLISTS -> playlists.isNotEmpty()
            LibraryTab.GENRES -> genres.isNotEmpty()
        }
    
    val isEmpty: Boolean
        get() = !isLoading && !hasData && error == null
    
    val hasError: Boolean
        get() = error != null || when (selectedTab) {
            LibraryTab.SONGS -> songsError != null
            LibraryTab.ALBUMS -> albumsError != null
            LibraryTab.ARTISTS -> artistsError != null
            LibraryTab.PLAYLISTS -> playlistsError != null
            LibraryTab.GENRES -> genresError != null
        }
    
    val currentError: String?
        get() = error ?: when (selectedTab) {
            LibraryTab.SONGS -> songsError
            LibraryTab.ALBUMS -> albumsError
            LibraryTab.ARTISTS -> artistsError
            LibraryTab.PLAYLISTS -> playlistsError
            LibraryTab.GENRES -> genresError
        }
    
    val isCurrentTabLoading: Boolean
        get() = when (selectedTab) {
            LibraryTab.SONGS -> songsLoading
            LibraryTab.ALBUMS -> albumsLoading
            LibraryTab.ARTISTS -> artistsLoading
            LibraryTab.PLAYLISTS -> playlistsLoading
            LibraryTab.GENRES -> genresLoading
        }
    
    val currentTabItemCount: Int
        get() = when (selectedTab) {
            LibraryTab.SONGS -> songs.size
            LibraryTab.ALBUMS -> albums.size
            LibraryTab.ARTISTS -> artists.size
            LibraryTab.PLAYLISTS -> playlists.size
            LibraryTab.GENRES -> genres.size
        }
    
    val filteredSongs: List<Song>
        get() = songs.filter { song ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesFavorites = if (filterByFavorites) {
                favoriteSongs.contains(song.id)
            } else true
            
            val matchesGenre = selectedGenre?.let { genre ->
                song.genre?.equals(genre, ignoreCase = true) == true
            } ?: true
            
            val matchesArtist = selectedArtist?.let { artist ->
                song.artist.equals(artist, ignoreCase = true)
            } ?: true
            
            val matchesAlbum = selectedAlbum?.let { album ->
                song.album.equals(album, ignoreCase = true)
            } ?: true
            
            matchesSearch && matchesFavorites && matchesGenre && matchesArtist && matchesAlbum
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
                SortOrder.ARTIST -> if (sortAscending) filtered.sortedBy { it.artist } else filtered.sortedByDescending { it.artist }
                SortOrder.ALBUM -> if (sortAscending) filtered.sortedBy { it.album } else filtered.sortedByDescending { it.album }
                SortOrder.YEAR -> if (sortAscending) filtered.sortedBy { it.year } else filtered.sortedByDescending { it.year }
                SortOrder.DURATION -> if (sortAscending) filtered.sortedBy { it.duration } else filtered.sortedByDescending { it.duration }
                SortOrder.DATE_ADDED -> if (sortAscending) filtered.sortedBy { it.dateAdded } else filtered.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED -> if (sortAscending) filtered.sortedBy { it.dateModified } else filtered.sortedByDescending { it.dateModified }
                SortOrder.PLAY_COUNT -> if (sortAscending) filtered.sortedBy { it.playCount } else filtered.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> if (sortAscending) filtered.sortedBy { it.lastPlayed } else filtered.sortedByDescending { it.lastPlayed }
            }
        }
    
    val filteredAlbums: List<Album>
        get() = albums.filter { album ->
            if (searchQuery.isBlank()) true else {
                album.name.contains(searchQuery, ignoreCase = true) ||
                album.artist.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                SortOrder.ARTIST -> if (sortAscending) filtered.sortedBy { it.artist } else filtered.sortedByDescending { it.artist }
                SortOrder.YEAR -> if (sortAscending) filtered.sortedBy { it.year } else filtered.sortedByDescending { it.year }
                else -> filtered
            }
        }
    
    val filteredArtists: List<Artist>
        get() = artists.filter { artist ->
            if (searchQuery.isBlank()) true else {
                artist.name.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                else -> filtered
            }
        }
    
    val filteredPlaylists: List<Playlist>
        get() = playlists.filter { playlist ->
            if (searchQuery.isBlank()) true else {
                playlist.name.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                SortOrder.DATE_ADDED -> if (sortAscending) filtered.sortedBy { it.createdAt } else filtered.sortedByDescending { it.createdAt }
                else -> filtered
            }
        }
    
    val selectedItemsCount: Int
        get() = selectedItems.size
    
    val hasSelectedItems: Boolean
        get() = selectedItems.isNotEmpty()
    
    val canSelectAll: Boolean
        get() = when (selectedTab) {
            LibraryTab.SONGS -> filteredSongs.isNotEmpty()
            LibraryTab.ALBUMS -> filteredAlbums.isNotEmpty()
            LibraryTab.ARTISTS -> filteredArtists.isNotEmpty()
            LibraryTab.PLAYLISTS -> filteredPlaylists.isNotEmpty()
            LibraryTab.GENRES -> genres.isNotEmpty()
        }
    
    val allItemsSelected: Boolean
        get() = when (selectedTab) {
            LibraryTab.SONGS -> selectedItems.containsAll(filteredSongs.map { it.id })
            LibraryTab.ALBUMS -> selectedItems.containsAll(filteredAlbums.map { it.id })
            LibraryTab.ARTISTS -> selectedItems.containsAll(filteredArtists.map { it.id })
            LibraryTab.PLAYLISTS -> selectedItems.containsAll(filteredPlaylists.map { it.id })
            LibraryTab.GENRES -> false // Genres don't have IDs
        }
    
    val searchResultsCount: Int
        get() = if (isSearchActive) {
            when (selectedTab) {
                LibraryTab.SONGS -> filteredSongs.size
                LibraryTab.ALBUMS -> filteredAlbums.size
                LibraryTab.ARTISTS -> filteredArtists.size
                LibraryTab.PLAYLISTS -> filteredPlaylists.size
                LibraryTab.GENRES -> genres.filter { it.contains(searchQuery, ignoreCase = true) }.size
            }
        } else 0
    
    // Helper methods
    fun isItemSelected(itemId: Long): Boolean {
        return selectedItems.contains(itemId)
    }
    
    fun isSongFavorite(song: Song): Boolean {
        return favoriteSongs.contains(song.id)
    }
    
    fun getTabDisplayName(tab: LibraryTab): String {
        return when (tab) {
            LibraryTab.SONGS -> "Songs (${songs.size})"
            LibraryTab.ALBUMS -> "Albums (${albums.size})"
            LibraryTab.ARTISTS -> "Artists (${artists.size})"
            LibraryTab.PLAYLISTS -> "Playlists (${playlists.size})"
            LibraryTab.GENRES -> "Genres (${genres.size})"
        }
    }
    
    fun getCurrentTabData(): List<Any> {
        return when (selectedTab) {
            LibraryTab.SONGS -> filteredSongs
            LibraryTab.ALBUMS -> filteredAlbums
            LibraryTab.ARTISTS -> filteredArtists
            LibraryTab.PLAYLISTS -> filteredPlaylists
            LibraryTab.GENRES -> genres.filter { 
                if (searchQuery.isBlank()) true else it.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
}

/**
 * Library tabs enum
 */
enum class LibraryTab(val displayName: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
    GENRES("Genres");
    
    companion object {
        fun fromDisplayName(displayName: String): LibraryTab? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Library view modes
 */
enum class LibraryViewMode {
    LIST,
    GRID,
    DETAILED_LIST;
    
    val displayName: String
        get() = when (this) {
            LIST -> "List"
            GRID -> "Grid"
            DETAILED_LIST -> "Detailed List"
        }
}

/**
 * Library statistics data class
 */
data class LibraryStatistics(
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalArtists: Int = 0,
    val totalPlaylists: Int = 0,
    val totalGenres: Int = 0,
    val totalDuration: Long = 0L,
    val totalSize: Long = 0L,
    val averageSongDuration: Long = 0L,
    val mostPlayedSong: Song? = null,
    val mostPlayedArtist: String = "",
    val mostPlayedGenre: String = "",
    val recentlyAddedCount: Int = 0,
    val favoritesCount: Int = 0
) {
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedTotalSize: String
        get() = formatSize(totalSize)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageSongDuration)
    
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
    
    private fun formatSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = sizeBytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", size, units[unitIndex])
    }
}

/**
 * Extension functions for LibraryUiState
 */
fun LibraryUiState.copyWithLoading(isLoading: Boolean): LibraryUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun LibraryUiState.copyWithError(error: String?): LibraryUiState {
    return copy(error = error, isLoading = false)
}

fun LibraryUiState.copyWithTab(tab: LibraryTab): LibraryUiState {
    return copy(
        selectedTab = tab,
        isSelectionMode = false,
        selectedItems = emptySet(),
        selectedItemsType = null
    )
}

fun LibraryUiState.copyWithSongs(songs: List<Song>, error: String? = null): LibraryUiState {
    return copy(
        songs = songs,
        songsLoading = false,
        songsError = error
    )
}

fun LibraryUiState.copyWithAlbums(albums: List<Album>, error: String? = null): LibraryUiState {
    return copy(
        albums = albums,
        albumsLoading = false,
        albumsError = error
    )
}

fun LibraryUiState.copyWithArtists(artists: List<Artist>, error: String? = null): LibraryUiState {
    return copy(
        artists = artists,
        artistsLoading = false,
        artistsError = error
    )
}

fun LibraryUiState.copyWithPlaylists(playlists: List<Playlist>, error: String? = null): LibraryUiState {
    return copy(
        playlists = playlists,
        playlistsLoading = false,
        playlistsError = error
    )
}

fun LibraryUiState.copyWithSearch(query: String, isActive: Boolean): LibraryUiState {
    return copy(
        searchQuery = query,
        isSearchActive = isActive,
        searchResults = if (query.isBlank()) emptyList() else searchResults
    )
}

fun LibraryUiState.copyWithSelection(
    isSelectionMode: Boolean,
    selectedItems: Set<Long> = if (isSelectionMode) this.selectedItems else emptySet(),
    selectedItemsType: LibraryTab? = if (isSelectionMode) selectedTab else null
): LibraryUiState {
    return copy(
        isSelectionMode = isSelectionMode,
        selectedItems = selectedItems,
        selectedItemsType = selectedItemsType
    )
}

/**
 * Preview data for LibraryUiState
 */
object LibraryUiStatePreview {
    val loading = LibraryUiState(isLoading = true)
    
    val error = LibraryUiState(error = "Failed to load library")
    
    val empty = LibraryUiState()
    
    val withData = LibraryUiState(
        songs = listOf(
            Song(
                id = 1, title = "Sample Song 1", artist = "Artist 1", 
                album = "Album 1", duration = 180000L, path = "/path1",
                playCount = 5, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 2023, genre = "Pop"
            ),
            Song(
                id = 2, title = "Sample Song 2", artist = "Artist 2",
                album = "Album 2", duration = 240000L, path = "/path2",
                playCount = 3, isFavorite = false, dateAdded = System.currentTimeMillis() - 86400000,
                year = 2022, genre = "Rock"
            )
        ),
        albums = listOf(
            Album(
                id = 1, name = "Album 1", artist = "Artist 1",
                songCount = 10, year = 2023, totalDuration = 1800000L
            )
        ),
        artists = listOf(
            Artist(
                id = 1, name = "Artist 1", songCount = 15,
                albumCount = 2, totalDuration = 2700000L
            )
        ),
        libraryStats = LibraryStatistics(
            totalSongs = 250,
            totalAlbums = 45,
            totalArtists = 78,
            totalPlaylists = 12,
            totalGenres = 25,
            totalDuration = 900000000L, // 250 hours
            favoritesCount = 32
        )
    )
    
    val searching = withData.copy(
        searchQuery = "sample",
        isSearchActive = true
    )
    
    val selectionMode = withData.copy(
        isSelectionMode = true,
        selectedItems = setOf(1L, 2L),
        selectedItemsType = LibraryTab.SONGS
    )
}
