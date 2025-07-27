package com.tinhtx.localplayerapplication.presentation.screens.home

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recentSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val recentlyPlayedSongs: List<Song> = emptyList(),
    val libraryStats: HomeLibraryStats = HomeLibraryStats(),
    val searchResults: List<Song> = emptyList(),
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val gridSize: GridSize = GridSize.MEDIUM,
    val isSearching: Boolean = false,
    val selectedSection: HomeSection? = null,
    val isRefreshing: Boolean = false
) {
    val hasData: Boolean
        get() = recentSongs.isNotEmpty() || 
                favoriteSongs.isNotEmpty() || 
                playlists.isNotEmpty() || 
                recentlyPlayedSongs.isNotEmpty()
    
    val isEmpty: Boolean
        get() = !isLoading && !hasData && error == null
    
    val hasError: Boolean
        get() = error != null
    
    val showSearchResults: Boolean
        get() = searchResults.isNotEmpty()
    
    val totalItems: Int
        get() = recentSongs.size + favoriteSongs.size + playlists.size + recentlyPlayedSongs.size
}

/**
 * Home screen sections enum
 */
enum class HomeSection(val displayName: String) {
    RECENT_SONGS("Recently Added"),
    FAVORITES("Your Favorites"),
    PLAYLISTS("Your Playlists"),
    RECENTLY_PLAYED("Recently Played");
    
    companion object {
        fun fromDisplayName(displayName: String): HomeSection? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Library statistics for home screen display
 */
data class HomeLibraryStats(
    val totalSongs: Int = 0,
    val totalFavorites: Int = 0,
    val totalPlaylists: Int = 0,
    val totalDuration: Long = 0L,
    val totalArtists: Int = 0,
    val totalAlbums: Int = 0,
    val totalGenres: Int = 0,
    val averageSongDuration: Long = 0L,
    val lastPlayedTime: Long = 0L,
    val mostPlayedSong: Song? = null
) {
    val formattedDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageSongDuration)
    
    val hasContent: Boolean
        get() = totalSongs > 0
    
    val librarySize: String
        get() = when {
            totalSongs == 0 -> "Empty"
            totalSongs < 100 -> "Small"
            totalSongs < 1000 -> "Medium" 
            totalSongs < 5000 -> "Large"
            else -> "Huge"
        }
    
    val diversityScore: Float
        get() = if (totalSongs > 0) {
            (totalArtists + totalAlbums + totalGenres).toFloat() / totalSongs * 100f
        } else 0f
    
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
    
    fun getStatsList(): List<StatItem> {
        return listOf(
            StatItem("Songs", totalSongs.toString(), "🎵"),
            StatItem("Artists", totalArtists.toString(), "👤"),
            StatItem("Albums", totalAlbums.toString(), "💿"),
            StatItem("Playlists", totalPlaylists.toString(), "📝"),
            StatItem("Duration", formattedDuration, "⏱️"),
            StatItem("Favorites", totalFavorites.toString(), "❤️")
        )
    }
}

/**
 * Stat item for display in UI
 */
data class StatItem(
    val label: String,
    val value: String,
    val icon: String
)

/**
 * Extension functions for HomeUiState
 */
fun HomeUiState.copyWithLoading(isLoading: Boolean): HomeUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun HomeUiState.copyWithError(error: String?): HomeUiState {
    return copy(error = error, isLoading = false)
}

fun HomeUiState.copyWithData(
    recentSongs: List<Song> = this.recentSongs,
    favoriteSongs: List<Song> = this.favoriteSongs,
    playlists: List<Playlist> = this.playlists,
    recentlyPlayedSongs: List<Song> = this.recentlyPlayedSongs,
    libraryStats: HomeLibraryStats = this.libraryStats
): HomeUiState {
    return copy(
        recentSongs = recentSongs,
        favoriteSongs = favoriteSongs,
        playlists = playlists,
        recentlyPlayedSongs = recentlyPlayedSongs,
        libraryStats = libraryStats,
        isLoading = false,
        error = null
    )
}

/**
 * Preview data for HomeUiState
 */
object HomeUiStatePreview {
    val loading = HomeUiState(isLoading = true)
    
    val error = HomeUiState(error = "Failed to load data")
    
    val empty = HomeUiState()
    
    val sample = HomeUiState(
        recentSongs = listOf(
            // Sample songs would go here
        ),
        favoriteSongs = listOf(
            // Sample favorite songs
        ),
        playlists = listOf(
            // Sample playlists
        ),
        libraryStats = HomeLibraryStats(
            totalSongs = 1250,
            totalArtists = 89,
            totalAlbums = 156,
            totalPlaylists = 12,
            totalFavorites = 47,
            totalDuration = 4500000L // 1.25 hours
        )
    )
}
