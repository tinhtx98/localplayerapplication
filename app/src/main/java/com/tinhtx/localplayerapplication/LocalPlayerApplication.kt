package com.tinhtx.localplayerapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.receivers.AudioNoisyReceiver
import com.tinhtx.localplayerapplication.core.receivers.MediaButtonReceiver
import com.tinhtx.localplayerapplication.core.utils.AppPreferences
import com.tinhtx.localplayerapplication.core.utils.CrashReportingTree
import com.tinhtx.localplayerapplication.core.utils.DebugTree
import com.tinhtx.localplayerapplication.data.database.MusicDatabase
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import com.tinhtx.localplayerapplication.presentation.service.MusicService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class LocalPlayerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var musicDatabase: MusicDatabase
    
    @Inject
    lateinit var getAppSettingsUseCase: GetAppSettingsUseCase

    // Application scope for coroutines
    private val applicationScope = MainScope()
    
    // Broadcast receivers
    private var audioNoisyReceiver: AudioNoisyReceiver? = null
    private var mediaButtonReceiver: MediaButtonReceiver? = null

    companion object {
        const val TAG = "LocalPlayerApplication"
        
        @Volatile
        private var INSTANCE: LocalPlayerApplication? = null
        
        fun getInstance(): LocalPlayerApplication {
            return INSTANCE ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val initTime = measureTimeMillis {
            INSTANCE = this
            
            // Initialize logging first
            initializeLogging()
            
            Timber.d("$TAG - Application starting...")
            
            // Initialize core components
            initializeStrictMode()
            initializeDynamicColors()
            initializeNotificationChannels()
            initializeBroadcastReceivers()
            initializeDatabase()
            initializeWorkManager()
            initializeAppSettings()
            
            // Register lifecycle callbacks
            registerActivityLifecycleCallbacks(AppLifecycleCallbacks())
            
            Timber.i("$TAG - Application initialized successfully")
        }
        
        Timber.i("$TAG - Initialization completed in ${initTime}ms")
    }

    override fun onTerminate() {
        super.onTerminate()
        
        Timber.d("$TAG - Application terminating...")
        
        // Clean up resources
        unregisterBroadcastReceivers()
        applicationScope.cancel()
        
        Timber.d("$TAG - Application terminated")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.w("$TAG - Low memory warning received")
        
        // Clear caches to free up memory
        applicationScope.launch {
            try {
                // Clear image cache
                // Clear audio cache
                // Clear temporary files
                clearTemporaryData()
                Timber.i("$TAG - Temporary data cleared due to low memory")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error clearing temporary data")
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        Timber.d("$TAG - Memory trim requested: level $level")
        
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                // App UI is hidden, trim UI-related caches
                trimUiCaches()
            }
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // App is running but system is under memory pressure
                trimRuntimeCaches()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // App is in background, aggressive trimming
                trimBackgroundCaches()
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.d("$TAG - Debug logging enabled")
        } else {
            Timber.plant(CrashReportingTree())
            Timber.d("$TAG - Release logging configured")
        }
    }

    private fun initializeStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog()
                    .build()
            )
            
            Timber.d("$TAG - StrictMode enabled for debugging")
        }
    }

    private fun initializeDynamicColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                DynamicColors.applyToActivitiesIfAvailable(this)
                Timber.d("$TAG - Dynamic colors applied")
            } catch (exception: Exception) {
                Timber.w(exception, "$TAG - Failed to apply dynamic colors")
            }
        }
    }

    private fun initializeNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Music playback channel
            val musicChannel = NotificationChannel(
                AppConstants.Notifications.MUSIC_CHANNEL_ID,
                getString(R.string.notification_channel_music),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_music_description)
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            
            // Sleep timer channel
            val sleepTimerChannel = NotificationChannel(
                AppConstants.Notifications.SLEEP_TIMER_CHANNEL_ID,
                getString(R.string.notification_channel_sleep_timer),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_sleep_timer_description)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            // Download channel
            val downloadChannel = NotificationChannel(
                AppConstants.Notifications.DOWNLOAD_CHANNEL_ID,
                getString(R.string.notification_channel_download),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_download_description)
                setShowBadge(true)
            }
            
            // Error channel
            val errorChannel = NotificationChannel(
                AppConstants.Notifications.ERROR_CHANNEL_ID,
                getString(R.string.notification_channel_error),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_error_description)
                setShowBadge(true)
            }
            
            // Scan channel
            val scanChannel = NotificationChannel(
                AppConstants.Notifications.SCAN_CHANNEL_ID,
                getString(R.string.notification_channel_scan),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_scan_description)
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(musicChannel, sleepTimerChannel, downloadChannel, errorChannel, scanChannel)
            )
            
            Timber.d("$TAG - Notification channels created")
        }
    }

    private fun initializeBroadcastReceivers() {
        try {
            // Audio becoming noisy receiver (headphones unplugged)
            audioNoisyReceiver = AudioNoisyReceiver()
            val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            ContextCompat.registerReceiver(
                this,
                audioNoisyReceiver,
                noisyFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            
            // Media button receiver
            mediaButtonReceiver = MediaButtonReceiver()
            val mediaButtonFilter = IntentFilter(Intent.ACTION_MEDIA_BUTTON)
            ContextCompat.registerReceiver(
                this,
                mediaButtonReceiver,
                mediaButtonFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            
            Timber.d("$TAG - Broadcast receivers registered")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error registering broadcast receivers")
        }
    }

    private fun unregisterBroadcastReceivers() {
        try {
            audioNoisyReceiver?.let { unregisterReceiver(it) }
            mediaButtonReceiver?.let { unregisterReceiver(it) }
            Timber.d("$TAG - Broadcast receivers unregistered")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error unregistering broadcast receivers")
        }
    }

    private fun initializeDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                val dbInitTime = measureTimeMillis {
                    // Initialize database connection
                    musicDatabase.songDao().getSongCount()
                }
                Timber.d("$TAG - Database initialized in ${dbInitTime}ms")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Database initialization failed")
            }
        }
    }

    private fun initializeWorkManager() {
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            
            // Schedule periodic tasks
            schedulePeriodicTasks()
            
            Timber.d("$TAG - WorkManager initialized")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - WorkManager initialization failed")
        }
    }

    private fun initializeAppSettings() {
        applicationScope.launch {
            try {
                // Load and apply app settings
                val settings = getAppSettingsUseCase()
                applyAppSettings(settings)
                Timber.d("$TAG - App settings applied")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error loading app settings")
            }
        }
    }

    private fun schedulePeriodicTasks() {
        applicationScope.launch {
            try {
                // Schedule library scan work
                LibraryScanWorker.scheduleWork(this@LocalPlayerApplication)
                
                // Schedule cache cleanup work
                CacheCleanupWorker.scheduleWork(this@LocalPlayerApplication)
                
                // Schedule analytics work
                AnalyticsWorker.scheduleWork(this@LocalPlayerApplication)
                
                Timber.d("$TAG - Periodic tasks scheduled")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error scheduling periodic tasks")
            }
        }
    }

    private fun applyAppSettings(settings: AppSettings) {
        try {
            // Apply theme settings
            // Apply audio settings
            // Apply notification settings
            Timber.d("$TAG - Settings applied: theme=${settings.appearanceSettings.theme}")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error applying settings")
        }
    }

    private fun clearTemporaryData() {
        try {
            // Clear cache directories
            cacheDir.deleteRecursively()
            
            // Clear temporary files
            val tempDir = File(filesDir, "temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            
            Timber.d("$TAG - Temporary data cleared")
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing temporary data")
        }
    }

    private fun trimUiCaches() {
        applicationScope.launch {
            try {
                // Clear UI-related caches
                // Image cache
                // View cache
                Timber.d("$TAG - UI caches trimmed")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error trimming UI caches")
            }
        }
    }

    private fun trimRuntimeCaches() {
        applicationScope.launch {
            try {
                // Clear runtime caches
                // Audio buffer cache
                // Metadata cache
                Timber.d("$TAG - Runtime caches trimmed")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error trimming runtime caches")
            }
        }
    }

    private fun trimBackgroundCaches() {
        applicationScope.launch {
            try {
                // Aggressive cache clearing
                clearTemporaryData()
                trimRuntimeCaches()
                trimUiCaches()
                
                // Stop non-essential services
                val musicServiceIntent = Intent(this@LocalPlayerApplication, MusicService::class.java)
                stopService(musicServiceIntent)
                
                Timber.d("$TAG - Background caches trimmed aggressively")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error trimming background caches")
            }
        }
    }

    private fun schedulePeriodicTasks() {
        applicationScope.launch {
            try {
                // Schedule library scan work
                LibraryScanWorker.schedulePeriodicWork(this@LocalPlayerApplication)
                
                // Schedule cache cleanup work
                CacheCleanupWorker.schedulePeriodicWork(this@LocalPlayerApplication)
                
                // Schedule analytics work
                AnalyticsWorker.schedulePeriodicWork(this@LocalPlayerApplication)
                
                Timber.d("$TAG - Periodic tasks scheduled")
            } catch (exception: Exception) {
                Timber.e(exception, "$TAG - Error scheduling periodic tasks")
            }
        }
    }

    /**
     * Get application scope for coroutines
     */
    fun getApplicationScope(): CoroutineScope = applicationScope

    /**
     * Check if app is in foreground
     */
    fun isInForeground(): Boolean {
        return AppLifecycleCallbacks.isInForeground
    }

    /**
     * Get app version info
     */
    fun getVersionInfo(): VersionInfo {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            VersionInfo(
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                buildType = BuildConfig.BUILD_TYPE,
                isDebug = BuildConfig.DEBUG
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting version info")
            VersionInfo("Unknown", 0, "Unknown", false)
        }
    }

    data class VersionInfo(
        val versionName: String,
        val versionCode: Long,
        val buildType: String,
        val isDebug: Boolean
    )
}
