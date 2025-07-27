package com.tinhtx.localplayerapplication.core.di

import android.content.Context
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.tinhtx.localplayerapplication.BuildConfig
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.local.cache.AlbumArtCache
import com.tinhtx.localplayerapplication.data.local.cache.ImageCacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Image loading and caching module
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient { okHttpClient }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(AppConstants.MAX_CACHE_SIZE_MB * 1024 * 1024)
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .crossfade(300)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideAlbumArtCache(
        @ApplicationContext context: Context
    ): AlbumArtCache = AlbumArtCache(context)

    @Provides
    @Singleton
    fun provideImageCacheManager(
        @ApplicationContext context: Context,
        imageLoader: ImageLoader
    ): ImageCacheManager = ImageCacheManager(context, imageLoader)

    @Provides
    @Singleton
    fun provideOkHttpClientForImages(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "LocalPlayer/${BuildConfig.VERSION_NAME}")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
}
