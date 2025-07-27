package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.ArtistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Artist operations
 */
@Dao
interface ArtistDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getAllArtists(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtistsFlow(): Flow<List<ArtistEntity>>
    
    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: Long): ArtistEntity?
    
    @Query("SELECT * FROM artists WHERE name = :name")
    suspend fun getArtistByName(name: String): ArtistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>): List<Long>
    
    @Update
    suspend fun updateArtist(artist: ArtistEntity)
    
    @Update
    suspend fun updateArtists(artists: List<ArtistEntity>)
    
    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)
    
    @Query("DELETE FROM artists WHERE id = :id")
    suspend fun deleteArtistById(id: Long)
    
    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()
    
    // Search Operations
    @Query("""
        SELECT * FROM artists 
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun searchArtists(query: String): List<ArtistEntity>
    
    @Query("""
        SELECT * FROM artists 
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchArtistsFlow(query: String): Flow<List<ArtistEntity>>
    
    // Statistics Operations
    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
    
    @Query("SELECT SUM(albumCount) FROM artists")
    suspend fun getTotalAlbumCount(): Int
    
    @Query("SELECT SUM(songCount) FROM artists")
    suspend fun getTotalSongCount(): Int
    
    @Query("SELECT AVG(albumCount) FROM artists")
    suspend fun getAverageAlbumCount(): Double
    
    @Query("SELECT AVG(songCount) FROM artists")
    suspend fun getAverageSongCount(): Double
    
    // Sorting Operations
    @Query("SELECT * FROM artists ORDER BY name ASC")
    suspend fun getArtistsSortedByName(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists ORDER BY albumCount DESC")
    suspend fun getArtistsSortedByAlbumCount(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists ORDER BY songCount DESC")
    suspend fun getArtistsSortedBySongCount(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists ORDER BY songCount DESC LIMIT :limit")
    suspend fun getMostPlayedArtists(limit: Int): List<ArtistEntity>
    
    // Update Operations
    @Query("""
        UPDATE artists SET 
        songCount = (SELECT COUNT(*) FROM songs WHERE artist = artists.name),
        albumCount = (SELECT COUNT(DISTINCT album) FROM songs WHERE artist = artists.name)
    """)
    suspend fun updateStatistics()
    
    @Query("UPDATE artists SET songCount = :songCount WHERE id = :artistId")
    suspend fun updateSongCount(artistId: Long, songCount: Int)
    
    @Query("UPDATE artists SET albumCount = :albumCount WHERE id = :artistId")
    suspend fun updateAlbumCount(artistId: Long, albumCount: Int)
    
    @Query("UPDATE artists SET artworkPath = :artworkPath WHERE id = :artistId")
    suspend fun updateArtworkPath(artistId: Long, artworkPath: String?)
    
    // Advanced Queries
    @Query("SELECT * FROM artists WHERE albumCount > :minAlbumCount ORDER BY albumCount DESC")
    suspend fun getArtistsWithMinAlbums(minAlbumCount: Int): List<ArtistEntity>
    
    @Query("SELECT * FROM artists WHERE songCount > :minSongCount ORDER BY songCount DESC")
    suspend fun getArtistsWithMinSongs(minSongCount: Int): List<ArtistEntity>
    
    @Query("SELECT * FROM artists WHERE artworkPath IS NOT NULL ORDER BY name ASC")
    suspend fun getArtistsWithArtwork(): List<ArtistEntity>
    
    @Query("SELECT * FROM artists WHERE artworkPath IS NULL ORDER BY name ASC")
    suspend fun getArtistsWithoutArtwork(): List<ArtistEntity>
    
    @Query("""
        SELECT name FROM artists 
        WHERE name LIKE :startLetter || '%' 
        ORDER BY name ASC
    """)
    suspend fun getArtistsByStartingLetter(startLetter: String): List<String>
    
    // Cleanup Operations
    @Query("DELETE FROM artists WHERE songCount = 0 AND albumCount = 0")
    suspend fun removeEmptyArtists()
}
