package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    
    @Query("SELECT * FROM albums ORDER BY album_name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE media_store_id = :mediaStoreId")
    suspend fun getAlbumByMediaStoreId(mediaStoreId: Long): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE artist_id = :artistId ORDER BY album_name ASC")
    fun getAlbumsByArtist(artistId: Long): Flow<List<AlbumEntity>>
    
    @Query("""
        SELECT * FROM albums 
        WHERE album_name LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%'
        ORDER BY album_name ASC
    """)
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums ORDER BY first_year DESC LIMIT :limit")
    fun getRecentAlbums(limit: Int = 20): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE first_year = :year ORDER BY album_name ASC")
    fun getAlbumsByYear(year: Int): Flow<List<AlbumEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)
    
    @Update
    suspend fun updateAlbum(album: AlbumEntity)
    
    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)
    
    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Long)
    
    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()
    
    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
    
    @Query("SELECT DISTINCT first_year FROM albums WHERE first_year > 0 ORDER BY first_year DESC")
    suspend fun getAllAlbumYears(): List<Int>
}
