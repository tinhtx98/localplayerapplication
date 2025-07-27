package com.tinhtx.localplayerapplication.data.service.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tinhtx.localplayerapplication.data.service.MusicService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    private val context: Context
) : ServiceConnection, DefaultLifecycleObserver {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _serviceBinder = MutableStateFlow<ServiceBinder?>(null)
    val serviceBinder: StateFlow<ServiceBinder?> = _serviceBinder.asStateFlow()
    
    private val _connectionState = MutableStateFlow(ServiceConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ServiceConnectionState> = _connectionState.asStateFlow()
    
    private var isBound = false
    private var serviceIntent: Intent? = null
    private val connectionListeners = mutableSetOf<ServiceConnectionListener>()
    private var retryCount = 0
    private val maxRetries = 3
    private val retryDelay = 1000L
    
    interface ServiceConnectionListener {
        fun onServiceConnected(binder: ServiceBinder)
        fun onServiceDisconnected()
        fun onServiceConnectionFailed(error: String)
    }
    
    /**
     * Bind to music service
     */
    fun bindService(lifecycleOwner: LifecycleOwner? = null): Boolean {
        lifecycleOwner?.lifecycle?.addObserver(this)
        
        if (isBound && _isConnected.value) {
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Already connected to service"
            )
            return true
        }
        
        _connectionState.value = ServiceConnectionState.CONNECTING
        
        serviceIntent = Intent(context, MusicService::class.java)
        
        return try {
            // Start service first to ensure it runs in foreground
            context.startService(serviceIntent)
            
            // Then bind to it
            val bound = context.bindService(
                serviceIntent!!,
                this,
                Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
            )
            
            if (bound) {
                isBound = true
                ServiceUtils.logPlaybackEvent(
                    "ServiceConnection", 
                    null, 
                    "Service binding initiated"
                )
            } else {
                _connectionState.value = ServiceConnectionState.ERROR
                ServiceUtils.logPlaybackEvent(
                    "ServiceConnection", 
                    null, 
                    "Failed to bind service"
                )
            }
            
            bound
        } catch (e: Exception) {
            _connectionState.value = ServiceConnectionState.ERROR
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Exception binding service: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Unbind from music service
     */
    fun unbindService() {
        if (!isBound) return
        
        _connectionState.value = ServiceConnectionState.DISCONNECTING
        
        try {
            context.unbindService(this)
            isBound = false
            
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Service unbound"
            )
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Exception unbinding service: ${e.message}"
            )
        } finally {
            cleanup()
        }
    }
    
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? ServiceBinder
        
        if (binder != null) {
            _serviceBinder.value = binder
            _isConnected.value = true
            _connectionState.value = ServiceConnectionState.CONNECTED
            retryCount = 0
            
            // Notify listeners
            connectionListeners.forEach { listener ->
                try {
                    listener.onServiceConnected(binder)
                } catch (e: Exception) {
                    ServiceUtils.logPlaybackEvent(
                        "ServiceConnection", 
                        null, 
                        "Error notifying service connected: ${e.message}"
                    )
                }
            }
            
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Service connected successfully"
            )
        } else {
            _connectionState.value = ServiceConnectionState.ERROR
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Invalid service binder received"
            )
        }
    }
    
    override fun onServiceDisconnected(name: ComponentName?) {
        ServiceUtils.logPlaybackEvent(
            "ServiceConnection", 
            null, 
            "Service disconnected unexpectedly"
        )
        
        handleServiceDisconnection()
        
        // Attempt to reconnect if not intentionally disconnected
        if (_connectionState.value != ServiceConnectionState.DISCONNECTING) {
            attemptReconnection()
        }
    }
    
    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
        
        ServiceUtils.logPlaybackEvent(
            "ServiceConnection", 
            null, 
            "Service binding died"
        )
        
        handleServiceDisconnection()
        attemptReconnection()
    }
    
    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
        
        ServiceUtils.logPlaybackEvent(
            "ServiceConnection", 
            null, 
            "Service returned null binding"
        )
        
        _connectionState.value = ServiceConnectionState.ERROR
        notifyConnectionFailed("Service returned null binding")
    }
    
    private fun handleServiceDisconnection() {
        _serviceBinder.value = null
        _isConnected.value = false
        _connectionState.value = ServiceConnectionState.DISCONNECTED
        
        // Notify listeners
        connectionListeners.forEach { listener ->
            try {
                listener.onServiceDisconnected()
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "ServiceConnection", 
                    null, 
                    "Error notifying service disconnected: ${e.message}"
                )
            }
        }
    }
    
    private fun attemptReconnection() {
        if (retryCount >= maxRetries) {
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Max reconnection attempts reached"
            )
            notifyConnectionFailed("Max reconnection attempts reached")
            return
        }
        
        retryCount++
        _connectionState.value = ServiceConnectionState.CONNECTING
        
        ServiceUtils.logPlaybackEvent(
            "ServiceConnection", 
            null, 
            "Attempting reconnection (${retryCount}/$maxRetries)"
        )
        
        // Use a coroutine scope if available, otherwise use a handler
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(retryDelay * retryCount)
            
            if (_connectionState.value == ServiceConnectionState.CONNECTING) {
                val success = bindService()
                if (!success) {
                    attemptReconnection()
                }
            }
        }
    }
    
    private fun notifyConnectionFailed(error: String) {
        connectionListeners.forEach { listener ->
            try {
                listener.onServiceConnectionFailed(error)
            } catch (e: Exception) {
                ServiceUtils.logPlaybackEvent(
                    "ServiceConnection", 
                    null, 
                    "Error notifying connection failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Add connection listener
     */
    fun addConnectionListener(listener: ServiceConnectionListener) {
        connectionListeners.add(listener)
        
        // If already connected, notify immediately
        if (_isConnected.value) {
            _serviceBinder.value?.let { binder ->
                listener.onServiceConnected(binder)
            }
        }
    }
    
    /**
     * Remove connection listener
     */
    fun removeConnectionListener(listener: ServiceConnectionListener) {
        connectionListeners.remove(listener)
    }
    
    /**
     * Get service binder if connected
     */
    fun getServiceBinder(): ServiceBinder? {
        return _serviceBinder.value
    }
    
    /**
     * Execute action with service binder
     */
    inline fun <T> withServiceBinder(action: (ServiceBinder) -> T): T? {
        return _serviceBinder.value?.let(action)
    }
    
    /**
     * Execute action if service is connected
     */
    inline fun ifConnected(action: (ServiceBinder) -> Unit) {
        if (_isConnected.value) {
            _serviceBinder.value?.let(action)
        }
    }
    
    /**
     * Wait for service connection
     */
    suspend fun waitForConnection(timeoutMs: Long = 5000L): Boolean {
        val startTime = System.currentTimeMillis()
        
        while (!_isConnected.value && 
               System.currentTimeMillis() - startTime < timeoutMs &&
               _connectionState.value != ServiceConnectionState.ERROR) {
            kotlinx.coroutines.delay(100)
        }
        
        return _isConnected.value
    }
    
    /**
     * Force reconnection
     */
    fun forceReconnect() {
        if (isBound) {
            unbindService()
        }
        
        retryCount = 0
        bindService()
    }
    
    /**
     * Check service health
     */
    fun checkServiceHealth(): Boolean {
        return try {
            _serviceBinder.value?.isServiceBound() == true
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "ServiceConnection", 
                null, 
                "Service health check failed: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Get connection diagnostics
     */
    fun getDiagnostics(): ConnectionDiagnostics {
        return ConnectionDiagnostics(
            isConnected = _isConnected.value,
            connectionState = _connectionState.value,
            isBound = isBound,
            retryCount = retryCount,
            hasServiceBinder = _serviceBinder.value != null,
            listenerCount = connectionListeners.size,
            serviceHealth = checkServiceHealth()
        )
    }
    
    // Lifecycle callbacks
    override fun onStart(owner: LifecycleOwner) {
        if (!_isConnected.value && !isBound) {
            bindService()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        // Don't unbind on stop to keep music playing
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        unbindService()
        cleanup()
    }
    
    private fun cleanup() {
        _serviceBinder.value = null
        _isConnected.value = false
        _connectionState.value = ServiceConnectionState.DISCONNECTED
        connectionListeners.clear()
        serviceIntent = null
        isBound = false
        retryCount = 0
    }
}

/**
 * Connection diagnostics data class
 */
data class ConnectionDiagnostics(
    val isConnected: Boolean,
    val connectionState: ServiceConnectionState,
    val isBound: Boolean,
    val retryCount: Int,
    val hasServiceBinder: Boolean,
    val listenerCount: Int,
    val serviceHealth: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Extension functions for easier service access
 */
suspend fun MusicServiceConnection.executeWhenConnected(action: suspend (ServiceBinder) -> Unit) {
    if (waitForConnection()) {
        withServiceBinder { binder ->
            kotlinx.coroutines.runBlocking {
                action(binder)
            }
        }
    }
}

fun MusicServiceConnection.executeIfConnected(action: (ServiceBinder) -> Unit) {
    ifConnected(action)
}
