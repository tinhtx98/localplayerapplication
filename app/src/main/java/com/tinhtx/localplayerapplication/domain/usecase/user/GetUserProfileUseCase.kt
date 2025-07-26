package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.domain.model.UserProfile
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserProfile> {
        return userPreferencesRepository.userProfile
    }
}
