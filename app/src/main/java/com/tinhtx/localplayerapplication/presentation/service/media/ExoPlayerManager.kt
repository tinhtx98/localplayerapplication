package com.tinhtx.localplayerapplication.data.service.media

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.data.service.PlayerState
import com.tinhtx.localplayerapplication.data.service.PlayerError
import com.tinhtx.localplayerapplication.data.service.PlaybackState
import com.tinhtx.localplayerapplication.data.service.components.ServiceUtils
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ExoPlayerManager @Inject constructor(
    private val context: Context
) {
    
    private var exoPlayer: ExoPlayer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private var positionUpdateRunnable: Runnable? = null
    private val positionUpdateInterval = 1000L // 1 second
    
    // Player listeners
    private val playerListeners = mutableSetOf<PlayerListener>()
    private var currentMediaItems = mutableListOf<MediaItem>()
    private var isInitialized = false
    
    interface PlayerListener {
        fun onPlaybackStateChanged(playbackState: PlaybackState)
        fun onPositionChanged(position: Long, duration: Long)
        fun onPlayerError(error: PlayerError)
        fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int)
        fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int)
        fun onIsLoadingChanged(isLoading: Boolean)
        fun onAudioSessionIdChanged(audioSessionId: Int)
    }
    
    /**
     * Initialize ExoPlayer
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            exoPlayer = ExoPlayer.Builder(context)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .build()
            
            setupPlayerListeners()
            isInitialized = true
            
            ServiceUtils.logPlaybackEvent(
                "ExoPlayer", 
                null, 
                "Player initialized successfully"
            )
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "ExoPlayer", 
                null, 
                "Failed to initialize player: ${e.message}"
            )
            throw e
        }
    }
    
    private fun setupPlayerListeners() {
        exoPlayer?.addListener(object : Player.Listener {
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                val state = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> PlaybackState.READY
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
                
                _playbackState.value = state
                notifyPlaybackStateChanged(state)
                
                ServiceUtils.logPlaybackEvent(
                    "ExoPlayer", 
                    null, 
                    "Playback state changed: $state"
                )
            }
            
            override fun onPlayerError(error: PlaybackException) {
                val playerError = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> 
                        PlayerError.FileNotFound("File not found", error)
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> 
                        PlayerError.NetworkError("Network connection failed", error)
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> 
                        PlayerError.UnsupportedFormat("Unsupported format", error)
                    else -> PlayerError.Unknown("Unknown error", error)
                }
                
                notifyPlayerError(playerError)
                
                ServiceUtils.logPlaybackEvent(
                    "ExoPlayer", 
                    null, 
                    "Player error: ${error.message}"
                )
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                notifyMediaItemTransition(mediaItem, reason)
                
                ServiceUtils.logPlaybackEvent(
                    "ExoPlayer", 
                    null, 
                    "Media item transition: ${mediaItem?.mediaId} (reason: $reason)"
                )
            }
            
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
                
                notifyPlayWhenReadyChanged(playWhenReady, reason)
                
                ServiceUtils.logPlaybackEvent(
                    "ExoPlayer", 
                    null, 
                    "Play when ready changed: $playWhenReady (reason: $reason)"
                )
            }
            
            override fun onIsLoadingChanged(isLoading: Boolean) {
                notifyIsLoadingChanged(isLoading)
            }
            
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                notifyAudioSessionIdChanged(audioSessionId)
                
                ServiceUtils.logPlaybackEvent(
                    "ExoPlayer", 
                    null, 
                    "Audio session ID changed: $audioSessionId"
                )
            }
        })
    }
    
    /**
     * Prepare and play a single song
     */
    fun playSong(song: Song) {
        if (!isInitialized) initialize()
        
        try {
            val mediaItem = createMediaItem(song)
            currentMediaItems.clear()
            currentMediaItems.add(mediaItem)
            
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
            
            ServiceUtils.logPlaybackEvent(
                "ExoPlayer", 
                song, 
                "Song prepared for playback"
            )
        } catch (e: Exception) {
            val error = PlayerError.Unknown("Failed to play song", e)
            notifyPlayerError(error)
        }
    }
    
    /**
     * Prepare and play a queue of songs
     */
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (!isInitialized) initialize()
        if (songs.isEmpty()) return
        
        try {
            val mediaItems = songs.map { createMediaItem(it) }
            currentMediaItems.clear()
            currentMediaItems.addAll(mediaItems)
            
            exoPlayer?.apply {
                setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                prepare()
                playWhenReady = true
            }
            
            ServiceUtils.logPlaybackEvent(
                "ExoPlayer", 
                null, 
                "Queue prepared: ${songs.size} songs, starting at index $startIndex"
            )
        } catch (e: Exception) {
            val error = PlayerError.Unknown("Failed to play queue", e)
            notifyPlayerError(error)
        }
    }
    
    /**
     * Play/Resume playback
     */
    fun play() {
        exoPlayer?.playWhenReady = true
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Play requested")
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        exoPlayer?.playWhenReady = false
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Pause requested")
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        exoPlayer?.apply {
            stop()
            clearMediaItems()
        }
        currentMediaItems.clear()
        stopPositionUpdates()
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Stop requested")
    }
    
    /**
     * Skip to next track
     */
    fun skipToNext() {
        if (hasNextMediaItem()) {
            exoPlayer?.seekToNextMediaItem()
            ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Skip to next")
        }
    }
    
    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        if (hasPreviousMediaItem()) {
            exoPlayer?.seekToPreviousMediaItem()
            ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Skip to previous")
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Seek to: ${positionMs}ms")
    }
    
    /**
     * Jump to queue item
     */
    fun jumpToQueueItem(index: Int) {
        if (index in 0 until currentMediaItems.size) {
            exoPlayer?.seekToDefaultPosition(index)
            ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Jump to queue item: $index")
        }
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.25f, 3.0f)
        exoPlayer?.setPlaybackSpeed(clampedSpeed)
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Playback speed: $clampedSpeed")
    }
    
    /**
     * Set volume
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        exoPlayer?.volume = clampedVolume
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Volume: $clampedVolume")
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatMode(repeatMode: com.tinhtx.localplayerapplication.data.service.RepeatMode) {
        val exoRepeatMode = when (repeatMode) {
            com.tinhtx.localplayerapplication.data.service.RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            com.tinhtx.localplayerapplication.data.service.RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            com.tinhtx.localplayerapplication.data.service.RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        exoPlayer?.repeatMode = exoRepeatMode
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Repeat mode: $repeatMode")
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleMode(enabled: Boolean) {
        exoPlayer?.shuffleModeEnabled = enabled
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Shuffle mode: $enabled")
    }
    
    /**
     * Add media item to queue
     */
    fun addToQueue(song: Song) {
        val mediaItem = createMediaItem(song)
        currentMediaItems.add(mediaItem)
        exoPlayer?.addMediaItem(mediaItem)
        ServiceUtils.logPlaybackEvent("ExoPlayer", song, "Added to queue")
    }
    
    /**
     * Remove media item from queue
     */
    fun removeFromQueue(index: Int) {
        if (index in 0 until currentMediaItems.size) {
            currentMediaItems.removeAt(index)
            exoPlayer?.removeMediaItem(index)
            ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Removed from queue: $index")
        }
    }
    
    /**
     * Move queue item
     */
    fun moveQueueItem(from: Int, to: Int) {
        if (from in 0 until currentMediaItems.size && to in 0 until currentMediaItems.size) {
            val item = currentMediaItems.removeAt(from)
            currentMediaItems.add(to, item)
            exoPlayer?.moveMediaItem(from, to)
            ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Moved queue item: $from -> $to")
        }
    }
    
    /**
     * Clear queue
     */
    fun clearQueue() {
        currentMediaItems.clear()
        exoPlayer?.clearMediaItems()
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Queue cleared")
    }
    
    private fun createMediaItem(song: Song): MediaItem {
        val uri = if (song.path.startsWith("content://")) {
            song.path.toUri()
        } else {
            Uri.fromFile(java.io.File(song.path))
        }
        
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setGenre(song.genre)
                    .setTrackNumber(song.trackNumber)
                    .setReleaseYear(song.year)
                    .setArtworkUri(song.artworkPath.takeIf { it.isNotEmpty() }?.toUri())
                    .build()
            )
            .build()
    }
    
    private fun createMediaSource(mediaItem: MediaItem): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val extractorsFactory = DefaultExtractorsFactory()
        
        return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .createMediaSource(mediaItem)
    }
    
    // Position tracking
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    val position = player.currentPosition
                    val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                    notifyPositionChanged(position, duration)
                }
                handler.postDelayed(this, positionUpdateInterval)
            }
        }
        handler.post(positionUpdateRunnable!!)
    }
    
    private fun stopPositionUpdates() {
        positionUpdateRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
            positionUpdateRunnable = null
        }
    }
    
    // Getters
    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true
    
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    
    fun getDuration(): Long = exoPlayer?.duration?.takeIf { it != C.TIME_UNSET } ?: 0L
    
    fun getBufferedPosition(): Long = exoPlayer?.bufferedPosition ?: 0L
    
    fun getPlaybackSpeed(): Float = exoPlayer?.playbackParameters?.speed ?: 1.0f
    
    fun getVolume(): Float = exoPlayer?.volume ?: 1.0f
    
    fun getCurrentMediaItem(): MediaItem? = exoPlayer?.currentMediaItem
    
    fun getCurrentMediaItemIndex(): Int = exoPlayer?.currentMediaItemIndex ?: -1
    
    fun getMediaItemCount(): Int = exoPlayer?.mediaItemCount ?: 0
    
    fun hasNextMediaItem(): Boolean = exoPlayer?.hasNextMediaItem() == true
    
    fun hasPreviousMediaItem(): Boolean = exoPlayer?.hasPreviousMediaItem() == true
    
    fun getAudioSessionId(): Int = exoPlayer?.audioSessionId ?: 0
    
    fun getPlaybackState(): Int = exoPlayer?.playbackState ?: Player.STATE_IDLE
    
    fun isLoading(): Boolean = exoPlayer?.isLoading == true
    
    // Listener management
    fun addPlayerListener(listener: PlayerListener) {
        playerListeners.add(listener)
    }
    
    fun removePlayerListener(listener: PlayerListener) {
        playerListeners.remove(listener)
    }
    
    private fun notifyPlaybackStateChanged(state: PlaybackState) {
        playerListeners.forEach { listener ->
            try {
                listener.onPlaybackStateChanged(state)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying state: ${e.message}")
            }
        }
    }
    
    private fun notifyPositionChanged(position: Long, duration: Long) {
        playerListeners.forEach { listener ->
            try {
                listener.onPositionChanged(position, duration)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying position: ${e.message}")
            }
        }
    }
    
    private fun notifyPlayerError(error: PlayerError) {
        playerListeners.forEach { listener ->
            try {
                listener.onPlayerError(error)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying error: ${e.message}")
            }
        }
    }
    
    private fun notifyMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        playerListeners.forEach { listener ->
            try {
                listener.onMediaItemTransition(mediaItem, reason)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying transition: ${e.message}")
            }
        }
    }
    
    private fun notifyPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        playerListeners.forEach { listener ->
            try {
                listener.onPlayWhenReadyChanged(playWhenReady, reason)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying ready: ${e.message}")
            }
        }
    }
    
    private fun notifyIsLoadingChanged(isLoading: Boolean) {
        playerListeners.forEach { listener ->
            try {
                listener.onIsLoadingChanged(isLoading)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying loading: ${e.message}")
            }
        }
    }
    
    private fun notifyAudioSessionIdChanged(audioSessionId: Int) {
        playerListeners.forEach { listener ->
            try {
                listener.onAudioSessionIdChanged(audioSessionId)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Error notifying session: ${e.message}")
            }
        }
    }
    
    /**
     * Release player resources
     */
    fun release() {
        stopPositionUpdates()
        playerListeners.clear()
        currentMediaItems.clear()
        
        exoPlayer?.apply {
            stop()
            release()
        }
        exoPlayer = null
        
        coroutineScope.cancel()
        isInitialized = false
        
        ServiceUtils.logPlaybackEvent("ExoPlayer", null, "Player released")
    }
    
    /**
     * Get player diagnostics
     */
    fun getDiagnostics(): ExoPlayerDiagnostics {
        return ExoPlayerDiagnostics(
            isInitialized = isInitialized,
            isPlaying = isPlaying(),
            playbackState = getPlaybackState(),
            currentPosition = getCurrentPosition(),
            duration = getDuration(),
            bufferedPosition = getBufferedPosition(),
            mediaItemCount = getMediaItemCount(),
            currentIndex = getCurrentMediaItemIndex(),
            audioSessionId = getAudioSessionId(),
            volume = getVolume(),
            playbackSpeed = getPlaybackSpeed(),
            hasListeners = playerListeners.isNotEmpty()
        )
    }
}

data class ExoPlayerDiagnostics(
    val isInitialized: Boolean,
    val isPlaying: Boolean,
    val playbackState: Int,
    val currentPosition: Long,
    val duration: Long,
    val bufferedPosition: Long,
    val mediaItemCount: Int,
    val currentIndex: Int,
    val audioSessionId: Int,
    val volume: Float,
    val playbackSpeed: Float,
    val hasListeners: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
