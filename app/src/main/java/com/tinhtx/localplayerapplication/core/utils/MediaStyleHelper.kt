package com.tinhtx.localplayerapplication.core.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat // Correct import
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.tinhtx.localplayerapplication.core.constants.NotificationConstants
import com.tinhtx.localplayerapplication.presentation.MainActivity

object MediaStyleHelper {
    
    /**
     * Create media style notification
     */
    @OptIn(UnstableApi::class)
    fun createMediaNotification(
        context: Context,
        mediaSession: MediaSession,
        player: Player,
        albumArt: Bitmap?
    ): Notification {
        val mediaMetadata = player.mediaMetadata
        
        val playPauseAction = if (player.isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                createPendingIntent(context, NotificationConstants.ACTION_PLAY_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Play",
                createPendingIntent(context, NotificationConstants.ACTION_PLAY_PAUSE)
            ).build()
        }
        
        val previousAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            createPendingIntent(context, NotificationConstants.ACTION_PREVIOUS)
        ).build()
        
        val nextAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            createPendingIntent(context, NotificationConstants.ACTION_NEXT)
        ).build()
        
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createPendingIntent(context, NotificationConstants.ACTION_STOP)
        ).build()
        
        val contentIntent = PendingIntent.getActivity(
            context,
            NotificationConstants.REQUEST_CODE_OPEN_PLAYER,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            getPendingIntentFlags()
        )
        
        return NotificationCompat.Builder(context, NotificationConstants.MUSIC_CHANNEL_ID)
            .setContentTitle(mediaMetadata.title ?: "Unknown")
            .setContentText(mediaMetadata.artist ?: "Unknown Artist")
            .setSubText(mediaMetadata.albumTitle ?: "Unknown Album")
            .setLargeIcon(albumArt)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .setDeleteIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(*NotificationConstants.SHOW_ACTIONS_IN_COMPACT_VIEW)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }
    
    /**
     * Create sleep timer notification
     */
    fun createSleepTimerNotification(
        context: Context,
        timeRemaining: String
    ): Notification {
        val cancelIntent = PendingIntent.getService(
            context,
            NotificationConstants.REQUEST_CODE_CANCEL_SLEEP_TIMER,
            Intent().apply {
                action = NotificationConstants.ACTION_CANCEL_SLEEP_TIMER
            },
            getPendingIntentFlags()
        )
        
        return NotificationCompat.Builder(context, NotificationConstants.SLEEP_TIMER_CHANNEL_ID)
            .setContentTitle("Sleep Timer")
            .setContentText("Music will stop in $timeRemaining")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setShowWhen(false)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Cancel",
                    cancelIntent
                ).build()
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    
    /**
     * Create pending intent for notification actions
     */
    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
        }
        
        val requestCode = when (action) {
            NotificationConstants.ACTION_PLAY_PAUSE -> NotificationConstants.REQUEST_CODE_PLAY_PAUSE
            NotificationConstants.ACTION_PREVIOUS -> NotificationConstants.REQUEST_CODE_PREVIOUS
            NotificationConstants.ACTION_NEXT -> NotificationConstants.REQUEST_CODE_NEXT
            NotificationConstants.ACTION_STOP -> NotificationConstants.REQUEST_CODE_STOP
            NotificationConstants.ACTION_CANCEL_SLEEP_TIMER -> NotificationConstants.REQUEST_CODE_CANCEL_SLEEP_TIMER
            else -> 0
        }
        
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            getPendingIntentFlags()
        )
    }
    
    /**
     * Get appropriate PendingIntent flags based on Android version
     */
    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
    
    /**
     * Extract notification info from MediaMetadata
     */
    data class NotificationInfo(
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val artworkBitmap: Bitmap?
    )

    @OptIn(UnstableApi::class)
    fun extractNotificationInfo(mediaMetadata: MediaMetadata, artworkBitmap: Bitmap?): NotificationInfo {
        return NotificationInfo(
            title = mediaMetadata.title?.toString() ?: "Unknown",
            artist = mediaMetadata.artist?.toString() ?: "Unknown Artist",
            album = mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
            duration = mediaMetadata.durationMs ?: 0L,
            artworkBitmap = artworkBitmap
        )
    }
}
