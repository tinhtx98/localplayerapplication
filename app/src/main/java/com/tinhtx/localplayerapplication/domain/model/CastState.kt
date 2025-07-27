package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing cast state
 */
@Parcelize
data class CastState(
    val isConnected: Boolean = false,
    val device: CastDevice? = null,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val volume: Float = 1.0f,
    val position: Long = 0L,
    val connectionError: String? = null
) : Parcelable {
    
    val hasActiveSession: Boolean
        get() = isConnected && device != null
    
    val isPlayingMedia: Boolean
        get() = hasActiveSession && currentSong != null && isPlaying
    
    val statusText: String
        get() = when {
            connectionError != null -> "Error: $connectionError"
            !isConnected -> "Not connected"
            device == null -> "No device"
            currentSong == null -> "Connected to ${device.displayName}"
            isPlaying -> "Playing on ${device.displayName}"
            else -> "Paused on ${device.displayName}"
        }
    
    val volumePercentage: Int
        get() = (volume * 100).toInt()
}
