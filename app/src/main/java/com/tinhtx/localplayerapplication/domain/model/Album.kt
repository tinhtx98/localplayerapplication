package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: Long = 0,
    val mediaStoreId: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val firstYear: Int,
    val lastYear: Int,
    val albumArtPath: String? = null
) : Parcelable {
    
    val displayName: String
        get() = if (name.isBlank() || name == "Unknown Album") "Unknown Album" else name
    
    val displayArtist: String
        get() = if (artist.isBlank() || artist == "Unknown Artist") "Unknown Artist" else artist
    
    val yearRange: String
        get() = when {
            firstYear == 0 && lastYear == 0 -> "Unknown"
            firstYear == lastYear -> firstYear.toString()
            else -> "$firstYear - $lastYear"
        }
    
    val songCountText: String
        get() = when (songCount) {
            0 -> "No songs"
            1 -> "1 song"
            else -> "$songCount songs"
        }
    
    companion object {
        fun empty() = Album(
            mediaStoreId = -1,
            name = "",
            artist = "",
            artistId = -1,
            songCount = 0,
            firstYear = 0,
            lastYear = 0
        )
    }
}
