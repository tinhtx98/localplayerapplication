package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a song
 */
@Parcelize
data class Song(
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
) : Parcelable {
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val formattedSize: String
        get() = formatFileSize(size)
    
    val isValidFile: Boolean
        get() = path.isNotEmpty() && duration > 0
    
    val displayArtist: String
        get() = if (artist.isBlank() || artist == "Unknown Artist") "Unknown Artist" else artist
    
    val displayAlbum: String
        get() = if (album.isBlank() || album == "Unknown Album") "Unknown Album" else album
    
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
    
    private fun formatFileSize(sizeBytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = sizeBytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", size, units[unitIndex])
    }
}
