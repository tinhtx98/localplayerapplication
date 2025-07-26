package com.tinhtx.localplayerapplication.presentation.service.components

import android.os.Binder
import com.tinhtx.localplayerapplication.presentation.service.MusicService

class ServiceBinder(private val service: MusicService) : Binder() {
    
    fun getService(): MusicService = service
    
    // Additional service communication methods
    fun isPlaying(): Boolean = service.isPlaying
    
    fun getCurrentSong() = service.currentSong
    
    fun getPlaybackPosition(): Long = service.playbackPosition
}
