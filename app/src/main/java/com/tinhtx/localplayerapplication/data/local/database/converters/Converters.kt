package com.tinhtx.localplayerapplication.data.local.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            Json.decodeFromString<List<Long>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
