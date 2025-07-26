package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistSongCrossRef
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoritePlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref ps ON s.id = ps.song_id
        WHERE ps.playlist_id = :playlistId
        ORDER BY ps.position ASC
    """)
    fun getPlaylistSongs(playlistId: Long): Flow<List<SongEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM playlist_song_cross_ref 
        WHERE playlist_id = :playlistId
    """)
    suspend fun getPlaylistSongCount(playlistId: Long): Int
    
    @Query("""
        SELECT SUM(s.duration) FROM songs s
        INNER JOIN playlist_song_cross_ref ps ON s.id = ps.song_id
        WHERE ps.playlist_id = :playlistId
    """)
    suspend fun getPlaylistDuration(playlistId: Long): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId")
    suspend fun deleteAllSongsFromPlaylist(playlistId: Long)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    @Query("""
        UPDATE playlist_song_cross_ref 
        SET position = :newPosition 
        WHERE playlist_id = :playlistId AND song_id = :songId
    """)
    suspend fun updateSongPosition(playlistId: Long, songId: Long, newPosition: Int)
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val currentCount = getPlaylistSongCount(playlistId)
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            position = currentCount
        )
        insertPlaylistSongCrossRef(crossRef)
        
        // Update playlist metadata
        val playlist = getPlaylistById(playlistId)
        playlist?.let { 
            val newSongCount = getPlaylistSongCount(playlistId)
            val newDuration = getPlaylistDuration(playlistId)
            updatePlaylist(
                it.copy(
                    songCount = newSongCount,
                    duration = newDuration,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    @Transaction
    suspend fun moveSong(playlistId: Long, fromPosition: Int, toPosition: Int) {
        // This is a simplified version - in practice you'd need more complex logic
        // to handle position updates for all affected songs
    }
}
