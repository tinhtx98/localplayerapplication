package com.tinhtx.localplayerapplication.data.local.datastore

import androidx.datastore.core.Serializer
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for complex preferences objects using JSON
 */
class PreferencesSerializer<T>(
    private val clazz: Class<T>,
    private val defaultValue: T
) : Serializer<T> {
    
    companion object {
        private const val TAG = "PreferencesSerializer"
    }
    
    private val gson = Gson()
    
    override val defaultValue: T = defaultValue
    
    override suspend fun readFrom(input: InputStream): T = withContext(Dispatchers.IO) {
        try {
            val json = input.readBytes().toString(Charsets.UTF_8)
            if (json.isBlank()) {
                return@withContext defaultValue
            }
            gson.fromJson(json, clazz) ?: defaultValue
        } catch (exception: JsonSyntaxException) {
            Timber.e(exception, "$TAG - Error parsing JSON for ${clazz.simpleName}")
            defaultValue
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error reading preferences for ${clazz.simpleName}")
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: T, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(t)
            output.write(json.toByteArray(Charsets.UTF_8))
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error writing preferences for ${clazz.simpleName}")
            throw exception
        }
    }
}

/**
 * Serializer for List<String> preferences
 */
class StringListSerializer(
    private val defaultValue: List<String> = emptyList()
) : Serializer<List<String>> {
    
    private val gson = Gson()
    
    override val defaultValue: List<String> = defaultValue
    
    override suspend fun readFrom(input: InputStream): List<String> = withContext(Dispatchers.IO) {
        try {
            val json = input.readBytes().toString(Charsets.UTF_8)
            if (json.isBlank()) {
                return@withContext defaultValue
            }
            gson.fromJson(json, Array<String>::class.java)?.toList() ?: defaultValue
        } catch (exception: Exception) {
            Timber.e(exception, "StringListSerializer - Error reading string list")
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: List<String>, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(t.toTypedArray())
            output.write(json.toByteArray(Charsets.UTF_8))
        } catch (exception: Exception) {
            Timber.e(exception, "StringListSerializer - Error writing string list")
            throw exception
        }
    }
}

/**
 * Serializer for Map<String, String> preferences
 */
class StringMapSerializer(
    private val defaultValue: Map<String, String> = emptyMap()
) : Serializer<Map<String, String>> {
    
    private val gson = Gson()
    
    override val defaultValue: Map<String, String> = defaultValue
    
    override suspend fun readFrom(input: InputStream): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val json = input.readBytes().toString(Charsets.UTF_8)
            if (json.isBlank()) {
                return@withContext defaultValue
            }
            
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(json, Map::class.java) as? Map<String, String> ?: defaultValue
        } catch (exception: Exception) {
            Timber.e(exception, "StringMapSerializer - Error reading string map")
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: Map<String, String>, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(t)
            output.write(json.toByteArray(Charsets.UTF_8))
        } catch (exception: Exception) {
            Timber.e(exception, "StringMapSerializer - Error writing string map")
            throw exception
        }
    }
}

/**
 * Serializer for List<Long> preferences (for song IDs, etc.)
 */
class LongListSerializer(
    private val defaultValue: List<Long> = emptyList()
) : Serializer<List<Long>> {
    
    private val gson = Gson()
    
    override val defaultValue: List<Long> = defaultValue
    
    override suspend fun readFrom(input: InputStream): List<Long> = withContext(Dispatchers.IO) {
        try {
            val json = input.readBytes().toString(Charsets.UTF_8)
            if (json.isBlank()) {
                return@withContext defaultValue
            }
            gson.fromJson(json, Array<Long>::class.java)?.toList() ?: defaultValue
        } catch (exception: Exception) {
            Timber.e(exception, "LongListSerializer - Error reading long list")
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: List<Long>, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(t.toTypedArray())
            output.write(json.toByteArray(Charsets.UTF_8))
        } catch (exception: Exception) {
            Timber.e(exception, "LongListSerializer - Error writing long list")
            throw exception
        }
    }
}

/**
 * Serializer for List<Float> preferences (for equalizer bands)
 */
class FloatListSerializer(
    private val defaultValue: List<Float> = emptyList()
) : Serializer<List<Float>> {
    
    private val gson = Gson()
    
    override val defaultValue: List<Float> = defaultValue
    
    override suspend fun readFrom(input: InputStream): List<Float> = withContext(Dispatchers.IO) {
        try {
            val json = input.readBytes().toString(Charsets.UTF_8)
            if (json.isBlank()) {
                return@withContext defaultValue
            }
            gson.fromJson(json, Array<Float>::class.java)?.toList() ?: defaultValue
        } catch (exception: Exception) {
            Timber.e(exception, "FloatListSerializer - Error reading float list")
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: List<Float>, output: OutputStream) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(t.toTypedArray())
            output.write(json.toByteArray(Charsets.UTF_8))
        } catch (exception: Exception) {
            Timber.e(exception, "FloatListSerializer - Error writing float list")
            throw exception
        }
    }
}

/**
 * Utility functions for creating serializers
 */
object SerializerFactory {
    
    inline fun <reified T> create(defaultValue: T): PreferencesSerializer<T> {
        return PreferencesSerializer(T::class.java, defaultValue)
    }
    
    fun createStringList(defaultValue: List<String> = emptyList()): StringListSerializer {
        return StringListSerializer(defaultValue)
    }
    
    fun createStringMap(defaultValue: Map<String, String> = emptyMap()): StringMapSerializer {
        return StringMapSerializer(defaultValue)
    }
    
    fun createLongList(defaultValue: List<Long> = emptyList()): LongListSerializer {
        return LongListSerializer(defaultValue)
    }
    
    fun createFloatList(defaultValue: List<Float> = emptyList()): FloatListSerializer {
        return FloatListSerializer(defaultValue)
    }
}
