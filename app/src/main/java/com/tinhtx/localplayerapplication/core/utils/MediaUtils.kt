package com.tinhtx.localplayerapplication.core.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object MediaUtils {
    
    /**
     * Get album art URI from MediaStore
     */
    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
    
    /**
     * Extract album art from audio file
     */
    suspend fun extractAlbumArt(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            if (art != null) {
                BitmapFactory.decodeByteArray(art, 0, art.size)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    /**
     * Get audio file metadata
     */
    suspend fun getAudioMetadata(filePath: String): AudioMetadata? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0
            val track = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull() ?: 0
            
            AudioMetadata(
                title = title ?: File(filePath).nameWithoutExtension,
                artist = artist ?: "Unknown Artist",
                album = album ?: "Unknown Album",
                duration = duration,
                year = year,
                track = track
            )
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    /**
     * Check if file is supported audio format
     */
    fun isSupportedAudioFile(filePath: String): Boolean {
        val extension = File(filePath).extension.lowercase()
        return MediaConstants.SUPPORTED_AUDIO_FORMATS.contains(extension)
    }
    
    /**
     * Check if MIME type is supported
     */
    fun isSupportedMimeType(mimeType: String?): Boolean {
        return mimeType != null && MediaConstants.SUPPORTED_MIME_TYPES.contains(mimeType)
    }
    
    /**
     * Generate unique media ID
     */
    fun generateMediaId(songId: Long): String {
        return "song_$songId"
    }
    
    /**
     * Parse media ID to get song ID
     */
    fun parseMediaId(mediaId: String): Long? {
        return try {
            if (mediaId.startsWith("song_")) {
                mediaId.removePrefix("song_").toLong()
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    /**
     * Format file size
     */
    fun formatFileSize(sizeInBytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            sizeInBytes >= gb -> String.format("%.1f GB", sizeInBytes / gb)
            sizeInBytes >= mb -> String.format("%.1f MB", sizeInBytes / mb)
            sizeInBytes >= kb -> String.format("%.1f KB", sizeInBytes / kb)
            else -> "$sizeInBytes B"
        }
    }
    
    /**
     * Save bitmap to cache directory
     */
    suspend fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "album_art")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val file = File(cacheDir, "$fileName.jpg")
        
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, MediaConstants.ALBUM_ART_QUALITY, stream)
            val bitmapData = stream.toByteArray()
            
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bitmapData)
            fileOutputStream.flush()
            fileOutputStream.close()
            
            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * Get audio session ID for equalizer
     */
    fun getAudioSessionId(context: Context): Int {
        // This would typically come from the media player
        return 0
    }
    
    /**
     * Calculate seek position percentage
     */
    fun calculateSeekPercentage(currentPosition: Long, duration: Long): Float {
        if (duration <= 0) return 0f
        return (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate position from percentage
     */
    fun calculatePositionFromPercentage(percentage: Float, duration: Long): Long {
        return (percentage * duration).toLong().coerceIn(0L, duration)
    }
    
    /**
     * Check if audio file exists and is readable
     */
    fun isAudioFileValid(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.canRead() && file.length() > 0
    }
    
    /**
     * Get content URI from file path
     */
    fun getContentUri(filePath: String): Uri? {
        return try {
            File(filePath).toUri()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clean up artist name (remove "Various Artists", etc.)
     */
    fun cleanArtistName(artist: String?): String {
        return when {
            artist.isNullOrBlank() -> "Unknown Artist"
            artist.equals("Various Artists", ignoreCase = true) -> "Various Artists"
            artist.equals("<unknown>", ignoreCase = true) -> "Unknown Artist"
            else -> artist.trim()
        }
    }
    
    /**
     * Clean up album name
     */
    fun cleanAlbumName(album: String?): String {
        return when {
            album.isNullOrBlank() -> "Unknown Album"
            album.equals("<unknown>", ignoreCase = true) -> "Unknown Album"
            else -> album.trim()
        }
    }
    
    data class AudioMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val year: Int,
        val track: Int
    )
}
