package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.data.local.database.dao.CastDeviceDao
import com.tinhtx.localplayerapplication.data.local.database.entities.toDomain
import com.tinhtx.localplayerapplication.data.local.database.entities.toEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.toDomainCastDevices
import com.tinhtx.localplayerapplication.data.local.database.entities.toEntityCastDevices
import com.tinhtx.localplayerapplication.domain.model.CastDevice
import com.tinhtx.localplayerapplication.domain.model.CastDeviceType
import com.tinhtx.localplayerapplication.domain.repository.CastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CastRepository using Room DAO
 */
@Singleton
class CastRepositoryImpl @Inject constructor(
    private val castDeviceDao: CastDeviceDao
) : CastRepository {
    
    // Device Discovery Operations - Mapped từ CastDeviceDao
    override suspend fun getAllDevices(): List<CastDevice> {
        return castDeviceDao.getAllDevices().toDomainCastDevices()
    }
    
    override fun getAllDevicesFlow(): Flow<List<CastDevice>> {
        return castDeviceDao.getAllDevicesFlow().map { entities ->
            entities.toDomainCastDevices()
        }
    }
    
    override suspend fun getAvailableDevices(): List<CastDevice> {
        return castDeviceDao.getAvailableDevices().toDomainCastDevices()
    }
    
    override fun getAvailableDevicesFlow(): Flow<List<CastDevice>> {
        return castDeviceDao.getAvailableDevicesFlow().map { entities ->
            entities.toDomainCastDevices()
        }
    }
    
    override suspend fun getConnectedDevices(): List<CastDevice> {
        return castDeviceDao.getConnectedDevices().toDomainCastDevices()
    }
    
    // Device Management Operations - Mapped từ CastDeviceDao
    override suspend fun getDeviceById(id: String): CastDevice? {
        return castDeviceDao.getDeviceById(id)?.toDomain()
    }
    
    override suspend fun getDeviceByName(name: String): CastDevice? {
        return castDeviceDao.getDeviceByName(name)?.toDomain()
    }
    
    override suspend fun insertDevice(device: CastDevice): Long {
        return castDeviceDao.insertDevice(device.toEntity())
    }
    
    override suspend fun insertDevices(devices: List<CastDevice>): List<Long> {
        return castDeviceDao.insertDevices(devices.toEntityCastDevices())
    }
    
    override suspend fun updateDevice(device: CastDevice) {
        castDeviceDao.updateDevice(device.toEntity())
    }
    
    override suspend fun deleteDevice(device: CastDevice) {
        castDeviceDao.deleteDevice(device.toEntity())
    }
    
    override suspend fun deleteDeviceById(id: String) {
        castDeviceDao.deleteDeviceById(id)
    }
    
    // Connection Management - Mapped từ CastDeviceDao methods
    override suspend fun updateConnectionStatus(deviceId: String, isConnected: Boolean) {
        castDeviceDao.updateConnectionStatus(deviceId, isConnected)
    }
    
    override suspend fun updateAvailabilityStatus(deviceId: String, isAvailable: Boolean) {
        castDeviceDao.updateAvailabilityStatus(deviceId, isAvailable)
    }
    
    override suspend fun disconnectAllDevices() {
        castDeviceDao.disconnectAllDevices()
    }
    
    override suspend fun getLastConnectedDevice(): CastDevice? {
        return castDeviceDao.getLastConnectedDevice()?.toDomain()
    }
    
    // Filter Operations - Mapped từ CastDeviceDao methods
    override suspend fun getDevicesByType(type: CastDeviceType): List<CastDevice> {
        return castDeviceDao.getDevicesByType(type).toDomainCastDevices()
    }
    
    override suspend fun searchDevices(query: String): List<CastDevice> {
        return castDeviceDao.searchDevices(query).toDomainCastDevices()
    }
    
    override suspend fun getRecentlyConnectedDevices(limit: Int): List<CastDevice> {
        return castDeviceDao.getRecentlyConnectedDevices(limit).toDomainCastDevices()
    }
    
    override suspend fun getDevicesConnectedSince(since: Long): List<CastDevice> {
        return castDeviceDao.getDevicesConnectedSince(since).toDomainCastDevices()
    }
    
    override suspend fun getDevicesWithCapability(capability: String): List<CastDevice> {
        return castDeviceDao.getDevicesWithCapability(capability).toDomainCastDevices()
    }
    
    // Statistics Operations - Mapped từ CastDeviceDao methods
    override suspend fun getDeviceCount(): Int {
        return castDeviceDao.getDeviceCount()
    }
    
    override suspend fun getAvailableDeviceCount(): Int {
        return castDeviceDao.getAvailableDeviceCount()
    }
    
    override suspend fun getConnectedDeviceCount(): Int {
        return castDeviceDao.getConnectedDeviceCount()
    }
    
    override suspend fun getDeviceCountByType(type: CastDeviceType): Int {
        return castDeviceDao.getDeviceCountByType(type)
    }
    
    override suspend fun getRecentConnectionCount(since: Long): Int {
        return castDeviceDao.getRecentConnectionCount(since)
    }
    
    override suspend fun getAllDeviceTypes(): List<CastDeviceType> {
        return castDeviceDao.getAllDeviceTypes()
    }
    
    // Batch Operations - Mapped từ CastDeviceDao methods
    override suspend fun markDevicesAsUnavailable(availableDeviceIds: List<String>) {
        castDeviceDao.markDevicesAsUnavailable(availableDeviceIds)
    }
    
    override suspend fun markDevicesAsAvailable(deviceIds: List<String>) {
        castDeviceDao.markDevicesAsAvailable(deviceIds)
    }
    
    override suspend fun refreshDeviceAvailability(availableDevices: List<CastDevice>) {
        // Get all available device IDs
        val availableDeviceIds = availableDevices.map { it.id }
        
        // Mark devices as unavailable if not in the available list
        if (availableDeviceIds.isNotEmpty()) {
            castDeviceDao.markDevicesAsUnavailable(availableDeviceIds)
        }
        
        // Update or insert available devices
        val devicesToUpdate = mutableListOf<CastDevice>()
        availableDevices.forEach { device ->
            val existingDevice = castDeviceDao.getDeviceById(device.id)
            if (existingDevice != null) {
                // Update existing device
                devicesToUpdate.add(device.copy(isAvailable = true))
            } else {
                // Insert new device
                castDeviceDao.insertDevice(device.copy(isAvailable = true).toEntity())
            }
        }
        
        // Batch update existing devices
        if (devicesToUpdate.isNotEmpty()) {
            castDeviceDao.updateDevices(devicesToUpdate.toEntityCastDevices())
        }
    }
    
    // Cleanup Operations - Mapped từ CastDeviceDao methods
    override suspend fun cleanupOldDevices(cutoffTime: Long) {
        castDeviceDao.cleanupOldDevices(cutoffTime)
    }
    
    override suspend fun removeUnavailableDevices(cutoffTime: Long) {
        castDeviceDao.removeUnavailableDevices(cutoffTime)
    }
    
    override suspend fun clearAllDevices() {
        castDeviceDao.deleteAllDevices()
    }
}
