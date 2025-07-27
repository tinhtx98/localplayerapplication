package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a cast device
 */
@Parcelize
data class CastDevice(
    val id: String,
    val name: String,
    val type: CastDeviceType,
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val lastConnected: Long = 0L,
    val capabilities: List<String> = emptyList()
) : Parcelable {
    
    val displayName: String
        get() = if (name.isNotBlank()) name else "Unknown Device"
    
    val typeDisplayName: String
        get() = type.displayName
    
    val connectionStatus: String
        get() = when {
            isConnected -> "Connected"
            isAvailable -> "Available"
            else -> "Unavailable"
        }
    
    val canPlayAudio: Boolean
        get() = capabilities.contains("audio") || type == CastDeviceType.AUDIO_SPEAKER
    
    val canPlayVideo: Boolean
        get() = capabilities.contains("video") || type == CastDeviceType.SMART_TV
}

/**
 * Types of cast devices
 */
enum class CastDeviceType {
    CHROMECAST,
    SMART_TV,
    AUDIO_SPEAKER,
    UNKNOWN;
    
    val displayName: String
        get() = when (this) {
            CHROMECAST -> "Chromecast"
            SMART_TV -> "Smart TV"
            AUDIO_SPEAKER -> "Audio Speaker"
            UNKNOWN -> "Unknown Device"
        }
}
