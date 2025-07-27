package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for playlist operations
 */
interface PlaylistRepository {
    
    // Basic CRUD operations
    suspend fun getAllPlaylists(): List<Playlist>
    fun getAllPlaylistsFlow(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun getPlaylistByName(name: String): Playlist?
    
    suspend fun insertPlaylist(playlist: Playlist): Long
    suspend fun insertPlaylists(playlists: List<Playlist>): List<Long>
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun updatePlaylists(playlists: List<Playlist>)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun deletePlaylistById(id: Long)
    suspend fun deletePlaylists(playlistIds: List<Long>): Int
    
    // Playlist content management
    suspend fun getSongsInPlaylist(playlistId: Long): List<Song>
    fun getSongsInPlaylistFlow(playlistId: Long): Flow<List<Song>>
    suspend fun getPlaylistSongCount(playlistId: Long): Int
    suspend fun getPlaylistDuration(playlistId: Long): Long
    
    // Adding songs to playlists
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int? = null)
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun addSongToPlaylistAtPosition(playlistId: Long, songId: Long, position: Int)
    suspend fun addSongsToPlaylistAtPosition(playlistId: Long, songIds: List<Long>, startPosition: Int)
    
    // Removing songs from playlists
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun removeSongFromPlaylistAtPosition(playlistId: Long, position: Int)
    suspend fun removeAllSongsFromPlaylist(playlistId: Long)
    
    // Playlist organization
    suspend fun reorderPlaylistSongs(playlistId: Long, fromPosition: Int, toPosition: Int)
    suspend fun shufflePlaylist(playlistId: Long)
    suspend fun sortPlaylistSongs(playlistId: Long, sortBy: String, ascending: Boolean = true)
    
    // Search and filtering
    suspend fun searchPlaylists(query: String): List<Playlist>
    fun searchPlaylistsFlow(query: String): Flow<List<Playlist>>
    suspend fun searchSongsInPlaylist(playlistId: Long, query: String): List<Song>
    suspend fun getPlaylistsContainingSong(songId: Long): List<Playlist>
    suspend fun checkIfSongInPlaylist(playlistId: Long, songId: Long): Boolean
    
    // Sorting and organization
    suspend fun getPlaylistsSortedByName(): List<Playlist>
    suspend fun getPlaylistsSortedByCreationDate(): List<Playlist>
    suspend fun getPlaylistsSortedByUpdateDate(): List<Playlist>
    suspend fun getPlaylistsSortedBySongCount(): List<Playlist>
    suspend fun getPlaylistsSortedByDuration(): List<Playlist>
    
    // Recent and popular
    suspend fun getRecentlyCreatedPlaylists(limit: Int): List<Playlist>
    suspend fun getRecentlyUpdatedPlaylists(limit: Int): List<Playlist>
    suspend fun getMostPopularPlaylists(limit: Int): List<Playlist>
    suspend fun getRecentlyPlayedPlaylists(limit: Int): List<Playlist>
    
    // Playlist duplication and export
    suspend fun duplicatePlaylist(sourceId: Long, newName: String): Long
    suspend fun mergePlaylist(targetId: Long, sourceId: Long): Boolean
    suspend fun exportPlaylist(playlistId: Long): String
    suspend fun importPlaylist(playlistData: String): Long
    
    // Statistics and analytics
    suspend fun getPlaylistCount(): Int
    suspend fun getTotalSongsInAllPlaylists(): Int
    suspend fun getTotalDurationOfAllPlaylists(): Long
    suspend fun getAverageSongsPerPlaylist(): Double
    suspend fun getAveragePlaylistDuration(): Long
    suspend fun getPlaylistStatistics(playlistId: Long): PlaylistStatistics
    
    // Maintenance and validation
    suspend fun removeOrphanedPlaylistSongReferences()
    suspend fun updatePlaylistStatistics(playlistId: Long)
    suspend fun updateAllPlaylistStatistics()
    suspend fun validatePlaylistIntegrity(): List<String>
    suspend fun fixPlaylistInconsistencies(): Int
    suspend fun cleanupEmptyPlaylists(): Int
    
    // System playlists
    suspend fun getSystemPlaylists(): List<Playlist>
    suspend fun getUserPlaylists(): List<Playlist>
    suspend fun createSystemPlaylist(name: String, type: String): Long
    suspend fun isSystemPlaylist(playlistId: Long): Boolean
    
    // Playlist sharing and collaboration
    suspend fun sharePlaylist(playlistId: Long): String
    suspend fun getSharedPlaylists(): List<Playlist>
    suspend fun subscribeToSharedPlaylist(shareId: String): Long
    
    // Smart playlists
    suspend fun createSmartPlaylist(name: String, criteria: String): Long
    suspend fun updateSmartPlaylistCriteria(playlistId: Long, criteria: String)
    suspend fun refreshSmartPlaylist(playlistId: Long)
    suspend fun getSmartPlaylists(): List<Playlist>
    
    // Playlist history and versioning
    suspend fun getPlaylistHistory(playlistId: Long): List<PlaylistHistoryEntry>
    suspend fun revertPlaylistToHistory(playlistId: Long, historyEntryId: Long)
    suspend fun createPlaylistSnapshot(playlistId: Long): Long
}
