package com.tinhtx.localplayerapplication.domain.service

import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for media player operations
 * This handles real-time playback controls (not repository pattern)
 */
interface MediaPlayerService {
    
    // Playback State
    val playbackState: Flow<PlaybackState>
    val currentSong: Flow<Song?>
    val playbackPosition: Flow<Long>
    val playbackQueue: Flow<PlaybackQueue>
    val repeatMode: Flow<RepeatMode>
    val shuffleMode: Flow<ShuffleMode>
    val audioFocusState: Flow<AudioFocusState>
    
    // Playback Controls
    fun play()
    fun pause()
    fun stop()
    fun playSong(song: Song)
    fun playQueue(songs: List<Song>, startIndex: Int = 0)
    fun seekTo(position: Long)
    fun skipToNext()
    fun skipToPrevious()
    
    // Queue Management
    fun addToQueue(song: Song)
    fun addToQueue(songs: List<Song>)
    fun removeFromQueue(index: Int)
    fun moveQueueItem(fromIndex: Int, toIndex: Int)
    fun clearQueue()
    fun skipToQueueItem(index: Int)
    
    // Playback Settings
    fun setRepeatMode(mode: RepeatMode)
    fun setShuffleMode(mode: ShuffleMode)
    fun setPlaybackSpeed(speed: Float)
    
    // Audio Focus
    fun requestAudioFocus(): Boolean
    fun abandonAudioFocus()
    
    // Sleep Timer
    fun setSleepTimer(minutes: Int)
    fun cancelSleepTimer()
    val sleepTimerRemaining: Flow<Long>
    
    // Service Lifecycle
    fun startService()
    fun stopService()
    fun bindService(): Boolean
    fun unbindService()
    val isServiceBound: Flow<Boolean>
    
    // Notification Controls
    fun updateNotification()
    fun showNotification()
    fun hideNotification()
    
    // Error Handling
    val playbackError: Flow<String?>
    fun clearError()
}
