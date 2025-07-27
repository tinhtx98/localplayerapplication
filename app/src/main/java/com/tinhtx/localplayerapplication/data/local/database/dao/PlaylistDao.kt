package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.PlaylistSongCrossRef
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Playlist operations
 */
@Dao
interface PlaylistDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    suspend fun getAllPlaylists(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    
    @Query("SELECT * FROM playlists WHERE name = :name")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>): List<Long>
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Update
    suspend fun updatePlaylists(playlists: List<PlaylistEntity>)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)
    
    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()
    
    // Playlist-Song Relationship Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)
    
    @Delete
    suspend fun deletePlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun removeAllSongsFromPlaylist(playlistId: Long)
    
    // Get Songs in Playlist
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref pscr ON s.id = pscr.songId
        WHERE pscr.playlistId = :playlistId
        ORDER BY pscr.position ASC
    """)
    suspend fun getSongsInPlaylist(playlistId: Long): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref pscr ON s.id = pscr.songId
        WHERE pscr.playlistId = :playlistId
        ORDER BY pscr.position ASC
    """)
    fun getSongsInPlaylistFlow(playlistId: Long): Flow<List<SongEntity>>
    
    @Query("""
        SELECT pscr.* FROM playlist_song_cross_ref pscr
        WHERE pscr.playlistId = :playlistId
        ORDER BY pscr.position ASC
    """)
    suspend fun getPlaylistSongCrossRefs(playlistId: Long): List<PlaylistSongCrossRef>
    
    // Search Operations
    @Query("""
        SELECT * FROM playlists 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchPlaylists(query: String): List<PlaylistEntity>
    
    @Query("""
        SELECT * FROM playlists 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchPlaylistsFlow(query: String): Flow<List<PlaylistEntity>>
    
    // Statistics Operations
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    @Query("SELECT SUM(songCount) FROM playlists")
    suspend fun getTotalSongsInAllPlaylists(): Int
    
    @Query("SELECT SUM(duration) FROM playlists")
    suspend fun getTotalDurationOfAllPlaylists(): Long
    
    @Query("SELECT AVG(songCount) FROM playlists")
    suspend fun getAverageSongsPerPlaylist(): Double
    
    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getSongCountInPlaylist(playlistId: Long): Int
    
    // Sorting Operations
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    suspend fun getPlaylistsSortedByName(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    suspend fun getPlaylistsSortedByCreationDate(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    suspend fun getPlaylistsSortedByUpdateDate(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY songCount DESC")
    suspend fun getPlaylistsSortedBySongCount(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists ORDER BY duration DESC")
    suspend fun getPlaylistsSortedByDuration(): List<PlaylistEntity>
    
    // Update Operations
    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE playlists SET 
        songCount = (SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = playlists.id),
        duration = (SELECT COALESCE(SUM(s.duration), 0) FROM songs s 
                   INNER JOIN playlist_song_cross_ref pscr ON s.id = pscr.songId 
                   WHERE pscr.playlistId = playlists.id),
        updatedAt = :timestamp
        WHERE id = :playlistId
    """)
    suspend fun updatePlaylistStatistics(playlistId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE playlists SET 
        songCount = (SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = playlists.id),
        duration = (SELECT COALESCE(SUM(s.duration), 0) FROM songs s 
                   INNER JOIN playlist_song_cross_ref pscr ON s.id = pscr.songId 
                   WHERE pscr.playlistId = playlists.id),
        updatedAt = :timestamp
    """)
    suspend fun updateAllPlaylistStatistics(timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE playlists SET artworkPath = :artworkPath WHERE id = :playlistId")
    suspend fun updatePlaylistArtwork(playlistId: Long, artworkPath: String?)
    
    // Position Management
    @Query("SELECT MAX(position) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxPositionInPlaylist(playlistId: Long): Int?
    
    @Query("""
        UPDATE playlist_song_cross_ref 
        SET position = position - 1 
        WHERE playlistId = :playlistId AND position > :removedPosition
    """)
    suspend fun adjustPositionsAfterRemoval(playlistId: Long, removedPosition: Int)
    
    @Query("""
        UPDATE playlist_song_cross_ref 
        SET position = CASE 
            WHEN position = :fromPosition THEN :toPosition
            WHEN position > :fromPosition AND position <= :toPosition THEN position - 1
            WHEN position < :fromPosition AND position >= :toPosition THEN position + 1
            ELSE position
        END
        WHERE playlistId = :playlistId
    """)
    suspend fun reorderSongInPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int)
    
    // Advanced Queries
    @Query("SELECT * FROM playlists WHERE songCount > :minSongCount ORDER BY songCount DESC")
    suspend fun getPlaylistsWithMinSongs(minSongCount: Int): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE duration > :minDuration ORDER BY duration DESC")
    suspend fun getPlaylistsWithMinDuration(minDuration: Long): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE createdAt > :since ORDER BY createdAt DESC")
    suspend fun getRecentlyCreatedPlaylists(since: Long): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE updatedAt > :since ORDER BY updatedAt DESC")
    suspend fun getRecentlyUpdatedPlaylists(since: Long): List<PlaylistEntity>
    
    @Query("""
        SELECT p.* FROM playlists p
        INNER JOIN playlist_song_cross_ref pscr ON p.id = pscr.playlistId
        WHERE pscr.songId = :songId
        ORDER BY p.name ASC
    """)
    suspend fun getPlaylistsContainingSong(songId: Long): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE artworkPath IS NOT NULL ORDER BY name ASC")
    suspend fun getPlaylistsWithArtwork(): List<PlaylistEntity>
    
    @Query("SELECT * FROM playlists WHERE artworkPath IS NULL ORDER BY name ASC")
    suspend fun getPlaylistsWithoutArtwork(): List<PlaylistEntity>
    
    // Duplicate Detection
    @Query("""
        SELECT COUNT(*) FROM playlist_song_cross_ref 
        WHERE playlistId = :playlistId AND songId = :songId
    """)
    suspend fun checkIfSongInPlaylist(playlistId: Long, songId: Long): Int
    
    // Cleanup Operations
    @Query("DELETE FROM playlists WHERE songCount = 0")
    suspend fun removeEmptyPlaylists()
    
    @Query("""
        DELETE FROM playlist_song_cross_ref 
        WHERE songId NOT IN (SELECT id FROM songs)
    """)
    suspend fun removeOrphanedPlaylistSongReferences()
}
