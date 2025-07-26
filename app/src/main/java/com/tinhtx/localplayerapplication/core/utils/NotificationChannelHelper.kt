package com.tinhtx.localplayerapplication.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.getSystemService
import com.tinhtx.localplayerapplication.core.constants.NotificationConstants

object NotificationChannelHelper {
    
    /**
     * Create all notification channels
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService<NotificationManager>()
            
            // Music playback channel
            val musicChannel = NotificationChannel(
                NotificationConstants.MUSIC_CHANNEL_ID,
                NotificationConstants.MUSIC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = NotificationConstants.MUSIC_CHANNEL_DESCRIPTION
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            // Sleep timer channel
            val sleepTimerChannel = NotificationChannel(
                NotificationConstants.SLEEP_TIMER_CHANNEL_ID,
                NotificationConstants.SLEEP_TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NotificationConstants.SLEEP_TIMER_CHANNEL_DESCRIPTION
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lightColor = android.graphics.Color.BLUE
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Download channel
            val downloadChannel = NotificationChannel(
                NotificationConstants.DOWNLOAD_CHANNEL_ID,
                NotificationConstants.DOWNLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = NotificationConstants.DOWNLOAD_CHANNEL_DESCRIPTION
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            notificationManager?.createNotificationChannels(
                listOf(musicChannel, sleepTimerChannel, downloadChannel)
            )
        }
    }
    
    /**
     * Check if notification channel exists
     */
    fun isChannelCreated(context: Context, channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService<NotificationManager>()
            notificationManager?.getNotificationChannel(channelId) != null
        } else {
            true
        }
    }
    
    /**
     * Delete notification channel
     */
    fun deleteChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService<NotificationManager>()
            notificationManager?.deleteNotificationChannel(channelId)
        }
    }
    
    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService<NotificationManager>()
        return notificationManager?.areNotificationsEnabled() ?: false
    }
    
    /**
     * Check if specific channel is enabled
     */
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService<NotificationManager>()
            val channel = notificationManager?.getNotificationChannel(channelId)
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            areNotificationsEnabled(context)
        }
    }
}
