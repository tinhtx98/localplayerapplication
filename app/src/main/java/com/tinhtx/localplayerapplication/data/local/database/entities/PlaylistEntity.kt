package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a playlist in the database
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val duration: Long = 0L, // total duration in milliseconds
    val artworkPath: String? = null
)
