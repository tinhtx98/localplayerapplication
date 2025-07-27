package com.tinhtx.localplayerapplication.data.local.database.dao

import androidx.room.*
import com.tinhtx.localplayerapplication.data.local.database.entities.CastDeviceEntity
import com.tinhtx.localplayerapplication.domain.model.CastDeviceType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Cast Device operations
 */
@Dao
interface CastDeviceDao {
    
    // Basic CRUD Operations
    @Query("SELECT * FROM cast_devices ORDER BY lastConnected DESC")
    suspend fun getAllDevices(): List<CastDeviceEntity>
    
    @Query("SELECT * FROM cast_devices ORDER BY lastConnected DESC")
    fun getAllDevicesFlow(): Flow<List<CastDeviceEntity>>
    
    @Query("SELECT * FROM cast_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): CastDeviceEntity?
    
    @Query("SELECT * FROM cast_devices WHERE name = :name")
    suspend fun getDeviceByName(name: String): CastDeviceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: CastDeviceEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<CastDeviceEntity>): List<Long>
    
    @Update
    suspend fun updateDevice(device: CastDeviceEntity)
    
    @Update
    suspend fun updateDevices(devices: List<CastDeviceEntity>)
    
    @Delete
    suspend fun deleteDevice(device: CastDeviceEntity)
    
    @Query("DELETE FROM cast_devices WHERE id = :id")
    suspend fun deleteDeviceById(id: String)
    
    @Query("DELETE FROM cast_devices")
    suspend fun deleteAllDevices()
    
    // Connection Status Operations
    @Query("SELECT * FROM cast_devices WHERE isConnected = 1")
    suspend fun getConnectedDevices(): List<CastDeviceEntity>
    
    @Query("SELECT * FROM cast_devices WHERE isAvailable = 1")
    suspend fun getAvailableDevices(): List<CastDeviceEntity>
    
    @Query("SELECT * FROM cast_devices WHERE isAvailable = 1")
    fun getAvailableDevicesFlow(): Flow<List<CastDeviceEntity>>
    
    @Query("UPDATE cast_devices SET isConnected = 0")
    suspend fun disconnectAllDevices()
    
    @Query("UPDATE cast_devices SET isConnected = :isConnected, lastConnected = :timestamp WHERE id = :deviceId")
    suspend fun updateConnectionStatus(deviceId: String, isConnected: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE cast_devices SET isAvailable = :isAvailable, updatedAt = :timestamp WHERE id = :deviceId")
    suspend fun updateAvailabilityStatus(deviceId: String, isAvailable: Boolean, timestamp: Long = System.currentTimeMillis())
    
    // Filter Operations
    @Query("SELECT * FROM cast_devices WHERE type = :type ORDER BY lastConnected DESC")
    suspend fun getDevicesByType(type: CastDeviceType): List<CastDeviceEntity>
    
    @Query("SELECT * FROM cast_devices WHERE name LIKE '%' || :query || '%' ORDER BY lastConnected DESC")
    suspend fun searchDevices(query: String): List<CastDeviceEntity>
    
    // Recent Operations
    @Query("SELECT * FROM cast_devices WHERE lastConnected > 0 ORDER BY lastConnected DESC LIMIT :limit")
    suspend fun getRecentlyConnectedDevices(limit: Int): List<CastDeviceEntity>
    
    @Query("SELECT * FROM cast_devices WHERE lastConnected > :since ORDER BY lastConnected DESC")
    suspend fun getDevicesConnectedSince(since: Long): List<CastDeviceEntity>
    
    // Statistics Operations
    @Query("SELECT COUNT(*) FROM cast_devices")
    suspend fun getDeviceCount(): Int
    
    @Query("SELECT COUNT(*) FROM cast_devices WHERE isAvailable = 1")
    suspend fun getAvailableDeviceCount(): Int
    
    @Query("SELECT COUNT(*) FROM cast_devices WHERE isConnected = 1")
    suspend fun getConnectedDeviceCount(): Int
    
    @Query("SELECT COUNT(*) FROM cast_devices WHERE type = :type")
    suspend fun getDeviceCountByType(type: CastDeviceType): Int
    
    @Query("SELECT COUNT(*) FROM cast_devices WHERE lastConnected > :since")
    suspend fun getRecentConnectionCount(since: Long): Int
    
    // Cleanup Operations
    @Query("DELETE FROM cast_devices WHERE lastConnected < :cutoffTime AND isConnected = 0")
    suspend fun cleanupOldDevices(cutoffTime: Long)
    
    @Query("DELETE FROM cast_devices WHERE isAvailable = 0 AND lastConnected < :cutoffTime")
    suspend fun removeUnavailableDevices(cutoffTime: Long)
    
    // Batch Operations
    @Query("UPDATE cast_devices SET isAvailable = 0 WHERE id NOT IN (:availableDeviceIds)")
    suspend fun markDevicesAsUnavailable(availableDeviceIds: List<String>)
    
    @Query("UPDATE cast_devices SET isAvailable = 1, updatedAt = :timestamp WHERE id IN (:deviceIds)")
    suspend fun markDevicesAsAvailable(deviceIds: List<String>, timestamp: Long = System.currentTimeMillis())
    
    // Advanced Queries
    @Query("SELECT DISTINCT type FROM cast_devices")
    suspend fun getAllDeviceTypes(): List<CastDeviceType>
    
    @Query("SELECT * FROM cast_devices WHERE lastConnected = (SELECT MAX(lastConnected) FROM cast_devices)")
    suspend fun getLastConnectedDevice(): CastDeviceEntity?
    
    @Query("SELECT * FROM cast_devices WHERE capabilities LIKE '%' || :capability || '%'")
    suspend fun getDevicesWithCapability(capability: String): List<CastDeviceEntity>
    
    // Connection History
    @Query("SELECT AVG(lastConnected) FROM cast_devices WHERE lastConnected > 0")
    suspend fun getAverageConnectionTime(): Long
    
    @Query("SELECT id, name, lastConnected FROM cast_devices WHERE lastConnected > 0 ORDER BY lastConnected DESC")
    suspend fun getConnectionHistory(): List<DeviceConnectionInfo>
}

/**
 * Data class for connection history queries
 */
data class DeviceConnectionInfo(
    val id: String,
    val name: String,
    val lastConnected: Long
)
