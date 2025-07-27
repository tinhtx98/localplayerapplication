package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a playlist
 */
@Parcelize
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val duration: Long = 0L, // total duration in milliseconds
    val artworkPath: String? = null
) : Parcelable {
    
    val displayName: String
        get() = if (name.isBlank()) "Untitled Playlist" else name
    
    val hasDescription: Boolean
        get() = !description.isNullOrBlank()
    
    val hasArtwork: Boolean
        get() = !artworkPath.isNullOrBlank()
    
    val songCountText: String
        get() = when (songCount) {
            0 -> "No songs"
            1 -> "1 song"
            else -> "$songCount songs"
        }
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val statsText: String
        get() = if (duration > 0) {
            "$songCountText • $formattedDuration"
        } else {
            songCountText
        }
    
    val isSystemPlaylist: Boolean
        get() = id < 0 // Negative IDs for system playlists
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
