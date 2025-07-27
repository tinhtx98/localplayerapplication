package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.CastDevice
import com.tinhtx.localplayerapplication.domain.model.CastDeviceType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cast device operations
 */
interface CastRepository {
    
    // Device Discovery Operations
    suspend fun getAllDevices(): List<CastDevice>
    fun getAllDevicesFlow(): Flow<List<CastDevice>>
    suspend fun getAvailableDevices(): List<CastDevice>
    fun getAvailableDevicesFlow(): Flow<List<CastDevice>>
    suspend fun getConnectedDevices(): List<CastDevice>
    
    // Device Management Operations
    suspend fun getDeviceById(id: String): CastDevice?
    suspend fun getDeviceByName(name: String): CastDevice?
    suspend fun insertDevice(device: CastDevice): Long
    suspend fun insertDevices(devices: List<CastDevice>): List<Long>
    suspend fun updateDevice(device: CastDevice)
    suspend fun deleteDevice(device: CastDevice)
    suspend fun deleteDeviceById(id: String)
    
    // Connection Management
    suspend fun updateConnectionStatus(deviceId: String, isConnected: Boolean)
    suspend fun updateAvailabilityStatus(deviceId: String, isAvailable: Boolean)
    suspend fun disconnectAllDevices()
    suspend fun getLastConnectedDevice(): CastDevice?
    
    // Filter Operations
    suspend fun getDevicesByType(type: CastDeviceType): List<CastDevice>
    suspend fun searchDevices(query: String): List<CastDevice>
    suspend fun getRecentlyConnectedDevices(limit: Int): List<CastDevice>
    suspend fun getDevicesConnectedSince(since: Long): List<CastDevice>
    suspend fun getDevicesWithCapability(capability: String): List<CastDevice>
    
    // Statistics Operations
    suspend fun getDeviceCount(): Int
    suspend fun getAvailableDeviceCount(): Int
    suspend fun getConnectedDeviceCount(): Int
    suspend fun getDeviceCountByType(type: CastDeviceType): Int
    suspend fun getRecentConnectionCount(since: Long): Int
    suspend fun getAllDeviceTypes(): List<CastDeviceType>
    
    // Batch Operations
    suspend fun markDevicesAsUnavailable(availableDeviceIds: List<String>)
    suspend fun markDevicesAsAvailable(deviceIds: List<String>)
    suspend fun refreshDeviceAvailability(availableDevices: List<CastDevice>)
    
    // Cleanup Operations
    suspend fun cleanupOldDevices(cutoffTime: Long)
    suspend fun removeUnavailableDevices(cutoffTime: Long)
    suspend fun clearAllDevices()
}
