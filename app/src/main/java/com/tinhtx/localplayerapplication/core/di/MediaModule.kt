package com.tinhtx.localplayerapplication.core.di

import android.content.Context
import coil.ImageLoader
import com.tinhtx.localplayerapplication.data.local.cache.AlbumArtCache
import com.tinhtx.localplayerapplication.data.local.cache.ImageCacheManager
import com.tinhtx.localplayerapplication.data.local.media.AudioMetadataExtractor
import com.tinhtx.localplayerapplication.data.local.media.MediaScanner
import com.tinhtx.localplayerapplication.data.local.media.MediaStoreScanner
import com.tinhtx.localplayerapplication.domain.service.MediaPlayerService
import com.tinhtx.localplayerapplication.presentation.service.media.MediaPlayerServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for media-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    
    // ✅ FIXED: All methods using @Provides in object
    @Provides
    @Singleton
    fun provideMediaPlayerService(): MediaPlayerService {
        return MediaPlayerServiceImpl()
    }
    
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideAlbumArtCache(
        @ApplicationContext context: Context
    ): AlbumArtCache {
        return AlbumArtCache(context)
    }

    @Provides
    @Singleton
    fun provideImageCacheManager(
        @ApplicationContext context: Context,
        imageLoader: ImageLoader
    ): ImageCacheManager {
        return ImageCacheManager(context, imageLoader)
    }

    @Provides
    @Singleton
    fun provideAudioMetadataExtractor(): AudioMetadataExtractor {
        return AudioMetadataExtractor()
    }

    @Provides
    @Singleton
    fun provideMediaScanner(
        @ApplicationContext context: Context,
        audioMetadataExtractor: AudioMetadataExtractor
    ): MediaScanner {
        return MediaScanner(context, audioMetadataExtractor)
    }

    @Provides
    @Singleton
    fun provideMediaStoreScanner(
        @ApplicationContext context: Context
    ): MediaStoreScanner {
        return MediaStoreScanner(context)
    }
}
