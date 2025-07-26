package com.tinhtx.localplayerapplication.presentation.service.components

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.Song

class MediaSessionManager(
    private val context: Context,
    private val onAction: (MediaSessionAction) -> Unit
) {
    
    val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "MusicService")
    
    init {
        setupMediaSession()
    }
    
    private fun setupMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                onAction(MediaSessionAction.Play)
            }
            
            override fun onPause() {
                onAction(MediaSessionAction.Pause)
            }
            
            override fun onSkipToNext() {
                onAction(MediaSessionAction.SkipToNext)
            }
            
            override fun onSkipToPrevious() {
                onAction(MediaSessionAction.SkipToPrevious)
            }
            
            override fun onSeekTo(pos: Long) {
                onAction(MediaSessionAction.SeekTo(pos))
            }
            
            override fun onStop() {
                onAction(MediaSessionAction.Stop)
            }
        })
        
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        
        mediaSession.isActive = true
    }
    
    fun updateMetadata(song: Song?) {
        if (song == null) {
            mediaSession.setMetadata(null)
            return
        }
        
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.displayArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.displayAlbum)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
            .apply {
                // Add album art if available
                try {
                    val albumArt = MediaUtils.getAlbumArt(context, song.albumId)
                    albumArt?.let { bitmap ->
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    }
                } catch (exception: Exception) {
                    android.util.Log.w("MediaSessionManager", "Could not load album art", exception)
                }
            }
            .build()
        
        mediaSession.setMetadata(metadata)
    }
    
    fun updatePlaybackState(state: Int, position: Long, playbackSpeed: Float) {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, playbackSpeed)
            .setActions(getAvailableActions())
            .build()
        
        mediaSession.setPlaybackState(playbackState)
    }
    
    private fun getAvailableActions(): Long {
        return PlaybackStateCompat.ACTION_PLAY or
               PlaybackStateCompat.ACTION_PAUSE or
               PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
               PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
               PlaybackStateCompat.ACTION_SEEK_TO or
               PlaybackStateCompat.ACTION_STOP
    }
    
    fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }
}

sealed class MediaSessionAction {
    object Play : MediaSessionAction()
    object Pause : MediaSessionAction()
    object SkipToNext : MediaSessionAction()
    object SkipToPrevious : MediaSessionAction()
    data class SeekTo(val position: Long) : MediaSessionAction()
    object Stop : MediaSessionAction()
}
