package com.tinhtx.localplayerapplication.presentation.service.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.MainActivity
import com.tinhtx.localplayerapplication.presentation.service.MusicService

// TODO: Implement media style notification class
class MediaStyleNotification(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
) {
    // Implementation will be added later
    fun create(
        song: Song?,
        isPlaying: Boolean,
        albumArt: Bitmap? = null,
        showPlaybackPosition: Boolean = true
    ): Notification {

        val builder = NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song?.title ?: "No song playing")
            .setContentText(song?.displayArtist ?: "Unknown Artist")
            .setSubText(song?.displayAlbum ?: "Unknown Album")
            .setLargeIcon(albumArt)
            .setContentIntent(createContentIntent())
            .setDeleteIntent(createDeleteIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setColor(context.getColor(R.color.primary))

        // Add playback actions
        addActions(builder, isPlaying)

        // Configure media style
        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(createDeleteIntent())

        if (showPlaybackPosition && song != null) {
            mediaStyle.setShowActionsInCompactView(0, 1, 2)
        }

        builder.setStyle(mediaStyle)

        return builder.build()
    }

    private fun addActions(builder: NotificationCompat.Builder, isPlaying: Boolean) {
        // Previous action
        builder.addAction(
            R.drawable.ic_skip_previous,
            "Previous",
            createActionIntent(MusicService.ACTION_SKIP_TO_PREVIOUS)
        )

        // Play/Pause action
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val playPauseText = if (isPlaying) "Pause" else "Play"
        builder.addAction(
            playPauseIcon,
            playPauseText,
            createActionIntent(MusicService.ACTION_PLAY_PAUSE)
        )

        // Next action
        builder.addAction(
            R.drawable.ic_skip_next,
            "Next",
            createActionIntent(MusicService.ACTION_SKIP_TO_NEXT)
        )

        // Stop action (shown in expanded view)
        builder.addAction(
            R.drawable.ic_stop,
            "Stop",
            createActionIntent(MusicService.ACTION_STOP)
        )
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_player", true)
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDeleteIntent(): PendingIntent {
        return createActionIntent(MusicService.ACTION_STOP)
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }

        val requestCode = when (action) {
            MusicService.ACTION_PLAY_PAUSE -> 1
            MusicService.ACTION_SKIP_TO_PREVIOUS -> 2
            MusicService.ACTION_SKIP_TO_NEXT -> 3
            MusicService.ACTION_STOP -> 4
            else -> 0
        }

        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
