package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isPrepared: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val bufferedPercentage: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val error: String? = null
) : Parcelable {
    
    val progress: Float
        get() = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    
    val hasQueue: Boolean
        get() = queue.isNotEmpty()
    
    val hasNext: Boolean
        get() = when {
            !hasQueue -> false
            repeatMode == RepeatMode.ONE -> true
            shuffleMode == ShuffleMode.ON -> queue.size > 1
            else -> queueIndex < queue.size - 1
        }
    
    val hasPrevious: Boolean
        get() = when {
            !hasQueue -> false
            repeatMode == RepeatMode.ONE -> true
            shuffleMode == ShuffleMode.ON -> queue.size > 1
            else -> queueIndex > 0
        }
    
    val canSeek: Boolean
        get() = isPrepared && duration > 0
    
    val isBuffering: Boolean
        get() = isPrepared && !isPlaying && position < duration && error == null
    
    val remainingTime: Long
        get() = (duration - position).coerceAtLeast(0L)
    
    val formattedPosition: String
        get() = formatDuration(position)
    
    val formattedDuration: String
        get() = formatDuration(duration)
    
    val formattedRemaining: String
        get() = "-${formatDuration(remainingTime)}"
    
    private fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    companion object {
        fun empty() = PlaybackState()
        
        fun idle() = PlaybackState(
            isPlaying = false,
            isPrepared = false
        )
        
        fun error(message: String) = PlaybackState(
            error = message
        )
    }
}
