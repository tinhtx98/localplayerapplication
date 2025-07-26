// TODO: Implement music service
package com.tinhtx.localplayerapplication.presentation.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.usecase.player.*
import com.tinhtx.localplayerapplication.presentation.service.components.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : LifecycleService() {

    @Inject
    lateinit var musicPlayer: MusicPlayer
    
    @Inject
    lateinit var getPlayerStateUseCase: GetPlayerStateUseCase
    
    @Inject
    lateinit var playPauseUseCase: PlayPauseUseCase
    
    @Inject
    lateinit var skipToNextUseCase: SkipToNextUseCase
    
    @Inject
    lateinit var skipToPreviousUseCase: SkipToPreviousUseCase
    
    @Inject
    lateinit var seekToUseCase: SeekToUseCase

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var notificationManager: MusicNotificationManager
    private lateinit var audioFocusManager: AudioFocusManager
    private lateinit var serviceBinder: ServiceBinder

    private var currentSong: Song? = null
    private var isPlaying = false
    private var playbackPosition = 0L

    companion object {
        const val SERVICE_ID = 1001
        const val CHANNEL_ID = "music_playback_channel"
        const val CHANNEL_NAME = "Music Playback"
        
        // Actions
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_SKIP_TO_NEXT = "action_skip_to_next"
        const val ACTION_SKIP_TO_PREVIOUS = "action_skip_to_previous"
        const val ACTION_STOP = "action_stop"
        const val ACTION_SEEK_TO = "action_seek_to"
        
        // Extras
        const val EXTRA_SEEK_POSITION = "seek_position"
    }

    override fun onCreate() {
        super.onCreate()
        
        initializeService()
        observePlayerState()
    }

    private fun initializeService() {
        // Initialize components
        serviceBinder = ServiceBinder(this)
        audioFocusManager = AudioFocusManager(this) { gainedFocus ->
            handleAudioFocusChange(gainedFocus)
        }
        
        mediaSessionManager = MediaSessionManager(this) { action ->
            handleMediaSessionAction(action)
        }
        
        notificationManager = MusicNotificationManager(
            context = this,
            mediaSession = mediaSessionManager.mediaSession
        ) { action ->
            handleNotificationAction(action)
        }
        
        createNotificationChannel()
    }

    private fun observePlayerState() {
        lifecycleScope.launch {
            getPlayerStateUseCase().collect { playerState ->
                updatePlayerState(playerState)
            }
        }
    }

    private fun updatePlayerState(playerState: PlayerState) {
        val wasPlaying = isPlaying
        val previousSong = currentSong
        
        currentSong = playerState.currentSong
        isPlaying = playerState.isPlaying
        playbackPosition = playerState.currentPosition
        
        // Update media session
        mediaSessionManager.updateMetadata(playerState.currentSong)
        mediaSessionManager.updatePlaybackState(
            state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            position = playbackPosition,
            playbackSpeed = if (isPlaying) 1.0f else 0.0f
        )
        
        // Handle foreground service state
        when {
            isPlaying && !wasPlaying -> startForegroundService()
            !isPlaying && wasPlaying -> handlePauseService()
            currentSong != previousSong && currentSong != null -> updateNotification()
        }
        
        // Update notification if service is running
        if (isPlaying || wasPlaying) {
            updateNotification()
        }
    }

    private fun startForegroundService() {
        val notification = notificationManager.createNotification(
            song = currentSong,
            isPlaying = isPlaying,
            playbackPosition = playbackPosition
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                SERVICE_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(SERVICE_ID, notification)
        }
    }

    private fun handlePauseService() {
        // Keep service running but update notification
        updateNotification()
        
        // Stop foreground after delay if still paused
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isPlaying) {
                stopForeground(false)
            }
        }, 30000) // 30 seconds delay
    }

    private fun updateNotification() {
        val notification = notificationManager.createNotification(
            song = currentSong,
            isPlaying = isPlaying,
            playbackPosition = playbackPosition
        )
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SERVICE_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        intent?.action?.let { action ->
            handleServiceAction(action, intent)
        }
        
        return START_STICKY
    }

    private fun handleServiceAction(action: String, intent: Intent) {
        lifecycleScope.launch {
            try {
                when (action) {
                    ACTION_PLAY_PAUSE -> playPauseUseCase()
                    ACTION_SKIP_TO_NEXT -> skipToNextUseCase()
                    ACTION_SKIP_TO_PREVIOUS -> skipToPreviousUseCase()
                    ACTION_SEEK_TO -> {
                        val position = intent.getLongExtra(EXTRA_SEEK_POSITION, 0L)
                        seekToUseCase(position)
                    }
                    ACTION_STOP -> stopPlayback()
                }
            } catch (exception: Exception) {
                android.util.Log.e("MusicService", "Error handling action: $action", exception)
            }
        }
    }

    private fun handleMediaSessionAction(action: MediaSessionAction) {
        lifecycleScope.launch {
            try {
                when (action) {
                    is MediaSessionAction.Play -> playPauseUseCase()
                    is MediaSessionAction.Pause -> playPauseUseCase()
                    is MediaSessionAction.SkipToNext -> skipToNextUseCase()
                    is MediaSessionAction.SkipToPrevious -> skipToPreviousUseCase()
                    is MediaSessionAction.SeekTo -> seekToUseCase(action.position)
                    is MediaSessionAction.Stop -> stopPlayback()
                }
            } catch (exception: Exception) {
                android.util.Log.e("MusicService", "Error handling media session action", exception)
            }
        }
    }

    private fun handleNotificationAction(action: NotificationAction) {
        lifecycleScope.launch {
            try {
                when (action) {
                    is NotificationAction.PlayPause -> playPauseUseCase()
                    is NotificationAction.SkipToNext -> skipToNextUseCase()
                    is NotificationAction.SkipToPrevious -> skipToPreviousUseCase()
                    is NotificationAction.Stop -> stopPlayback()
                }
            } catch (exception: Exception) {
                android.util.Log.e("MusicService", "Error handling notification action", exception)
            }
        }
    }

    private fun handleAudioFocusChange(gainedFocus: Boolean) {
        lifecycleScope.launch {
            try {
                if (gainedFocus) {
                    // Resume playback if we had focus before
                    if (!isPlaying && currentSong != null) {
                        playPauseUseCase()
                    }
                } else {
                    // Pause playback when losing focus
                    if (isPlaying) {
                        playPauseUseCase()
                    }
                }
            } catch (exception: Exception) {
                android.util.Log.e("MusicService", "Error handling audio focus change", exception)
            }
        }
    }

    private suspend fun stopPlayback() {
        try {
            musicPlayer.stop()
            stopForeground(true)
            stopSelf()
        } catch (exception: Exception) {
            android.util.Log.e("MusicService", "Error stopping playback", exception)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return serviceBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up resources
        mediaSessionManager.release()
        audioFocusManager.release()
        notificationManager.release()
        musicPlayer.release()
        
        android.util.Log.d("MusicService", "Service destroyed")
    }

    inner class ServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
