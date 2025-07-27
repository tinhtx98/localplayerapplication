package com.tinhtx.localplayerapplication.core.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.tinhtx.localplayerapplication.R
import com.tinhtx.localplayerapplication.core.constants.NotificationConstants
import com.tinhtx.localplayerapplication.presentation.MainActivity
import com.tinhtx.localplayerapplication.presentation.service.MusicService

/**
 * Helper class for creating MediaStyle notifications
 */
object MediaStyleHelper {
    
    /**
     * Create MediaStyle notification builder
     */
    fun createMediaStyleNotification(
        context: Context,
        title: String,
        artist: String,
        album: String,
        isPlaying: Boolean,
        mediaSession: MediaSessionCompat,
        channelId: String
    ): NotificationCompat.Builder {
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album)
            .setContentIntent(createContentIntent(context))
            .setDeleteIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(createAction(context, R.drawable.ic_skip_previous, "Previous", NotificationConstants.ACTION_SKIP_PREVIOUS))
            .addAction(createPlayPauseAction(context, isPlaying))
            .addAction(createAction(context, R.drawable.ic_skip_next, "Next", NotificationConstants.ACTION_SKIP_NEXT))
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(*NotificationConstants.SHOW_ACTIONS_IN_COMPACT_VIEW)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            )
            .setColor(context.getColor(R.color.primary))
    }
    
    /**
     * Create play/pause action based on current state
     */
    private fun createPlayPauseAction(context: Context, isPlaying: Boolean): NotificationCompat.Action {
        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val title = if (isPlaying) "Pause" else "Play"
        val action = NotificationConstants.ACTION_PLAY_PAUSE
        
        return createAction(context, icon, title, action)
    }
    
    /**
     * Create notification action
     */
    private fun createAction(
        context: Context,
        icon: Int,
        title: String,
        action: String
    ): NotificationCompat.Action {
        val pendingIntent = createPendingIntent(context, action)
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }
    
    /**
     * Create PendingIntent for service actions
     */
    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        
        val requestCode = getRequestCode(action)
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Create PendingIntent for opening the app
     */
    private fun createContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            NotificationConstants.REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Get unique request code for each action
     */
    private fun getRequestCode(action: String): Int {
        return when (action) {
            NotificationConstants.ACTION_PLAY_PAUSE -> NotificationConstants.REQUEST_CODE_PLAY_PAUSE
            NotificationConstants.ACTION_SKIP_PREVIOUS -> NotificationConstants.REQUEST_CODE_SKIP_PREVIOUS
            NotificationConstants.ACTION_SKIP_NEXT -> NotificationConstants.REQUEST_CODE_SKIP_NEXT
            NotificationConstants.ACTION_STOP -> NotificationConstants.REQUEST_CODE_STOP
            else -> 0
        }
    }
    
    /**
     * Create expanded MediaStyle notification with more actions
     */
    fun createExpandedMediaStyleNotification(
        context: Context,
        title: String,
        artist: String,
        album: String,
        isPlaying: Boolean,
        isShuffleOn: Boolean,
        repeatMode: Int,
        mediaSession: MediaSessionCompat,
        channelId: String
    ): NotificationCompat.Builder {
        
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album)
            .setContentIntent(createContentIntent(context))
            .setDeleteIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(createShuffleAction(context, isShuffleOn))
            .addAction(createAction(context, R.drawable.ic_skip_previous, "Previous", NotificationConstants.ACTION_SKIP_PREVIOUS))
            .addAction(createPlayPauseAction(context, isPlaying))
            .addAction(createAction(context, R.drawable.ic_skip_next, "Next", NotificationConstants.ACTION_SKIP_NEXT))
            .addAction(createRepeatAction(context, repeatMode))
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3) // Previous, Play/Pause, Next
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(context, NotificationConstants.ACTION_STOP))
            )
            .setColor(context.getColor(R.color.primary))
    }
    
    /**
     * Create shuffle action
     */
    private fun createShuffleAction(context: Context, isShuffleOn: Boolean): NotificationCompat.Action {
        val icon = if (isShuffleOn) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
        val title = if (isShuffleOn) "Shuffle Off" else "Shuffle On"
        return createAction(context, icon, title, "action_shuffle")
    }
    
    /**
     * Create repeat action
     */
    private fun createRepeatAction(context: Context, repeatMode: Int): NotificationCompat.Action {
        val (icon, title) = when (repeatMode) {
            1 -> Pair(R.drawable.ic_repeat_one, "Repeat One")
            2 -> Pair(R.drawable.ic_repeat_on, "Repeat Off")
            else -> Pair(R.drawable.ic_repeat, "Repeat All")
        }
        return createAction(context, icon, title, "action_repeat")
    }
}
