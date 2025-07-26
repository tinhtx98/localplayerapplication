package com.tinhtx.localplayerapplication

import android.app.Activity
import android.app.Application
import android.os.Bundle
import timber.log.Timber

class AppLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    
    companion object {
        private var activityReferences = 0
        private var isActivityChangingConfigurations = false
        
        val isInForeground: Boolean
            get() = activityReferences > 0 && !isActivityChangingConfigurations
        
        val isInBackground: Boolean
            get() = !isInForeground
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Timber.d("Activity created: ${activity.localClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            Timber.d("App entered foreground")
            onAppForegrounded()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Timber.v("Activity resumed: ${activity.localClassName}")
    }

    override fun onActivityPaused(activity: Activity) {
        Timber.v("Activity paused: ${activity.localClassName}")
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            Timber.d("App entered background")
            onAppBackgrounded()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Timber.v("Activity save instance state: ${activity.localClassName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Timber.d("Activity destroyed: ${activity.localClassName}")
    }

    private fun onAppForegrounded() {
        // Handle app coming to foreground
        // Resume services if needed
        // Refresh data if needed
    }

    private fun onAppBackgrounded() {
        // Handle app going to background
        // Save state
        // Pause non-essential operations
    }
}
