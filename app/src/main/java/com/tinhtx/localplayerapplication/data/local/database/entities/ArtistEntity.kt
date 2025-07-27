package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an artist in the database
 */
@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val albumCount: Int = 0,
    val songCount: Int = 0,
    val artworkPath: String? = null
)
