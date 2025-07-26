package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun updateName(name: String): Result<Unit> {
        return try {
            if (name.isBlank()) {
                Result.failure(IllegalArgumentException("Name cannot be empty"))
            } else {
                userPreferencesRepository.updateUserName(name.trim())
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfileImage(uri: String): Result<Unit> {
        return try {
            userPreferencesRepository.updateProfileImage(uri)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateThemeMode(themeMode: AppConstants.ThemeMode): Result<Unit> {
        return try {
            userPreferencesRepository.updateThemeMode(themeMode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
