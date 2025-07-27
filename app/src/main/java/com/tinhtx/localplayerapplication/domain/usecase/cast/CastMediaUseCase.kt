package com.tinhtx.localplayerapplication.domain.usecase.cast

import com.tinhtx.localplayerapplication.domain.model.CastDevice
import com.tinhtx.localplayerapplication.domain.model.CastDeviceType
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.CastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for casting media to devices
 */
class CastMediaUseCase @Inject constructor(
    private val castRepository: CastRepository
) {
    
    /**
     * Get all available cast devices
     */
    suspend fun getAvailableDevices(): List<CastDevice> {
        return castRepository.getAvailableDevices()
    }
    
    /**
     * Get available devices as Flow for reactive UI
     */
    fun getAvailableDevicesFlow(): Flow<List<CastDevice>> {
        return castRepository.getAvailableDevicesFlow()
    }
    
    /**
     * Connect to a cast device
     */
    suspend fun connectToDevice(deviceId: String): Result<CastDevice> {
        return try {
            castRepository.updateConnectionStatus(deviceId, true)
            val device = castRepository.getDeviceById(deviceId)
            if (device != null) {
                Result.success(device)
            } else {
                Result.failure(Exception("Device not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from current cast device
     */
    suspend fun disconnectFromDevice(deviceId: String): Result<Unit> {
        return try {
            castRepository.updateConnectionStatus(deviceId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from all devices
     */
    suspend fun disconnectFromAllDevices(): Result<Unit> {
        return try {
            castRepository.disconnectAllDevices()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cast a song to connected device
     */
    suspend fun castSong(song: Song, deviceId: String): Result<Unit> {
        return try {
            val device = castRepository.getDeviceById(deviceId)
            if (device == null) {
                return Result.failure(Exception("Device not found"))
            }
            
            if (!device.isConnected) {
                return Result.failure(Exception("Device not connected"))
            }
            
            if (!device.canPlayAudio) {
                return Result.failure(Exception("Device cannot play audio"))
            }
            
            // TODO: Implement actual casting logic
            // This would integrate with Google Cast SDK or similar
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cast a playlist to connected device
     */
    suspend fun castPlaylist(songs: List<Song>, deviceId: String): Result<Unit> {
        return try {
            val device = castRepository.getDeviceById(deviceId)
            if (device == null) {
                return Result.failure(Exception("Device not found"))
            }
            
            if (!device.isConnected) {
                return Result.failure(Exception("Device not connected"))
            }
            
            if (!device.canPlayAudio) {
                return Result.failure(Exception("Device cannot play audio"))
            }
            
            // TODO: Implement actual playlist casting logic
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get connected devices
     */
    suspend fun getConnectedDevices(): List<CastDevice> {
        return castRepository.getConnectedDevices()
    }
    
    /**
     * Get last connected device
     */
    suspend fun getLastConnectedDevice(): CastDevice? {
        return castRepository.getLastConnectedDevice()
    }
    
    /**
     * Search for devices by name
     */
    suspend fun searchDevices(query: String): List<CastDevice> {
        return castRepository.searchDevices(query)
    }
    
    /**
     * Get devices by type
     */
    suspend fun getDevicesByType(type: CastDeviceType): List<CastDevice> {
        return castRepository.getDevicesByType(type)
    }
    
    /**
     * Update device availability
     */
    suspend fun updateDeviceAvailability(deviceId: String, isAvailable: Boolean): Result<Unit> {
        return try {
            castRepository.updateAvailabilityStatus(deviceId, isAvailable)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh all device availability
     */
    suspend fun refreshDeviceAvailability(availableDevices: List<CastDevice>): Result<Unit> {
        return try {
            castRepository.refreshDeviceAvailability(availableDevices)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up old devices
     */
    suspend fun cleanupOldDevices(olderThanDays: Int = 30): Result<Unit> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            castRepository.cleanupOldDevices(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
