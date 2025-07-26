package com.tinhtx.localplayerapplication.presentation.service.timer

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.tinhtx.localplayerapplication.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SleepTimerService : LifecycleService() {
    
    @Inject
    lateinit var sleepTimerManager: SleepTimerManager
    
    companion object {
        const val SERVICE_ID = 1002
        const val CHANNEL_ID = "sleep_timer_channel"
        const val CHANNEL_NAME = "Sleep Timer"
        
        const val ACTION_START_TIMER = "action_start_timer"
        const val ACTION_STOP_TIMER = "action_stop_timer"
        const val ACTION_ADD_TIME = "action_add_time"
        
        const val EXTRA_DURATION_MS = "duration_ms"
        const val EXTRA_FADE_OUT = "fade_out"
        const val EXTRA_ADDITIONAL_MINUTES = "additional_minutes"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)
                val fadeOut = intent.getBooleanExtra(EXTRA_FADE_OUT, true)
                startForegroundService(durationMs)
            }
            ACTION_STOP_TIMER -> {
                stopSelf()
            }
            ACTION_ADD_TIME -> {
                val additionalMinutes = intent.getIntExtra(EXTRA_ADDITIONAL_MINUTES, 15)
                sleepTimerManager.addTime(additionalMinutes)
                updateNotification()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun startForegroundService(durationMs: Long) {
        val notification = createNotification(durationMs)
        startForeground(SERVICE_ID, notification)
    }
    
    private fun createNotification(durationMs: Long): Notification {
        val minutes = durationMs / (1000 * 60)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bedtime)
            .setContentTitle("Sleep Timer Active")
            .setContentText("Music will stop in ${minutes} minutes")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(createAddTimeAction())
            .addAction(createStopTimerAction())
            .build()
    }
    
    private fun updateNotification() {
        val timerState = sleepTimerManager.timerState.value
        if (timerState.isActive) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bedtime)
                .setContentTitle("Sleep Timer Active")
                .setContentText("Time remaining: ${sleepTimerManager.getFormattedRemainingTime()}")
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(createAddTimeAction())
                .addAction(createStopTimerAction())
                .build()
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(SERVICE_ID, notification)
        }
    }
    
    private fun createAddTimeAction(): NotificationCompat.Action {
        val intent = Intent(this, SleepTimerService::class.java).apply {
            action = ACTION_ADD_TIME
            putExtra(EXTRA_ADDITIONAL_MINUTES, 15)
        }
        
        val pendingIntent = PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_add,
            "+15 min",
            pendingIntent
        ).build()
    }
    
    private fun createStopTimerAction(): NotificationCompat.Action {
        val intent = Intent(this, SleepTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        
        val pendingIntent = PendingIntent.getService(
            this,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_stop,
            "Stop",
            pendingIntent
        ).build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sleep timer notifications"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("SleepTimerService", "Service destroyed")
    }
}
