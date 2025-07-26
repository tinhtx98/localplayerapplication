package com.tinhtx.localplayerapplication.presentation.service.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.tinhtx.localplayerapplication.presentation.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicServiceConnection(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _musicService = MutableStateFlow<MusicService?>(null)
    val musicService: StateFlow<MusicService?> = _musicService
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.ServiceBinder
            _musicService.value = binder?.getService()
            _isConnected.value = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            _musicService.value = null
            _isConnected.value = false
        }
    }
    
    fun connect() {
        lifecycleOwner.lifecycleScope.launch {
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    fun disconnect() {
        try {
            context.unbindService(serviceConnection)
            _isConnected.value = false
            _musicService.value = null
        } catch (exception: Exception) {
            android.util.Log.w("MusicServiceConnection", "Error disconnecting service", exception)
        }
    }
    
    fun startService() {
        val intent = Intent(context, MusicService::class.java)
        context.startForegroundService(intent)
    }
    
    fun stopService() {
        val intent = Intent(context, MusicService::class.java)
        context.stopService(intent)
    }
}
