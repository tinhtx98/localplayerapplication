package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a song in the database
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long, // in milliseconds
    val path: String,
    val size: Long, // in bytes
    val mimeType: String,
    val dateAdded: Long,
    val dateModified: Long,
    val year: Int,
    val trackNumber: Int,
    val genre: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
)
