package com.tinhtx.localplayerapplication.data.local.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import com.tinhtx.localplayerapplication.core.di.IoDispatcher
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheManager @Inject constructor(
    private val context: Context,
    private val imageLoader: ImageLoader,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    private val cacheDir by lazy {
        File(context.cacheDir, "album_art").apply {
            if (!exists()) mkdirs()
        }
    }
    
    suspend fun getAlbumArt(
        albumId: Long,
        filePath: String? = null
    ): Bitmap? = withContext(ioDispatcher) {
        // Try to get from cache first
        getCachedAlbumArt(albumId)?.let { return@withContext it }
        
        // Extract from file if path provided
        filePath?.let { path ->
            MediaUtils.extractAlbumArt(path)?.let { bitmap ->
                cacheAlbumArt(albumId, bitmap)
                return@withContext bitmap
            }
        }
        
        // Try to get from MediaStore
        val albumArtUri = MediaUtils.getAlbumArtUri(albumId)
        try {
            val request = ImageRequest.Builder(context)
                .data(albumArtUri)
                .size(MediaConstants.ALBUM_ART_SIZE)
                .build()
            
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                bitmap?.let { 
                    cacheAlbumArt(albumId, it)
                    return@withContext it
                }
            }
        } catch (e: Exception) {
            // Fallback to default
        }
        
        null
    }
    
    private suspend fun getCachedAlbumArt(albumId: Long): Bitmap? = withContext(ioDispatcher) {
        val cacheFile = File(cacheDir, "album_$albumId.jpg")
        if (cacheFile.exists()) {
            try {
                return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
            } catch (e: Exception) {
                cacheFile.delete()
            }
        }
        null
    }
    
    private suspend fun cacheAlbumArt(
        albumId: Long, 
        bitmap: Bitmap
    ) = withContext(ioDispatcher) {
        try {
            val cacheFile = File(cacheDir, "album_$albumId.jpg")
            val outputStream = FileOutputStream(cacheFile)
            bitmap.compress(
                Bitmap.CompressFormat.JPEG, 
                MediaConstants.ALBUM_ART_QUALITY, 
                outputStream
            )
            outputStream.close()
        } catch (e: Exception) {
            // Ignore cache errors
        }
    }
    
    suspend fun preloadAlbumArt(albumIds: List<Long>) = withContext(ioDispatcher) {
        albumIds.forEach { albumId ->
            if (!isCached(albumId)) {
                getAlbumArt(albumId)
            }
        }
    }
    
    private fun isCached(albumId: Long): Boolean {
        val cacheFile = File(cacheDir, "album_$albumId.jpg")
        return cacheFile.exists()
    }
    
    suspend fun clearCache() = withContext(ioDispatcher) {
        try {
            cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    suspend fun getCacheSize(): Long = withContext(ioDispatcher) {
        try {
            cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    suspend fun cleanOldCache(maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L) = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
