package com.tinhtx.localplayerapplication.presentation.screens.settings

import com.tinhtx.localplayerapplication.domain.model.AppSettings

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val appInfo: AppInfo = AppInfo(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showThemeDialog: Boolean = false,
    val showResetDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val exportProgress: Int = 0,
    val importProgress: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
) {
    val hasError: Boolean get() = error != null
    val isIdle: Boolean get() = !isLoading && !isExporting && !isImporting
}

data class AppInfo(
    val appName: String = "LocalPlayer",
    val versionName: String = "1.0.0",
    val versionCode: Long = 1L,
    val buildType: String = "debug",
    val packageName: String = "com.tinhtx.localplayerapplication",
    val buildDate: String = "",
    val gitCommit: String = "",
    val targetSdkVersion: Int = 34,
    val minSdkVersion: Int = 24,
    val compileSdkVersion: Int = 34,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val storageInfo: StorageInfo = StorageInfo(),
    val libraryStats: LibraryStats = LibraryStats()
)

data class DeviceInfo(
    val manufacturer: String = android.os.Build.MANUFACTURER,
    val model: String = android.os.Build.MODEL,
    val androidVersion: String = android.os.Build.VERSION.RELEASE,
    val apiLevel: Int = android.os.Build.VERSION.SDK_INT,
    val brand: String = android.os.Build.BRAND,
    val device: String = android.os.Build.DEVICE,
    val board: String = android.os.Build.BOARD,
    val hardware: String = android.os.Build.HARDWARE,
    val bootloader: String = android.os.Build.BOOTLOADER,
    val fingerprint: String = android.os.Build.FINGERPRINT,
    val totalMemory: Long = 0L,
    val availableMemory: Long = 0L,
    val processorCount: Int = Runtime.getRuntime().availableProcessors(),
    val locale: String = java.util.Locale.getDefault().toString()
)

data class StorageInfo(
    val totalInternalStorage: Long = 0L,
    val availableInternalStorage: Long = 0L,
    val usedInternalStorage: Long = 0L,
    val totalExternalStorage: Long = 0L,
    val availableExternalStorage: Long = 0L,
    val usedExternalStorage: Long = 0L,
    val cacheSize: Long = 0L,
    val dataSize: Long = 0L,
    val musicFolderSize: Long = 0L
) {
    val internalStoragePercentageUsed: Float
        get() = if (totalInternalStorage > 0) (usedInternalStorage.toFloat() / totalInternalStorage) * 100f else 0f

    val externalStoragePercentageUsed: Float
        get() = if (totalExternalStorage > 0) (usedExternalStorage.toFloat() / totalExternalStorage) * 100f else 0f
}

data class LibraryStats(
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalArtists: Int = 0,
    val totalPlaylists: Int = 0,
    val favoriteSongs: Int = 0,
    val totalPlaytime: Long = 0L, // milliseconds
    val totalSize: Long = 0L, // bytes
    val lastScanDate: Long = 0L,
    val averageSongDuration: Long = 0L,
    val mostPlayedGenre: String = "",
    val recentlyAddedCount: Int = 0,
    val duplicateSongs: Int = 0
) {
    val totalPlaytimeFormatted: String
        get() = formatDuration(totalPlaytime)

    val totalSizeFormatted: String
        get() = formatBytes(totalSize)

    val averageSongDurationFormatted: String
        get() = formatDuration(averageSongDuration)

    private fun formatDuration(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.1f %s", size, units[unitIndex])
    }
}
