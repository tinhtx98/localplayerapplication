package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting playlists
 */
class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    
    /**
     * Get all playlists
     */
    suspend fun execute(): Result<List<Playlist>> {
        return try {
            val playlists = playlistRepository.getAllPlaylists()
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all playlists as Flow for reactive UI
     */
    fun executeFlow(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylistsFlow()
    }
    
    /**
     * Get playlist by ID
     */
    suspend fun getPlaylistById(playlistId: Long): Result<Playlist?> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            Result.success(playlist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playlist by name
     */
    suspend fun getPlaylistByName(name: String): Result<Playlist?> {
        return try {
            val playlist = playlistRepository.getPlaylistByName(name)
            Result.success(playlist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs in playlist
     */
    suspend fun getSongsInPlaylist(playlistId: Long): Result<List<Song>> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs in playlist as Flow
     */
    fun getSongsInPlaylistFlow(playlistId: Long): Flow<List<Song>> {
        return playlistRepository.getSongsInPlaylistFlow(playlistId)
    }
    
    /**
     * Search playlists
     */
    suspend fun searchPlaylists(query: String): Result<List<Playlist>> {
        return try {
            if (query.isBlank()) {
                Result.success(emptyList())
            } else {
                val playlists = playlistRepository.searchPlaylists(query)
                Result.success(playlists)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search playlists with Flow
     */
    fun searchPlaylistsFlow(query: String): Flow<List<Playlist>> {
        return playlistRepository.searchPlaylistsFlow(query)
    }
    
    /**
     * Get playlists sorted by different criteria
     */
    suspend fun getPlaylistsSorted(sortBy: PlaylistSortOrder): Result<List<Playlist>> {
        return try {
            val playlists = when (sortBy) {
                PlaylistSortOrder.NAME -> playlistRepository.getPlaylistsSortedByName()
                PlaylistSortOrder.CREATION_DATE -> playlistRepository.getPlaylistsSortedByCreationDate()
                PlaylistSortOrder.UPDATE_DATE -> playlistRepository.getPlaylistsSortedByUpdateDate()
                PlaylistSortOrder.SONG_COUNT -> playlistRepository.getPlaylistsSortedBySongCount()
                PlaylistSortOrder.DURATION -> playlistRepository.getPlaylistsSortedByDuration()
            }
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently created playlists
     */
    suspend fun getRecentlyCreatedPlaylists(limit: Int = 10): Result<List<Playlist>> {
        return try {
            val playlists = playlistRepository.getRecentlyCreatedPlaylists(limit)
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently updated playlists
     */
    suspend fun getRecentlyUpdatedPlaylists(limit: Int = 10): Result<List<Playlist>> {
        return try {
            val playlists = playlistRepository.getRecentlyUpdatedPlaylists(limit)
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playlists containing specific song
     */
    suspend fun getPlaylistsContainingSong(songId: Long): Result<List<Playlist>> {
        return try {
            val playlists = playlistRepository.getPlaylistsContainingSong(songId)
            Result.success(playlists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get empty playlists
     */
    suspend fun getEmptyPlaylists(): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val emptyPlaylists = allPlaylists.filter { it.songCount == 0 }
            Result.success(emptyPlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playlist statistics
     */
    suspend fun getPlaylistStatistics(): Result<PlaylistStatistics> {
        return try {
            val totalPlaylists = playlistRepository.getPlaylistCount()
            val totalSongs = playlistRepository.getTotalSongsInAllPlaylists()
            val totalDuration = playlistRepository.getTotalDurationOfAllPlaylists()
            val averageSongs = playlistRepository.getAverageSongsPerPlaylist()
            
            val allPlaylists = playlistRepository.getAllPlaylists()
            val largestPlaylist = allPlaylists.maxByOrNull { it.songCount }
            val longestPlaylist = allPlaylists.maxByOrNull { it.duration }
            val newestPlaylist = allPlaylists.maxByOrNull { it.createdAt }
            val oldestPlaylist = allPlaylists.minByOrNull { it.createdAt }
            
            val statistics = PlaylistStatistics(
                totalPlaylists = totalPlaylists,
                totalSongs = totalSongs,
                totalDuration = totalDuration,
                averageSongsPerPlaylist = averageSongs,
                largestPlaylist = largestPlaylist,
                longestPlaylist = longestPlaylist,
                newestPlaylist = newestPlaylist,
                oldestPlaylist = oldestPlaylist
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playlists by minimum song count
     */
    suspend fun getPlaylistsWithMinSongs(minSongs: Int): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val filteredPlaylists = allPlaylists.filter { it.songCount >= minSongs }
            Result.success(filteredPlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playlists by duration range
     */
    suspend fun getPlaylistsByDurationRange(minDuration: Long, maxDuration: Long): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val filteredPlaylists = allPlaylists.filter { playlist ->
                playlist.duration in minDuration..maxDuration
            }
            Result.success(filteredPlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get system playlists (favorites, recently played, etc.)
     */
    suspend fun getSystemPlaylists(): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val systemPlaylists = allPlaylists.filter { it.isSystemPlaylist }
            Result.success(systemPlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user-created playlists
     */
    suspend fun getUserPlaylists(): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val userPlaylists = allPlaylists.filter { !it.isSystemPlaylist }
            Result.success(userPlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Enum for playlist sorting options
 */
enum class PlaylistSortOrder {
    NAME,
    CREATION_DATE,
    UPDATE_DATE,
    SONG_COUNT,
    DURATION
}

/**
 * Data class for playlist statistics
 */
data class PlaylistStatistics(
    val totalPlaylists: Int,
    val totalSongs: Int,
    val totalDuration: Long,
    val averageSongsPerPlaylist: Double,
    val largestPlaylist: Playlist?,
    val longestPlaylist: Playlist?,
    val newestPlaylist: Playlist?,
    val oldestPlaylist: Playlist?
) {
    val totalDurationHours: Double
        get() = totalDuration / (1000.0 * 60.0 * 60.0)
    
    val averageDurationPerPlaylist: Long
        get() = if (totalPlaylists > 0) totalDuration / totalPlaylists else 0L
}
