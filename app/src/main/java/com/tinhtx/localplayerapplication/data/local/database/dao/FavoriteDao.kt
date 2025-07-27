package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.FavoriteEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Favorite operations
 */
@Dao
interface FavoriteDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: Long): FavoriteEntity?
    
    @Query("SELECT * FROM favorites WHERE songId = :songId")
    suspend fun getFavoriteBySongId(songId: Long): FavoriteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>): List<Long>
    
    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)
    
    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavoriteBySongId(songId: Long)
    
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
    
    // Favorite Songs Operations
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY f.addedAt DESC
    """)
    suspend fun getFavoriteSongs(): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY f.addedAt DESC
    """)
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY s.title ASC
    """)
    suspend fun getFavoriteSongsSortedByTitle(): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY s.artist ASC, s.title ASC
    """)
    suspend fun getFavoriteSongsSortedByArtist(): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY s.album ASC, s.trackNumber ASC
    """)
    suspend fun getFavoriteSongsSortedByAlbum(): List<SongEntity>
    
    // Search Operations
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.title LIKE '%' || :query || '%' 
        OR s.artist LIKE '%' || :query || '%' 
        OR s.album LIKE '%' || :query || '%'
        ORDER BY f.addedAt DESC
    """)
    suspend fun searchFavoriteSongs(query: String): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.title LIKE '%' || :query || '%' 
        OR s.artist LIKE '%' || :query || '%' 
        OR s.album LIKE '%' || :query || '%'
        ORDER BY f.addedAt DESC
    """)
    fun searchFavoriteSongsFlow(query: String): Flow<List<SongEntity>>
    
    // Filter Operations
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.artist = :artist
        ORDER BY f.addedAt DESC
    """)
    suspend fun getFavoriteSongsByArtist(artist: String): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.album = :album
        ORDER BY f.addedAt DESC
    """)
    suspend fun getFavoriteSongsByAlbum(album: String): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.genre = :genre
        ORDER BY f.addedAt DESC
    """)
    suspend fun getFavoriteSongsByGenre(genre: String): List<SongEntity>
    
    // Statistics Operations
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
    
    @Query("""
        SELECT COUNT(DISTINCT s.artist) FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
    """)
    suspend fun getFavoriteArtistCount(): Int
    
    @Query("""
        SELECT COUNT(DISTINCT s.album) FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
    """)
    suspend fun getFavoriteAlbumCount(): Int
    
    @Query("""
        SELECT COALESCE(SUM(s.duration), 0) FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
    """)
    suspend fun getTotalFavoriteDuration(): Long
    
    @Query("""
        SELECT COUNT(*) FROM favorites 
        WHERE addedAt > :since
    """)
    suspend fun getRecentFavoriteCount(since: Long): Int
    
    // Recent Operations
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY f.addedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentlyAddedFavorites(limit: Int): List<SongEntity>
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE f.addedAt > :since
        ORDER BY f.addedAt DESC
    """)
    suspend fun getFavoritesAddedSince(since: Long): List<SongEntity>
    
    // Check Operations
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isSongFavorite(songId: Long): Boolean
    
    @Query("SELECT songId FROM favorites")
    suspend fun getAllFavoriteSongIds(): List<Long>
    
    // Batch Operations
    @Query("SELECT songId FROM favorites WHERE songId IN (:songIds)")
    suspend fun getFavoriteSongIdsFromList(songIds: List<Long>): List<Long>
    
    @Query("DELETE FROM favorites WHERE songId IN (:songIds)")
    suspend fun removeFavoritesBySongIds(songIds: List<Long>)
    
    // Advanced Queries
    @Query("""
        SELECT DISTINCT s.artist FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY s.artist ASC
    """)
    suspend fun getFavoriteArtists(): List<String>
    
    @Query("""
        SELECT DISTINCT s.album FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY s.album ASC
    """)
    suspend fun getFavoriteAlbums(): List<String>
    
    @Query("""
        SELECT DISTINCT s.genre FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        WHERE s.genre IS NOT NULL AND s.genre != ''
        ORDER BY s.genre ASC
    """)
    suspend fun getFavoriteGenres(): List<String>
    
    @Query("""
        SELECT s.artist, COUNT(*) as count FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        GROUP BY s.artist
        ORDER BY count DESC
        LIMIT :limit
    """)
    suspend fun getTopFavoriteArtists(limit: Int): List<ArtistFavoriteCount>
    
    @Query("""
        SELECT s.album, COUNT(*) as count FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        GROUP BY s.album
        ORDER BY count DESC
        LIMIT :limit
    """)
    suspend fun getTopFavoriteAlbums(limit: Int): List<AlbumFavoriteCount>
    
    // Cleanup Operations
    @Query("""
        DELETE FROM favorites 
        WHERE songId NOT IN (SELECT id FROM songs)
    """)
    suspend fun removeOrphanedFavorites()
    
    // Toggle Favorite (utility for single operation)
    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        val favorite = getFavoriteBySongId(songId)
        if (favorite != null) {
            deleteFavorite(favorite)
        } else {
            insertFavorite(FavoriteEntity(songId = songId))
        }
    }
}

/**
 * Data classes for query results
 */
data class ArtistFavoriteCount(
    val artist: String,
    val count: Int
)

data class AlbumFavoriteCount(
    val album: String,
    val count: Int
)
