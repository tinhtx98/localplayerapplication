package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an album in the database
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaStoreId: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val year: Int,
    val songCount: Int = 0,
    val artworkPath: String? = null
)
