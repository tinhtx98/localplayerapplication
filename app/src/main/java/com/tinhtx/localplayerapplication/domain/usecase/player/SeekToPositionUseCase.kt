package com.tinhtx.localplayerapplication.domain.usecase.player

import javax.inject.Inject

class SeekToPositionUseCase @Inject constructor() {
    operator fun invoke(position: Long, duration: Long): Result<Long> {
        return try {
            val validPosition = position.coerceIn(0L, duration)
            Result.success(validPosition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun seekByPercentage(percentage: Float, duration: Long): Result<Long> {
        return try {
            val validPercentage = percentage.coerceIn(0f, 1f)
            val position = (duration * validPercentage).toLong()
            Result.success(position)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
