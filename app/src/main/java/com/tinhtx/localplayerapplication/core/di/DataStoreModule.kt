package com.tinhtx.localplayerapplication.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tinhtx.localplayerapplication.data.local.datastore.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStore module providing preference storage
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val USER_PREFERENCES_NAME = "user_preferences"
    private const val SETTINGS_PREFERENCES_NAME = "settings_preferences"
    private const val PLAYBACK_PREFERENCES_NAME = "playback_preferences"

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )

    private val Context.settingsPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = SETTINGS_PREFERENCES_NAME
    )

    private val Context.playbackPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = PLAYBACK_PREFERENCES_NAME
    )

    @Provides
    @Singleton
    @UserPreferencesDataStore
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.userPreferencesDataStore

    @Provides
    @Singleton
    @SettingsPreferencesDataStore
    fun provideSettingsPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.settingsPreferencesDataStore

    @Provides
    @Singleton
    @PlaybackPreferencesDataStore
    fun providePlaybackPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.playbackPreferencesDataStore

    @Provides
    @Singleton
    fun provideUserPreferences(
        @UserPreferencesDataStore userPreferencesDataStore: DataStore<Preferences>
    ): UserPreferences = UserPreferences(userPreferencesDataStore)
}

/**
 * Qualifier annotations for different DataStore instances
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlaybackPreferencesDataStore
