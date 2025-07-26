package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["song_id"], unique = true),
        Index(value = ["added_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "song_id")
    val songId: Long,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)
