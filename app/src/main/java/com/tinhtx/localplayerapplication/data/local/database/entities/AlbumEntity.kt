package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["media_store_id"], unique = true),
        Index(value = ["artist_id"]),
        Index(value = ["album_name"])
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "media_store_id")
    val mediaStoreId: Long,
    
    @ColumnInfo(name = "album_name")
    val albumName: String,
    
    @ColumnInfo(name = "artist")
    val artist: String,
    
    @ColumnInfo(name = "artist_id")
    val artistId: Long,
    
    @ColumnInfo(name = "song_count")
    val songCount: Int,
    
    @ColumnInfo(name = "first_year")
    val firstYear: Int,
    
    @ColumnInfo(name = "last_year")
    val lastYear: Int,
    
    @ColumnInfo(name = "album_art_path")
    val albumArtPath: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
