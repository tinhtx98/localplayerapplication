package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.FavoriteEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorites f ON s.id = f.songId
        ORDER BY f.addedAt DESC
    """)
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE songId = :songId")
    suspend fun getFavoriteForSong(songId: Long): FavoriteEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isSongFavorite(songId: Long): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavoriteForSong(songId: Long)
    
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
    
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
    
    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        val existing = getFavoriteForSong(songId)
        if (existing != null) {
            deleteFavorite(existing)
        } else {
            insertFavorite(FavoriteEntity(songId = songId))
        }
    }
}
