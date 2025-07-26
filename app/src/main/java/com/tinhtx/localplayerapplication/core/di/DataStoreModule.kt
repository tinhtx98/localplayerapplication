package com.tinhtx.localplayerapplication.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.USER_PREFERENCES_NAME
)

private val Context.settingsPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.SETTINGS_PREFERENCES_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    
    @Provides
    @Singleton
    @UserPreferencesDataStore
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.userPreferencesDataStore
    }
    
    @Provides
    @Singleton
    @SettingsPreferencesDataStore
    fun provideSettingsPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.settingsPreferencesDataStore
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsPreferencesDataStore
