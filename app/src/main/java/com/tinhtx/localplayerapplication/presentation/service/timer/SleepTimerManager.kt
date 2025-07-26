package com.tinhtx.localplayerapplication.presentation.service.timer

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimerManager @Inject constructor(
    private val context: Context
) {
    
    private var timerJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()
    
    fun startTimer(durationMinutes: Int, fadeOut: Boolean = true) {
        stopTimer() // Stop any existing timer
        
        val durationMs = durationMinutes * 60 * 1000L
        val startTime = System.currentTimeMillis()
        
        _timerState.value = SleepTimerState(
            isActive = true,
            remainingTimeMs = durationMs,
            totalTimeMs = durationMs,
            fadeOutEnabled = fadeOut
        )
        
        timerJob = coroutineScope.launch {
            while (_timerState.value.isActive && _timerState.value.remainingTimeMs > 0) {
                delay(1000) // Update every second
                
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (durationMs - elapsed).coerceAtLeast(0L)
                
                _timerState.value = _timerState.value.copy(
                    remainingTimeMs = remaining
                )
                
                // Start fade out in the last 30 seconds if enabled
                if (fadeOut && remaining <= 30000 && remaining > 0) {
                    val fadeProgress = 1f - (remaining / 30000f)
                    onFadeOut(fadeProgress)
                }
                
                // Timer finished
                if (remaining <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
        
        // Start the sleep timer service
        val serviceIntent = Intent(context, SleepTimerService::class.java).apply {
            action = SleepTimerService.ACTION_START_TIMER
            putExtra(SleepTimerService.EXTRA_DURATION_MS, durationMs)
            putExtra(SleepTimerService.EXTRA_FADE_OUT, fadeOut)
        }
        context.startForegroundService(serviceIntent)
        
        android.util.Log.d("SleepTimerManager", "Sleep timer started: $durationMinutes minutes")
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        
        _timerState.value = SleepTimerState()
        
        // Stop the sleep timer service
        val serviceIntent = Intent(context, SleepTimerService::class.java)
        context.stopService(serviceIntent)
        
        android.util.Log.d("SleepTimerManager", "Sleep timer stopped")
    }
    
    fun addTime(additionalMinutes: Int) {
        if (_timerState.value.isActive) {
            val additionalMs = additionalMinutes * 60 * 1000L
            _timerState.value = _timerState.value.copy(
                remainingTimeMs = _timerState.value.remainingTimeMs + additionalMs,
                totalTimeMs = _timerState.value.totalTimeMs + additionalMs
            )
            
            android.util.Log.d("SleepTimerManager", "Added $additionalMinutes minutes to sleep timer")
        }
    }
    
    fun getFormattedRemainingTime(): String {
        val remainingMs = _timerState.value.remainingTimeMs
        val totalSeconds = remainingMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun onFadeOut(progress: Float) {
        // Send fade out progress to music service
        val intent = Intent("ACTION_SLEEP_TIMER_FADE_OUT").apply {
            putExtra("fade_progress", progress)
        }
        context.sendBroadcast(intent)
    }
    
    private fun onTimerFinished() {
        android.util.Log.d("SleepTimerManager", "Sleep timer finished - stopping playback")
        
        // Send stop playback intent
        val intent = Intent("ACTION_SLEEP_TIMER_FINISHED")
        context.sendBroadcast(intent)
        
        stopTimer()
    }
    
    fun release() {
        stopTimer()
        coroutineScope.cancel()
    }
}

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingTimeMs: Long = 0L,
    val totalTimeMs: Long = 0L,
    val fadeOutEnabled: Boolean = true
) {
    val progress: Float
        get() = if (totalTimeMs > 0) {
            1f - (remainingTimeMs.toFloat() / totalTimeMs.toFloat())
        } else 0f
}
