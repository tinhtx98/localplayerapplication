package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val playDuration: Long = 0L,
    val completionPercentage: Float = 0f,
    val source: String? = null,
    val sessionId: String = "",
    val skipped: Boolean = false
) {
    // Computed properties for analytics
    val wasCompleted: Boolean get() = completionPercentage >= 0.8f
    val wasSkipped: Boolean get() = completionPercentage < 0.1f || skipped
    val playSource: PlaySource get() = when (source?.lowercase()) {
        "playlist" -> PlaySource.PLAYLIST
        "album" -> PlaySource.ALBUM
        "artist" -> PlaySource.ARTIST
        "search" -> PlaySource.SEARCH
        "queue" -> PlaySource.QUEUE
        "shuffle" -> PlaySource.SHUFFLE
        else -> PlaySource.UNKNOWN
    }
}

enum class PlaySource {
    PLAYLIST,
    ALBUM,
    ARTIST,
    SEARCH,
    QUEUE,
    SHUFFLE,
    UNKNOWN
}
