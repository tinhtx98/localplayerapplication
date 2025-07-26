package com.tinhtx.localplayerapplication.presentation.screens.settings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playbackSettings: PlaybackSettings = PlaybackSettings(),
    val librarySettings: LibrarySettings = LibrarySettings(),
    val appearanceSettings: AppearanceSettings = AppearanceSettings(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val storageSettings: StorageSettings = StorageSettings(),
    val storageInfo: StorageInfo = StorageInfo(),
    val appInfo: AppInfo = AppInfo(),
    val showResetDialog: Boolean = false
)