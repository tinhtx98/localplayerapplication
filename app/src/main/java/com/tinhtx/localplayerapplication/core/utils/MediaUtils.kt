package com.tinhtx.localplayerapplication.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility functions for media operations
 */
object MediaUtils {

    /**
     * Check if file extension is supported audio format
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
     * Extract album art from audio file
     */
    fun extractAlbumArt(filePath: String): Bitmap? {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Extract album art from URI
     */
    fun extractAlbumArt(context: Context, uri: Uri): Bitmap? {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val art = retriever.embeddedPicture
            art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Get audio duration from file path
     */
    fun getAudioDuration(filePath: String): Long {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Extract metadata from audio file
     */
    data class AudioMetadata(
        val title: String?,
        val artist: String?,
        val album: String?,
        val duration: Long,
        val year: String?,
        val genre: String?,
        val trackNumber: String?,
        val albumArtist: String?
    )

    fun extractMetadata(filePath: String): AudioMetadata {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            AudioMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            )
        } catch (e: Exception) {
            AudioMetadata(null, null, null, 0L, null, null, null, null)
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Get album art URI from MediaStore
     */
    fun getAlbumArtUri(albumId: Long): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }

    /**
     * Save bitmap to cache directory
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val cacheDir = File(context.cacheDir, "album_art")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val file = File(cacheDir, "$fileName.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Load bitmap from cache
     */
    fun loadBitmapFromCache(context: Context, fileName: String): Bitmap? {
        return try {
            val cacheDir = File(context.cacheDir, "album_art")
            val file = File(cacheDir, "$fileName.jpg")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear album art cache
     */
    fun clearAlbumArtCache(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "album_art")
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Get file size in bytes
     */
    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get file extension
     */
    fun getFileExtension(filePath: String): String {
        return File(filePath).extension.lowercase()
    }

    /**
     * Generate unique file name based on metadata
     */
    fun generateCacheKey(artist: String?, album: String?, title: String?): String {
        val cleanArtist = artist?.replace(Regex("[^a-zA-Z0-9]"), "") ?: "unknown"
        val cleanAlbum = album?.replace(Regex("[^a-zA-Z0-9]"), "") ?: "unknown"
        val cleanTitle = title?.replace(Regex("[^a-zA-Z0-9]"), "") ?: "unknown"
        return "${cleanArtist}_${cleanAlbum}_${cleanTitle}".take(50)
    }

    /**
     * Convert milliseconds to readable time format
     */
    fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Validate audio file integrity
     */
    fun validateAudioFile(filePath: String): Boolean {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration != null && duration.toLongOrNull() != null && duration.toLong() > 0
        } catch (e: Exception) {
            false
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Get audio bitrate
     */
    fun getAudioBitrate(filePath: String): Int {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            bitrate?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
