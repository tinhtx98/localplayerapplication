package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateThemeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(themeMode: AppConstants.ThemeMode): Result<Unit> {
        return try {
            userPreferencesRepository.updateThemeMode(themeMode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
