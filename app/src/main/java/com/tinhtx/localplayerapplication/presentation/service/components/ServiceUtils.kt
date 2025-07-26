package com.tinhtx.localplayerapplication.presentation.service.components

import android.content.Context
import android.content.Intent
import com.tinhtx.localplayerapplication.presentation.service.MusicService

object ServiceUtils {
    
    fun startMusicService(context: Context) {
        val intent = Intent(context, MusicService::class.java)
        context.startForegroundService(intent)
    }
    
    fun stopMusicService(context: Context) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        context.startService(intent)
    }
    
    fun sendPlayPauseAction(context: Context) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY_PAUSE
        }
        context.startService(intent)
    }
    
    fun sendSkipToNextAction(context: Context) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_TO_NEXT
        }
        context.startService(intent)
    }
    
    fun sendSkipToPreviousAction(context: Context) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_TO_PREVIOUS
        }
        context.startService(intent)
    }
    
    fun sendSeekToAction(context: Context, position: Long) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK_TO
            putExtra(MusicService.EXTRA_SEEK_POSITION, position)
        }
        context.startService(intent)
    }
}
