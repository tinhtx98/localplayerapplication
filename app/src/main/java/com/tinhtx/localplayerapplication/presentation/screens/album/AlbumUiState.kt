package com.tinhtx.localplayerapplication.presentation.screens.album

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Album Screen - Complete state management
 */
data class AlbumUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Album data
    val album: Album? = null,
    val albumLoading: Boolean = false,
    val albumError: String? = null,
    
    // Songs data
    val songs: List<Song> = emptyList(),
    val songsLoading: Boolean = false,
    val songsError: String? = null,
    
    // Playback state
    val currentPlayingSong: Song? = null,
    val isPlaying: Boolean = false,
    val shuffleMode: Boolean = false,
    
    // Selection mode
    val isSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    
    // Sorting and view
    val sortOrder: SortOrder = SortOrder.TRACK_NUMBER,
    val sortAscending: Boolean = true,
    val viewMode: AlbumViewMode = AlbumViewMode.LIST,
    
    // Favorites
    val favoriteSongs: Set<Long> = emptySet(),
    val isAlbumFavorite: Boolean = false,
    
    // Statistics
    val albumStats: AlbumStatistics = AlbumStatistics(),
    
    // Navigation
    val selectedSongForPlayback: Song? = null,
    val navigateToArtist: String? = null,
    
    // Interaction states
    val showMoreOptions: Boolean = false,
    val showSortOptions: Boolean = false,
    val showAddToPlaylist: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
) {
    
    // Computed properties
    val hasData: Boolean
        get() = album != null && songs.isNotEmpty()
    
    val isEmpty: Boolean
        get() = !isLoading && album != null && songs.isEmpty()
    
    val hasError: Boolean
        get() = error != null || albumError != null || songsError != null
    
    val currentError: String?
        get() = error ?: albumError ?: songsError
    
    val sortedSongs: List<Song>
        get() = when (sortOrder) {
            SortOrder.TRACK_NUMBER -> if (sortAscending) songs.sortedBy { it.trackNumber } else songs.sortedByDescending { it.trackNumber }
            SortOrder.TITLE -> if (sortAscending) songs.sortedBy { it.title } else songs.sortedByDescending { it.title }
            SortOrder.DURATION -> if (sortAscending) songs.sortedBy { it.duration } else songs.sortedByDescending { it.duration }
            SortOrder.PLAY_COUNT -> if (sortAscending) songs.sortedBy { it.playCount } else songs.sortedByDescending { it.playCount }
            SortOrder.DATE_ADDED -> if (sortAscending) songs.sortedBy { it.dateAdded } else songs.sortedByDescending { it.dateAdded }
            else -> songs
        }
    
    val selectedSongsCount: Int
        get() = selectedSongs.size
    
    val hasSelectedSongs: Boolean
        get() = selectedSongs.isNotEmpty()
    
    val allSongsSelected: Boolean
        get() = selectedSongs.containsAll(songs.map { it.id })
    
    val canSelectAll: Boolean
        get() = songs.isNotEmpty() && !allSongsSelected
    
    val totalDuration: Long
        get() = songs.sumOf { it.duration }
    
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val playedSongsCount: Int
        get() = songs.count { it.playCount > 0 }
    
    val favoriteCount: Int
        get() = songs.count { favoriteSongs.contains(it.id) }
    
    val averageRating: Float
        get() = if (songs.isNotEmpty()) {
            songs.map { it.rating }.average().toFloat()
        } else 0f
    
    // Helper methods
    fun isSongSelected(songId: Long): Boolean {
        return selectedSongs.contains(songId)
    }
    
    fun isSongFavorite(song: Song): Boolean {
        return favoriteSongs.contains(song.id)
    }
    
    fun isCurrentlyPlaying(song: Song): Boolean {
        return currentPlayingSong?.id == song.id && isPlaying
    }
    
    fun getSongIndex(song: Song): Int {
        return sortedSongs.indexOf(song)
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
 * Album view modes
 */
enum class AlbumViewMode {
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
 * Album statistics data class
 */
data class AlbumStatistics(
    val totalSongs: Int = 0,
    val totalDuration: Long = 0L,
    val totalPlays: Int = 0,
    val averageSongDuration: Long = 0L,
    val mostPlayedSong: Song? = null,
    val leastPlayedSong: Song? = null,
    val totalFavorites: Int = 0,
    val albumRating: Float = 0f,
    val releaseYear: Int = 0,
    val genre: String = "",
    val recordLabel: String = "",
    val producer: String = ""
) {
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageSongDuration)
    
    val completionPercentage: Float
        get() = if (totalSongs > 0) (totalFavorites.toFloat() / totalSongs) * 100f else 0f
    
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
 * Extension functions for AlbumUiState
 */
fun AlbumUiState.copyWithLoading(isLoading: Boolean): AlbumUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun AlbumUiState.copyWithError(error: String?): AlbumUiState {
    return copy(error = error, isLoading = false)
}

fun AlbumUiState.copyWithAlbum(album: Album?, error: String? = null): AlbumUiState {
    return copy(
        album = album,
        albumLoading = false,
        albumError = error
    )
}

fun AlbumUiState.copyWithSongs(songs: List<Song>, error: String? = null): AlbumUiState {
    return copy(
        songs = songs,
        songsLoading = false,
        songsError = error
    )
}

fun AlbumUiState.copyWithSelection(
    isSelectionMode: Boolean,
    selectedSongs: Set<Long> = if (isSelectionMode) this.selectedSongs else emptySet()
): AlbumUiState {
    return copy(
        isSelectionMode = isSelectionMode,
        selectedSongs = selectedSongs
    )
}

fun AlbumUiState.copyWithPlayback(
    currentSong: Song?,
    isPlaying: Boolean
): AlbumUiState {
    return copy(
        currentPlayingSong = currentSong,
        isPlaying = isPlaying
    )
}

/**
 * Preview data for AlbumUiState
 */
object AlbumUiStatePreview {
    val loading = AlbumUiState(isLoading = true)
    
    val error = AlbumUiState(error = "Failed to load album")
    
    val empty = AlbumUiState(
        album = Album(
            id = 1,
            name = "Sample Album",
            artist = "Sample Artist",
            songCount = 0,
            year = 2023,
            totalDuration = 0L
        )
    )
    
    val withData = AlbumUiState(
        album = Album(
            id = 1,
            name = "Greatest Hits",
            artist = "The Beatles",
            songCount = 12,
            year = 1967,
            totalDuration = 2700000L, // 45 minutes
            albumArtPath = "/path/to/art.jpg"
        ),
        songs = listOf(
            Song(
                id = 1, title = "Here Comes the Sun", artist = "The Beatles", 
                album = "Greatest Hits", duration = 185000L, trackNumber = 1,
                playCount = 25, rating = 5f, isFavorite = true,
                dateAdded = System.currentTimeMillis(), year = 1967, genre = "Rock"
            ),
            Song(
                id = 2, title = "Come Together", artist = "The Beatles",
                album = "Greatest Hits", duration = 259000L, trackNumber = 2,
                playCount = 18, rating = 4.5f, isFavorite = false,
                dateAdded = System.currentTimeMillis() - 86400000, year = 1967, genre = "Rock"
            ),
            Song(
                id = 3, title = "Something", artist = "The Beatles",
                album = "Greatest Hits", duration = 182000L, trackNumber = 3,
                playCount = 32, rating = 5f, isFavorite = true,
                dateAdded = System.currentTimeMillis() - 172800000, year = 1967, genre = "Rock"
            )
        ),
        favoriteSongs = setOf(1L, 3L),
        isAlbumFavorite = true,
        albumStats = AlbumStatistics(
            totalSongs = 12,
            totalDuration = 2700000L,
            totalPlays = 156,
            averageSongDuration = 225000L,
            totalFavorites = 8,
            albumRating = 4.7f,
            releaseYear = 1967,
            genre = "Rock"
        )
    )
    
    val selectionMode = withData.copy(
        isSelectionMode = true,
        selectedSongs = setOf(1L, 3L)
    )
    
    val playing = withData.copy(
        currentPlayingSong = withData.songs.first(),
        isPlaying = true
    )
}
