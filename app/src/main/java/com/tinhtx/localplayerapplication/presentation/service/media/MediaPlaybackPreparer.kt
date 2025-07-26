package com.tinhtx.localplayerapplication.presentation.service.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaSessionManager
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.usecase.music.GetSongByIdUseCase
import com.tinhtx.localplayerapplication.domain.usecase.player.PlaySongUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MediaPlaybackPreparer @Inject constructor(
    private val getSongByIdUseCase: GetSongByIdUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val serviceScope: CoroutineScope
) : MediaSessionManager.MediaSessionCallback.MediaPlaybackPreparer {
    
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        serviceScope.launch {
            try {
                val songId = mediaId.toLongOrNull() ?: return@launch
                val song = getSongByIdUseCase(songId) ?: return@launch
                
                playSongUseCase(song, "media_session_prepare")
                
                android.util.Log.d("MediaPlaybackPreparer", "Prepared song: ${song.title}")
            } catch (exception: Exception) {
                android.util.Log.e("MediaPlaybackPreparer", "Error preparing from media ID", exception)
            }
        }
    }
    
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        serviceScope.launch {
            try {
                // Implement search-based playback preparation
                // This could integrate with your search functionality
                android.util.Log.d("MediaPlaybackPreparer", "Prepare from search: $query")
            } catch (exception: Exception) {
                android.util.Log.e("MediaPlaybackPreparer", "Error preparing from search", exception)
            }
        }
    }
    
    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
        serviceScope.launch {
            try {
                // Handle URI-based playback preparation
                android.util.Log.d("MediaPlaybackPreparer", "Prepare from URI: $uri")
            } catch (exception: Exception) {
                android.util.Log.e("MediaPlaybackPreparer", "Error preparing from URI", exception)
            }
        }
    }
    
    override fun onPrepare(playWhenReady: Boolean) {
        serviceScope.launch {
            try {
                // Handle generic prepare request
                android.util.Log.d("MediaPlaybackPreparer", "Generic prepare request")
            } catch (exception: Exception) {
                android.util.Log.e("MediaPlaybackPreparer", "Error in generic prepare", exception)
            }
        }
    }
    
    override fun onCommand(
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) {
        serviceScope.launch {
            try {
                when (command) {
                    "GET_CURRENT_MEDIA" -> {
                        // Return current media information
                    }
                    "UPDATE_QUEUE" -> {
                        // Handle queue updates
                    }
                    // Add more commands as needed
                }
            } catch (exception: Exception) {
                android.util.Log.e("MediaPlaybackPreparer", "Error handling command", exception)
            }
        }
    }
}
