package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing playback queue
 */
@Parcelize
data class PlaybackQueue(
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val originalOrder: List<Song> = emptyList(),
    val isShuffled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF
) : Parcelable {
    
    val currentSong: Song?
        get() = songs.getOrNull(currentIndex)
    
    val hasNext: Boolean
        get() = when (repeatMode) {
            RepeatMode.OFF -> currentIndex < songs.size - 1
            RepeatMode.ONE, RepeatMode.ALL -> songs.isNotEmpty()
        }
    
    val hasPrevious: Boolean
        get() = when (repeatMode) {
            RepeatMode.OFF -> currentIndex > 0
            RepeatMode.ONE, RepeatMode.ALL -> songs.isNotEmpty()
        }
    
    val nextSong: Song?
        get() = when {
            songs.isEmpty() -> null
            repeatMode == RepeatMode.ONE -> currentSong
            hasNext && repeatMode == RepeatMode.OFF -> songs.getOrNull(currentIndex + 1)
            repeatMode == RepeatMode.ALL -> {
                val nextIndex = if (currentIndex >= songs.size - 1) 0 else currentIndex + 1
                songs.getOrNull(nextIndex)
            }
            else -> null
        }
    
    val previousSong: Song?
        get() = when {
            songs.isEmpty() -> null
            repeatMode == RepeatMode.ONE -> currentSong
            hasPrevious && repeatMode == RepeatMode.OFF -> songs.getOrNull(currentIndex - 1)
            repeatMode == RepeatMode.ALL -> {
                val prevIndex = if (currentIndex <= 0) songs.size - 1 else currentIndex - 1
                songs.getOrNull(prevIndex)
            }
            else -> null
        }
    
    val queueSize: Int
        get() = songs.size
    
    val remainingSongs: Int
        get() = maxOf(0, songs.size - currentIndex - 1)
    
    val queueDuration: Long
        get() = songs.sumOf { it.duration }
    
    val remainingDuration: Long
        get() = songs.drop(currentIndex + 1).sumOf { it.duration }
    
    val progress: Float
        get() = if (songs.isEmpty()) 0f else (currentIndex + 1).toFloat() / songs.size
    
    fun getFormattedQueueInfo(): String {
        return if (songs.isEmpty()) {
            "Empty queue"
        } else {
            "${currentIndex + 1} of ${songs.size} songs"
        }
    }
    
    fun getFormattedDuration(): String {
        val hours = queueDuration / 3600000
        val minutes = (queueDuration % 3600000) / 60000
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
