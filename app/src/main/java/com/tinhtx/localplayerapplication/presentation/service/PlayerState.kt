package com.tinhtx.localplayerapplication.presentation.service

import com.tinhtx.localplayerapplication.domain.model.*

data class PlayerState(
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val audioSessionId: Int = 0,
    val equalizerSettings: EqualizerSettings = EqualizerSettings(),
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3,
    val sleepTimer: SleepTimerState = SleepTimerState(),
    val castState: CastState = CastState(),
    val bufferingPercentage: Int = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val error: PlayerError? = null
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
        
    val remainingTime: Long
        get() = duration - currentPosition
        
    val isBuffering: Boolean
        get() = bufferingPercentage < 100 && isPlaying
}

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingTimeMs: Long = 0L,
    val totalTimeMs: Long = 0L,
    val fadeOutEnabled: Boolean = true
)

data class CastState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val connectionState: CastConnectionState = CastConnectionState.DISCONNECTED
)

enum class CastConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class PlayerError(
    val type: PlayerErrorType,
    val message: String,
    val cause: Throwable? = null
)

enum class PlayerErrorType {
    PLAYBACK_ERROR,
    NETWORK_ERROR,
    FILE_NOT_FOUND,
    PERMISSION_DENIED,
    UNSUPPORTED_FORMAT,
    CAST_ERROR,
    UNKNOWN
}
