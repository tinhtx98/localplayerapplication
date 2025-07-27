package com.tinhtx.localplayerapplication.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility functions for handling permissions
 */
object PermissionUtils {

    /**
     * Storage permissions based on Android version
     */
    val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * All required permissions for the app
     */
    val REQUIRED_PERMISSIONS = buildList {
        addAll(STORAGE_PERMISSIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    /**
     * Optional permissions that enhance functionality
     */
    val OPTIONAL_PERMISSIONS = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE
    )

    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            hasPermission(context, permission)
        }
    }

    /**
     * Check if storage permissions are granted
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            hasPermission(context, permission)
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Check if audio permission is granted
     */
    fun hasAudioPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Check if specific permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get missing required permissions
     */
    fun getMissingRequiredPermissions(context: Context): Array<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            !hasPermission(context, permission)
        }.toTypedArray()
    }

    /**
     * Get missing storage permissions
     */
    fun getMissingStoragePermissions(context: Context): Array<String> {
        return STORAGE_PERMISSIONS.filter { permission ->
            !hasPermission(context, permission)
        }.toTypedArray()
    }

    /**
     * Get granted permissions
     */
    fun getGrantedPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            hasPermission(context, permission)
        }
    }

    /**
     * Get denied permissions
     */
    fun getDeniedPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            !hasPermission(context, permission)
        }
    }

    /**
     * Check if permission is permanently denied (should show rationale)
     */
    fun isPermissionPermanentlyDenied(context: Context, permission: String): Boolean {
        // This check would typically be done in an Activity context
        // For utility purposes, we return false
        return false
    }

    /**
     * Get permission group for a permission
     */
    fun getPermissionGroup(permission: String): PermissionGroup {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO -> PermissionGroup.STORAGE
            
            Manifest.permission.POST_NOTIFICATIONS -> PermissionGroup.NOTIFICATION
            
            Manifest.permission.MODIFY_AUDIO_SETTINGS -> PermissionGroup.AUDIO
            
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.FOREGROUND_SERVICE -> PermissionGroup.SYSTEM
            
            else -> PermissionGroup.OTHER
        }
    }

    /**
     * Get user-friendly name for permission
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage Access"
            Manifest.permission.READ_MEDIA_AUDIO -> "Audio Files Access"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.MODIFY_AUDIO_SETTINGS -> "Audio Settings"
            Manifest.permission.WAKE_LOCK -> "Wake Lock"
            Manifest.permission.FOREGROUND_SERVICE -> "Background Service"
            else -> permission.substringAfterLast('.')
        }
    }

    /**
     * Get user-friendly description for permission
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> 
                "Allows the app to read music files from your device storage"
            
            Manifest.permission.READ_MEDIA_AUDIO -> 
                "Allows the app to access and play audio files from your device"
            
            Manifest.permission.POST_NOTIFICATIONS -> 
                "Allows the app to show playback controls in notifications"
            
            Manifest.permission.MODIFY_AUDIO_SETTINGS -> 
                "Allows the app to control audio volume and effects"
            
            Manifest.permission.WAKE_LOCK -> 
                "Allows the app to keep playing music in the background"
            
            Manifest.permission.FOREGROUND_SERVICE -> 
                "Allows the app to run music service in the background"
            
            else -> "Required for app functionality"
        }
    }

    /**
     * Permission groups for organizing permissions
     */
    enum class PermissionGroup {
        STORAGE,
        NOTIFICATION,
        AUDIO,
        SYSTEM,
        OTHER
    }

    /**
     * Permission request result
     */
    data class PermissionResult(
        val permission: String,
        val isGranted: Boolean,
        val isPermanentlyDenied: Boolean = false
    )

    /**
     * Batch permission result
     */
    data class BatchPermissionResult(
        val results: List<PermissionResult>,
        val allGranted: Boolean,
        val hasPermissionPermanentlyDenied: Boolean
    ) {
        val grantedPermissions: List<String> = results.filter { it.isGranted }.map { it.permission }
        val deniedPermissions: List<String> = results.filter { !it.isGranted }.map { it.permission }
        val permanentlyDeniedPermissions: List<String> = results.filter { it.isPermanentlyDenied }.map { it.permission }
    }
}
