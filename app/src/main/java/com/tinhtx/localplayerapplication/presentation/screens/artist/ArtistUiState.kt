package com.tinhtx.localplayerapplication.presentation.screens.artist

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Artist Screen - Complete state management
 */
data class ArtistUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Artist data
    val artist: Artist? = null,
    val artistLoading: Boolean = false,
    val artistError: String? = null,
    
    // Artist songs data
    val songs: List<Song> = emptyList(),
    val songsLoading: Boolean = false,
    val songsError: String? = null,
    
    // Artist albums data
    val albums: List<Album> = emptyList(),
    val albumsLoading: Boolean = false,
    val albumsError: String? = null,
    
    // Popular songs (top tracks)
    val popularSongs: List<Song> = emptyList(),
    val popularSongsLoading: Boolean = false,
    
    // Playback state
    val currentPlayingSong: Song? = null,
    val isPlaying: Boolean = false,
    val shuffleMode: Boolean = false,
    
    // Selection mode
    val isSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    val selectedAlbums: Set<Long> = emptySet(),
    
    // View preferences
    val currentTab: ArtistTab = ArtistTab.SONGS,
    val viewMode: ArtistViewMode = ArtistViewMode.LIST,
    val sortOrder: SortOrder = SortOrder.POPULARITY,
    val sortAscending: Boolean = false, // Default descending for popularity
    
    // Search functionality
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    
    // Favorites
    val favoriteSongs: Set<Long> = emptySet(),
    val isArtistFavorite: Boolean = false,
    
    // Statistics
    val artistStats: ArtistStatistics = ArtistStatistics(),
    
    // Navigation
    val selectedSongForPlayback: Song? = null,
    val selectedAlbumForNavigation: Album? = null,
    
    // Interaction states
    val showMoreOptions: Boolean = false,
    val showSortOptions: Boolean = false,
    val showAddToPlaylist: Boolean = false,
    val isFollowing: Boolean = false
) {
    
    // Computed properties
    val hasData: Boolean
        get() = artist != null && (songs.isNotEmpty() || albums.isNotEmpty())
    
    val isEmpty: Boolean
        get() = !isLoading && artist != null && songs.isEmpty() && albums.isEmpty()
    
    val hasError: Boolean
        get() = error != null || artistError != null || songsError != null || albumsError != null
    
    val currentError: String?
        get() = error ?: artistError ?: songsError ?: albumsError
    
    val isCurrentTabLoading: Boolean
        get() = when (currentTab) {
            ArtistTab.SONGS -> songsLoading
            ArtistTab.ALBUMS -> albumsLoading
            ArtistTab.POPULAR -> popularSongsLoading
        }
    
    val currentTabData: List<Any>
        get() = when (currentTab) {
            ArtistTab.SONGS -> filteredSongs
            ArtistTab.ALBUMS -> filteredAlbums
            ArtistTab.POPULAR -> popularSongs
        }
    
    val filteredSongs: List<Song>
        get() = songs.filter { song ->
            if (searchQuery.isBlank()) true else {
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
                SortOrder.ALBUM -> if (sortAscending) filtered.sortedBy { it.album } else filtered.sortedByDescending { it.album }
                SortOrder.YEAR -> if (sortAscending) filtered.sortedBy { it.year } else filtered.sortedByDescending { it.year }
                SortOrder.DURATION -> if (sortAscending) filtered.sortedBy { it.duration } else filtered.sortedByDescending { it.duration }
                SortOrder.PLAY_COUNT -> if (sortAscending) filtered.sortedBy { it.playCount } else filtered.sortedByDescending { it.playCount }
                SortOrder.POPULARITY -> filtered.sortedByDescending { it.playCount } // Always descending for popularity
                else -> filtered
            }
        }
    
    val filteredAlbums: List<Album>
        get() = albums.filter { album ->
            if (searchQuery.isBlank()) true else {
                album.name.contains(searchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (sortOrder) {
                SortOrder.TITLE -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                SortOrder.YEAR -> if (sortAscending) filtered.sortedBy { it.year } else filtered.sortedByDescending { it.year }
                else -> filtered.sortedByDescending { it.year } // Default by year desc
            }
        }
    
    val selectedSongsCount: Int
        get() = selectedSongs.size
    
    val selectedAlbumsCount: Int
        get() = selectedAlbums.size
    
    val hasSelectedItems: Boolean
        get() = selectedSongs.isNotEmpty() || selectedAlbums.isNotEmpty()
    
    val totalSelectedCount: Int
        get() = selectedSongsCount + selectedAlbumsCount
    
    val allSongsSelected: Boolean
        get() = when (currentTab) {
            ArtistTab.SONGS -> selectedSongs.containsAll(filteredSongs.map { it.id })
            ArtistTab.POPULAR -> selectedSongs.containsAll(popularSongs.map { it.id })
            ArtistTab.ALBUMS -> selectedAlbums.containsAll(filteredAlbums.map { it.id })
        }
    
    val canSelectAll: Boolean
        get() = when (currentTab) {
            ArtistTab.SONGS -> filteredSongs.isNotEmpty() && !allSongsSelected
            ArtistTab.POPULAR -> popularSongs.isNotEmpty() && !allSongsSelected
            ArtistTab.ALBUMS -> filteredAlbums.isNotEmpty() && !allSongsSelected
        }
    
    val totalDuration: Long
        get() = songs.sumOf { it.duration }
    
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val searchResultsCount: Int
        get() = when (currentTab) {
            ArtistTab.SONGS -> filteredSongs.size
            ArtistTab.ALBUMS -> filteredAlbums.size
            ArtistTab.POPULAR -> popularSongs.filter { song ->
                if (searchQuery.isBlank()) true else {
                    song.title.contains(searchQuery, ignoreCase = true)
                }
            }.size
        }
    
    // Helper methods
    fun isSongSelected(songId: Long): Boolean {
        return selectedSongs.contains(songId)
    }
    
    fun isAlbumSelected(albumId: Long): Boolean {
        return selectedAlbums.contains(albumId)
    }
    
    fun isSongFavorite(song: Song): Boolean {
        return favoriteSongs.contains(song.id)
    }
    
    fun isCurrentlyPlaying(song: Song): Boolean {
        return currentPlayingSong?.id == song.id && isPlaying
    }
    
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

/**
 * Artist tabs enum
 */
enum class ArtistTab(val displayName: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    POPULAR("Popular");
    
    companion object {
        fun fromDisplayName(displayName: String): ArtistTab? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Artist view modes
 */
enum class ArtistViewMode {
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
 * Artist statistics data class
 */
data class ArtistStatistics(
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalDuration: Long = 0L,
    val totalPlays: Int = 0,
    val averageSongDuration: Long = 0L,
    val mostPlayedSong: Song? = null,
    val mostPopularAlbum: Album? = null,
    val totalFavorites: Int = 0,
    val artistRating: Float = 0f,
    val firstReleaseYear: Int = 0,
    val latestReleaseYear: Int = 0,
    val genres: List<String> = emptyList(),
    val followers: Int = 0,
    val monthlyListeners: Int = 0
) {
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageSongDuration)
    
    val yearRange: String
        get() = when {
            firstReleaseYear == 0 && latestReleaseYear == 0 -> "Unknown"
            firstReleaseYear == latestReleaseYear -> firstReleaseYear.toString()
            else -> "$firstReleaseYear - $latestReleaseYear"
        }
    
    val primaryGenre: String
        get() = genres.firstOrNull() ?: "Unknown"
    
    val formattedFollowers: String
        get() = when {
            followers >= 1_000_000 -> "${followers / 1_000_000}M"
            followers >= 1_000 -> "${followers / 1_000}K"
            else -> followers.toString()
        }
    
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

/**
 * Extension functions for ArtistUiState
 */
fun ArtistUiState.copyWithLoading(isLoading: Boolean): ArtistUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun ArtistUiState.copyWithError(error: String?): ArtistUiState {
    return copy(error = error, isLoading = false)
}

fun ArtistUiState.copyWithArtist(artist: Artist?, error: String? = null): ArtistUiState {
    return copy(
        artist = artist,
        artistLoading = false,
        artistError = error
    )
}

fun ArtistUiState.copyWithSongs(songs: List<Song>, error: String? = null): ArtistUiState {
    return copy(
        songs = songs,
        songsLoading = false,
        songsError = error
    )
}

fun ArtistUiState.copyWithAlbums(albums: List<Album>, error: String? = null): ArtistUiState {
    return copy(
        albums = albums,
        albumsLoading = false,
        albumsError = error
    )
}

fun ArtistUiState.copyWithTab(tab: ArtistTab): ArtistUiState {
    return copy(
        currentTab = tab,
        isSelectionMode = false,
        selectedSongs = emptySet(),
        selectedAlbums = emptySet()
    )
}

fun ArtistUiState.copyWithSelection(
    isSelectionMode: Boolean,
    selectedSongs: Set<Long> = if (isSelectionMode) this.selectedSongs else emptySet(),
    selectedAlbums: Set<Long> = if (isSelectionMode) this.selectedAlbums else emptySet()
): ArtistUiState {
    return copy(
        isSelectionMode = isSelectionMode,
        selectedSongs = selectedSongs,
        selectedAlbums = selectedAlbums
    )
}

fun ArtistUiState.copyWithPlayback(
    currentSong: Song?,
    isPlaying: Boolean
): ArtistUiState {
    return copy(
        currentPlayingSong = currentSong,
        isPlaying = isPlaying
    )
}

fun ArtistUiState.copyWithSearch(query: String, isActive: Boolean): ArtistUiState {
    return copy(
        searchQuery = query,
        isSearchActive = isActive
    )
}

/**
 * Preview data for ArtistUiState
 */
object ArtistUiStatePreview {
    val loading = ArtistUiState(isLoading = true)
    
    val error = ArtistUiState(error = "Failed to load artist")
    
    val empty = ArtistUiState(
        artist = Artist(
            id = 1,
            name = "Sample Artist",
            songCount = 0,
            albumCount = 0,
            totalDuration = 0L
        )
    )
    
    val withData = ArtistUiState(
        artist = Artist(
            id = 1,
            name = "The Beatles",
            songCount = 213,
            albumCount = 13,
            totalDuration = 32400000L, // 9 hours
            imagePath = "/path/to/artist.jpg"
        ),
        songs = listOf(
            Song(
                id = 1, title = "Here Comes the Sun", artist = "The Beatles", 
                album = "Abbey Road", duration = 185000L, playCount = 1250,
                rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 1969, genre = "Rock"
            ),
            Song(
                id = 2, title = "Come Together", artist = "The Beatles",
                album = "Abbey Road", duration = 259000L, playCount = 980,
                rating = 4.5f, isFavorite = false, dateAdded = System.currentTimeMillis() - 86400000,
                year = 1969, genre = "Rock"
            )
        ),
        albums = listOf(
            Album(
                id = 1, name = "Abbey Road", artist = "The Beatles",
                songCount = 17, year = 1969, totalDuration = 2873000L
            ),
            Album(
                id = 2, name = "Sgt. Pepper's", artist = "The Beatles",
                songCount = 13, year = 1967, totalDuration = 2387000L
            )
        ),
        popularSongs = listOf(
            Song(
                id = 1, title = "Here Comes the Sun", artist = "The Beatles", 
                album = "Abbey Road", duration = 185000L, playCount = 1250,
                rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 1969, genre = "Rock"
            )
        ),
        favoriteSongs = setOf(1L),
        isArtistFavorite = true,
        isFollowing = true,
        artistStats = ArtistStatistics(
            totalSongs = 213,
            totalAlbums = 13,
            totalDuration = 32400000L,
            totalPlays = 25600,
            averageSongDuration = 152000L,
            totalFavorites = 89,
            artistRating = 4.8f,
            firstReleaseYear = 1962,
            latestReleaseYear = 1970,
            genres = listOf("Rock", "Pop", "Psychedelic Rock"),
            followers = 45_300_000,
            monthlyListeners = 28_500_000
        )
    )
    
    val selectionMode = withData.copy(
        isSelectionMode = true,
        selectedSongs = setOf(1L, 2L)
    )
    
    val searching = withData.copy(
        searchQuery = "here",
        isSearchActive = true
    )
}
