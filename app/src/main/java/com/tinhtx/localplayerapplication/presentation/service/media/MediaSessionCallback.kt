package com.tinhtx.localplayerapplication.data.service.media

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.tinhtx.localplayerapplication.data.service.components.ServiceUtils
import com.tinhtx.localplayerapplication.data.service.RepeatMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionCallback @Inject constructor(
    private val exoPlayerManager: ExoPlayerManager
) : MediaSessionCompat.Callback() {
    
    private var callback: MediaSessionCallbackListener? = null
    
    interface MediaSessionCallbackListener {
        fun onPlayFromMediaId(mediaId: String, extras: Bundle?)
        fun onPlayFromSearch(query: String, extras: Bundle?)
        fun onCustomAction(action: String, extras: Bundle?)
        fun onAddQueueItem(description: android.support.v4.media.MediaDescriptionCompat)
        fun onRemoveQueueItem(description: android.support.v4.media.MediaDescriptionCompat)
    }
    
    fun setCallback(callback: MediaSessionCallbackListener) {
        this.callback = callback
    }
    
    override fun onPlay() {
        super.onPlay()
        exoPlayerManager.play()
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlay")
    }
    
    override fun onPause() {
        super.onPause()
        exoPlayerManager.pause()
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        exoPlayerManager.stop()
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onStop")
    }
    
    override fun onSkipToNext() {
        super.onSkipToNext()
        exoPlayerManager.skipToNext()
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSkipToNext")
    }
    
    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        exoPlayerManager.skipToPrevious()
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSkipToPrevious")
    }
    
    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        exoPlayerManager.seekTo(pos)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSeekTo: $pos")
    }
    
    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val mode = repeatMode.toRepeatMode()
        exoPlayerManager.setRepeatMode(mode)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSetRepeatMode: $mode")
    }
    
    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        val enabled = shuffleMode == android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
        exoPlayerManager.setShuffleMode(enabled)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSetShuffleMode: $enabled")
    }
    
    override fun onSkipToQueueItem(id: Long) {
        super.onSkipToQueueItem(id)
        exoPlayerManager.jumpToQueueItem(id.toInt())
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSkipToQueueItem: $id")
    }
    
    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        callback?.onPlayFromMediaId(mediaId, extras)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlayFromMediaId: $mediaId")
    }
    
    override fun onPlayFromSearch(query: String, extras: Bundle?) {
        super.onPlayFromSearch(query, extras)
        callback?.onPlayFromSearch(query, extras)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onPlayFromSearch: $query")
    }
    
    override fun onCustomAction(action: String, extras: Bundle?) {
        super.onCustomAction(action, extras)
        
        when (action) {
            "TOGGLE_REPEAT" -> {
                // Handle repeat toggle through callback
                callback?.onCustomAction(action, extras)
            }
            "TOGGLE_SHUFFLE" -> {
                // Handle shuffle toggle through callback
                callback?.onCustomAction(action, extras)
            }
            "TOGGLE_FAVORITE" -> {
                // Handle favorite toggle through callback
                callback?.onCustomAction(action, extras)
            }
            else -> {
                callback?.onCustomAction(action, extras)
            }
        }
        
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onCustomAction: $action")
    }
    
    override fun onAddQueueItem(description: android.support.v4.media.MediaDescriptionCompat) {
        super.onAddQueueItem(description)
        callback?.onAddQueueItem(description)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onAddQueueItem: ${description.mediaId}")
    }
    
    override fun onRemoveQueueItem(description: android.support.v4.media.MediaDescriptionCompat) {
        super.onRemoveQueueItem(description)
        callback?.onRemoveQueueItem(description)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onRemoveQueueItem: ${description.mediaId}")
    }
    
    override fun onFastForward() {
        super.onFastForward()
        val currentPosition = exoPlayerManager.getCurrentPosition()
        val newPosition = currentPosition + 15000 // 15 seconds
        exoPlayerManager.seekTo(newPosition)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onFastForward")
    }
    
    override fun onRewind() {
        super.onRewind()
        val currentPosition = exoPlayerManager.getCurrentPosition()
        val newPosition = (currentPosition - 15000).coerceAtLeast(0) // 15 seconds
        exoPlayerManager.seekTo(newPosition)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onRewind")
    }
    
    override fun onSetPlaybackSpeed(speed: Float) {
        super.onSetPlaybackSpeed(speed)
        exoPlayerManager.setPlaybackSpeed(speed)
        ServiceUtils.logPlaybackEvent("MediaSession", null, "onSetPlaybackSpeed: $speed")
    }
    
    private fun Int.toRepeatMode(): RepeatMode {
        return when (this) {
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE -> RepeatMode.OFF
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL -> RepeatMode.ALL
            android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    }
}
