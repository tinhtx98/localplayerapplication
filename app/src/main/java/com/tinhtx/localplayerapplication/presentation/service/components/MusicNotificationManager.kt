package com.tinhtx.localplayerapplication.presentation.service.components

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.MainActivity
import com.tinhtx.localplayerapplication.presentation.service.MusicService

class MusicNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSessionCompat,
    private val onAction: (NotificationAction) -> Unit
) {
    
    fun createNotification(
        song: Song?,
        isPlaying: Boolean,
        playbackPosition: Long
    ): Notification {
        
        return NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song?.title ?: "No song playing")
            .setContentText(song?.displayArtist ?: "Unknown Artist")
            .setSubText(song?.displayAlbum ?: "Unknown Album")
            .setLargeIcon(getAlbumArt(song))
            .setContentIntent(createContentIntent())
            .setDeleteIntent(createDeleteIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(createPreviousAction())
            .addAction(createPlayPauseAction(isPlaying))
            .addAction(createNextAction())
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createDeleteIntent())
            )
            .setColor(context.getColor(R.color.primary))
            .build()
    }
    
    private fun getAlbumArt(song: Song?): Bitmap? {
        return try {
            song?.let { MediaUtils.getAlbumArt(context, it.albumId) }
        } catch (exception: Exception) {
            null
        }
    }
    
    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDeleteIntent(): PendingIntent {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        
        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createPlayPauseAction(isPlaying: Boolean): NotificationCompat.Action {
        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val title = if (isPlaying) "Pause" else "Play"
        
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY_PAUSE
        }
        
        val pendingIntent = PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }
    
    private fun createPreviousAction(): NotificationCompat.Action {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_TO_PREVIOUS
        }
        
        val pendingIntent = PendingIntent.getService(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_skip_previous,
            "Previous",
            pendingIntent
        ).build()
    }
    
    private fun createNextAction(): NotificationCompat.Action {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_TO_NEXT
        }
        
        val pendingIntent = PendingIntent.getService(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_skip_next,
            "Next",
            pendingIntent
        ).build()
    }
    
    fun release() {
        // Clean up resources if needed
    }
}

sealed class NotificationAction {
    object PlayPause : NotificationAction()
    object SkipToNext : NotificationAction()
    object SkipToPrevious : NotificationAction()
    object Stop : NotificationAction()
}
