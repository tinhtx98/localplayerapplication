package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    
    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String, description: String? = null): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    fun searchPlaylists(query: String): Flow<List<Playlist>>
    fun getFavoritePlaylists(): Flow<List<Playlist>>
    
    // Playlist songs management
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun reorderPlaylistSongs(playlistId: Long, fromPosition: Int, toPosition: Int)
    
    // Favorites
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun addToFavorites(songId: Long)
    suspend fun removeFromFavorites(songId: Long)
    suspend fun isSongFavorite(songId: Long): Boolean
    
    // Statistics
    suspend fun getPlaylistCount(): Int
    suspend fun getFavoriteCount(): Int
}
