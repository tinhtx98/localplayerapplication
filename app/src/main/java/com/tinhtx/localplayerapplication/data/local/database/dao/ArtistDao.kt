package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    
    @Query("SELECT * FROM artists ORDER BY artist_name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>
    
    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: Long): ArtistEntity?
    
    @Query("SELECT * FROM artists WHERE media_store_id = :mediaStoreId")
    suspend fun getArtistByMediaStoreId(mediaStoreId: Long): ArtistEntity?
    
    @Query("SELECT * FROM artists WHERE artist_name LIKE '%' || :query || '%' ORDER BY artist_name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>
    
    @Query("SELECT * FROM artists ORDER BY track_count DESC LIMIT :limit")
    fun getTopArtists(limit: Int = 20): Flow<List<ArtistEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)
    
    @Update
    suspend fun updateArtist(artist: ArtistEntity)
    
    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)
    
    @Query("DELETE FROM artists WHERE id = :id")
    suspend fun deleteArtistById(id: Long)
    
    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()
    
    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
}
