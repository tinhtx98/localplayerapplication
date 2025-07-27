package com.tinhtx.localplayerapplication.data.local.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room type converters for complex data types
 */
class Converters {

    private val gson = Gson()

    // String List Converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    // Long List Converters
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Long>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    // Int List Converters
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    // Float List Converters (for equalizer bands)
    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Float>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    // Map Converters (for analytics data)
    @TypeConverter
    fun fromStringToStringMap(value: Map<String, String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringToStringMap(value: String?): Map<String, String>? {
        return if (value == null) null else {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(value, mapType)
        }
    }

    @TypeConverter
    fun fromStringToAnyMap(value: Map<String, Any>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringToAnyMap(value: String?): Map<String, Any>? {
        return if (value == null) null else {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(value, mapType)
        }
    }

    // Enum Converters for AppSettings
    @TypeConverter
    fun fromAppTheme(theme: com.tinhtx.localplayerapplication.domain.model.AppTheme?): String? {
        return theme?.name
    }

    @TypeConverter
    fun toAppTheme(theme: String?): com.tinhtx.localplayerapplication.domain.model.AppTheme? {
        return theme?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.AppTheme.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.AppTheme.SYSTEM
            }
        }
    }

    @TypeConverter
    fun fromGridSize(size: com.tinhtx.localplayerapplication.domain.model.GridSize?): String? {
        return size?.name
    }

    @TypeConverter
    fun toGridSize(size: String?): com.tinhtx.localplayerapplication.domain.model.GridSize? {
        return size?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.GridSize.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.GridSize.MEDIUM
            }
        }
    }

    @TypeConverter
    fun fromSortOrder(order: com.tinhtx.localplayerapplication.domain.model.SortOrder?): String? {
        return order?.name
    }

    @TypeConverter
    fun toSortOrder(order: String?): com.tinhtx.localplayerapplication.domain.model.SortOrder? {
        return order?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.SortOrder.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.SortOrder.TITLE
            }
        }
    }

    @TypeConverter
    fun fromRepeatMode(mode: com.tinhtx.localplayerapplication.domain.model.RepeatMode?): String? {
        return mode?.name
    }

    @TypeConverter
    fun toRepeatMode(mode: String?): com.tinhtx.localplayerapplication.domain.model.RepeatMode? {
        return mode?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.RepeatMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF
            }
        }
    }

    @TypeConverter
    fun fromShuffleMode(mode: com.tinhtx.localplayerapplication.domain.model.ShuffleMode?): String? {
        return mode?.name
    }

    @TypeConverter
    fun toShuffleMode(mode: String?): com.tinhtx.localplayerapplication.domain.model.ShuffleMode? {
        return mode?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.ShuffleMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.ShuffleMode.OFF
            }
        }
    }

    // Audio Focus State Converter
    @TypeConverter
    fun fromAudioFocusState(state: com.tinhtx.localplayerapplication.domain.model.AudioFocusState?): String? {
        return state?.name
    }

    @TypeConverter
    fun toAudioFocusState(state: String?): com.tinhtx.localplayerapplication.domain.model.AudioFocusState? {
        return state?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.AudioFocusState.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.AudioFocusState.NONE
            }
        }
    }

    // PlaybackState Converter (if stored in database)
    @TypeConverter
    fun fromPlaybackState(state: com.tinhtx.localplayerapplication.domain.model.PlaybackState?): String? {
        return state?.name
    }

    @TypeConverter
    fun toPlaybackState(state: String?): com.tinhtx.localplayerapplication.domain.model.PlaybackState? {
        return state?.let { 
            try {
                com.tinhtx.localplayerapplication.domain.model.PlaybackState.valueOf(it)
            } catch (e: IllegalArgumentException) {
                com.tinhtx.localplayerapplication.domain.model.PlaybackState.STOPPED
            }
        }
    }

    // Generic JSON Object Converter
    @TypeConverter
    fun fromJsonObject(value: Any?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toJsonObject(value: String?): Any? {
        return if (value == null) null else {
            try {
                gson.fromJson(value, Any::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    // ByteArray Converter (for album art thumbnails if stored in DB)
    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? {
        return if (value == null) null else {
            android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
        }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return if (value == null) null else {
            try {
                android.util.Base64.decode(value, android.util.Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
