package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.AlbumEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Album operations
 */
@Dao
interface AlbumDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM albums ORDER BY name ASC")
    suspend fun getAllAlbums(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbumsFlow(): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE mediaStoreId = :mediaStoreId")
    suspend fun getAlbumByMediaStoreId(mediaStoreId: Long): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE name = :name AND artist = :artist")
    suspend fun getAlbumByNameAndArtist(name: String, artist: String): AlbumEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>): List<Long>
    
    @Update
    suspend fun updateAlbum(album: AlbumEntity)
    
    @Update
    suspend fun updateAlbums(albums: List<AlbumEntity>)
    
    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)
    
    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Long)
    
    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()
    
    // Search Operations
    @Query("""
        SELECT * FROM albums 
        WHERE name LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchAlbums(query: String): List<AlbumEntity>
    
    @Query("""
        SELECT * FROM albums 
        WHERE name LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchAlbumsFlow(query: String): Flow<List<AlbumEntity>>
    
    // Filter Operations
    @Query("SELECT * FROM albums WHERE artist = :artist ORDER BY year DESC, name ASC")
    suspend fun getAlbumsByArtist(artist: String): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE year = :year ORDER BY name ASC")
    suspend fun getAlbumsByYear(year: Int): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE year BETWEEN :startYear AND :endYear ORDER BY year DESC, name ASC")
    suspend fun getAlbumsByYearRange(startYear: Int, endYear: Int): List<AlbumEntity>
    
    // Statistics Operations
    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
    
    @Query("SELECT COUNT(DISTINCT artist) FROM albums")
    suspend fun getUniqueArtistCount(): Int
    
    @Query("SELECT AVG(songCount) FROM albums")
    suspend fun getAverageSongCount(): Double
    
    @Query("SELECT SUM(songCount) FROM albums")
    suspend fun getTotalSongCount(): Int
    
    // Sorting Operations
    @Query("SELECT * FROM albums ORDER BY name ASC")
    suspend fun getAlbumsSortedByName(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums ORDER BY artist ASC, name ASC")
    suspend fun getAlbumsSortedByArtist(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums ORDER BY year DESC, name ASC")
    suspend fun getAlbumsSortedByYear(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums ORDER BY songCount DESC")
    suspend fun getAlbumsSortedBySongCount(): List<AlbumEntity>
    
    // Update Operations
    @Query("UPDATE albums SET songCount = (SELECT COUNT(*) FROM songs WHERE albumId = albums.mediaStoreId)")
    suspend fun updateStatistics()
    
    @Query("UPDATE albums SET songCount = :songCount WHERE id = :albumId")
    suspend fun updateSongCount(albumId: Long, songCount: Int)
    
    @Query("UPDATE albums SET artworkPath = :artworkPath WHERE id = :albumId")
    suspend fun updateArtworkPath(albumId: Long, artworkPath: String?)
    
    // Advanced Queries
    @Query("""
        SELECT DISTINCT artist FROM albums 
        WHERE artist != '' 
        ORDER BY artist ASC
    """)
    suspend fun getAllArtistsFromAlbums(): List<String>
    
    @Query("""
        SELECT DISTINCT year FROM albums 
        WHERE year > 0 
        ORDER BY year DESC
    """)
    suspend fun getAllYearsFromAlbums(): List<Int>
    
    @Query("SELECT * FROM albums WHERE songCount > :minSongCount ORDER BY songCount DESC")
    suspend fun getAlbumsWithMinSongs(minSongCount: Int): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE artworkPath IS NOT NULL ORDER BY name ASC")
    suspend fun getAlbumsWithArtwork(): List<AlbumEntity>
    
    @Query("SELECT * FROM albums WHERE artworkPath IS NULL ORDER BY name ASC")
    suspend fun getAlbumsWithoutArtwork(): List<AlbumEntity>
}
