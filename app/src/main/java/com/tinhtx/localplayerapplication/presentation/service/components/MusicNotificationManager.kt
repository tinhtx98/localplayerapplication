package com.tinhtx.localplayerapplication.data.service.components

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.data.service.PlayerState
import com.tinhtx.localplayerapplication.data.service.RepeatMode
import com.tinhtx.localplayerapplication.presentation.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicNotificationManager @Inject constructor(
    private val context: Context,
    private val notificationHelper: com.tinhtx.localplayerapplication.data.service.notifications.NotificationHelper
) {
    
    private var currentNotification: android.app.Notification? = null
    private var isNotificationShowing = false
    
    companion object {
        private const val REQUEST_CODE_OPEN_APP = 100
        private const val REQUEST_CODE_PLAY_PAUSE = 101
        private const val REQUEST_CODE_SKIP_NEXT = 102
        private const val REQUEST_CODE_SKIP_PREVIOUS = 103
        private const val REQUEST_CODE_STOP = 104
        private const val REQUEST_CODE_REPEAT = 105
        private const val REQUEST_CODE_SHUFFLE = 106
    }
    
    /**
     * Create and show music notification
     */
    fun showNotification(
        playerState: PlayerState,
        mediaSession: MediaSessionCompat?,
        albumArt: Bitmap? = null
    ): android.app.Notification? {
        val song = playerState.currentSong ?: return null
        
        val notification = createNotification(
            song = song,
            isPlaying = playerState.isPlaying,
            repeatMode = playerState.repeatMode,
            shuffleMode = playerState.shuffleMode,
            mediaSession = mediaSession,
            albumArt = albumArt
        )
        
        currentNotification = notification
        
        try {
            NotificationManagerCompat.from(context).notify(
                ServiceUtils.NOTIFICATION_ID,
                notification
            )
            isNotificationShowing = true
            
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                song, 
                "Notification shown - Playing: ${playerState.isPlaying}"
            )
        } catch (e: SecurityException) {
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                song, 
                "Failed to show notification: ${e.message}"
            )
        }
        
        return notification
    }
    
    private fun createNotification(
        song: Song,
        isPlaying: Boolean,
        repeatMode: RepeatMode,
        shuffleMode: Boolean,
        mediaSession: MediaSessionCompat?,
        albumArt: Bitmap?
    ): android.app.Notification {
        
        val builder = NotificationCompat.Builder(context, ServiceUtils.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setLargeIcon(albumArt)
            .setContentIntent(createOpenAppIntent())
            .setDeleteIntent(createStopIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
        
        // Add media style
        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2) // Previous, Play/Pause, Next
            .setShowCancelButton(true)
            .setCancelButtonIntent(createStopIntent())
        
        mediaSession?.let { session ->
            mediaStyle.setMediaSession(session.sessionToken)
        }
        
        builder.setStyle(mediaStyle)
        
        // Add action buttons
        addNotificationActions(builder, isPlaying, repeatMode, shuffleMode)
        
        // Set color theme
        builder.setColor(androidx.core.content.ContextCompat.getColor(context, R.color.notification_color))
        
        // Add progress for buffering
        if (!isPlaying && song.duration > 0) {
            builder.setProgress(100, 0, false)
        }
        
        return builder.build()
    }
    
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        isPlaying: Boolean,
        repeatMode: RepeatMode,
        shuffleMode: Boolean
    ) {
        // Previous action
        builder.addAction(
            R.drawable.ic_skip_previous,
            "Previous",
            createSkipPreviousIntent()
        )
        
        // Play/Pause action
        val playPauseIcon = if (isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play_arrow
        }
        
        val playPauseText = if (isPlaying) "Pause" else "Play"
        
        builder.addAction(
            playPauseIcon,
            playPauseText,
            createPlayPauseIntent()
        )
        
        // Next action
        builder.addAction(
            R.drawable.ic_skip_next,
            "Next",
            createSkipNextIntent()
        )
        
        // Additional actions for expanded notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Repeat action
            val repeatIcon = when (repeatMode) {
                RepeatMode.OFF -> R.drawable.ic_repeat_off
                RepeatMode.ALL -> R.drawable.ic_repeat
                RepeatMode.ONE -> R.drawable.ic_repeat_one
            }
            
            builder.addAction(
                repeatIcon,
                "Repeat",
                createRepeatIntent()
            )
            
            // Shuffle action
            val shuffleIcon = if (shuffleMode) {
                R.drawable.ic_shuffle_on
            } else {
                R.drawable.ic_shuffle_off
            }
            
            builder.addAction(
                shuffleIcon,
                "Shuffle",
                createShuffleIntent()
            )
        }
    }
    
    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createPlayPauseIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_PLAY)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_PLAY_PAUSE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createSkipNextIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_SKIP_NEXT)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_SKIP_NEXT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createSkipPreviousIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_SKIP_PREVIOUS)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_SKIP_PREVIOUS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createStopIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_STOP)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_STOP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createRepeatIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_TOGGLE_REPEAT)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_REPEAT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createShuffleIntent(): PendingIntent {
        val intent = context.createServiceIntent(ServiceUtils.ACTION_TOGGLE_SHUFFLE)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_SHUFFLE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Update existing notification
     */
    fun updateNotification(
        playerState: PlayerState,
        mediaSession: MediaSessionCompat?,
        albumArt: Bitmap? = null
    ) {
        if (isNotificationShowing) {
            showNotification(playerState, mediaSession, albumArt)
        }
    }
    
    /**
     * Hide notification
     */
    fun hideNotification() {
        try {
            NotificationManagerCompat.from(context).cancel(ServiceUtils.NOTIFICATION_ID)
            isNotificationShowing = false
            currentNotification = null
            
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                null, 
                "Notification hidden"
            )
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                null, 
                "Failed to hide notification: ${e.message}"
            )
        }
    }
    
    /**
     * Check if notification is showing
     */
    fun isNotificationVisible(): Boolean {
        return isNotificationShowing
    }
    
    /**
     * Get current notification
     */
    fun getCurrentNotification(): android.app.Notification? {
        return currentNotification
    }
    
    /**
     * Create notification for foreground service
     */
    fun createForegroundNotification(
        song: Song?,
        isPlaying: Boolean
    ): android.app.Notification {
        if (song == null) {
            return createEmptyNotification()
        }
        
        return createNotification(
            song = song,
            isPlaying = isPlaying,
            repeatMode = RepeatMode.OFF,
            shuffleMode = false,
            mediaSession = null,
            albumArt = null
        )
    }
    
    private fun createEmptyNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, ServiceUtils.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("LocalPlayer")
            .setContentText("Ready to play music")
            .setContentIntent(createOpenAppIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setShowWhen(false)
            .build()
    }
    
    /**
     * Update notification with loading state
     */
    fun showLoadingNotification(song: Song) {
        val builder = NotificationCompat.Builder(context, ServiceUtils.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText("Loading...")
            .setSubText(song.artist)
            .setContentIntent(createOpenAppIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setProgress(0, 0, true) // Indeterminate progress
        
        try {
            NotificationManagerCompat.from(context).notify(
                ServiceUtils.NOTIFICATION_ID,
                builder.build()
            )
            isNotificationShowing = true
        } catch (e: SecurityException) {
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                song, 
                "Failed to show loading notification: ${e.message}"
            )
        }
    }
    
    /**
     * Update notification with error state
     */
    fun showErrorNotification(song: Song, error: String) {
        val builder = NotificationCompat.Builder(context, ServiceUtils.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Playback Error")
            .setContentText(error)
            .setSubText(song.title)
            .setContentIntent(createOpenAppIntent())
            .setDeleteIntent(createStopIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)
            .setAutoCancel(true)
            .setShowWhen(true)
        
        try {
            NotificationManagerCompat.from(context).notify(
                ServiceUtils.NOTIFICATION_ID + 1, // Different ID for error
                builder.build()
            )
        } catch (e: SecurityException) {
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                song, 
                "Failed to show error notification: ${e.message}"
            )
        }
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        try {
            NotificationManagerCompat.from(context).cancelAll()
            isNotificationShowing = false
            currentNotification = null
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "Notification", 
                null, 
                "Failed to clear notifications: ${e.message}"
            )
        }
    }
}
