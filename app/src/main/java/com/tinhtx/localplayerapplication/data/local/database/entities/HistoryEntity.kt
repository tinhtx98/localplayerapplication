package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing playback history
 */
@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["songId"]), Index(value = ["playedAt"])]
)
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
    val wasCompleted: Boolean get() = completionPercentage >= 0.8f
    val wasSkipped: Boolean get() = completionPercentage < 0.1f || skipped
}
