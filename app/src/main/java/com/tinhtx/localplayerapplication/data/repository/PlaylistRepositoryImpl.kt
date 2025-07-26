package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.core.di.IoDispatcher
import com.tinhtx.localplayerapplication.data.local.database.dao.FavoriteDao
import com.tinhtx.localplayerapplication.data.local.database.dao.PlaylistDao
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistEntity
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaylistRepository {
    
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getPlaylistById(id: Long): Playlist? = withContext(ioDispatcher) {
        playlistDao.getPlaylistById(id)?.toDomain()
    }
    
    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongs(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun createPlaylist(name: String, description: String?): Long = withContext(ioDispatcher) {
        val playlist = PlaylistEntity(
            name = name,
            description = description
        )
        playlistDao.insertPlaylist(playlist)
    }
    
    override suspend fun updatePlaylist(playlist: Playlist) = withContext(ioDispatcher) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }
    
    override suspend fun deletePlaylist(playlistId: Long) = withContext(ioDispatcher) {
        playlistDao.deletePlaylistById(playlistId)
    }
    
    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(ioDispatcher) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }
    
    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(ioDispatcher) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    override suspend fun reorderPlaylistSongs(
        playlistId: Long,
        fromPosition: Int,
        toPosition: Int
    ) = withContext(ioDispatcher) {
        playlistDao.moveSong(playlistId, fromPosition, toPosition)
    }
    
    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylists(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getFavoritePlaylists(): Flow<List<Playlist>> {
        return playlistDao.getFavoritePlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getFavoriteSongs(): Flow<List<Song>> {
        return favoriteDao.getFavoriteSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun addToFavorites(songId: Long) = withContext(ioDispatcher) {
        favoriteDao.toggleFavorite(songId)
    }
    
    override suspend fun removeFromFavorites(songId: Long) = withContext(ioDispatcher) {
        favoriteDao.deleteFavoriteForSong(songId)
    }
    
    override suspend fun isSongFavorite(songId: Long): Boolean = withContext(ioDispatcher) {
        favoriteDao.isSongFavorite(songId)
    }
    
    override suspend fun getPlaylistCount(): Int = withContext(ioDispatcher) {
        playlistDao.getPlaylistCount()
    }
    
    override suspend fun getFavoriteCount(): Int = withContext(ioDispatcher) {
        favoriteDao.getFavoriteCount()
    }
}
