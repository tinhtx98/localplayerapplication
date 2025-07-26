package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["media_store_id"], unique = true),
        Index(value = ["artist_name"])
    ]
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "media_store_id")
    val mediaStoreId: Long,
    
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    
    @ColumnInfo(name = "album_count")
    val albumCount: Int,
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int,
    
    @ColumnInfo(name = "artist_art_path")
    val artistArtPath: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
