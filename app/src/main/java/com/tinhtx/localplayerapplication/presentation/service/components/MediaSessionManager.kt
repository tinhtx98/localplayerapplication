package com.tinhtx.localplayerapplication.data.service.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.*
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.data.service.PlayerState
import com.tinhtx.localplayerapplication.data.service.RepeatMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionManager @Inject constructor(
    private val context: Context,
    private val imageLoader: ImageLoader
) {
    
    private var mediaSession: MediaSessionCompat? = null
    private var sessionCallback: MediaSessionCallback? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Cache for album art bitmaps
    private val albumArtCache = mutableMapOf<String, Bitmap>()
    private val maxCacheSize = 10
    
    interface MediaSessionCallback {
        fun onPlay()
        fun onPause()
        fun onSkipToNext()
        fun onSkipToPrevious()
        fun onStop()
        fun onSeekTo(position: Long)
        fun onSetRepeatMode(repeatMode: Int)
        fun onSetShuffleMode(shuffleMode: Int)
        fun onCustomAction(action: String, extras: Bundle?)
        fun onPlayFromMediaId(mediaId: String, extras: Bundle?)
        fun onPlayFromSearch(query: String, extras: Bundle?)
    }
    
    /**
     * Initialize media session
     */
    fun initializeSession(callback: MediaSessionCallback): MediaSessionCompat {
        this.sessionCallback = callback
        
        mediaSession = MediaSessionCompat(context, ServiceUtils.MEDIA_SESSION_TAG).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            
            setCallback(createMediaSessionCallback())
            
            // Set initial playback state
            setPlaybackState(createInitialPlaybackState())
            
            // Enable session
            isActive = true
        }
        
        ServiceUtils.logPlaybackEvent(
            "MediaSession", 
            null, 
            "Session initialized"
        )
        
        return mediaSession!!
    }
    
    private fun createMediaSessionCallback() = object : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlay called")
            sessionCallback?.onPlay()
        }
        
        override fun onPause() {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onPause called")
            sessionCallback?.onPause()
        }
        
        override fun onSkipToNext() {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onSkipToNext called")
            sessionCallback?.onSkipToNext()
        }
        
        override fun onSkipToPrevious() {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onSkipToPrevious called")
            sessionCallback?.onSkipToPrevious()
        }
        
        override fun onStop() {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onStop called")
            sessionCallback?.onStop()
        }
        
        override fun onSeekTo(pos: Long) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onSeekTo: $pos")
            sessionCallback?.onSeekTo(pos)
        }
        
        override fun onSetRepeatMode(repeatMode: Int) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onSetRepeatMode: $repeatMode")
            sessionCallback?.onSetRepeatMode(repeatMode)
        }
        
        override fun onSetShuffleMode(shuffleMode: Int) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onSetShuffleMode: $shuffleMode")
            sessionCallback?.onSetShuffleMode(shuffleMode)
        }
        
        override fun onCustomAction(action: String, extras: Bundle?) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onCustomAction: $action")
            sessionCallback?.onCustomAction(action, extras)
        }
        
        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlayFromMediaId: $mediaId")
            sessionCallback?.onPlayFromMediaId(mediaId, extras)
        }
        
        override fun onPlayFromSearch(query: String, extras: Bundle?) {
            ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlayFromSearch: $query")
            sessionCallback?.onPlayFromSearch(query, extras)
        }
    }
    
    private fun createInitialPlaybackState(): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setActions(ServiceUtils.PLAYBACK_ACTIONS)
            .setState(
                PlaybackStateCompat.STATE_NONE,
                0L,
                1.0f
            )
            .build()
    }
    
    /**
     * Update playback state
     */
    fun updatePlaybackState(playerState: PlayerState) {
        val playbackState = when {
            playerState.isLoading -> PlaybackStateCompat.STATE_BUFFERING
            playerState.isPlaying -> PlaybackStateCompat.STATE_PLAYING
            playerState.currentSong != null -> PlaybackStateCompat.STATE_PAUSED
            else -> PlaybackStateCompat.STATE_STOPPED
        }
        
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(ServiceUtils.PLAYBACK_ACTIONS)
            .setState(
                playbackState,
                playerState.playbackPosition,
                playerState.playbackSpeed
            )
            .setBufferedPosition(playerState.bufferedPosition)
        
        // Add custom actions
        addCustomActions(stateBuilder, playerState)
        
        // Set extras
        val extras = Bundle().apply {
            putBoolean("shuffle", playerState.shuffleMode)
            putInt("repeat_mode", playerState.repeatMode.toPlaybackStateRepeatMode())
            putFloat("volume", playerState.volume)
            putInt("audio_session_id", playerState.audioSessionId)
        }
        stateBuilder.setExtras(extras)
        
        mediaSession?.setPlaybackState(stateBuilder.build())
        
        ServiceUtils.logPlaybackEvent(
            "MediaSession", 
            playerState.currentSong, 
            "Playback state updated: $playbackState"
        )
    }
    
    private fun addCustomActions(
        builder: PlaybackStateCompat.Builder,
        playerState: PlayerState
    ) {
        // Repeat action
        val repeatIcon = when (playerState.repeatMode) {
            RepeatMode.OFF -> android.R.drawable.ic_menu_rotate
            RepeatMode.ALL -> android.R.drawable.ic_menu_rotate
            RepeatMode.ONE -> android.R.drawable.ic_menu_rotate
        }
        
        builder.addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                "TOGGLE_REPEAT",
                "Toggle Repeat",
                repeatIcon
            ).build()
        )
        
        // Shuffle action
        val shuffleIcon = if (playerState.shuffleMode) {
            android.R.drawable.ic_menu_sort_by_size
        } else {
            android.R.drawable.ic_menu_sort_alphabetically
        }
        
        builder.addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                "TOGGLE_SHUFFLE",
                "Toggle Shuffle",
                shuffleIcon
            ).build()
        )
        
        // Favorite action
        builder.addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                "TOGGLE_FAVORITE",
                "Toggle Favorite",
                android.R.drawable.btn_star
            ).build()
        )
    }
    
    /**
     * Update media metadata
     */
    fun updateMetadata(song: Song?) {
        if (song == null) {
            mediaSession?.setMetadata(null)
            return
        }
        
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.genre)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.path)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.trackNumber.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year.toLong())
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "${song.artist} • ${song.album}")
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.album)
        
        // Load album art asynchronously
        loadAlbumArt(song) { bitmap ->
            if (bitmap != null) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
            }
            
            mediaSession?.setMetadata(metadataBuilder.build())
            
            ServiceUtils.logPlaybackEvent(
                "MediaSession", 
                song, 
                "Metadata updated with album art: ${bitmap != null}"
            )
        }
        
        // Set metadata immediately without album art
        mediaSession?.setMetadata(metadataBuilder.build())
    }
    
    private fun loadAlbumArt(song: Song, callback: (Bitmap?) -> Unit) {
        if (song.artworkPath.isEmpty()) {
            callback(null)
            return
        }
        
        // Check cache first
        albumArtCache[song.artworkPath]?.let { cachedBitmap ->
            callback(cachedBitmap)
            return
        }
        
        coroutineScope.launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(song.artworkPath)
                    .size(512, 512) // Optimal size for media session
                    .build()
                
                val drawable = imageLoader.execute(request).drawable
                val bitmap = drawable?.toBitmap(512, 512)
                
                bitmap?.let { bmp ->
                    // Cache the bitmap
                    cacheAlbumArt(song.artworkPath, bmp)
                }
                
                withContext(Dispatchers.Main) {
                    callback(bitmap)
                }
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "MediaSession", 
                    song, 
                    "Failed to load album art: ${e.message}"
                )
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }
    
    private fun cacheAlbumArt(artworkPath: String, bitmap: Bitmap) {
        // Remove oldest entries if cache is full
        if (albumArtCache.size >= maxCacheSize) {
            val oldestKey = albumArtCache.keys.first()
            albumArtCache[oldestKey]?.recycle()
            albumArtCache.remove(oldestKey)
        }
        
        albumArtCache[artworkPath] = bitmap
    }
    
    /**
     * Set session token for MediaController
     */
    fun getSessionToken(): MediaSessionCompat.Token? {
        return mediaSession?.sessionToken
    }
    
    /**
     * Set queue for media session
     */
    fun setQueue(queue: List<Song>, currentIndex: Int) {
        val queueItems = queue.mapIndexed { index, song ->
            MediaSessionCompat.QueueItem(
                song.toMediaMetadata().description,
                index.toLong()
            )
        }
        
        mediaSession?.setQueue(queueItems)
        mediaSession?.setQueueTitle("Current Queue (${queue.size} songs)")
        
        ServiceUtils.logPlaybackEvent(
            "MediaSession", 
            null, 
            "Queue updated: ${queue.size} songs, current: $currentIndex"
        )
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatMode(repeatMode: RepeatMode) {
        mediaSession?.setRepeatMode(repeatMode.toPlaybackStateRepeatMode())
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleMode(shuffleMode: Boolean) {
        val mode = if (shuffleMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL
        } else {
            PlaybackStateCompat.SHUFFLE_MODE_NONE
        }
        mediaSession?.setShuffleMode(mode)
    }
    
    /**
     * Update session activity
     */
    fun updateSessionActivity(pendingIntent: android.app.PendingIntent) {
        mediaSession?.setSessionActivity(pendingIntent)
    }
    
    /**
     * Handle media button events
     */
    fun handleMediaButtonEvent(intent: android.content.Intent): Boolean {
        return mediaSession?.controller?.dispatchMediaButtonEvent(
            intent.getParcelableExtra(android.content.Intent.EXTRA_KEY_EVENT)
        ) ?: false
    }
    
    /**
     * Get current playback info
     */
    fun getCurrentPlaybackInfo(): Bundle {
        val playbackState = mediaSession?.controller?.playbackState
        val metadata = mediaSession?.controller?.metadata
        
        return Bundle().apply {
            playbackState?.let { state ->
                putInt("state", state.state)
                putLong("position", state.position)
                putFloat("playback_speed", state.playbackSpeed)
                putLong("buffered_position", state.bufferedPosition)
            }
            
            metadata?.let { meta ->
                putString("title", meta.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                putString("artist", meta.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                putString("album", meta.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                putLong("duration", meta.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
            }
        }
    }
    
    /**
     * Release media session
     */
    fun release() {
        coroutineScope.cancel()
        
        // Clear album art cache
        albumArtCache.values.forEach { bitmap ->
            bitmap.recycle()
        }
        albumArtCache.clear()
        
        mediaSession?.apply {
            isActive = false
            release()
        }
        mediaSession = null
        sessionCallback = null
        
        ServiceUtils.logPlaybackEvent(
            "MediaSession", 
            null, 
            "Session released"
        )
    }
    
    /**
     * Check if session is active
     */
    fun isSessionActive(): Boolean {
        return mediaSession?.isActive == true
    }
    
    /**
     * Get media session for service integration
     */
    fun getMediaSession(): MediaSessionCompat? {
        return mediaSession
    }
}
