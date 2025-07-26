package com.tinhtx.localplayerapplication.domain.usecase.player

import javax.inject.Inject

class PauseSongUseCase @Inject constructor() {
    operator fun invoke(): Result<Unit> {
        return try {
            // This will be handled by the media service
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
