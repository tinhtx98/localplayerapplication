package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?
    
    @Query("SELECT * FROM songs WHERE media_store_id = :mediaStoreId")
    suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?
    
    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE artist_id = :artistId ORDER BY title ASC")
    fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>
    
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int = 20): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int = 20): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY date_added DESC LIMIT :limit")
    fun getRecentlyAddedSongs(limit: Int = 20): Flow<List<SongEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Update
    suspend fun updateSong(song: SongEntity)
    
    @Delete
    suspend fun deleteSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("UPDATE songs SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Query("UPDATE songs SET play_count = play_count + 1, last_played = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT SUM(duration) FROM songs")
    suspend fun getTotalDuration(): Long
    
    @Query("SELECT DISTINCT year FROM songs WHERE year > 0 ORDER BY year DESC")
    suspend fun getAllYears(): List<Int>
    
    @Query("SELECT * FROM songs WHERE year = :year ORDER BY title ASC")
    fun getSongsByYear(year: Int): Flow<List<SongEntity>>
}
