package com.tinhtx.localplayerapplication.data.local.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val userName: String = "Music Lover",
    val profileImageUri: String = "",
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val isFirstLaunch: Boolean = true,
    val lastScanTime: Long = 0L,
    val autoScanEnabled: Boolean = true
)

@Serializable
data class SettingsPreferences(
    val audioQuality: String = "HIGH", // LOW, MEDIUM, HIGH
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3000, // milliseconds
    val gaplessPlayback: Boolean = true,
    val repeatMode: Int = 0, // 0=OFF, 1=ONE, 2=ALL
    val shuffleMode: Int = 0, // 0=OFF, 1=ON
    val volumeLevel: Float = 1.0f,
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 500, // 0-1000
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 500, // 0-1000
    val sleepTimerDuration: Int = 0, // minutes, 0=disabled
    val headphoneAutoPlay: Boolean = true,
    val headphoneAutoPause: Boolean = true,
    val showNotification: Boolean = true,
    val showLockScreenControls: Boolean = true,
    val libraryTabOrder: List<String> = listOf("SONGS", "ALBUMS", "ARTISTS"),
    val gridSize: Int = 2, // columns in grid view
    val sortOrder: String = "TITLE_ASC"
)
