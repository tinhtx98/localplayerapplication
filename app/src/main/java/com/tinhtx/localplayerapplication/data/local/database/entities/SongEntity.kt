package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["media_store_id"], unique = true),
        Index(value = ["album_id"]),
        Index(value = ["artist_id"]),
        Index(value = ["title"]),
        Index(value = ["date_added"])
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "media_store_id")
    val mediaStoreId: Long,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist")
    val artist: String,
    
    @ColumnInfo(name = "album")
    val album: String,
    
    @ColumnInfo(name = "duration")
    val duration: Long,
    
    @ColumnInfo(name = "data")
    val  String, // File path
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,
    
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    
    @ColumnInfo(name = "artist_id")
    val artistId: Long,
    
    @ColumnInfo(name = "track")
    val track: Int,
    
    @ColumnInfo(name = "year")
    val year: Int,
    
    @ColumnInfo(name = "size")
    val size: Long,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
