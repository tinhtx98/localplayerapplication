package com.tinhtx.localplayerapplication.presentation.screens.queue

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * UI State for Queue Screen - Complete queue/playback state management
 */
data class QueueUiState(
    // =================================================================================
    // QUEUE DATA & PLAYBACK STATE
    // =================================================================================
    
    val queueSongs: List<Song> = emptyList(),
    val originalQueue: List<Song> = emptyList(), // Before shuffle
    val queueHistory: List<Song> = emptyList(), // Previously played songs
    val upNextSongs: List<Song> = emptyList(), // Songs after current
    
    // Current playback state
    val currentSong: Song? = null,
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    
    // Playback modes
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3, // seconds
    
    // =================================================================================
    // QUEUE MANAGEMENT
    // =================================================================================
    
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Queue operations
    val isReordering: Boolean = false,
    val isAddingSongs: Boolean = false,
    val isClearingQueue: Boolean = false,
    val isShuffling: Boolean = false,
    
    // Queue source info
    val queueSource: QueueSource? = null,
    val queueName: String = "",
    val queueDescription: String = "",
    
    // =================================================================================
    // SEARCH & FILTERING
    // =================================================================================
    
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filteredSongs: List<Song> = emptyList(),
    val searchResults: List<Song> = emptyList(),
    
    // =================================================================================
    // SELECTION & BATCH OPERATIONS
    // =================================================================================
    
    val isSelectionMode: Boolean = false,
    val selectedSongs: Set<Long> = emptySet(),
    val selectedIndices: Set<Int> = emptySet(),
    
    // =================================================================================
    // DRAG & DROP REORDERING
    // =================================================================================
    
    val isDragging: Boolean = false,
    val draggedSongIndex: Int? = null,
    val dropTargetIndex: Int? = null,
    val canReorderQueue: Boolean = true,
    
    // =================================================================================
    // ADD SONGS TO QUEUE
    // =================================================================================
    
    val showAddSongsBottomSheet: Boolean = false,
    val availableSongs: List<Song> = emptyList(),
    val availableSongsLoading: Boolean = false,
    val addSongsSearchQuery: String = "",
    val selectedSongsToAdd: Set<Long> = emptySet(),
    
    // =================================================================================
    // QUEUE ACTIONS & DIALOGS
    // =================================================================================
    
    val showQueueOptionsMenu: Boolean = false,
    val showClearQueueDialog: Boolean = false,
    val showSaveQueueDialog: Boolean = false,
    val showQueueStatsDialog: Boolean = false,
    val showShareQueueDialog: Boolean = false,
    
    // Save queue dialog
    val newPlaylistName: String = "",
    val isSavingQueue: Boolean = false,
    
    // =================================================================================
    // QUEUE STATISTICS
    // =================================================================================
    
    val queueStats: QueueStats = QueueStats(),
    
    // =================================================================================
    // FAVORITES & USER DATA
    // =================================================================================
    
    val favoriteSongs: Set<Long> = emptySet(),
    val recentlyPlayed: List<Song> = emptyList(),
    
    // =================================================================================
    // UI PREFERENCES
    // =================================================================================
    
    val showMiniPlayer: Boolean = true,
    val showQueueStats: Boolean = false,
    val compactView: Boolean = false,
    val showAlbumArt: Boolean = true,
    val showSongNumbers: Boolean = true,
    
    // =================================================================================
    // NAVIGATION & EXTERNAL ACTIONS
    // =================================================================================
    
    val selectedSongForPlayback: Song? = null,
    val selectedAlbumForNavigation: Album? = null,
    val selectedArtistForNavigation: Artist? = null,
    val navigateToNowPlaying: Boolean = false,
    
    // =================================================================================
    // AUTO-PLAY & RECOMMENDATIONS
    // =================================================================================
    
    val autoPlayEnabled: Boolean = true,
    val recommendedSongs: List<Song> = emptyList(),
    val showRecommendations: Boolean = false,
    
    // =================================================================================
    // QUEUE PERSISTENCE
    // =================================================================================
    
    val canSaveQueue: Boolean = true,
    val lastSavedTime: Long = 0L,
    val autoSaveEnabled: Boolean = true,
    
    // =================================================================================
    // UNDO/REDO OPERATIONS
    // =================================================================================
    
    val undoOperations: List<QueueOperation> = emptyList(),
    val redoOperations: List<QueueOperation> = emptyList(),
    val showUndoSnackbar: Boolean = false,
    val undoMessage: String = "",
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    
    // =================================================================================
    // COMPUTED PROPERTIES
    // =================================================================================
    
    val hasQueue: Boolean
        get() = queueSongs.isNotEmpty()
    
    val isQueueEmpty: Boolean
        get() = queueSongs.isEmpty()
    
    val queueSize: Int
        get() = queueSongs.size
    
    val hasCurrentSong: Boolean
        get() = currentSong != null
    
    val isValidIndex: Boolean
        get() = currentIndex >= 0 && currentIndex < queueSongs.size
    
    val hasPrevious: Boolean
        get() = when (repeatMode) {
            RepeatMode.OFF -> currentIndex > 0 || queueHistory.isNotEmpty()
            RepeatMode.ALL -> hasQueue
            RepeatMode.ONE -> hasCurrentSong
        }
    
    val hasNext: Boolean
        get() = when (repeatMode) {
            RepeatMode.OFF -> currentIndex < queueSongs.size - 1
            RepeatMode.ALL -> hasQueue
            RepeatMode.ONE -> hasCurrentSong
        }
    
    val totalDuration: Long
        get() = queueSongs.sumOf { it.duration }
    
    val remainingDuration: Long
        get() = if (currentIndex >= 0) {
            queueSongs.drop(currentIndex + 1).sumOf { it.duration } + 
            (currentSong?.duration?.minus(currentPosition) ?: 0L)
        } else totalDuration
    
    val playedDuration: Long
        get() = totalDuration - remainingDuration
    
    val progressPercentage: Float
        get() = if (totalDuration > 0) playedDuration.toFloat() / totalDuration else 0f
    
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedRemainingDuration: String
        get() = formatDuration(remainingDuration)
    
    val formattedCurrentPosition: String
        get() = formatDuration(currentPosition)
    
    val formattedCurrentDuration: String
        get() = formatDuration(currentSong?.duration ?: 0L)
    
    val queueTitle: String
        get() = when {
            queueName.isNotEmpty() -> queueName
            queueSource != null -> queueSource.getDisplayName()
            else -> "Queue"
        }
    
    val queueSubtitle: String
        get() = when {
            queueDescription.isNotEmpty() -> queueDescription
            queueSize > 0 -> "$queueSize songs • $formattedTotalDuration"
            else -> "No songs in queue"
        }
    
    val displayedSongs: List<Song>
        get() = if (isSearchActive && searchQuery.isNotEmpty()) {
            filteredSongs
        } else {
            queueSongs
        }
    
    val selectedSongsCount: Int
        get() = selectedSongs.size
    
    val hasSelectedSongs: Boolean
        get() = selectedSongs.isNotEmpty()
    
    val allSongsSelected: Boolean
        get() = selectedSongs.containsAll(displayedSongs.map { it.id })
    
    val canSelectAll: Boolean
        get() = displayedSongs.isNotEmpty() && !allSongsSelected
    
    val currentSongProgress: String
        get() = if (hasCurrentSong) {
            "$formattedCurrentPosition / $formattedCurrentDuration"
        } else "0:00 / 0:00"
    
    val isCurrentSongFavorite: Boolean
        get() = currentSong?.let { favoriteSongs.contains(it.id) } ?: false
    
    val playbackStateText: String
        get() = when {
            isBuffering -> "Buffering..."
            isPlaying -> "Playing"
            isPaused -> "Paused"
            else -> "Stopped"
        }
    
    val canAddMoreSongs: Boolean
        get() = queueSize < MAX_QUEUE_SIZE
    
    val searchResultsCount: Int
        get() = filteredSongs.size
    
    val hasSearchResults: Boolean
        get() = isSearchActive && filteredSongs.isNotEmpty()
    
    val shouldShowEmptyState: Boolean
        get() = isQueueEmpty && !isLoading
    
    val shouldShowSearchEmptyState: Boolean
        get() = isSearchActive && searchQuery.isNotEmpty() && filteredSongs.isEmpty()
    
    // Helper methods
    fun isSongSelected(songId: Long): Boolean = selectedSongs.contains(songId)
    fun isSongFavorite(song: Song): Boolean = favoriteSongs.contains(song.id)
    fun isCurrentSong(song: Song): Boolean = currentSong?.id == song.id
    fun isSongInQueue(songId: Long): Boolean = queueSongs.any { it.id == songId }
    fun getSongIndex(song: Song): Int = queueSongs.indexOf(song)
    fun getSongAtIndex(index: Int): Song? = queueSongs.getOrNull(index)
    
    fun canMoveSong(fromIndex: Int, toIndex: Int): Boolean {
        return canReorderQueue && fromIndex != toIndex && 
               fromIndex in queueSongs.indices && toIndex in queueSongs.indices
    }
    
    fun getNextSong(): Song? = when (repeatMode) {
        RepeatMode.ONE -> currentSong
        RepeatMode.ALL -> {
            if (hasNext) queueSongs.getOrNull(currentIndex + 1)
            else queueSongs.firstOrNull()
        }
        RepeatMode.OFF -> queueSongs.getOrNull(currentIndex + 1)
    }
    
    fun getPreviousSong(): Song? = when (repeatMode) {
        RepeatMode.ONE -> currentSong
        RepeatMode.ALL -> {
            if (currentIndex > 0) queueSongs.getOrNull(currentIndex - 1)
            else queueSongs.lastOrNull()
        }
        RepeatMode.OFF -> queueSongs.getOrNull(currentIndex - 1)
    }
    
    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"
        
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    companion object {
        const val MAX_QUEUE_SIZE = 1000
    }
}

