package com.tinhtx.localplayerapplication.presentation.service.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.tinhtx.localplayerapplication.R

class CastOptionsProvider : OptionsProvider {
    
    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(MainActivity::class.java.name)
            .build()
        
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()
        
        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_app_id))
            .setCastMediaOptions(mediaOptions)
            .build()
    }
    
    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}

// Placeholder activities - you would need to create these
class MainActivity
class ExpandedControlsActivity
