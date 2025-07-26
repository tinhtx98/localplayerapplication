package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.HistoryEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN history h ON s.id = h.song_id
        ORDER BY h.played_at DESC
        LIMIT :limit
    """)
    fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM history ORDER BY played_at DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE song_id = :songId ORDER BY played_at DESC")
    fun getSongHistory(songId: Long): Flow<List<HistoryEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM history 
        WHERE song_id = :songId AND played_at >= :since
    """)
    suspend fun getPlayCountSince(songId: Long, since: Long): Int
    
    @Query("SELECT * FROM history WHERE played_at >= :since ORDER BY played_at DESC")
    fun getHistorySince(since: Long): Flow<List<HistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<HistoryEntity>)
    
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)
    
    @Query("DELETE FROM history WHERE song_id = :songId")
    suspend fun deleteHistoryForSong(songId: Long)
    
    @Query("DELETE FROM history WHERE played_at < :before")
    suspend fun deleteHistoryBefore(before: Long)
    
    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
    
    @Query("SELECT COUNT(*) FROM history")
    suspend fun getHistoryCount(): Int
    
    @Query("""
        SELECT AVG(completion_percentage) FROM history 
        WHERE song_id = :songId AND completion_percentage > 0.1
    """)
    suspend fun getAverageCompletionRate(songId: Long): Float
}
