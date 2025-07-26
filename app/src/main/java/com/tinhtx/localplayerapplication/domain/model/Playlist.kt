package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverArtPath: String? = null,
    val songCount: Int = 0,
    val duration: Long = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val songCountText: String
        get() = when (songCount) {
            0 -> "Empty playlist"
            1 -> "1 song"
            else -> "$songCount songs"
        }
    
    val summaryText: String
        get() = if (duration > 0) {
            "$songCountText • $formattedDuration"
        } else {
            songCountText
        }
    
    val isEmpty: Boolean
        get() = songCount == 0
    
    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return if (hours > 0) {
            String.format("%d hr %d min", hours, minutes)
        } else {
            String.format("%d min", minutes)
        }
    }
    
    companion object {
        fun empty() = Playlist(
            name = "",
            description = null
        )
        
        fun favorites() = Playlist(
            id = -1,
            name = "Favorites",
            description = "Your favorite songs"
        )
        
        fun recentlyPlayed() = Playlist(
            id = -2,
            name = "Recently Played",
            description = "Songs you've played recently"
        )
        
        fun mostPlayed() = Playlist(
            id = -3,
            name = "Most Played",
            description = "Your most played songs"
        )
    }
}
