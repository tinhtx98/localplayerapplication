package com.tinhtx.localplayerapplication.domain.usecase.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SleepTimerUseCase @Inject constructor() {
    
    fun startTimer(durationMinutes: Int): Flow<SleepTimerState> = flow {
        if (durationMinutes <= 0) {
            emit(SleepTimerState.Stopped)
            return@flow
        }
        
        val totalMillis = durationMinutes * 60 * 1000L
        var remainingMillis = totalMillis
        
        emit(SleepTimerState.Running(remainingMillis, totalMillis))
        
        while (remainingMillis > 0) {
            delay(1000L) // Update every second
            remainingMillis -= 1000L
            
            if (remainingMillis <= 0) {
                emit(SleepTimerState.Finished)
                break
            } else {
                emit(SleepTimerState.Running(remainingMillis, totalMillis))
            }
        }
    }
    
    sealed class SleepTimerState {
        object Stopped : SleepTimerState()
        data class Running(val remainingMillis: Long, val totalMillis: Long) : SleepTimerState() {
            val remainingMinutes: Int get() = (remainingMillis / 60000).toInt()
            val remainingSeconds: Int get() = ((remainingMillis % 60000) / 1000).toInt()
            val formattedTime: String get() = String.format("%02d:%02d", remainingMinutes, remainingSeconds)
            val progress: Float get() = (totalMillis - remainingMillis).toFloat() / totalMillis.toFloat()
        }
        object Finished : SleepTimerState()
    }
}
