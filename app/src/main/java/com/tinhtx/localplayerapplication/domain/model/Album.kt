package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing an album
 */
@Parcelize
data class Album(
    val id: Long = 0,
    val mediaStoreId: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val year: Int,
    val songCount: Int = 0,
    val artworkPath: String? = null
) : Parcelable {
    
    val displayName: String
        get() = if (name.isBlank() || name == "Unknown Album") "Unknown Album" else name
    
    val displayArtist: String
        get() = if (artist.isBlank() || artist == "Unknown Artist") "Unknown Artist" else artist
    
    val displayYear: String
        get() = if (year > 0) year.toString() else "Unknown"
    
    val hasArtwork: Boolean
        get() = !artworkPath.isNullOrBlank()
    
    val songCountText: String
        get() = when (songCount) {
            0 -> "No songs"
            1 -> "1 song"
            else -> "$songCount songs"
        }
}
