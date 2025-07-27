package com.tinhtx.localplayerapplication.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinhtx.localplayerapplication.domain.model.CastDeviceType

/**
 * Room entity for cast devices
 */
@Entity(tableName = "cast_devices")
data class CastDeviceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: CastDeviceType,
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val lastConnected: Long = 0L,
    val capabilities: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
