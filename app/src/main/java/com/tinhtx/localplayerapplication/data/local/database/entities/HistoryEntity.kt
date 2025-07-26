package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [
        Index(value = ["song_id"]),
        Index(value = ["played_at"]),
        Index(value = ["completion_percentage"])
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
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "song_id")
    val songId: Long,
    
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
    
    @ColumnInfo(name = "completion_percentage")
    val completionPercentage: Float, // 0.0 to 1.0
    
    @ColumnInfo(name = "play_duration")
    val playDuration: Long, // How long the song was played in milliseconds
    
    @ColumnInfo(name = "source")
    val source: String? = null // "album", "playlist", "search", etc.
)
