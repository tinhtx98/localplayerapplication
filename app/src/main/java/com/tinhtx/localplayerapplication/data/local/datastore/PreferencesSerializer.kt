package com.tinhtx.localplayerapplication.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences()
    
    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            Json.decodeFromString(
                UserPreferences.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPreferences", serialization)
        }
    }
    
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserPreferences.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}

object SettingsPreferencesSerializer : Serializer<SettingsPreferences> {
    override val defaultValue: SettingsPreferences = SettingsPreferences()
    
    override suspend fun readFrom(input: InputStream): SettingsPreferences {
        return try {
            Json.decodeFromString(
                SettingsPreferences.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read SettingsPreferences", serialization)
        }
    }
    
    override suspend fun writeTo(t: SettingsPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(SettingsPreferences.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}
