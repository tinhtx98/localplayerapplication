package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long = 0,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String, // File path
    val dateAdded: Long,
    val albumId: Long,
    val artistId: Long,
    val track: Int,
    val year: Int,
    val size: Long,
    val mimeType: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null
) : Parcelable {
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val formattedSize: String
        get() = formatFileSize(size)
    
    val displayArtist: String
        get() = if (artist.isBlank() || artist == "Unknown Artist") "Unknown Artist" else artist
    
    val displayAlbum: String
        get() = if (album.isBlank() || album == "Unknown Album") "Unknown Album" else album
    
    val isValidFile: Boolean
        get() = path.isNotBlank() && java.io.File(path).exists()

    private fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun formatFileSize(sizeBytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            sizeBytes >= gb -> String.format("%.1f GB", sizeBytes / gb)
            sizeBytes >= mb -> String.format("%.1f MB", sizeBytes / mb)
            sizeBytes >= kb -> String.format("%.1f KB", sizeBytes / kb)
            else -> "$sizeBytes B"
        }
    }
    
    companion object {
        fun empty() = Song(
            mediaStoreId = -1,
            title = "",
            artist = "",
            album = "",
            duration = 0,
            path = "",
            dateAdded = 0,
            albumId = -1,
            artistId = -1,
            track = 0,
            year = 0,
            size = 0
        )
    }
}
