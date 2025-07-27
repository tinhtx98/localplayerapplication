package com.tinhtx.localplayerapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing user profile
 */
@Parcelize
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val avatarPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
) : Parcelable {
    
    val hasName: Boolean
        get() = name.isNotBlank()
    
    val hasEmail: Boolean
        get() = email.isNotBlank()
    
    val hasAvatar: Boolean
        get() = !avatarPath.isNullOrBlank()
    
    val displayName: String
        get() = if (hasName) name else "User"
    
    val initials: String
        get() = if (hasName) {
            name.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
        } else {
            "U"
        }
    
    val formattedCreatedDate: String
        get() = formatDate(createdAt)
    
    val formattedLastLogin: String
        get() = formatDate(lastLogin)
    
    val isNewUser: Boolean
        get() = System.currentTimeMillis() - createdAt < 24 * 60 * 60 * 1000L // Less than 24 hours
    
    private fun formatDate(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }
}
