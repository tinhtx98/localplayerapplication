package com.tinhtx.localplayerapplication.presentation.screens.playlist

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Playlist Module - Supports both PlaylistsScreen and PlaylistDetailScreen
 */
data class PlaylistUiState(
    // =================================================================================
    // PLAYLISTS LIST STATE (for PlaylistsScreen)
    // =================================================================================
    
    // Playlists data
    val playlists: List<Playlist> = emptyList(),
    val playlistsLoading: Boolean = false,
    val playlistsError: String? = null,
    
    // Playlists view mode
    val playlistsViewMode: PlaylistsViewMode = PlaylistsViewMode.LIST,
    val playlistsSortOrder: PlaylistsSortOrder = PlaylistsSortOrder.RECENT,
    val playlistsSortAscending: Boolean = false,
    
    // Playlists search
    val playlistsSearchQuery: String = "",
    val playlistsSearchActive: Boolean = false,
    
    // Playlists selection
    val playlistsSelectionMode: Boolean = false,
    val selectedPlaylists: Set<Long> = emptySet(),
    
    // Create playlist
    val showCreatePlaylistDialog: Boolean = false,
    val newPlaylistName: String = "",
    val newPlaylistDescription: String = "",
    val creatingPlaylist: Boolean = false,
    
    // =================================================================================
    // PLAYLIST DETAIL STATE (for PlaylistDetailScreen)
    // =================================================================================
    
    // Current playlist detail
    val currentPlaylist: Playlist? = null,
    val playlistDetailLoading: Boolean = false,
    val playlistDetailError: String? = null,
    
    // Playlist songs
    val playlistSongs: List<Song> = emptyList(),
    val playlistSongsLoading: Boolean = false,
    val playlistSongsError: String? = null,
    
    // Playback state
    val currentPlayingSong: Song? = null,
    val isPlaying: Boolean = false,
    val shuffleMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    
    // Playlist detail selection
    val songsSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    
    // Edit playlist
    val editMode: Boolean = false,
    val showEditPlaylistDialog: Boolean = false,
    val editingPlaylistName: String = "",
    val editingPlaylistDescription: String = "",
    val updatingPlaylist: Boolean = false,
    
    // Search in playlist
    val playlistSearchQuery: String = "",
    val playlistSearchActive: Boolean = false,
    
    // Sort playlist songs
    val playlistSongsSortOrder: SortOrder = SortOrder.CUSTOM,
    val playlistSongsSortAscending: Boolean = true,
    val showPlaylistSortDialog: Boolean = false,
    
    // Add songs
    val showAddSongsBottomSheet: Boolean = false,
    val availableSongs: List<Song> = emptyList(),
    val availableSongsLoading: Boolean = false,
    val addSongsSearchQuery: String = "",
    val selectedSongsToAdd: Set<Long> = emptySet(),
    val addingSongs: Boolean = false,
    
    // Playlist options
    val showPlaylistOptionsMenu: Boolean = false,
    val showDeletePlaylistDialog: Boolean = false,
    val deletingPlaylist: Boolean = false,
    
    // Drag and drop
    val isDragging: Boolean = false,
    val draggedSongId: Long? = null,
    val dropTargetIndex: Int? = null,
    
    // =================================================================================
    // SHARED STATE
    // =================================================================================
    
    // General loading & error
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Favorites
    val favoriteSongs: Set<Long> = emptySet(),
    
    // Navigation
    val selectedSongForPlayback: Song? = null,
    val selectedPlaylistForNavigation: Playlist? = null,
    val navigateToAlbum: Album? = null,
    val navigateToArtist: Artist? = null,
    
    // Undo system
    val lastRemovedSongs: List<Pair<Song, Int>> = emptyList(),
    val lastDeletedPlaylist: Playlist? = null,
    val showUndoSnackbar: Boolean = false,
    val undoMessage: String = ""
) {
    
    // =================================================================================
    // COMPUTED PROPERTIES - PLAYLISTS LIST
    // =================================================================================
    
    val hasPlaylists: Boolean
        get() = playlists.isNotEmpty()
    
    val playlistsEmpty: Boolean
        get() = !playlistsLoading && playlists.isEmpty()
    
    val filteredPlaylists: List<Playlist>
        get() = playlists.filter { playlist ->
            if (playlistsSearchQuery.isBlank()) true else {
                playlist.name.contains(playlistsSearchQuery, ignoreCase = true) ||
                playlist.description.contains(playlistsSearchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (playlistsSortOrder) {
                PlaylistsSortOrder.NAME -> if (playlistsSortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
                PlaylistsSortOrder.RECENT -> if (playlistsSortAscending) filtered.sortedBy { it.updatedAt ?: it.createdAt } else filtered.sortedByDescending { it.updatedAt ?: it.createdAt }
                PlaylistsSortOrder.SONG_COUNT -> if (playlistsSortAscending) filtered.sortedBy { it.songCount } else filtered.sortedByDescending { it.songCount }
                PlaylistsSortOrder.DURATION -> if (playlistsSortAscending) filtered.sortedBy { it.totalDuration } else filtered.sortedByDescending { it.totalDuration }
                PlaylistsSortOrder.CREATED -> if (playlistsSortAscending) filtered.sortedBy { it.createdAt } else filtered.sortedByDescending { it.createdAt }
            }
        }
    
    val selectedPlaylistsCount: Int
        get() = selectedPlaylists.size
    
    val hasSelectedPlaylists: Boolean
        get() = selectedPlaylists.isNotEmpty()
    
    val allPlaylistsSelected: Boolean
        get() = selectedPlaylists.containsAll(filteredPlaylists.map { it.id })
    
    val canSelectAllPlaylists: Boolean
        get() = filteredPlaylists.isNotEmpty() && !allPlaylistsSelected
    
    val playlistsSearchResultsCount: Int
        get() = filteredPlaylists.size
    
    // =================================================================================
    // COMPUTED PROPERTIES - PLAYLIST DETAIL
    // =================================================================================
    
    val hasPlaylistData: Boolean
        get() = currentPlaylist != null && playlistSongs.isNotEmpty()
    
    val playlistEmpty: Boolean
        get() = !playlistDetailLoading && currentPlaylist != null && playlistSongs.isEmpty()
    
    val hasPlaylistError: Boolean
        get() = playlistDetailError != null || playlistSongsError != null
    
    val currentPlaylistError: String?
        get() = playlistDetailError ?: playlistSongsError
    
    val filteredPlaylistSongs: List<Song>
        get() = playlistSongs.filter { song ->
            if (playlistSearchQuery.isBlank()) true else {
                song.title.contains(playlistSearchQuery, ignoreCase = true) ||
                song.artist.contains(playlistSearchQuery, ignoreCase = true) ||
                song.album.contains(playlistSearchQuery, ignoreCase = true)
            }
        }.let { filtered ->
            when (playlistSongsSortOrder) {
                SortOrder.CUSTOM -> filtered // Maintain playlist order
                SortOrder.TITLE -> if (playlistSongsSortAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
                SortOrder.ARTIST -> if (playlistSongsSortAscending) filtered.sortedBy { it.artist } else filtered.sortedByDescending { it.artist }
                SortOrder.ALBUM -> if (playlistSongsSortAscending) filtered.sortedBy { it.album } else filtered.sortedByDescending { it.album }
                SortOrder.DURATION -> if (playlistSongsSortAscending) filtered.sortedBy { it.duration } else filtered.sortedByDescending { it.duration }
                SortOrder.DATE_ADDED -> if (playlistSongsSortAscending) filtered.sortedBy { it.dateAdded } else filtered.sortedByDescending { it.dateAdded }
                SortOrder.PLAY_COUNT -> if (playlistSongsSortAscending) filtered.sortedBy { it.playCount } else filtered.sortedByDescending { it.playCount }
                else -> filtered
            }
        }
    
    val filteredAvailableSongs: List<Song>
        get() = availableSongs.filter { song ->
            // Exclude songs already in playlist
            !playlistSongs.any { it.id == song.id } &&
            // Apply search filter
            if (addSongsSearchQuery.isBlank()) true else {
                song.title.contains(addSongsSearchQuery, ignoreCase = true) ||
                song.artist.contains(addSongsSearchQuery, ignoreCase = true) ||
                song.album.contains(addSongsSearchQuery, ignoreCase = true)
            }
        }
    
    val selectedSongsCount: Int
        get() = selectedSongs.size
    
    val selectedSongsToAddCount: Int
        get() = selectedSongsToAdd.size
    
    val hasSelectedSongs: Boolean
        get() = selectedSongs.isNotEmpty()
    
    val hasSelectedSongsToAdd: Boolean
        get() = selectedSongsToAdd.isNotEmpty()
    
    val allSongsSelected: Boolean
        get() = selectedSongs.containsAll(filteredPlaylistSongs.map { it.id })
    
    val canSelectAllSongs: Boolean
        get() = filteredPlaylistSongs.isNotEmpty() && !allSongsSelected
    
    val playlistSearchResultsCount: Int
        get() = filteredPlaylistSongs.size
    
    val totalDuration: Long
        get() = playlistSongs.sumOf { it.duration }
    
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val canEditPlaylist: Boolean
        get() = currentPlaylist?.isEditable == true
    
    val canDeletePlaylist: Boolean
        get() = currentPlaylist?.isEditable == true && currentPlaylist?.isSystem != true
    
    val canAddSongs: Boolean
        get() = currentPlaylist?.isEditable == true
    
    val canReorderSongs: Boolean
        get() = currentPlaylist?.isEditable == true && playlistSongsSortOrder == SortOrder.CUSTOM
    
    // =================================================================================
    // HELPER METHODS
    // =================================================================================
    
    fun isPlaylistSelected(playlistId: Long): Boolean {
        return selectedPlaylists.contains(playlistId)
    }
    
    fun isSongSelected(songId: Long): Boolean {
        return selectedSongs.contains(songId)
    }
    
    fun isSongSelectedToAdd(songId: Long): Boolean {
        return selectedSongsToAdd.contains(songId)
    }
    
    fun isSongFavorite(song: Song): Boolean {
        return favoriteSongs.contains(song.id)
    }
    
    fun isCurrentlyPlaying(song: Song): Boolean {
        return currentPlayingSong?.id == song.id && isPlaying
    }
    
    fun getSongIndex(song: Song): Int {
        return playlistSongs.indexOf(song)
    }
    
    fun canMoveSong(fromIndex: Int, toIndex: Int): Boolean {
        return canReorderSongs && fromIndex != toIndex && 
               fromIndex in playlistSongs.indices && toIndex in playlistSongs.indices
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
 * Playlists view modes (for PlaylistsScreen)
 */
enum class PlaylistsViewMode {
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
 * Playlists sort orders (for PlaylistsScreen)
 */
enum class PlaylistsSortOrder {
    NAME,
    RECENT,
    SONG_COUNT,
    DURATION,
    CREATED;
    
    val displayName: String
        get() = when (this) {
            NAME -> "Name"
            RECENT -> "Recently Updated"
            SONG_COUNT -> "Song Count"
            DURATION -> "Duration"
            CREATED -> "Date Created"
        }
}

/**
 * Repeat modes
 */
enum class RepeatMode {
    OFF,
    ALL,
    ONE;
    
    val displayName: String
        get() = when (this) {
            OFF -> "Off"
            ALL -> "Repeat All"
            ONE -> "Repeat One"
        }
}
