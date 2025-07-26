package com.tinhtx.localplayerapplication.presentation.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.tinhtx.localplayerapplication.presentation.service.MusicService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Music playback channel
            val musicChannel = NotificationChannel(
                MusicService.CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls and information"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            
            // Sleep timer channel
            val sleepTimerChannel = NotificationChannel(
                "sleep_timer_channel",
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sleep timer notifications"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            // Download channel
            val downloadChannel = NotificationChannel(
                "download_channel",
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music and artwork download progress"
                setShowBadge(true)
            }
            
            // Error channel
            val errorChannel = NotificationChannel(
                "error_channel",
                "Errors",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Error notifications"
                setShowBadge(true)
            }
            
            systemNotificationManager.createNotificationChannels(
                listOf(musicChannel, sleepTimerChannel, downloadChannel, errorChannel)
            )
        }
    }
    
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    fun isChannelEnabled(channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(channelId)
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            areNotificationsEnabled()
        }
    }
    
    fun getChannelImportance(channelId: String): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(channelId)
            channel?.importance ?: NotificationManager.IMPORTANCE_NONE
        } else {
            if (areNotificationsEnabled()) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_NONE
        }
    }
    
    fun deleteNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.deleteNotificationChannel(channelId)
        }
    }
}