/**
 * Queue statistics data
 */
data class QueueStats(
    val totalSongs: Int = 0,
    val totalDuration: Long = 0L,
    val averageSongDuration: Long = 0L,
    val uniqueArtists: Int = 0,
    val uniqueAlbums: Int = 0,
    val uniqueGenres: Int = 0,
    val totalPlayCount: Int = 0,
    val averageRating: Float = 0f,
    val favoriteSongsCount: Int = 0,
    val mostPlayedSong: Song? = null,
    val longestSong: Song? = null,
    val shortestSong: Song? = null,
    val newestSong: Song? = null,
    val oldestSong: Song? = null
) {
    val formattedTotalDuration: String
        get() = formatDuration(totalDuration)
    
    val formattedAverageDuration: String
        get() = formatDuration(averageSongDuration)
    
    val diversityScore: Float
        get() = if (totalSongs > 0) {
            (uniqueArtists + uniqueAlbums + uniqueGenres).toFloat() / (totalSongs * 3)
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
}

/**
 * Queue source information
 */
sealed class QueueSource {
    object Manual : QueueSource()
    data class Album(val album: com.tinhtx.localplayerapplication.domain.model.Album) : QueueSource()
    data class Artist(val artist: com.tinhtx.localplayerapplication.domain.model.Artist) : QueueSource()
    data class Playlist(val playlist: com.tinhtx.localplayerapplication.domain.model.Playlist) : QueueSource()
    data class Search(val query: String) : QueueSource()
    data class Shuffle(val source: String) : QueueSource()
    data class Radio(val seed: Song) : QueueSource()
    
    fun getDisplayName(): String = when (this) {
        is Manual -> "Manual Queue"
        is Album -> "Playing from ${album.name}"
        is Artist -> "Playing from ${artist.name}"
        is Playlist -> "Playing from ${playlist.name}"
        is Search -> "Search results for \"$query\""
        is Shuffle -> "Shuffling $source"
        is Radio -> "Radio based on ${seed.title}"
    }
}

/**
 * Repeat modes for queue playback
 */
enum class RepeatMode(val displayName: String) {
    OFF("Off"),
    ALL("Repeat All"),
    ONE("Repeat One");
    
    fun getNextMode(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}

/**
 * Queue operations for undo/redo
 */
sealed class QueueOperation {
    data class AddSong(val song: Song, val index: Int) : QueueOperation()
    data class RemoveSong(val song: Song, val index: Int) : QueueOperation()
    data class MoveSong(val fromIndex: Int, val toIndex: Int) : QueueOperation()
    data class AddMultipleSongs(val songs: List<Song>, val startIndex: Int) : QueueOperation()
    data class RemoveMultipleSongs(val songs: List<Pair<Song, Int>>) : QueueOperation()
    object ClearQueue : QueueOperation()
    data class ShuffleQueue(val originalOrder: List<Song>) : QueueOperation()
    data class ReplaceQueue(val oldQueue: List<Song>, val newQueue: List<Song>) : QueueOperation()
    
    fun getDescription(): String = when (this) {
        is AddSong -> "Added \"${song.title}\""
        is RemoveSong -> "Removed \"${song.title}\""
        is MoveSong -> "Moved song"
        is AddMultipleSongs -> "Added ${songs.size} songs"
        is RemoveMultipleSongs -> "Removed ${songs.size} songs"
        is ClearQueue -> "Cleared queue"
        is ShuffleQueue -> "Shuffled queue"
        is ReplaceQueue -> "Replaced queue"
    }
}

/**
 * Extension functions for QueueUiState
 */
fun QueueUiState.copyWithLoading(isLoading: Boolean): QueueUiState {
    return copy(isLoading = isLoading, error = if (isLoading) null else error)
}

fun QueueUiState.copyWithError(error: String?): QueueUiState {
    return copy(error = error, isLoading = false)
}

fun QueueUiState.copyWithQueue(
    songs: List<Song>,
    currentIndex: Int = -1,
    source: QueueSource? = null
): QueueUiState {
    return copy(
        queueSongs = songs,
        currentIndex = currentIndex,
        queueSource = source,
        queueStats = calculateQueueStats(songs),
        isLoading = false,
        error = null
    )
}

fun QueueUiState.copyWithPlaybackState(
    isPlaying: Boolean,
    currentSong: Song?,
    currentIndex: Int,
    progress: Float = 0f,
    currentPosition: Long = 0L
): QueueUiState {
    return copy(
        isPlaying = isPlaying,
        isPaused = !isPlaying && currentSong != null,
        currentSong = currentSong,
        currentIndex = currentIndex,
        progress = progress,
        currentPosition = currentPosition,
        duration = currentSong?.duration ?: 0L
    )
}

fun QueueUiState.copyWithSelection(
    isSelectionMode: Boolean,
    selectedSongs: Set<Long> = emptySet()
): QueueUiState {
    return copy(
        isSelectionMode = isSelectionMode,
        selectedSongs = if (isSelectionMode) selectedSongs else emptySet()
    )
}

private fun calculateQueueStats(songs: List<Song>): QueueStats {
    if (songs.isEmpty()) return QueueStats()
    
    val totalDuration = songs.sumOf { it.duration }
    val averageDuration = totalDuration / songs.size
    val uniqueArtists = songs.map { it.artist }.distinct().size
    val uniqueAlbums = songs.map { it.album }.distinct().size
    val uniqueGenres = songs.map { it.genre }.filter { it.isNotEmpty() }.distinct().size
    val totalPlayCount = songs.sumOf { it.playCount }
    val averageRating = songs.filter { it.rating > 0 }.map { it.rating }.average().toFloat()
    val favoriteSongs = songs.filter { it.isFavorite }
    
    return QueueStats(
        totalSongs = songs.size,
        totalDuration = totalDuration,
        averageSongDuration = averageDuration,
        uniqueArtists = uniqueArtists,
        uniqueAlbums = uniqueAlbums,
        uniqueGenres = uniqueGenres,
        totalPlayCount = totalPlayCount,
        averageRating = if (averageRating.isNaN()) 0f else averageRating,
        favoriteSongsCount = favoriteSongs.size,
        mostPlayedSong = songs.maxByOrNull { it.playCount },
        longestSong = songs.maxByOrNull { it.duration },
        shortestSong = songs.minByOrNull { it.duration },
        newestSong = songs.maxByOrNull { it.dateAdded },
        oldestSong = songs.minByOrNull { it.dateAdded }
    )
}

/**
 * Preview data for QueueUiState
 */
object QueueUiStatePreview {
    val empty = QueueUiState()
    
    val loading = QueueUiState(isLoading = true)
    
    val error = QueueUiState(error = "Failed to load queue")
    
    val withQueue = QueueUiState(
        queueSongs = listOf(
            Song(
                id = 1, title = "Bohemian Rhapsody", artist = "Queen",
                album = "A Night at the Opera", duration = 355000L, playCount = 89,
                rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 1975, genre = "Rock"
            ),
            Song(
                id = 2, title = "Hotel California", artist = "Eagles",
                album = "Hotel California", duration = 391000L, playCount = 156,
                rating = 4.8f, isFavorite = false, dateAdded = System.currentTimeMillis() - 86400000,
                year = 1976, genre = "Rock"
            ),
            Song(
                id = 3, title = "Stairway to Heaven", artist = "Led Zeppelin",
                album = "Led Zeppelin IV", duration = 482000L, playCount = 203,
                rating = 4.9f, isFavorite = true, dateAdded = System.currentTimeMillis() - 172800000,
                year = 1971, genre = "Rock"
            )
        ),
        currentSong = Song(
            id = 1, title = "Bohemian Rhapsody", artist = "Queen",
            album = "A Night at the Opera", duration = 355000L, playCount = 89,
            rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
            year = 1975, genre = "Rock"
        ),
        currentIndex = 0,
        isPlaying = true,
        progress = 0.35f,
        currentPosition = 124250L,
        queueSource = QueueSource.Album(
            Album(
                id = 1, name = "A Night at the Opera", artist = "Queen",
                songCount = 12, year = 1975, totalDuration = 2548000L
            )
        ),
        favoriteSongs = setOf(1L, 3L)
    )
    
    val shuffled = withQueue.copy(
        isShuffleEnabled = true,
        repeatMode = RepeatMode.ALL
    )
    
    val searching = withQueue.copy(
        isSearchActive = true,
        searchQuery = "queen",
        filteredSongs = listOf(
            Song(
                id = 1, title = "Bohemian Rhapsody", artist = "Queen",
                album = "A Night at the Opera", duration = 355000L, playCount = 89,
                rating = 5f, isFavorite = true, dateAdded = System.currentTimeMillis(),
                year = 1975, genre = "Rock"
            )
        )
    )
    
    val selectionMode = withQueue.copy(
        isSelectionMode = true,
        selectedSongs = setOf(1L, 2L)
    )
    
    val dragging = withQueue.copy(
        isDragging = true,
        draggedSongIndex = 0,
        dropTargetIndex = 2
    )
}
