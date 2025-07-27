package com.tinhtx.localplayerapplication.data.service.media

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.usecase.*
import com.tinhtx.localplayerapplication.data.service.*
import com.tinhtx.localplayerapplication.data.service.components.*
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MediaPlayerServiceImpl @Inject constructor(
    private val context: Context,
    private val exoPlayerManager: ExoPlayerManager,
    private val audioFocusManager: AudioFocusManager,
    private val mediaSessionManager: MediaSessionManager,
    private val notificationManager: MusicNotificationManager,
    private val mediaPlaybackPreparer: MediaPlaybackPreparer,
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val updatePlayCountUseCase: UpdatePlayCountUseCase
) : ExoPlayerManager.PlayerListener, 
    MediaSessionManager.MediaSessionCallback,
    MediaPlaybackPreparer.MediaPlaybackPreparerCallback,
    AudioFocusManager.AudioFocusChangeListener {
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Player state management
    private val playerStateManager = PlayerStateManager()
    val playerState: StateFlow<PlayerState> = playerStateManager.playerState
    
    // Media session
    private var mediaSession: MediaSessionCompat? = null
    
    // Service callback
    private var serviceCallback: MediaPlayerServiceCallback? = null
    
    // Favorites cache
    private var favoriteSongs = setOf<String>()
    
    // Statistics
    private var sessionStartTime = System.currentTimeMillis()
    private var totalSongsPlayed = 0
    private var totalPlaybackTime = 0L
    
    interface MediaPlayerServiceCallback {
        fun onPlayerStateChanged(playerState: PlayerState)
        fun onNotificationRequired(playerState: PlayerState, albumArt: Bitmap?)
        fun onStopForeground()
        fun onError(error: PlayerError)
    }
    
    /**
     * Initialize the service implementation
     */
    fun initialize(callback: MediaPlayerServiceCallback) {
        this.serviceCallback = callback
        
        // Initialize components
        exoPlayerManager.initialize()
        exoPlayerManager.addPlayerListener(this)
        
        // Setup audio focus
        audioFocusManager.setAudioFocusChangeListener(this)
        
        // Setup media session
        mediaSession = mediaSessionManager.initializeSession(this)
        
        // Setup media preparer
        mediaPlaybackPreparer.setCallback(this)
        
        // Load favorites
        loadFavorites()
        
        ServiceUtils.logPlaybackEvent(
            "MediaPlayerService", 
            null, 
            "Service implementation initialized"
        )
    }
    
    // ====================
    // PUBLIC API METHODS
    // ====================
    
    /**
     * Play a single song
     */
    fun playSong(song: Song) {
        if (requestAudioFocusIfNeeded()) {
            playerStateManager.setCurrentSong(song)
            playerStateManager.setLoading(true)
            exoPlayerManager.playSong(song)
            updatePlayCount(song)
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", song, "Playing song")
        }
    }
    
    /**
     * Play a queue of songs
     */
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        
        if (requestAudioFocusIfNeeded()) {
            playerStateManager.setQueue(songs, startIndex)
            playerStateManager.setLoading(true)
            exoPlayerManager.playQueue(songs, startIndex)
            
            songs.getOrNull(startIndex)?.let { song ->
                updatePlayCount(song)
            }
            
            ServiceUtils.logPlaybackEvent(
                "MediaPlayerService", 
                null, 
                "Playing queue: ${songs.size} songs, start: $startIndex"
            )
        }
    }
    
    /**
     * Play/Resume playback
     */
    fun play() {
        if (requestAudioFocusIfNeeded()) {
            exoPlayerManager.play()
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Play")
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        exoPlayerManager.pause()
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Pause")
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        exoPlayerManager.stop()
        audioFocusManager.abandonAudioFocus()
        playerStateManager.clear()
        serviceCallback?.onStopForeground()
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Stop")
    }
    
    /**
     * Skip to next track
     */
    fun skipToNext() {
        if (playerStateManager.hasNext()) {
            exoPlayerManager.skipToNext()
            playerStateManager.moveToNext()
            
            playerStateManager.getCurrentState().currentSong?.let { song ->
                updatePlayCount(song)
            }
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Skip next")
        }
    }
    
    /**
     * Skip to previous track  
     */
    fun skipToPrevious() {
        if (playerStateManager.hasPrevious()) {
            exoPlayerManager.skipToPrevious()
            playerStateManager.moveToPrevious()
            
            playerStateManager.getCurrentState().currentSong?.let { song ->
                updatePlayCount(song)
            }
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Skip previous")
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        exoPlayerManager.seekTo(positionMs)
        playerStateManager.setPosition(positionMs)
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Seek to: ${positionMs}ms")
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatMode(mode: RepeatMode) {
        playerStateManager.setRepeatMode(mode)
        exoPlayerManager.setRepeatMode(mode)
        mediaSessionManager.setRepeatMode(mode)
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Repeat mode: $mode")
    }
    
    /**
     * Toggle repeat mode
     */
    fun toggleRepeatMode() {
        val currentMode = playerStateManager.getCurrentState().repeatMode
        val newMode = currentMode.next()
        setRepeatMode(newMode)
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleMode(enabled: Boolean) {
        playerStateManager.setShuffleMode(enabled)
        exoPlayerManager.setShuffleMode(enabled)
        mediaSessionManager.setShuffleMode(enabled)
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Shuffle: $enabled")
    }
    
    /**
     * Toggle shuffle mode
     */
    fun toggleShuffleMode() {
        val enabled = !playerStateManager.getCurrentState().shuffleMode
        setShuffleMode(enabled)
    }
    
    /**
     * Add song to queue
     */
    fun addToQueue(song: Song) {
        val currentState = playerStateManager.getCurrentState()
        val newQueue = currentState.queue + song
        playerStateManager.updateState { it.copy(queue = newQueue) }
        exoPlayerManager.addToQueue(song)
        
        ServiceUtils.logPlaybackEvent("MediaPlayerService", song, "Added to queue")
    }
    
    /**
     * Add multiple songs to queue
     */
    fun addToQueue(songs: List<Song>) {
        val currentState = playerStateManager.getCurrentState()
        val newQueue = currentState.queue + songs
        playerStateManager.updateState { it.copy(queue = newQueue) }
        
        songs.forEach { song ->
            exoPlayerManager.addToQueue(song)
        }
        
        ServiceUtils.logPlaybackEvent(
            "MediaPlayerService", 
            null, 
            "Added ${songs.size} songs to queue"
        )
    }
    
    /**
     * Remove song from queue
     */
    fun removeFromQueue(index: Int) {
        val currentState = playerStateManager.getCurrentState()
        if (index in 0 until currentState.queue.size) {
            val newQueue = currentState.queue.toMutableList().apply { removeAt(index) }
            val adjustedIndex = if (index <= currentState.currentIndex) {
                (currentState.currentIndex - 1).coerceAtLeast(0)
            } else {
                currentState.currentIndex
            }
            
            playerStateManager.updateState { 
                it.copy(
                    queue = newQueue,
                    currentIndex = adjustedIndex
                )
            }
            exoPlayerManager.removeFromQueue(index)
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Removed from queue: $index")
        }
    }
    
    /**
     * Clear queue
     */
    fun clearQueue() {
        playerStateManager.updateState { it.copy(queue = emptyList(), currentIndex = -1) }
        exoPlayerManager.clearQueue()
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Queue cleared")
    }
    
    /**
     * Jump to queue item
     */
    fun jumpToQueueItem(index: Int) {
        val currentState = playerStateManager.getCurrentState()
        if (index in 0 until currentState.queue.size) {
            playerStateManager.updateState { 
                it.copy(
                    currentIndex = index,
                    currentSong = currentState.queue[index]
                )
            }
            exoPlayerManager.jumpToQueueItem(index)
            
            currentState.queue[index].let { song ->
                updatePlayCount(song)
            }
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Jump to queue item: $index")
        }
    }
    
    /**
     * Move queue item
     */
    fun moveQueueItem(from: Int, to: Int) {
        val currentState = playerStateManager.getCurrentState()
        if (from in 0 until currentState.queue.size && to in 0 until currentState.queue.size) {
            val newQueue = currentState.queue.toMutableList()
            val item = newQueue.removeAt(from)
            newQueue.add(to, item)
            
            // Adjust current index
            val newCurrentIndex = when {
                currentState.currentIndex == from -> to
                from < currentState.currentIndex && to >= currentState.currentIndex -> 
                    currentState.currentIndex - 1
                from > currentState.currentIndex && to <= currentState.currentIndex -> 
                    currentState.currentIndex + 1
                else -> currentState.currentIndex
            }
            
            playerStateManager.updateState { 
                it.copy(
                    queue = newQueue,
                    currentIndex = newCurrentIndex
                )
            }
            exoPlayerManager.moveQueueItem(from, to)
            
            ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Moved queue item: $from -> $to")
        }
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        playerStateManager.setPlaybackSpeed(speed)
        exoPlayerManager.setPlaybackSpeed(speed)
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Playback speed: $speed")
    }
    
    /**
     * Set volume
     */
    fun setVolume(volume: Float) {
        playerStateManager.setVolume(volume)
        exoPlayerManager.setVolume(volume)
        audioFocusManager.setVolume(volume)
        ServiceUtils.logPlaybackEvent("MediaPlayerService", null, "Volume: $volume")
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavorite(song: Song) {
        coroutineScope.launch {
            try {
                toggleFavoriteUseCase(song.id).collect { result ->
                    result.fold(
                        onSuccess = { isFavorite ->
                            if (isFavorite) {
                                favoriteSongs = favoriteSongs + song.id
                            } else {
                                favoriteSongs = favoriteSongs - song.id
                            }
                            
                            ServiceUtils.logPlaybackEvent(
                                "MediaPlayerService", 
                                song, 
                                "Favorite toggled: $isFavorite"
                            )
                        },
                        onFailure = { error ->
                            ServiceUtils.logPlaybackEvent(
                                "MediaPlayerService", 
                                song, 
                                "Failed to toggle favorite: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "MediaPlayerService", 
                    song, 
                    "Exception toggling favorite: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check if song is favorite
     */
    fun isFavorite(song: Song): Boolean {
        return favoriteSongs.contains(song.id)
    }
    
    // ====================
    // GETTERS
    // ====================
    
    fun getCurrentSong(): Song? = playerStateManager.getCurrentState().currentSong
    fun getQueue(): List<Song> = playerStateManager.getCurrentState().queue
    fun getCurrentIndex(): Int = playerStateManager.getCurrentState().currentIndex
    fun isPlaying(): Boolean = playerStateManager.getCurrentState().isPlaying
    fun getDuration(): Long = exoPlayerManager.getDuration()
    fun getCurrentPosition(): Long = exoPlayerManager.getCurrentPosition()
    fun getBufferedPosition(): Long = exoPlayerManager.getBufferedPosition()
    fun getPlaybackSpeed(): Float = playerStateManager.getCurrentState().playbackSpeed
    fun getVolume(): Float = playerStateManager.getCurrentState().volume
    fun getRepeatMode(): RepeatMode = playerStateManager.getCurrentState().repeatMode
    fun isShuffleEnabled(): Boolean = playerStateManager.getCurrentState().shuffleMode
    fun getAudioSessionId(): Int = exoPlayerManager.getAudioSessionId()
    fun getMediaSessionToken(): MediaSessionCompat.Token? = mediaSession?.sessionToken
    
    // ====================
    // LISTENER IMPLEMENTATIONS
    // ====================
    
    // ExoPlayerManager.PlayerListener
    override fun onPlaybackStateChanged(playbackState: com.tinhtx.localplayerapplication.data.service.PlaybackState) {
        val isPlaying = playbackState == com.tinhtx.localplayerapplication.data.service.PlaybackState.READY && exoPlayerManager.isPlaying()
        val isLoading = playbackState == com.tinhtx.localplayerapplication.data.service.PlaybackState.BUFFERING
        
        playerStateManager.setPlaying(isPlaying)
        playerStateManager.setLoading(isLoading)
        
        updateMediaSession()
        notifyServiceCallback()
    }
    
    override fun onPositionChanged(position: Long, duration: Long) {
        playerStateManager.setPosition(position)
        playerStateManager.setDuration(duration)
        
        // Update total playback time
        totalPlaybackTime += 1000 // Approximate 1 second increment
    }
    
    override fun onPlayerError(error: PlayerError) {
        playerStateManager.setError(error)
        serviceCallback?.onError(error)
        
        ServiceUtils.logPlaybackEvent(
            "MediaPlayerService", 
            null, 
            "Player error: ${error.message}"
        )
    }
    
    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
        // Update current song based on media item
        val currentState = playerStateManager.getCurrentState()
        val newIndex = exoPlayerManager.getCurrentMediaItemIndex()
        
        if (newIndex >= 0 && newIndex < currentState.queue.size) {
            val newSong = currentState.queue[newIndex]
            playerStateManager.updateState { 
                it.copy(
                    currentSong = newSong,
                    currentIndex = newIndex
                )
            }
            
            updatePlayCount(newSong)
            totalSongsPlayed++
            
            updateMediaSession()
            notifyServiceCallback()
        }
    }
    
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        playerStateManager.setPlaying(playWhenReady && exoPlayerManager.getPlaybackState() == androidx.media3.common.Player.STATE_READY)
        updateMediaSession()
        notifyServiceCallback()
    }
    
    override fun onIsLoadingChanged(isLoading: Boolean) {
        playerStateManager.setLoading(isLoading)
        notifyServiceCallback()
    }
    
    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        playerStateManager.setAudioSessionId(audioSessionId)
    }
    
    // MediaSessionManager.MediaSessionCallback
    override fun onPlay() = play()
    override fun onPause() = pause()
    override fun onSkipToNext() = skipToNext()
    override fun onSkipToPrevious() = skipToPrevious()
    override fun onStop() = stop()
    override fun onSeekTo(position: Long) = seekTo(position)
    override fun onSetRepeatMode(repeatMode: Int) = setRepeatMode(repeatMode.toRepeatMode())
    override fun onSetShuffleMode(shuffleMode: Int) = setShuffleMode(shuffleMode == 1)
    
    override fun onCustomAction(action: String, extras: Bundle?) {
        when (action) {
            "TOGGLE_REPEAT" -> toggleRepeatMode()
            "TOGGLE_SHUFFLE" -> toggleShuffleMode()
            "TOGGLE_FAVORITE" -> {
                getCurrentSong()?.let { song ->
                    toggleFavorite(song)
                }
            }
        }
    }
    
    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        mediaPlaybackPreparer.onPrepareFromMediaId(mediaId, true, extras)
    }
    
    override fun onPlayFromSearch(query: String, extras: Bundle?) {
        mediaPlaybackPreparer.onPrepareFromSearch(query, true, extras)
    }
    
    // MediaPlaybackPreparer.MediaPlaybackPreparerCallback
    override fun onPrepared(songs: List<Song>, startIndex: Int) {
        playQueue(songs, startIndex)
    }
    
    override fun onError(error: String) {
        val playerError = PlayerError.Unknown(error)
        playerStateManager.setError(playerError)
        serviceCallback?.onError(playerError)
    }
    
    // AudioFocusManager.AudioFocusChangeListener
    override fun onAudioFocusGained() {
        exoPlayerManager.setVolume(audioFocusManager.getEffectiveVolume())
        // Resume if was playing before focus loss
    }
    
    override fun onAudioFocusLost() {
        pause()
    }
    
    override fun onAudioFocusLostTransient() {
        pause()
    }
    
    override fun onAudioFocusLostTransientCanDuck() {
        exoPlayerManager.setVolume(audioFocusManager.getEffectiveVolume())
    }
    
    // ====================
    // PRIVATE METHODS
    // ====================
    
    private fun requestAudioFocusIfNeeded(): Boolean {
        return if (!audioFocusManager.hasAudioFocus()) {
            audioFocusManager.requestAudioFocus()
        } else {
            true
        }
    }
    
    private fun updateMediaSession() {
        val currentState = playerStateManager.getCurrentState()
        mediaSessionManager.updatePlaybackState(currentState)
        mediaSessionManager.updateMetadata(currentState.currentSong)
        mediaSessionManager.setQueue(currentState.queue, currentState.currentIndex)
    }
    
    private fun notifyServiceCallback() {
        val currentState = playerStateManager.getCurrentState()
        serviceCallback?.onPlayerStateChanged(currentState)
        
        // Request notification update if playing
        if (currentState.currentSong != null) {
            serviceCallback?.onNotificationRequired(currentState, null)
        }
    }
    
    private fun loadFavorites() {
        coroutineScope.launch {
            try {
                getFavoriteSongsUseCase().collect { result ->
                    result.fold(
                        onSuccess = { songs ->
                            favoriteSongs = songs.map { it.id }.toSet()
                        },
                        onFailure = { error ->
                            ServiceUtils.logPlaybackEvent(
                                "MediaPlayerService", 
                                null, 
                                "Failed to load favorites: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "MediaPlayerService", 
                    null, 
                    "Exception loading favorites: ${e.message}"
                )
            }
        }
    }
    
    private fun updatePlayCount(song: Song) {
        coroutineScope.launch {
            try {
                updatePlayCountUseCase(song.id)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "MediaPlayerService", 
                    song, 
                    "Failed to update play count: ${e.message}"
                )
            }
        }
    }
    
    private fun Int.toRepeatMode(): RepeatMode {
        return when (this) {
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE -> RepeatMode.OFF
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL -> RepeatMode.ALL
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    }
    
    /**
     * Get service statistics
     */
    fun getStatistics(): com.tinhtx.localplayerapplication.data.service.components.PlaybackStatistics {
        return com.tinhtx.localplayerapplication.data.service.components.PlaybackStatistics(
            totalSongsPlayed = totalSongsPlayed,
            totalPlaybackTime = totalPlaybackTime,
            sessionsCount = 1,
            favoriteGenre = getCurrentSong()?.genre ?: "",
            mostPlayedSong = getCurrentSong(),
            averageSessionLength = totalPlaybackTime,
            skipRate = 0f, // TODO: Calculate skip rate
            errorRate = 0f, // TODO: Calculate error rate
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Reset statistics
     */
    fun resetStatistics() {
        totalSongsPlayed = 0
        totalPlaybackTime = 0L
        sessionStartTime = System.currentTimeMillis()
    }
    
    /**
     * Get service uptime
     */
    fun getServiceUptime(): Long {
        return System.currentTimeMillis() - sessionStartTime
    }
    
    /**
     * Release resources
     */
    fun release() {
        coroutineScope.cancel()
        
        exoPlayerManager.removePlayerListener(this)
        exoPlayerManager.release()
        
        audioFocusManager.cleanup()
        mediaSessionManager.release()
        mediaPlaybackPreparer.release()
        
        playerStateManager.clear()
        serviceCallback = null
        
        ServiceUtils.logPlaybackEvent(
            "MediaPlayerService", 
            null, 
            "Service implementation released"
        )
    }
}
