package com.tinhtx.localplayerapplication.domain.usecase.settings

import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend fun updatePlaybackSettings(settings: PlaybackSettings) {
        settingsRepository.updatePlaybackSettings(settings)
    }

    suspend fun updateAppearanceSettings(settings: AppearanceSettings) {
        settingsRepository.updateAppearanceSettings(settings)
    }

    suspend fun updateLibrarySettings(settings: LibrarySettings) {
        settingsRepository.updateLibrarySettings(settings)
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        settingsRepository.updateNotificationSettings(settings)
    }

    suspend fun updateStorageSettings(settings: StorageSettings) {
        settingsRepository.updateStorageSettings(settings)
    }
}
