package com.tinhtx.localplayerapplication.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tinhtx.localplayerapplication.core.constants.NotificationConstants

/**
 * Helper class for managing notification channels
 */
object NotificationChannelHelper {

    /**
     * Create all notification channels
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            createMusicChannel(notificationManager)
            createTimerChannel(notificationManager)
            createDownloadChannel(notificationManager)
            createErrorChannel(notificationManager)
        }
    }

    /**
     * Create music playback notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMusicChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NotificationConstants.MUSIC_CHANNEL_ID,
            NotificationConstants.MUSIC_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = NotificationConstants.MUSIC_CHANNEL_DESC
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null) // No sound for music notifications
            enableVibration(false)
            enableLights(false)
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Create sleep timer notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTimerChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NotificationConstants.TIMER_CHANNEL_ID,
            NotificationConstants.TIMER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = NotificationConstants.TIMER_CHANNEL_DESC
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            
            // Set custom sound
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            setSound(soundUri, audioAttributes)
            
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
            enableLights(true)
            lightColor = android.graphics.Color.BLUE
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Create download notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDownloadChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NotificationConstants.DOWNLOAD_CHANNEL_ID,
            NotificationConstants.DOWNLOAD_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = NotificationConstants.DOWNLOAD_CHANNEL_DESC
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Create error notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createErrorChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NotificationConstants.ERROR_CHANNEL_ID,
            NotificationConstants.ERROR_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NotificationConstants.ERROR_CHANNEL_DESC
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            
            // Set default notification sound
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            setSound(soundUri, audioAttributes)
            
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            enableLights(true)
            lightColor = android.graphics.Color.RED
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Delete notification channel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteNotificationChannel(context: Context, channelId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(channelId)
    }

    /**
     * Check if notification channel exists
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun channelExists(context: Context, channelId: String): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.getNotificationChannel(channelId) != null
    }

    /**
     * Update notification channel importance
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateChannelImportance(context: Context, channelId: String, importance: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        channel?.let {
            val updatedChannel = NotificationChannel(channelId, it.name, importance).apply {
                description = it.description
                setShowBadge(it.canShowBadge())
                lockscreenVisibility = it.lockscreenVisibility
                setSound(it.sound, it.audioAttributes)
                enableVibration(it.shouldVibrate())
                vibrationPattern = it.vibrationPattern
                enableLights(it.shouldShowLights())
                lightColor = it.lightColor
            }
            notificationManager.createNotificationChannel(updatedChannel)
        }
    }

    /**
     * Get all notification channels
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllChannels(context: Context): List<NotificationChannel> {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.notificationChannels
    }

    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    /**
     * Check if specific channel is enabled
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    /**
     * Get channel importance
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getChannelImportance(context: Context, channelId: String): Int {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        return channel?.importance ?: NotificationManager.IMPORTANCE_NONE
    }
}
