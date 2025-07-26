package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlist_id", "song_id"],
    indices = [
        Index(value = ["playlist_id"]),
        Index(value = ["song_id"]),
        Index(value = ["position"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,
    
    @ColumnInfo(name = "song_id")
    val songId: Long,
    
    @ColumnInfo(name = "position")
    val position: Int,
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()
)
