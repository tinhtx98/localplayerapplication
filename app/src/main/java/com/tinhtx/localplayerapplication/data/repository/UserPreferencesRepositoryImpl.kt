package com.tinhtx.localplayerapplication.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.di.SettingsPreferencesDataStore
import com.tinhtx.localplayerapplication.core.di.UserPreferencesDataStore
import com.tinhtx.localplayerapplication.data.local.datastore.PreferencesKeys
import com.tinhtx.localplayerapplication.data.local.datastore.SettingsPreferences
import com.tinhtx.localplayerapplication.data.local.datastore.UserPreferences
import com.tinhtx.localplayerapplication.domain.model.UserProfile
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @UserPreferencesDataStore private val userPreferencesDataStore: DataStore<Preferences>,
    @SettingsPreferencesDataStore private val settingsPreferencesDataStore: DataStore<Preferences>
) : UserPreferencesRepository {
    
    override val userProfile: Flow<UserProfile> = userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserProfile(
                name = preferences[PreferencesKeys.USER_NAME] ?: AppConstants.DEFAULT_USER_NAME,
                profileImageUri = preferences[PreferencesKeys.PROFILE_IMAGE_URI] ?: "",
                themeMode = AppConstants.ThemeMode.valueOf(
                    preferences[PreferencesKeys.THEME_MODE] ?: AppConstants.ThemeMode.SYSTEM.name
                ),
                isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
            )
        }
    
    override suspend fun updateUserName(name: String) {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
    
    override suspend fun updateProfileImage(uri: String) {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_IMAGE_URI] = uri
        }
    }
    
    override suspend fun updateThemeMode(themeMode: AppConstants.ThemeMode) {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }
    
    override suspend fun setFirstLaunchCompleted() {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
    
    override suspend fun updateLastScanTime(timestamp: Long) {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SCAN_TIME] = timestamp
        }
    }
    
    override suspend fun setAutoScanEnabled(enabled: Boolean) {
        userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCAN_ENABLED] = enabled
        }
    }
    
    override val settingsPreferences: Flow<SettingsPreferences> = settingsPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsPreferences(
                audioQuality = preferences[PreferencesKeys.AUDIO_QUALITY] ?: "HIGH",
                crossfadeEnabled = preferences[PreferencesKeys.CROSSFADE_ENABLED] ?: false,
                crossfadeDuration = preferences[PreferencesKeys.CROSSFADE_DURATION] ?: 3000,
                gaplessPlayback = preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
                repeatMode = preferences[PreferencesKeys.REPEAT_MODE] ?: 0,
                shuffleMode = preferences[PreferencesKeys.SHUFFLE_MODE] ?: 0,
                volumeLevel = preferences[PreferencesKeys.VOLUME_LEVEL] ?: 1.0f,
                bassBoostEnabled = preferences[PreferencesKeys.BASS_BOOST_ENABLED] ?: false,
                bassBoostStrength = preferences[PreferencesKeys.BASS_BOOST_STRENGTH] ?: 500,
                virtualizerEnabled = preferences[PreferencesKeys.VIRTUALIZER_ENABLED] ?: false,
                virtualizerStrength = preferences[PreferencesKeys.VIRTUALIZER_STRENGTH] ?: 500,
                sleepTimerDuration = preferences[PreferencesKeys.SLEEP_TIMER_DURATION] ?: 0,
                headphoneAutoPlay = preferences[PreferencesKeys.HEADPHONE_AUTO_PLAY] ?: true,
                headphoneAutoPause = preferences[PreferencesKeys.HEADPHONE_AUTO_PAUSE] ?: true,
                showNotification = preferences[PreferencesKeys.SHOW_NOTIFICATION] ?: true,
                showLockScreenControls = preferences[PreferencesKeys.SHOW_LOCK_SCREEN_CONTROLS] ?: true,
                libraryTabOrder = try {
                    Json.decodeFromString<List<String>>(
                        preferences[PreferencesKeys.LIBRARY_TAB_ORDER] ?: "[]"
                    ).ifEmpty { listOf("SONGS", "ALBUMS", "ARTISTS") }
                } catch (e: Exception) {
                    listOf("SONGS", "ALBUMS", "ARTISTS")
                },
                gridSize = preferences[PreferencesKeys.GRID_SIZE] ?: 2,
                sortOrder = preferences[PreferencesKeys.SORT_ORDER] ?: "TITLE_ASC"
            )
        }
    
    override suspend fun updateAudioQuality(quality: String) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality
        }
    }
    
    override suspend fun updateCrossfadeSettings(enabled: Boolean, duration: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CROSSFADE_ENABLED] = enabled
            preferences[PreferencesKeys.CROSSFADE_DURATION] = duration
        }
    }
    
    override suspend fun updateGaplessPlayback(enabled: Boolean) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.GAPLESS_PLAYBACK] = enabled
        }
    }
    
    override suspend fun updateRepeatMode(mode: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] = mode
        }
    }
    
    override suspend fun updateShuffleMode(mode: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_MODE] = mode
        }
    }
    
    override suspend fun updateVolumeLevel(level: Float) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.VOLUME_LEVEL] = level
        }
    }
    
    override suspend fun updateBassBoostSettings(enabled: Boolean, strength: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.BASS_BOOST_ENABLED] = enabled
            preferences[PreferencesKeys.BASS_BOOST_STRENGTH] = strength
        }
    }
    
    override suspend fun updateVirtualizerSettings(enabled: Boolean, strength: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.VIRTUALIZER_ENABLED] = enabled
            preferences[PreferencesKeys.VIRTUALIZER_STRENGTH] = strength
        }
    }
    
    override suspend fun updateSleepTimerDuration(duration: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_TIMER_DURATION] = duration
        }
    }
    
    override suspend fun updateHeadphoneSettings(autoPlay: Boolean, autoPause: Boolean) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.HEADPHONE_AUTO_PLAY] = autoPlay
            preferences[PreferencesKeys.HEADPHONE_AUTO_PAUSE] = autoPause
        }
    }
    
    override suspend fun updateNotificationSettings(showNotification: Boolean, showLockScreen: Boolean) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_NOTIFICATION] = showNotification
            preferences[PreferencesKeys.SHOW_LOCK_SCREEN_CONTROLS] = showLockScreen
        }
    }
    
    override suspend fun updateLibraryTabOrder(tabOrder: List<String>) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LIBRARY_TAB_ORDER] = Json.encodeToString(tabOrder)
        }
    }
    
    override suspend fun updateGridSize(size: Int) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_SIZE] = size
        }
    }
    
    override suspend fun updateSortOrder(sortOrder: String) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder
        }
    }
    
    override suspend fun savePlaybackState(
        songId: Long,
        position: Long,
        playlistId: Long?,
        queueSongIds: List<Long>,
        queueIndex: Int
    ) {
        settingsPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_SONG_ID] = songId
            preferences[PreferencesKeys.CURRENT_POSITION] = position
            playlistId?.let { preferences[PreferencesKeys.CURRENT_PLAYLIST_ID] = it }
            preferences[PreferencesKeys.QUEUE_SONG_IDS] = Json.encodeToString(queueSongIds)
            preferences[PreferencesKeys.QUEUE_INDEX] = queueIndex
        }
    }
    
    override suspend fun getPlaybackState(): Flow<PlaybackState?> {
        return settingsPreferencesDataStore.data.map { preferences ->
            val songId = preferences[PreferencesKeys.CURRENT_SONG_ID]
            if (songId != null) {
                PlaybackState(
                    songId = songId,
                    position = preferences[PreferencesKeys.CURRENT_POSITION] ?: 0L,
                    playlistId = preferences[PreferencesKeys.CURRENT_PLAYLIST_ID],
                    queueSongIds = try {
                        Json.decodeFromString<List<Long>>(
                            preferences[PreferencesKeys.QUEUE_SONG_IDS] ?: "[]"
                        )
                    } catch (e: Exception) {
                        emptyList()
                    },
                    queueIndex = preferences[PreferencesKeys.QUEUE_INDEX] ?: 0
                )
            } else {
                null
            }
        }
    }
    
    data class PlaybackState(
        val songId: Long,
        val position: Long,
        val playlistId: Long?,
        val queueSongIds: List<Long>,
        val queueIndex: Int
    )
}
