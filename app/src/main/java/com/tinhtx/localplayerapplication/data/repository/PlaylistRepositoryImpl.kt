package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.data.local.database.dao.PlaylistDao
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistSongCrossRef
import com.tinhtx.localplayerapplication.data.local.database.entities.toDomain
import com.tinhtx.localplayerapplication.data.local.database.entities.toEntity
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlaylistRepository using Room DAO
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {
    
    // Playlist CRUD Operations - Mapped từ PlaylistDao
    override suspend fun getAllPlaylists(): List<Playlist> {
        return playlistDao.getAllPlaylists().map { it.toDomain() }
    }
    
    override fun getAllPlaylistsFlow(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getPlaylistById(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)?.toDomain()
    }
    
    override suspend fun getPlaylistByName(name: String): Playlist? {
        return playlistDao.getPlaylistByName(name)?.toDomain()
    }
    
    override suspend fun insertPlaylist(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(playlist.toEntity())
    }
    
    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }
    
    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }
    
    override suspend fun deletePlaylistById(id: Long) {
        playlistDao.deletePlaylistById(id)
    }
    
    // Playlist-Song Operations - Mapped từ PlaylistDao methods
    override suspend fun getSongsInPlaylist(playlistId: Long): List<Song> {
        return playlistDao.getSongsInPlaylist(playlistId).map { it.toDomain() }
    }
    
    override fun getSongsInPlaylistFlow(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylistFlow(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int?) {
        val maxPosition = playlistDao.getMaxPositionInPlaylist(playlistId) ?: -1
        val newPosition = position ?: (maxPosition + 1)
        
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            position = newPosition
        )
        playlistDao.insertPlaylistSongCrossRef(crossRef)
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val maxPosition = playlistDao.getMaxPositionInPlaylist(playlistId) ?: -1
        val crossRefs = songIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                position = maxPosition + 1 + index
            )
        }
        playlistDao.insertPlaylistSongCrossRefs(crossRefs)
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>) {
        songIds.forEach { songId ->
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun removeAllSongsFromPlaylist(playlistId: Long) {
        playlistDao.removeAllSongsFromPlaylist(playlistId)
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun moveSongInPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int) {
        playlistDao.reorderSongInPlaylist(playlistId, fromPosition, toPosition)
    }
    
    // Search and Filter Operations - Mapped từ PlaylistDao methods
    override suspend fun searchPlaylists(query: String): List<Playlist> {
        return playlistDao.searchPlaylists(query).map { it.toDomain() }
    }
    
    override fun searchPlaylistsFlow(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylistsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getPlaylistsContainingSong(songId: Long): List<Playlist> {
        return playlistDao.getPlaylistsContainingSong(songId).map { it.toDomain() }
    }
    
    override suspend fun getRecentlyCreatedPlaylists(limit: Int): List<Playlist> {
        val since = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
        return playlistDao.getRecentlyCreatedPlaylists(since).take(limit).map { it.toDomain() }
    }
    
    override suspend fun getRecentlyUpdatedPlaylists(limit: Int): List<Playlist> {
        val since = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days
        return playlistDao.getRecentlyUpdatedPlaylists(since).take(limit).map { it.toDomain() }
    }
    
    // Statistics Operations - Mapped từ PlaylistDao methods
    override suspend fun getPlaylistCount(): Int {
        return playlistDao.getPlaylistCount()
    }
    
    override suspend fun getTotalSongsInAllPlaylists(): Int {
        return playlistDao.getTotalSongsInAllPlaylists()
    }
    
    override suspend fun getTotalDurationOfAllPlaylists(): Long {
        return playlistDao.getTotalDurationOfAllPlaylists()
    }
    
    override suspend fun getAverageSongsPerPlaylist(): Double {
        return playlistDao.getAverageSongsPerPlaylist()
    }
    
    override suspend fun getSongCountInPlaylist(playlistId: Long): Int {
        return playlistDao.getSongCountInPlaylist(playlistId)
    }
    
    // Sorting Operations - Mapped từ PlaylistDao methods
    override suspend fun getPlaylistsSortedByName(): List<Playlist> {
        return playlistDao.getPlaylistsSortedByName().map { it.toDomain() }
    }
    
    override suspend fun getPlaylistsSortedByCreationDate(): List<Playlist> {
        return playlistDao.getPlaylistsSortedByCreationDate().map { it.toDomain() }
    }
    
    override suspend fun getPlaylistsSortedByUpdateDate(): List<Playlist> {
        return playlistDao.getPlaylistsSortedByUpdateDate().map { it.toDomain() }
    }
    
    override suspend fun getPlaylistsSortedBySongCount(): List<Playlist> {
        return playlistDao.getPlaylistsSortedBySongCount().map { it.toDomain() }
    }
    
    override suspend fun getPlaylistsSortedByDuration(): List<Playlist> {
        return playlistDao.getPlaylistsSortedByDuration().map { it.toDomain() }
    }
    
    // Playlist Management - Using available DAO methods
    override suspend fun updatePlaylistStatistics(playlistId: Long) {
        playlistDao.updatePlaylistStatistics(playlistId)
    }
    
    override suspend fun updateAllPlaylistStatistics() {
        playlistDao.updateAllPlaylistStatistics()
    }
    
    override suspend fun duplicatePlaylist(playlistId: Long, newName: String): Long {
        val originalPlaylist = playlistDao.getPlaylistById(playlistId) ?: return -1
        val songs = playlistDao.getSongsInPlaylist(playlistId)
        
        val newPlaylist = originalPlaylist.copy(
            id = 0,
            name = newName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val newPlaylistId = playlistDao.insertPlaylist(newPlaylist)
        
        val crossRefs = songs.mapIndexed { index, song ->
            PlaylistSongCrossRef(
                playlistId = newPlaylistId,
                songId = song.id,
                position = index
            )
        }
        
        playlistDao.insertPlaylistSongCrossRefs(crossRefs)
        playlistDao.updatePlaylistStatistics(newPlaylistId)
        
        return newPlaylistId
    }
    
    override suspend fun mergePlaylist(sourcePlaylistId: Long, targetPlaylistId: Long) {
        val sourceSongs = playlistDao.getSongsInPlaylist(sourcePlaylistId)
        val maxPosition = playlistDao.getMaxPositionInPlaylist(targetPlaylistId) ?: -1
        
        val crossRefs = sourceSongs.mapIndexed { index, song ->
            PlaylistSongCrossRef(
                playlistId = targetPlaylistId,
                songId = song.id,
                position = maxPosition + 1 + index
            )
        }
        
        playlistDao.insertPlaylistSongCrossRefs(crossRefs)
        playlistDao.updatePlaylistStatistics(targetPlaylistId)
    }
    
    override suspend fun checkIfSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        return playlistDao.checkIfSongInPlaylist(playlistId, songId) > 0
    }
    
    // Cleanup Operations - Mapped từ PlaylistDao methods
    override suspend fun removeEmptyPlaylists() {
        playlistDao.removeEmptyPlaylists()
    }
    
    override suspend fun removeOrphanedPlaylistSongReferences() {
        playlistDao.removeOrphanedPlaylistSongReferences()
    }
}
