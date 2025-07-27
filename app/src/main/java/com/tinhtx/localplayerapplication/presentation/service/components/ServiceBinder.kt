package com.tinhtx.localplayerapplication.data.service.components

import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.StateFlow
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.data.service.PlayerState
import com.tinhtx.localplayerapplication.data.service.RepeatMode
import com.tinhtx.localplayerapplication.data.service.MusicService

class ServiceBinder(
    private val musicService: MusicService
) : Binder() {
    
    /**
     * Get the service instance
     */
    fun getService(): MusicService = musicService
    
    // Playback Control Methods
    fun play() = musicService.play()
    
    fun pause() = musicService.pause()
    
    fun stop() = musicService.stop()
    
    fun skipToNext() = musicService.skipToNext()
    
    fun skipToPrevious() = musicService.skipToPrevious()
    
    fun seekTo(position: Long) = musicService.seekTo(position)
    
    fun setPlaybackSpeed(speed: Float) = musicService.setPlaybackSpeed(speed)
    
    fun setVolume(volume: Float) = musicService.setVolume(volume)
    
    // Queue Management
    fun playSong(song: Song) = musicService.playSong(song)
    
    fun playQueue(songs: List<Song>, startIndex: Int = 0) = 
        musicService.playQueue(songs, startIndex)
    
    fun addToQueue(song: Song) = musicService.addToQueue(song)
    
    fun addToQueue(songs: List<Song>) = musicService.addToQueue(songs)
    
    fun removeFromQueue(index: Int) = musicService.removeFromQueue(index)
    
    fun moveQueueItem(from: Int, to: Int) = musicService.moveQueueItem(from, to)
    
    fun clearQueue() = musicService.clearQueue()
    
    fun jumpToQueueItem(index: Int) = musicService.jumpToQueueItem(index)
    
    // Repeat and Shuffle
    fun setRepeatMode(mode: RepeatMode) = musicService.setRepeatMode(mode)
    
    fun toggleRepeatMode() = musicService.toggleRepeatMode()
    
    fun setShuffleMode(enabled: Boolean) = musicService.setShuffleMode(enabled)
    
    fun toggleShuffleMode() = musicService.toggleShuffleMode()
    
    // State Access
    fun getPlayerState(): StateFlow<PlayerState> = musicService.playerState
    
    fun getCurrentSong(): Song? = musicService.getCurrentSong()
    
    fun getQueue(): List<Song> = musicService.getQueue()
    
    fun getCurrentIndex(): Int = musicService.getCurrentIndex()
    
    fun isPlaying(): Boolean = musicService.isPlaying()
    
    fun getDuration(): Long = musicService.getDuration()
    
    fun getCurrentPosition(): Long = musicService.getCurrentPosition()
    
    fun getBufferedPosition(): Long = musicService.getBufferedPosition()
    
    fun getPlaybackSpeed(): Float = musicService.getPlaybackSpeed()
    
    fun getVolume(): Float = musicService.getVolume()
    
    fun getRepeatMode(): RepeatMode = musicService.getRepeatMode()
    
    fun isShuffleEnabled(): Boolean = musicService.isShuffleEnabled()
    
    fun getAudioSessionId(): Int = musicService.getAudioSessionId()
    
    // Additional Features
    fun toggleFavorite(song: Song) = musicService.toggleFavorite(song)
    
    fun isFavorite(song: Song): Boolean = musicService.isFavorite(song)
    
    fun startSleepTimer(minutes: Int) = musicService.startSleepTimer(minutes)
    
    fun cancelSleepTimer() = musicService.cancelSleepTimer()
    
    fun getSleepTimerRemaining(): Long = musicService.getSleepTimerRemaining()
    
    // Media Session
    fun getMediaSessionToken(): android.support.v4.media.session.MediaSessionCompat.Token? = 
        musicService.getMediaSessionToken()
    
    // Service State
    fun isServiceBound(): Boolean = true
    
    fun getServiceId(): String = musicService.javaClass.simpleName
    
    fun getServiceUptime(): Long = musicService.getServiceUptime()
    
    // Callbacks
    fun setPlaybackStateCallback(callback: PlaybackStateCallback) = 
        musicService.setPlaybackStateCallback(callback)
    
    fun removePlaybackStateCallback() = musicService.removePlaybackStateCallback()
    
    // Advanced Controls
    fun fastForward(seconds: Int = 15) = musicService.fastForward(seconds)
    
    fun rewind(seconds: Int = 15) = musicService.rewind(seconds)
    
    fun setEqualizerPreset(preset: Int) = musicService.setEqualizerPreset(preset)
    
    fun setBassBoost(strength: Int) = musicService.setBassBoost(strength)
    
    fun setVirtualizer(strength: Int) = musicService.setVirtualizer(strength)
    
    // Debug and Analytics
    fun getPlaybackStatistics(): PlaybackStatistics = musicService.getPlaybackStatistics()
    
    fun exportLogs(): String = musicService.exportLogs()
    
    fun resetStatistics() = musicService.resetStatistics()
}

/**
 * Callback interface for playback state changes
 */
interface PlaybackStateCallback {
    fun onPlaybackStateChanged(playerState: PlayerState)
    fun onSongChanged(song: Song?)
    fun onQueueChanged(queue: List<Song>)
    fun onRepeatModeChanged(repeatMode: RepeatMode)
    fun onShuffleModeChanged(enabled: Boolean)
    fun onPlaybackError(error: com.tinhtx.localplayerapplication.data.service.PlayerError)
    fun onBufferingStateChanged(isBuffering: Boolean)
    fun onAudioSessionIdChanged(audioSessionId: Int)
}

/**
 * Playback statistics data class
 */
data class PlaybackStatistics(
    val totalSongsPlayed: Int = 0,
    val totalPlaybackTime: Long = 0L,
    val sessionsCount: Int = 0,
    val favoriteGenre: String = "",
    val mostPlayedSong: Song? = null,
    val averageSessionLength: Long = 0L,
    val skipRate: Float = 0f,
    val errorRate: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Service connection state enum
 */
enum class ServiceConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

/**
 * Binder wrapper with connection state
 */
class MusicServiceBinder(
    private val serviceBinder: ServiceBinder
) : IBinder by serviceBinder {
    
    private var connectionState = ServiceConnectionState.DISCONNECTED
    private val connectionListeners = mutableSetOf<ConnectionStateListener>()
    
    interface ConnectionStateListener {
        fun onConnectionStateChanged(state: ServiceConnectionState)
    }
    
    fun addConnectionStateListener(listener: ConnectionStateListener) {
        connectionListeners.add(listener)
    }
    
    fun removeConnectionStateListener(listener: ConnectionStateListener) {
        connectionListeners.remove(listener)
    }
    
    fun setConnectionState(state: ServiceConnectionState) {
        if (connectionState != state) {
            connectionState = state
            connectionListeners.forEach { listener ->
                try {
                    listener.onConnectionStateChanged(state)
                } catch (e: Exception) {
                    ServiceUtils.logPlaybackEvent(
                        "ServiceBinder", 
                        null, 
                        "Error notifying connection state: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun getConnectionState(): ServiceConnectionState = connectionState
    
    fun isConnected(): Boolean = connectionState == ServiceConnectionState.CONNECTED
    
    fun getServiceBinder(): ServiceBinder = serviceBinder
    
    fun cleanup() {
        connectionListeners.clear()
        setConnectionState(ServiceConnectionState.DISCONNECTED)
    }
}
