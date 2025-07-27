package com.tinhtx.localplayerapplication.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * User preferences wrapper for DataStore operations
 */
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private const val TAG = "UserPreferences"
    }
    
    // User Profile Operations
    val userProfile: Flow<UserProfile> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading user profile")
            emit(emptyPreferences())
        }
        .map { preferences ->
            UserProfile(
                name = preferences[PreferencesKeys.USER_NAME] ?: "",
                email = preferences[PreferencesKeys.USER_EMAIL] ?: "",
                avatarPath = preferences[PreferencesKeys.USER_AVATAR_PATH],
                createdAt = preferences[PreferencesKeys.USER_CREATED_AT] ?: System.currentTimeMillis(),
                lastLogin = preferences[PreferencesKeys.USER_LAST_LOGIN] ?: System.currentTimeMillis()
            )
        }
    
    suspend fun updateUserProfile(userProfile: UserProfile) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_NAME] = userProfile.name
                preferences[PreferencesKeys.USER_EMAIL] = userProfile.email
                userProfile.avatarPath?.let { 
                    preferences[PreferencesKeys.USER_AVATAR_PATH] = it 
                }
                preferences[PreferencesKeys.USER_CREATED_AT] = userProfile.createdAt
                preferences[PreferencesKeys.USER_LAST_LOGIN] = userProfile.lastLogin
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating user profile")
            throw exception
        }
    }
    
    // App Settings Operations
    val appSettings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading app settings")
            emit(emptyPreferences())
        }
        .map { preferences ->
            AppSettings(
                theme = preferences[PreferencesKeys.APP_THEME]?.let { 
                    AppTheme.valueOf(it) 
                } ?: AppTheme.SYSTEM,
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                gridSize = preferences[PreferencesKeys.GRID_SIZE]?.let { 
                    GridSize.valueOf(it) 
                } ?: GridSize.MEDIUM,
                sortOrder = preferences[PreferencesKeys.SORT_ORDER]?.let { 
                    SortOrder.valueOf(it) 
                } ?: SortOrder.TITLE,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "system",
                firstLaunch = preferences[PreferencesKeys.FIRST_LAUNCH] ?: true,
                appVersion = preferences[PreferencesKeys.APP_VERSION] ?: ""
            )
        }
    
    suspend fun updateAppSettings(settings: AppSettings) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.APP_THEME] = settings.theme.name
                preferences[PreferencesKeys.DYNAMIC_COLOR] = settings.dynamicColor
                preferences[PreferencesKeys.GRID_SIZE] = settings.gridSize.name
                preferences[PreferencesKeys.SORT_ORDER] = settings.sortOrder.name
                preferences[PreferencesKeys.LANGUAGE] = settings.language
                preferences[PreferencesKeys.FIRST_LAUNCH] = settings.firstLaunch
                preferences[PreferencesKeys.APP_VERSION] = settings.appVersion
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating app settings")
            throw exception
        }
    }
    
    // Playback Settings Operations
    val playbackSettings: Flow<PlaybackSettings> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading playback settings")
            emit(emptyPreferences())
        }
        .map { preferences ->
            PlaybackSettings(
                repeatMode = preferences[PreferencesKeys.REPEAT_MODE]?.let { 
                    RepeatMode.valueOf(it) 
                } ?: RepeatMode.OFF,
                shuffleMode = preferences[PreferencesKeys.SHUFFLE_MODE]?.let { 
                    ShuffleMode.valueOf(it) 
                } ?: ShuffleMode.OFF,
                crossfadeEnabled = preferences[PreferencesKeys.CROSSFADE_ENABLED] ?: false,
                crossfadeDuration = preferences[PreferencesKeys.CROSSFADE_DURATION] ?: 3,
                playbackSpeed = preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f,
                autoPlay = preferences[PreferencesKeys.AUTO_PLAY] ?: true,
                gaplessPlayback = preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
                resumeOnHeadphones = preferences[PreferencesKeys.RESUME_ON_HEADPHONES] ?: true,
                pauseOnDisconnect = preferences[PreferencesKeys.PAUSE_ON_DISCONNECT] ?: true
            )
        }
    
    suspend fun updatePlaybackSettings(settings: PlaybackSettings) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.REPEAT_MODE] = settings.repeatMode.name
                preferences[PreferencesKeys.SHUFFLE_MODE] = settings.shuffleMode.name
                preferences[PreferencesKeys.CROSSFADE_ENABLED] = settings.crossfadeEnabled
                preferences[PreferencesKeys.CROSSFADE_DURATION] = settings.crossfadeDuration
                preferences[PreferencesKeys.PLAYBACK_SPEED] = settings.playbackSpeed
                preferences[PreferencesKeys.AUTO_PLAY] = settings.autoPlay
                preferences[PreferencesKeys.GAPLESS_PLAYBACK] = settings.gaplessPlayback
                preferences[PreferencesKeys.RESUME_ON_HEADPHONES] = settings.resumeOnHeadphones
                preferences[PreferencesKeys.PAUSE_ON_DISCONNECT] = settings.pauseOnDisconnect
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating playback settings")
            throw exception
        }
    }
    
    // Audio Settings Operations
    val audioSettings: Flow<AudioSettings> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading audio settings")
            emit(emptyPreferences())
        }
        .map { preferences ->
            AudioSettings(
                equalizerEnabled = preferences[PreferencesKeys.EQUALIZER_ENABLED] ?: false,
                equalizerPreset = preferences[PreferencesKeys.EQUALIZER_PRESET] ?: "Normal",
                equalizerBands = parseFloatList(preferences[PreferencesKeys.EQUALIZER_BANDS]),
                bassBoost = preferences[PreferencesKeys.BASS_BOOST] ?: 0,
                virtualizer = preferences[PreferencesKeys.VIRTUALIZER] ?: 0,
                loudnessEnhancer = preferences[PreferencesKeys.LOUDNESS_ENHANCER] ?: 0,
                audioFocusEnabled = preferences[PreferencesKeys.AUDIO_FOCUS_ENABLED] ?: true,
                duckVolume = preferences[PreferencesKeys.DUCK_VOLUME] ?: true
            )
        }
    
    suspend fun updateAudioSettings(settings: AudioSettings) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.EQUALIZER_ENABLED] = settings.equalizerEnabled
                preferences[PreferencesKeys.EQUALIZER_PRESET] = settings.equalizerPreset
                preferences[PreferencesKeys.EQUALIZER_BANDS] = formatFloatList(settings.equalizerBands)
                preferences[PreferencesKeys.BASS_BOOST] = settings.bassBoost
                preferences[PreferencesKeys.VIRTUALIZER] = settings.virtualizer
                preferences[PreferencesKeys.LOUDNESS_ENHANCER] = settings.loudnessEnhancer
                preferences[PreferencesKeys.AUDIO_FOCUS_ENABLED] = settings.audioFocusEnabled
                preferences[PreferencesKeys.DUCK_VOLUME] = settings.duckVolume
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating audio settings")
            throw exception
        }
    }
    
    // Library Settings Operations
    val librarySettings: Flow<LibrarySettings> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading library settings")
            emit(emptyPreferences())
        }
        .map { preferences ->
            LibrarySettings(
                autoScan = preferences[PreferencesKeys.AUTO_SCAN] ?: true,
                scanIntervalHours = preferences[PreferencesKeys.SCAN_INTERVAL_HOURS] ?: 24,
                ignoreShortTracks = preferences[PreferencesKeys.IGNORE_SHORT_TRACKS] ?: true,
                minTrackDuration = preferences[PreferencesKeys.MIN_TRACK_DURATION] ?: 30,
                includedFolders = parseStringList(preferences[PreferencesKeys.INCLUDED_FOLDERS]),
                excludedFolders = parseStringList(preferences[PreferencesKeys.EXCLUDED_FOLDERS]),
                lastScanTime = preferences[PreferencesKeys.LAST_SCAN_TIME] ?: 0L,
                librarySortOrder = preferences[PreferencesKeys.LIBRARY_SORT_ORDER]?.let { 
                    SortOrder.valueOf(it) 
                } ?: SortOrder.TITLE,
                showUnknownArtist = preferences[PreferencesKeys.SHOW_UNKNOWN_ARTIST] ?: true,
                groupByAlbumArtist = preferences[PreferencesKeys.GROUP_BY_ALBUM_ARTIST] ?: false
            )
        }
    
    suspend fun updateLibrarySettings(settings: LibrarySettings) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.AUTO_SCAN] = settings.autoScan
                preferences[PreferencesKeys.SCAN_INTERVAL_HOURS] = settings.scanIntervalHours
                preferences[PreferencesKeys.IGNORE_SHORT_TRACKS] = settings.ignoreShortTracks
                preferences[PreferencesKeys.MIN_TRACK_DURATION] = settings.minTrackDuration
                preferences[PreferencesKeys.INCLUDED_FOLDERS] = formatStringList(settings.includedFolders)
                preferences[PreferencesKeys.EXCLUDED_FOLDERS] = formatStringList(settings.excludedFolders)
                preferences[PreferencesKeys.LAST_SCAN_TIME] = settings.lastScanTime
                preferences[PreferencesKeys.LIBRARY_SORT_ORDER] = settings.librarySortOrder.name
                preferences[PreferencesKeys.SHOW_UNKNOWN_ARTIST] = settings.showUnknownArtist
                preferences[PreferencesKeys.GROUP_BY_ALBUM_ARTIST] = settings.groupByAlbumArtist
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating library settings")
            throw exception
        }
    }
    
    // Privacy Settings Operations
    val privacySettings: Flow<PrivacySettings> = dataStore.data
        .catch { exception ->
            Timber.e(exception, "$TAG - Error reading privacy settings")
            emit(emptyPreferences())
        }
        .map { preferences ->
            PrivacySettings(
                analyticsEnabled = preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true,
                crashReportingEnabled = preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] ?: true,
                usageStatistics = preferences[PreferencesKeys.USAGE_STATISTICS] ?: true,
                historyEnabled = preferences[PreferencesKeys.HISTORY_ENABLED] ?: true,
                scrobbleEnabled = preferences[PreferencesKeys.SCROBBLE_ENABLED] ?: false,
                shareNowPlaying = preferences[PreferencesKeys.SHARE_NOW_PLAYING] ?: false
            )
        }
    
    suspend fun updatePrivacySettings(settings: PrivacySettings) {
        try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ANALYTICS_ENABLED] = settings.analyticsEnabled
                preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] = settings.crashReportingEnabled
                preferences[PreferencesKeys.USAGE_STATISTICS] = settings.usageStatistics
                preferences[PreferencesKeys.HISTORY_ENABLED] = settings.historyEnabled
                preferences[PreferencesKeys.SCROBBLE_ENABLED] = settings.scrobbleEnabled
                preferences[PreferencesKeys.SHARE_NOW_PLAYING] = settings.shareNowPlaying
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error updating privacy settings")
            throw exception
        }
    }
    
    // Simple Preference Operations
    suspend fun setString(key: Preferences.Key<String>, value: String) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error setting string preference")
            throw exception
        }
    }
    
    suspend fun setBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error setting boolean preference")
            throw exception
        }
    }
    
    suspend fun setInt(key: Preferences.Key<Int>, value: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error setting int preference")
            throw exception
        }
    }
    
    suspend fun setLong(key: Preferences.Key<Long>, value: Long) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error setting long preference")
            throw exception
        }
    }
    
    suspend fun setFloat(key: Preferences.Key<Float>, value: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error setting float preference")
            throw exception
        }
    }
    
    // Get simple preferences with defaults
    fun getString(key: Preferences.Key<String>, defaultValue: String = ""): Flow<String> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }
    
    fun getBoolean(key: Preferences.Key<Boolean>, defaultValue: Boolean = false): Flow<Boolean> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }
    
    fun getInt(key: Preferences.Key<Int>, defaultValue: Int = 0): Flow<Int> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }
    
    fun getLong(key: Preferences.Key<Long>, defaultValue: Long = 0L): Flow<Long> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }
    
    fun getFloat(key: Preferences.Key<Float>, defaultValue: Float = 0f): Flow<Float> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }
    
    // Clear all preferences
    suspend fun clearAll() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error clearing all preferences")
            throw exception
        }
    }
    
    // Clear specific preference
    suspend fun <T> remove(key: Preferences.Key<T>) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(key)
            }
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error removing preference")
            throw exception
        }
    }
    
    // Utility functions
    private fun parseStringList(value: String?): List<String> {
        return if (value.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } catch (exception: Exception) {
                emptyList()
            }
        }
    }
    
    private fun formatStringList(list: List<String>): String {
        return list.joinToString(",")
    }
    
    private fun parseFloatList(value: String?): List<Float> {
        return if (value.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                value.split(",").map { it.trim().toFloat() }
            } catch (exception: Exception) {
                emptyList()
            }
        }
    }
    
    private fun formatFloatList(list: List<Float>): String {
        return list.joinToString(",")
    }
}
