package com.tinhtx.localplayerapplication.core.di

import android.content.Context
import androidx.room.Room
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.local.database.LocalPlayerDatabase
import com.tinhtx.localplayerapplication.data.local.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLocalPlayerDatabase(
        @ApplicationContext context: Context
    ): LocalPlayerDatabase {
        return LocalPlayerDatabase.create(context)
    }

    @Provides
    fun provideSongDao(database: LocalPlayerDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideAlbumDao(database: LocalPlayerDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideArtistDao(database: LocalPlayerDatabase): ArtistDao {
        return database.artistDao()
    }

    @Provides
    fun providePlaylistDao(database: LocalPlayerDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideFavoriteDao(database: LocalPlayerDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideHistoryDao(database: LocalPlayerDatabase): HistoryDao {
        return database.historyDao()
    }

    // 🆕 Added Cast Device DAO
    @Provides
    fun provideCastDeviceDao(database: LocalPlayerDatabase): CastDeviceDao {
        return database.castDeviceDao()
    }
}
