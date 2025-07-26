package com.tinhtx.localplayerapplication.presentation.service.cast

import android.content.Context
import com.google.android.gms.cast.*
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.ResultCallback
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.service.CastConnectionState
import com.tinhtx.localplayerapplication.presentation.service.CastState
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastManager @Inject constructor(
    private val context: Context
) {
    
    private var castContext: CastContext? = null
    private var sessionManager: SessionManager? = null
    private var remoteMediaClient: RemoteMediaClient? = null
    
    private val _castState = MutableStateFlow(CastState())
    val castState: StateFlow<CastState> = _castState.asStateFlow()
    
    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            onApplicationConnected(session)
        }
        
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            onApplicationConnected(session)
        }
        
        override fun onSessionEnded(session: CastSession, error: Int) {
            onApplicationDisconnected()
        }
        
        override fun onSessionSuspended(session: CastSession, reason: Int) {
            onApplicationDisconnected()
        }
    }
    
    private val remoteMediaClientCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            updateMediaStatus()
        }
        
        override fun onMetadataUpdated() {
            updateMediaStatus()
        }
        
        override fun onQueueStatusUpdated() {
            updateMediaStatus()
        }
    }
    
    fun initialize() {
        try {
            castContext = CastContext.getSharedInstance(context)
            sessionManager = castContext?.sessionManager
            sessionManager?.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        } catch (exception: Exception) {
            android.util.Log.e("CastManager", "Cast initialization failed", exception)
        }
    }
    
    fun startCasting(song: Song) {
        try {
            val mediaInfo = createMediaInfo(song)
            val request = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()
            
            remoteMediaClient?.load(request)?.setResultCallback { result ->
                if (result.status.isSuccess) {
                    android.util.Log.d("CastManager", "Media loaded successfully")
                } else {
                    android.util.Log.e("CastManager", "Failed to load media: ${result.status}")
                }
            }
        } catch (exception: Exception) {
            android.util.Log.e("CastManager", "Error starting cast", exception)
        }
    }
    
    fun playCast() {
        remoteMediaClient?.play()
    }
    
    fun pauseCast() {
        remoteMediaClient?.pause()
    }
    
    fun seekCast(position: Long) {
        remoteMediaClient?.seek(position)
    }
    
    fun stopCasting() {
        try {
            sessionManager?.endCurrentSession(true)
        } catch (exception: Exception) {
            android.util.Log.e("CastManager", "Error stopping cast", exception)
        }
    }
    
    fun setVolume(volume: Float) {
        try {
            castContext?.sessionManager?.currentCastSession?.let { session ->
                session.setVolume(volume.toDouble())
            }
        } catch (exception: Exception) {
            android.util.Log.e("CastManager", "Error setting volume", exception)
        }
    }
    
    fun isCasting(): Boolean = castContext?.sessionManager?.currentCastSession != null
    
    fun getCastDeviceName(): String? {
        return castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName
    }
    
    private fun onApplicationConnected(castSession: CastSession) {
        remoteMediaClient = castSession.remoteMediaClient
        remoteMediaClient?.registerCallback(remoteMediaClientCallback)
        
        _castState.value = _castState.value.copy(
            isConnected = true,
            connectionState = CastConnectionState.CONNECTED,
            deviceName = castSession.castDevice.friendlyName
        )
        
        android.util.Log.d("CastManager", "Connected to: ${castSession.castDevice.friendlyName}")
    }
    
    private fun onApplicationDisconnected() {
        remoteMediaClient?.unregisterCallback(remoteMediaClientCallback)
        remoteMediaClient = null
        
        _castState.value = _castState.value.copy(
            isConnected = false,
            connectionState = CastConnectionState.DISCONNECTED,
            deviceName = null
        )
        
        android.util.Log.d("CastManager", "Cast session ended")
    }
    
    private fun updateMediaStatus() {
        // Update cast state based on media status
        remoteMediaClient?.let { client ->
            val mediaStatus = client.mediaStatus
            // Update your cast state based on media status
            android.util.Log.d("CastManager", "Media status updated: ${mediaStatus?.playerState}")
        }
    }
    
    private fun createMediaInfo(song: Song): MediaInfo {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, song.title)
            putString(MediaMetadata.KEY_ARTIST, song.displayArtist)
            putString(MediaMetadata.KEY_ALBUM_TITLE, song.displayAlbum)
            // Add album art URL if available
        }
        
        return MediaInfo.Builder(song.path)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("audio/mpeg")
            .setMetadata(metadata)
            .setStreamDuration(song.duration)
            .build()
    }
    
    fun release() {
        try {
            remoteMediaClient?.unregisterCallback(remoteMediaClientCallback)
            sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
            remoteMediaClient = null
            sessionManager = null
            castContext = null
        } catch (exception: Exception) {
            android.util.Log.e("CastManager", "Error releasing cast manager", exception)
        }
    }
}
