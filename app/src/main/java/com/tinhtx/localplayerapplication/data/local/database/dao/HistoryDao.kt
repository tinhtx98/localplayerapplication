package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.HistoryEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for History operations
 */
@Dao
interface HistoryDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM history ORDER BY playedAt DESC")
    suspend fun getAllHistory(): List<HistoryEntity>
    
    @Query("SELECT * FROM history ORDER BY playedAt DESC")
    fun getAllHistoryFlow(): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: Long): HistoryEntity?
    
    @Query("SELECT * FROM history WHERE songId = :songId ORDER BY playedAt DESC")
    suspend fun getHistoryForSong(songId: Long): List<HistoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(historyList: List<HistoryEntity>): List<Long>
    
    @Update
    suspend fun updateHistory(history: HistoryEntity)
    
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)
    
    @Query("DELETE FROM history WHERE playedAt < :cutoffTime")
    suspend fun deleteOldHistory(cutoffTime: Long)
    
    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
    
    // Recently Played Operations
    @Query("""
        SELECT DISTINCT s.* FROM songs s 
        INNER JOIN history h ON s.id = h.songId 
        ORDER BY h.playedAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecentlyPlayedSongs(limit: Int): List<SongEntity>
    
    @Query("""
        SELECT DISTINCT s.* FROM songs s 
        INNER JOIN history h ON s.id = h.songId 
        ORDER BY h.playedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyPlayedSongsFlow(limit: Int): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<HistoryEntity>
    
    @Query("""
        SELECT * FROM history 
        WHERE playedAt > :since 
        ORDER BY playedAt DESC
    """)
    suspend fun getHistorySince(since: Long): List<HistoryEntity>
    
    // Analytics Operations
    @Query("SELECT AVG(completionPercentage) FROM history WHERE playedAt > :since")
    suspend fun getAverageCompletionRate(since: Long): Float
    
    @Query("SELECT COUNT(*) FROM history WHERE playedAt > :since")
    suspend fun getPlayCountSince(since: Long): Int
    
    @Query("SELECT COUNT(DISTINCT sessionId) FROM history WHERE playedAt > :since")
    suspend fun getSessionCount(since: Long): Int
    
    @Query("SELECT SUM(playDuration) FROM history WHERE playedAt > :since")
    suspend fun getTotalPlaytimeSince(since: Long): Long
    
    @Query("SELECT AVG(playDuration) FROM history WHERE playedAt > :since")
    suspend fun getAveragePlayDuration(since: Long): Long
    
    @Query("SELECT COUNT(*) FROM history WHERE completionPercentage >= 0.8 AND playedAt > :since")
    suspend fun getCompletedPlaysCount(since: Long): Int
    
    @Query("SELECT COUNT(*) FROM history WHERE completionPercentage < 0.1 AND playedAt > :since")
    suspend fun getSkippedPlaysCount(since: Long): Int
    
    @Query("SELECT COUNT(*) FROM history WHERE skipped = 1 AND playedAt > :since")
    suspend fun getExplicitSkipsCount(since: Long): Int
    
    // Session Operations
    @Query("SELECT DISTINCT sessionId FROM history ORDER BY playedAt DESC")
    suspend fun getAllSessionIds(): List<String>
    
    @Query("SELECT * FROM history WHERE sessionId = :sessionId ORDER BY playedAt ASC")
    suspend fun getHistoryForSession(sessionId: String): List<HistoryEntity>
    
    @Query("""
        SELECT sessionId, COUNT(*) as songCount, SUM(playDuration) as totalDuration 
        FROM history 
        WHERE playedAt > :since 
        GROUP BY sessionId 
        ORDER BY MIN(playedAt) DESC
    """)
    suspend fun getSessionStatistics(since: Long): List<SessionStats>
    
    // Source Statistics
    @Query("SELECT source, COUNT(*) as count FROM history WHERE playedAt > :since GROUP BY source ORDER BY count DESC")
    suspend fun getPlaySourceStats(since: Long): List<SourceStat>
    
    @Query("SELECT * FROM history WHERE source = :source ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getHistoryBySource(source: String, limit: Int): List<HistoryEntity>
    
    // Most Played Operations
    @Query("""
        SELECT songId, COUNT(*) as playCount 
        FROM history 
        WHERE playedAt > :since 
        GROUP BY songId 
        ORDER BY playCount DESC 
        LIMIT :limit
    """)
    suspend fun getMostPlayedSongIds(since: Long, limit: Int): List<SongPlayCount>
    
    @Query("""
        SELECT s.*, COUNT(h.id) as playCount FROM songs s
        INNER JOIN history h ON s.id = h.songId
        WHERE h.playedAt > :since
        GROUP BY s.id
        ORDER BY playCount DESC
        LIMIT :limit
    """)
    suspend fun getMostPlayedSongs(since: Long, limit: Int): List<SongWithPlayCount>
    
    // Detailed History with Song Info
    @Query("""
        SELECT h.*, s.title, s.artist, s.album 
        FROM history h 
        INNER JOIN songs s ON h.songId = s.id 
        WHERE h.playedAt > :since 
        ORDER BY h.playedAt DESC
    """)
    suspend fun getDetailedHistorySince(since: Long): List<DetailedHistoryEntity>
    
    @Query("""
        SELECT h.*, s.title, s.artist, s.album 
        FROM history h 
        INNER JOIN songs s ON h.songId = s.id 
        ORDER BY h.playedAt DESC 
        LIMIT :limit
    """)
    suspend fun getDetailedRecentHistory(limit: Int): List<DetailedHistoryEntity>
    
    // Search Operations
    @Query("""
        SELECT DISTINCT s.* FROM songs s
        INNER JOIN history h ON s.id = h.songId
        WHERE s.title LIKE '%' || :query || '%' 
        OR s.artist LIKE '%' || :query || '%' 
        OR s.album LIKE '%' || :query || '%'
        ORDER BY h.playedAt DESC
    """)
    suspend fun searchPlayedSongs(query: String): List<SongEntity>
    
    // Time-based Statistics
    @Query("SELECT COUNT(DISTINCT DATE(playedAt/1000, 'unixepoch')) FROM history WHERE playedAt > :since")
    suspend fun getActiveDaysCount(since: Long): Int
    
    @Query("""
        SELECT DATE(playedAt/1000, 'unixepoch') as date, COUNT(*) as playCount
        FROM history 
        WHERE playedAt > :since
        GROUP BY date
        ORDER BY date DESC
    """)
    suspend fun getDailyPlayCounts(since: Long): List<DailyPlayCount>
    
    @Query("""
        SELECT strftime('%H', datetime(playedAt/1000, 'unixepoch')) as hour, COUNT(*) as playCount
        FROM history 
        WHERE playedAt > :since
        GROUP BY hour
        ORDER BY hour ASC
    """)
    suspend fun getHourlyPlayCounts(since: Long): List<HourlyPlayCount>
    
    // Advanced Analytics
    @Query("""
        SELECT AVG(sessionDuration) FROM (
            SELECT sessionId, SUM(playDuration) as sessionDuration 
            FROM history 
            WHERE playedAt > :since 
            GROUP BY sessionId
        )
    """)
    suspend fun getAverageSessionDuration(since: Long): Long
    
    @Query("""
        SELECT completionPercentage, COUNT(*) as count
        FROM history 
        WHERE playedAt > :since
        GROUP BY CAST(completionPercentage * 10 AS INTEGER)
        ORDER BY completionPercentage ASC
    """)
    suspend fun getCompletionDistribution(since: Long): List<CompletionDistribution>
    
    // Cleanup Operations
    @Query("""
        DELETE FROM history 
        WHERE songId NOT IN (SELECT id FROM songs)
    """)
    suspend fun removeOrphanedHistory()
    
    @Query("DELETE FROM history WHERE playedAt < :cutoffTime")
    suspend fun cleanupOldHistory(cutoffTime: Long)
    
    @Query("""
        DELETE FROM history 
        WHERE id NOT IN (
            SELECT id FROM history 
            ORDER BY playedAt DESC 
            LIMIT :maxRecords
        )
    """)
    suspend fun limitHistoryRecords(maxRecords: Int)
    
    // Flow Operations for Real-time Updates
    @Query("SELECT * FROM history WHERE completionPercentage >= :minCompletion ORDER BY playedAt DESC")
    fun getCompletedPlaysFlow(minCompletion: Float = 0.8f): Flow<List<HistoryEntity>>
    
    @Query("SELECT COUNT(*) FROM history")
    fun getTotalHistoryCountFlow(): Flow<Int>
}

/**
 * Data classes for query results
 */
data class SourceStat(
    val source: String?,
    val count: Int
)

data class SongPlayCount(
    val songId: Long,
    val playCount: Int
)

data class SongWithPlayCount(
    val id: Long,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val size: Long,
    val mimeType: String,
    val dateAdded: Long,
    val dateModified: Long,
    val year: Int,
    val trackNumber: Int,
    val genre: String?,
    val isFavorite: Boolean,
    val playCount: Int,
    val lastPlayed: Long
)

data class DetailedHistoryEntity(
    val id: Long,
    val songId: Long,
    val playedAt: Long,
    val playDuration: Long,
    val completionPercentage: Float,
    val source: String?,
    val sessionId: String,
    val skipped: Boolean,
    val title: String,
    val artist: String,
    val album: String
)

data class SessionStats(
    val sessionId: String,
    val songCount: Int,
    val totalDuration: Long
)

data class DailyPlayCount(
    val date: String,
    val playCount: Int
)

data class HourlyPlayCount(
    val hour: String,
    val playCount: Int
)

data class CompletionDistribution(
    val completionPercentage: Float,
    val count: Int
)
