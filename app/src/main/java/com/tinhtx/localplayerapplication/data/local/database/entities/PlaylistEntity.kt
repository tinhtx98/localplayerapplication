package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "cover_art_path")
    val coverArtPath: String? = null,
    
    @ColumnInfo(name = "song_count")
    val songCount: Int = 0,
    
    @ColumnInfo(name = "duration")
    val duration: Long = 0,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
