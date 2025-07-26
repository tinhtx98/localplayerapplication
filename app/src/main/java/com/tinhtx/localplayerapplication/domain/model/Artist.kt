package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: Long = 0,
    val mediaStoreId: Long,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
    val artistArtPath: String? = null
) : Parcelable {
    
    val displayName: String
        get() = if (name.isBlank() || name == "Unknown Artist") "Unknown Artist" else name
    
    val albumCountText: String
        get() = when (albumCount) {
            0 -> "No albums"
            1 -> "1 album"
            else -> "$albumCount albums"
        }
    
    val trackCountText: String
        get() = when (trackCount) {
            0 -> "No tracks"
            1 -> "1 track"
            else -> "$trackCount tracks"
        }
    
    val summaryText: String
        get() = "$albumCountText • $trackCountText"
    
    companion object {
        fun empty() = Artist(
            mediaStoreId = -1,
            name = "",
            albumCount = 0,
            trackCount = 0
        )
    }
}
