package com.tinhtx.localplayerapplication.domain.usecase.settings

import com.tinhtx.localplayerapplication.domain.model.AppSettings
import com.tinhtx.localplayerapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): AppSettings {
        return settingsRepository.getAppSettings()
    }

    fun flow(): Flow<AppSettings> {
        return settingsRepository.getAppSettingsFlow()
    }
}
