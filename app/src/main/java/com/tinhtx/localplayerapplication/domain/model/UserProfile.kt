package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val name: String = AppConstants.DEFAULT_USER_NAME,
    val profileImageUri: String = "",
    val themeMode: AppConstants.ThemeMode = AppConstants.ThemeMode.SYSTEM,
    val isFirstLaunch: Boolean = true
) : Parcelable {
    
    val displayName: String
        get() = name.ifBlank { AppConstants.DEFAULT_USER_NAME }
    
    val hasProfileImage: Boolean
        get() = profileImageUri.isNotBlank()
    
    val greeting: String
        get() {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Good night"
            }
        }
    
    val fullGreeting: String
        get() = "$greeting, $displayName"
    
    companion object {
        fun default() = UserProfile()
    }
}
