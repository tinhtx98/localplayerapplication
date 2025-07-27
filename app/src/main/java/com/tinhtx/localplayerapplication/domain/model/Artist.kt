package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing an artist
 */
@Parcelize
data class Artist(
    val id: Long = 0,
    val name: String,
    val albumCount: Int = 0,
    val songCount: Int = 0,
    val artworkPath: String? = null
) : Parcelable {
    
    val displayName: String
        get() = if (name.isBlank() || name == "Unknown Artist") "Unknown Artist" else name
    
    val hasArtwork: Boolean
        get() = !artworkPath.isNullOrBlank()
    
    val albumCountText: String
        get() = when (albumCount) {
            0 -> "No albums"
            1 -> "1 album"
            else -> "$albumCount albums"
        }
    
    val songCountText: String
        get() = when (songCount) {
            0 -> "No songs"
            1 -> "1 song"
            else -> "$songCount songs"
        }
    
    val statsText: String
        get() = "$albumCountText • $songCountText"
}
