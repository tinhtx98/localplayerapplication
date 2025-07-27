package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Song operations
 */
@Dao
interface SongDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<SongEntity>
    
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?
    
    @Query("SELECT * FROM songs WHERE path = :path")
    suspend fun getSongByPath(path: String): SongEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>): List<Long>
    
    @Update
    suspend fun updateSong(song: SongEntity)
    
    @Update
    suspend fun updateSongs(songs: List<SongEntity>)
    
    @Delete
    suspend fun deleteSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)
    
    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    // Filtering and Searching
    @Query("SELECT * FROM songs WHERE album = :album ORDER BY trackNumber ASC, title ASC")
    suspend fun getSongsByAlbum(album: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC, title ASC")
    suspend fun getSongsByAlbumId(albumId: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album ASC, trackNumber ASC, title ASC")
    suspend fun getSongsByArtist(artist: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE artistId = :artistId ORDER BY album ASC, trackNumber ASC, title ASC")
    suspend fun getSongsByArtistId(artistId: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY artist ASC, album ASC, trackNumber ASC")
    suspend fun getSongsByGenre(genre: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE year = :year ORDER BY artist ASC, album ASC, trackNumber ASC")
    suspend fun getSongsByYear(year: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE path LIKE :pathPattern ORDER BY path ASC")
    suspend fun getSongsInPath(pathPattern: String): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<Long>): List<SongEntity>
    
    // Search Operations
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        OR genre LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN artist LIKE :query || '%' THEN 2
                WHEN album LIKE :query || '%' THEN 3
                ELSE 4
            END,
            title ASC
    """)
    suspend fun searchSongs(query: String): List<SongEntity>
    
    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        OR genre LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchSongsFlow(query: String): Flow<List<SongEntity>>
    
    // Recently Added and Popular
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAddedSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE dateAdded > :since ORDER BY dateAdded DESC")
    suspend fun getSongsAddedSince(since: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE dateModified > :since ORDER BY dateModified DESC")
    suspend fun getSongsModifiedAfter(since: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    suspend fun getRecentlyPlayedSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE lastPlayed > :since ORDER BY lastPlayed DESC")
    suspend fun getRecentlyPlayedSongs(since: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC, lastPlayed DESC LIMIT :limit")
    suspend fun getMostPlayedSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE playCount >= :minCount ORDER BY playCount DESC, title ASC")
    suspend fun getSongsWithMinPlayCount(minCount: Int): List<SongEntity>
    
    // Favorites
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    suspend fun getFavoriteSongs(): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>
    
    @Query("SELECT COUNT(*) FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    
    @Query("UPDATE songs SET isFavorite = 1 WHERE id IN (:songIds)")
    suspend fun markSongsAsFavorite(songIds: List<Long>)
    
    @Query("UPDATE songs SET isFavorite = 0 WHERE id IN (:songIds)")
    suspend fun unmarkSongsAsFavorite(songIds: List<Long>)
    
    // Play Count and History
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE songs SET playCount = :playCount WHERE id = :songId")
    suspend fun updatePlayCount(songId: Long, playCount: Int)
    
    @Query("UPDATE songs SET lastPlayed = :timestamp WHERE id = :songId")
    suspend fun updateLastPlayed(songId: Long, timestamp: Long)
    
    @Query("UPDATE songs SET playCount = 0, lastPlayed = 0 WHERE id = :songId")
    suspend fun clearSongHistory(songId: Long)
    
    @Query("UPDATE songs SET playCount = 0, lastPlayed = 0")
    suspend fun clearAllHistory()
    
    // Statistics Queries
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT COUNT(*) FROM songs WHERE playCount > 0")
    suspend fun getPlayedSongCount(): Int
    
    @Query("SELECT COUNT(*) FROM songs WHERE playCount = 0")
    suspend fun getUnplayedSongCount(): Int
    
    @Query("SELECT SUM(duration) FROM songs")
    suspend fun getTotalDuration(): Long
    
    @Query("SELECT SUM(size) FROM songs")
    suspend fun getTotalSize(): Long
    
    @Query("SELECT AVG(duration) FROM songs")
    suspend fun getAverageDuration(): Long
    
    @Query("SELECT AVG(size) FROM songs")
    suspend fun getAverageSize(): Long
    
    @Query("SELECT SUM(playCount) FROM songs")
    suspend fun getTotalPlayCount(): Int
    
    @Query("SELECT AVG(playCount) FROM songs WHERE playCount > 0")
    suspend fun getAveragePlayCount(): Double
    
    // Genre and Year Statistics
    @Query("SELECT genre, COUNT(*) as count FROM songs WHERE genre IS NOT NULL AND genre != '' GROUP BY genre ORDER BY count DESC")
    suspend fun getGenreStatistics(): Map<String, Int>
    
    @Query("SELECT year, COUNT(*) as count FROM songs WHERE year > 0 GROUP BY year ORDER BY count DESC")
    suspend fun getYearStatistics(): Map<Int, Int>
    
    @Query("SELECT artist, COUNT(*) as count FROM songs WHERE artist IS NOT NULL AND artist != '' GROUP BY artist ORDER BY count DESC")
    suspend fun getArtistSongCounts(): Map<String, Int>
    
    @Query("SELECT album, COUNT(*) as count FROM songs WHERE album IS NOT NULL AND album != '' GROUP BY album ORDER BY count DESC")
    suspend fun getAlbumSongCounts(): Map<String, Int>
    
    // Distinct Values
    @Query("SELECT DISTINCT album FROM songs WHERE album IS NOT NULL AND album != '' ORDER BY album ASC")
    suspend fun getAllAlbumNames(): List<String>
    
    @Query("SELECT DISTINCT artist FROM songs WHERE artist IS NOT NULL AND artist != '' ORDER BY artist ASC")
    suspend fun getAllArtistNames(): List<String>
    
    @Query("SELECT DISTINCT genre FROM songs WHERE genre IS NOT NULL AND genre != '' ORDER BY genre ASC")
    suspend fun getAllGenres(): List<String>
    
    @Query("SELECT DISTINCT year FROM songs WHERE year > 0 ORDER BY year DESC")
    suspend fun getAllYears(): List<Int>
    
    @Query("SELECT DISTINCT format FROM songs WHERE format IS NOT NULL AND format != '' ORDER BY format ASC")
    suspend fun getAllFormats(): List<String>
    
    // Extremes
    @Query("SELECT * FROM songs ORDER BY dateAdded ASC LIMIT 1")
    suspend fun getOldestSong(): SongEntity?
    
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 1")
    suspend fun getNewestSong(): SongEntity?
    
    @Query("SELECT * FROM songs ORDER BY duration DESC LIMIT 1")
    suspend fun getLongestSong(): SongEntity?
    
    @Query("SELECT * FROM songs ORDER BY duration ASC LIMIT 1")
    suspend fun getShortestSong(): SongEntity?
    
    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC, lastPlayed DESC LIMIT 1")
    suspend fun getMostPlayedSong(): SongEntity?
    
    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT 1")
    suspend fun getLastPlayedSong(): SongEntity?
    
    // Filtering by Properties
    @Query("SELECT * FROM songs WHERE duration BETWEEN :minDuration AND :maxDuration ORDER BY title ASC")
    suspend fun getSongsByDurationRange(minDuration: Long, maxDuration: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE size BETWEEN :minSize AND :maxSize ORDER BY title ASC")
    suspend fun getSongsBySizeRange(minSize: Long, maxSize: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE year BETWEEN :startYear AND :endYear ORDER BY year DESC, artist ASC, album ASC")
    suspend fun getSongsByYearRange(startYear: Int, endYear: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE bitrate >= :minBitrate ORDER BY bitrate DESC, title ASC")
    suspend fun getSongsByMinBitrate(minBitrate: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE sampleRate >= :minSampleRate ORDER BY sampleRate DESC, title ASC")
    suspend fun getSongsByMinSampleRate(minSampleRate: Int): List<SongEntity>
    
    // Advanced Queries
    @Query("""
        SELECT * FROM songs 
        WHERE (:artist IS NULL OR artist = :artist)
        AND (:album IS NULL OR album = :album)
        AND (:genre IS NULL OR genre = :genre)
        AND (:year IS NULL OR year = :year)
        AND (:minDuration IS NULL OR duration >= :minDuration)
        AND (:maxDuration IS NULL OR duration <= :maxDuration)
        ORDER BY artist ASC, album ASC, trackNumber ASC, title ASC
    """)
    suspend fun getFilteredSongs(
        artist: String? = null,
        album: String? = null,
        genre: String? = null,
        year: Int? = null,
        minDuration: Long? = null,
        maxDuration: Long? = null
    ): List<SongEntity>
    
    // Validation Queries
    @Query("SELECT * FROM songs WHERE path NOT LIKE '%/%' OR title IS NULL OR title = '' OR artist IS NULL OR artist = ''")
    suspend fun getInvalidSongs(): List<SongEntity>
    
    @Query("SELECT path, COUNT(*) as count FROM songs GROUP BY path HAVING count > 1")
    suspend fun getDuplicatePaths(): Map<String, Int>
    
    @Query("SELECT COUNT(*) FROM songs WHERE size = 0 OR duration = 0")
    suspend fun getCorruptedSongCount(): Int
    
    @Query("DELETE FROM songs WHERE size = 0 OR duration = 0 OR path IS NULL OR path = ''")
    suspend fun removeInvalidSongs(): Int
    
    // Batch Operations
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE artist = :artist")
    suspend fun updateFavoriteStatusByArtist(artist: String, isFavorite: Boolean)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE album = :album")
    suspend fun updateFavoriteStatusByAlbum(album: String, isFavorite: Boolean)
    
    @Query("UPDATE songs SET genre = :newGenre WHERE genre = :oldGenre")
    suspend fun updateGenre(oldGenre: String, newGenre: String)
    
    @Query("UPDATE songs SET artist = :newArtist WHERE artist = :oldArtist")
    suspend fun updateArtist(oldArtist: String, newArtist: String)
    
    // Custom Sorting
    @Query("SELECT * FROM songs ORDER BY playCount DESC, lastPlayed DESC, title ASC LIMIT :limit")
    suspend fun getTopSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE playCount = 0 AND lastPlayed = 0 ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getNeverPlayedSongs(limit: Int): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE lastPlayed < :cutoffTime AND lastPlayed > 0 ORDER BY lastPlayed ASC LIMIT :limit")
    suspend fun getNotRecentlyPlayedSongs(cutoffTime: Long, limit: Int): List<SongEntity>
}
