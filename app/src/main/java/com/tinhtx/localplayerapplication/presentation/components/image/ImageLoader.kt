package com.tinhtx.localplayerapplication.presentation.components.image

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    
    fun create(): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
    
    fun createWithCustomCache(
        memoryCachePercent: Double = 0.25,
        diskCacheSizeBytes: Long = 100 * 1024 * 1024
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(memoryCachePercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskCacheSizeBytes)
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(true)
            .build()
    }
}

// Image loading utilities
object ImageLoadingUtils {
    
    fun getCacheKey(url: String, size: coil.size.Size? = null): String {
        return if (size != null) {
            "${url}_${size.width}x${size.height}"
        } else {
            url
        }
    }
    
    fun getOptimalImageSize(containerSize: androidx.compose.ui.unit.Dp): coil.size.Size {
        val pixels = (containerSize.value * 2).toInt() // 2x for high density screens
        return coil.size.Size(pixels, pixels)
    }
    
    fun isValidImageUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        
        val validExtensions = listOf("jpg", "jpeg", "png", "webp", "gif", "svg")
        val extension = url.substringAfterLast('.', "").lowercase()
        
        return validExtensions.contains(extension) || 
               url.startsWith("http") || 
               url.startsWith("content://") ||
               url.startsWith("file://")
    }
    
    fun preloadImages(
        imageLoader: ImageLoader,
        urls: List<String>,
        size: coil.size.Size = coil.size.Size.ORIGINAL
    ) {
        urls.forEach { url ->
            if (isValidImageUrl(url)) {
                val request = coil.request.ImageRequest.Builder(imageLoader.context)
                    .data(url)
                    .size(size)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                
                imageLoader.enqueue(request)
            }
        }
    }
    
    fun clearCache(imageLoader: ImageLoader) {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
    
    fun clearMemoryCache(imageLoader: ImageLoader) {
        imageLoader.memoryCache?.clear()
    }
    
    fun getCacheSize(imageLoader: ImageLoader): Pair<Long, Long> {
        val memorySize = imageLoader.memoryCache?.size ?: 0L
        val diskSize = imageLoader.diskCache?.size ?: 0L
        return Pair(memorySize, diskSize)
    }
}

// Image transformation utilities
object ImageTransformations {
    
    fun circularCrop() = coil.transform.CircleCropTransformation()
    
    fun roundedCorners(radius: Float) = coil.transform.RoundedCornersTransformation(radius)
    
    fun blur(radius: Float, sampling: Float = 1f) = 
        coil.transform.BlurTransformation(radius = radius, sampling = sampling)
    
    fun grayscale() = coil.transform.GrayscaleTransformation()
    
    // Combine multiple transformations
    fun combined(vararg transformations: coil.transform.Transformation) = 
        transformations.toList()
}

// Custom image request builders
object ImageRequestBuilder {
    
    fun albumArtwork(
        context: Context,
        url: String?,
        size: androidx.compose.ui.unit.Dp = 200.dp
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(ImageLoadingUtils.getOptimalImageSize(size))
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .transformations(ImageTransformations.roundedCorners(16f))
        .build()
    
    fun artistImage(
        context: Context,
        url: String?,
        size: androidx.compose.ui.unit.Dp = 64.dp
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(ImageLoadingUtils.getOptimalImageSize(size))
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .transformations(ImageTransformations.circularCrop())
        .build()
    
    fun playlistCover(
        context: Context,
        url: String?,
        size: androidx.compose.ui.unit.Dp = 48.dp
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(ImageLoadingUtils.getOptimalImageSize(size))
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .transformations(ImageTransformations.roundedCorners(8f))
        .build()
    
    fun thumbnail(
        context: Context,
        url: String?,
        size: androidx.compose.ui.unit.Dp = 32.dp
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(ImageLoadingUtils.getOptimalImageSize(size))
        .crossfade(false) // Disable crossfade for thumbnails for better performance
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()
    
    fun highQuality(
        context: Context,
        url: String?
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(coil.size.Size.ORIGINAL)
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .allowHardware(true)
        .build()
    
    fun lowQuality(
        context: Context,
        url: String?,
        size: androidx.compose.ui.unit.Dp = 24.dp
    ) = coil.request.ImageRequest.Builder(context)
        .data(url)
        .size(ImageLoadingUtils.getOptimalImageSize(size))
        .crossfade(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.DISABLED) // Don't cache low quality images to disk
        .allowHardware(false)
        .build()
}

// Image loading states for UI feedback
sealed class ImageLoadingState {
    object Idle : ImageLoadingState()
    object Loading : ImageLoadingState()
    data class Success(val painter: androidx.compose.ui.graphics.painter.Painter) : ImageLoadingState()
    data class Error(val throwable: Throwable) : ImageLoadingState()
}

// Image loading manager for batch operations
class ImageLoadingManager @Inject constructor(
    private val imageLoader: ImageLoader
) {
    
    suspend fun preloadAlbumArtworks(
        context: Context,
        artworkUrls: List<String?>,
        size: androidx.compose.ui.unit.Dp = 200.dp
    ) {
        artworkUrls.filterNotNull().forEach { url ->
            if (ImageLoadingUtils.isValidImageUrl(url)) {
                val request = ImageRequestBuilder.albumArtwork(context, url, size)
                imageLoader.execute(request)
            }
        }
    }
    
    suspend fun preloadArtistImages(
        context: Context,
        imageUrls: List<String?>,
        size: androidx.compose.ui.unit.Dp = 64.dp
    ) {
        imageUrls.filterNotNull().forEach { url ->
            if (ImageLoadingUtils.isValidImageUrl(url)) {
                val request = ImageRequestBuilder.artistImage(context, url, size)
                imageLoader.execute(request)
            }
        }
    }
    
    fun clearAllCaches() {
        ImageLoadingUtils.clearCache(imageLoader)
    }
    
    fun getCacheInfo(): Triple<Long, Long, Long> {
        val (memorySize, diskSize) = ImageLoadingUtils.getCacheSize(imageLoader)
        val totalSize = memorySize + diskSize
        return Triple(memorySize, diskSize, totalSize)
    }
}

// Image error handling
object ImageErrorHandler {
    
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timeout"
            is java.io.FileNotFoundException -> "Image not found"
            is coil.network.HttpException -> "Server error: ${throwable.response.code}"
            else -> "Failed to load image"
        }
    }
    
    fun shouldRetry(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> true
            is coil.network.HttpException -> throwable.response.code in 500..599
            else -> false
        }
    }
}
