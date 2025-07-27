package com.tinhtx.localplayerapplication.data.local.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts metadata from audio files using MediaMetadataRetriever
 */
@Singleton
class AudioMetadataExtractor @Inject constructor() {
    
    companion object {
        private const val TAG = "AudioMetadataExtractor"
        private const val UNKNOWN_ARTIST = "Unknown Artist"
        private const val UNKNOWN_ALBUM = "Unknown Album"
        private const val UNKNOWN_GENRE = "Unknown Genre"
    }
    
    /**
     * Extract complete metadata from audio file
     */
    suspend fun extractMetadata(filePath: String): AudioMetadata? = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            val metadata = AudioMetadata(
                title = extractTitle(retriever, filePath),
                artist = extractArtist(retriever),
                album = extractAlbum(retriever),
                albumArtist = extractAlbumArtist(retriever),
                genre = extractGenre(retriever),
                year = extractYear(retriever),
                trackNumber = extractTrackNumber(retriever),
                discNumber = extractDiscNumber(retriever),
                duration = extractDuration(retriever),
                bitrate = extractBitrate(retriever),
                sampleRate = extractSampleRate(retriever),
                channels = extractChannels(retriever),
                mimeType = extractMimeType(retriever),
                composer = extractComposer(retriever),
                writer = extractWriter(retriever),
                date = extractDate(retriever),
                hasEmbeddedArtwork = hasEmbeddedArtwork(retriever)
            )
            
            Timber.d("$TAG - Extracted metadata for: ${metadata.title} by ${metadata.artist}")
            metadata
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error extracting metadata from: $filePath")
            null
        } finally {
            try {
                retriever?.release()
            } catch (exception: Exception) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Extract metadata from URI
     */
    suspend fun extractMetadata(context: Context, uri: Uri): AudioMetadata? = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val fileName = uri.getFileName(context) ?: "Unknown"
            
            val metadata = AudioMetadata(
                title = extractTitle(retriever, fileName),
                artist = extractArtist(retriever),
                album = extractAlbum(retriever),
                albumArtist = extractAlbumArtist(retriever),
                genre = extractGenre(retriever),
                year = extractYear(retriever),
                trackNumber = extractTrackNumber(retriever),
                discNumber = extractDiscNumber(retriever),
                duration = extractDuration(retriever),
                bitrate = extractBitrate(retriever),
                sampleRate = extractSampleRate(retriever),
                channels = extractChannels(retriever),
                mimeType = extractMimeType(retriever),
                composer = extractComposer(retriever),
                writer = extractWriter(retriever),
                date = extractDate(retriever),
                hasEmbeddedArtwork = hasEmbeddedArtwork(retriever)
            )
            
            metadata
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error extracting metadata from URI: $uri")
            null
        } finally {
            try {
                retriever?.release()
            } catch (exception: Exception) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Extract album artwork from audio file
     */
    suspend fun extractAlbumArt(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            } else {
                null
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error extracting album art from: $filePath")
            null
        } finally {
            try {
                retriever?.release()
            } catch (exception: Exception) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Extract album artwork from URI
     */
    suspend fun extractAlbumArt(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            } else {
                null
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error extracting album art from URI: $uri")
            null
        } finally {
            try {
                retriever?.release()
            } catch (exception: Exception) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Quick validation of audio file
     */
    suspend fun isValidAudioFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration != null && duration.toLongOrNull() != null && duration.toLong() > 0
            
        } catch (exception: Exception) {
            false
        } finally {
            try {
                retriever?.release()
            } catch (exception: Exception) {
                // Ignore release errors
            }
        }
    }
    
    /**
     * Batch extract metadata from multiple files
     */
    suspend fun extractMetadataBatch(filePaths: List<String>): List<AudioMetadata> = withContext(Dispatchers.IO) {
        val results = mutableListOf<AudioMetadata>()
        
        filePaths.forEach { filePath ->
            extractMetadata(filePath)?.let { metadata ->
                results.add(metadata)
            }
        }
        
        Timber.d("$TAG - Extracted metadata for ${results.size}/${filePaths.size} files")
        results
    }
    
    // Private extraction methods
    private fun extractTitle(retriever: MediaMetadataRetriever, filePath: String): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: File(filePath).nameWithoutExtension
    }
    
    private fun extractArtist(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: UNKNOWN_ARTIST
    }
    
    private fun extractAlbum(retriever: MediaMetadataRetriever): String {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            ?: UNKNOWN_ALBUM
    }
    
    private fun extractAlbumArtist(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
    }
    
    private fun extractGenre(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
    }
    
    private fun extractYear(retriever: MediaMetadataRetriever): Int {
        return try {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0
        } catch (exception: Exception) {
            0
        }
    }
    
    private fun extractTrackNumber(retriever: MediaMetadataRetriever): Int {
        return try {
            val trackInfo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            if (trackInfo != null) {
                // Handle formats like "1/12" or just "1"
                trackInfo.split("/").firstOrNull()?.toIntOrNull() ?: 0
            } else {
                0
            }
        } catch (exception: Exception) {
            0
        }
    }
    
    private fun extractDiscNumber(retriever: MediaMetadataRetriever): Int {
        return try {
            val discInfo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
            if (discInfo != null) {
                // Handle formats like "1/2" or just "1"
                discInfo.split("/").firstOrNull()?.toIntOrNull() ?: 1
            } else {
                1
            }
        } catch (exception: Exception) {
            1
        }
    }
    
    private fun extractDuration(retriever: MediaMetadataRetriever): Long {
        return try {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (exception: Exception) {
            0L
        }
    }
    
    private fun extractBitrate(retriever: MediaMetadataRetriever): Int {
        return try {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
        } catch (exception: Exception) {
            0
        }
    }
    
    private fun extractSampleRate(retriever: MediaMetadataRetriever): Int {
        return try {
            // Note: METADATA_KEY_CAPTURE_FRAMERATE is not the same as sample rate
            // This is a limitation of MediaMetadataRetriever
            0
        } catch (exception: Exception) {
            0
        }
    }
    
    private fun extractChannels(retriever: MediaMetadataRetriever): Int {
        return try {
            // MediaMetadataRetriever doesn't provide channel count directly
            // We would need to use MediaExtractor for this
            0
        } catch (exception: Exception) {
            0
        }
    }
    
    private fun extractMimeType(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    }
    
    private fun extractComposer(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
    }
    
    private fun extractWriter(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER)
    }
    
    private fun extractDate(retriever: MediaMetadataRetriever): String? {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
    }
    
    private fun hasEmbeddedArtwork(retriever: MediaMetadataRetriever): Boolean {
        return try {
            retriever.embeddedPicture != null
        } catch (exception: Exception) {
            false
        }
    }
    
    /**
     * Convert AudioMetadata to Song domain model
     */
    fun audioMetadataToSong(
        metadata: AudioMetadata,
        filePath: String,
        fileSize: Long,
        mediaStoreId: Long = 0L,
        albumId: Long = 0L,
        dateAdded: Long = System.currentTimeMillis(),
        dateModified: Long = File(filePath).lastModified()
    ): Song {
        return Song(
            id = 0L,
            mediaStoreId = mediaStoreId,
            title = metadata.title,
            artist = metadata.artist,
            album = metadata.album,
            albumId = albumId,
            duration = metadata.duration,
            path = filePath,
            size = fileSize,
            mimeType = metadata.mimeType ?: "audio/mpeg",
            dateAdded = dateAdded,
            dateModified = dateModified,
            year = metadata.year,
            trackNumber = metadata.trackNumber,
            genre = metadata.genre
        )
    }
}

/**
 * Audio metadata data class
 */
data class AudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int = 0,
    val trackNumber: Int = 0,
    val discNumber: Int = 1,
    val duration: Long = 0L,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val channels: Int = 0,
    val mimeType: String? = null,
    val composer: String? = null,
    val writer: String? = null,
    val date: String? = null,
    val hasEmbeddedArtwork: Boolean = false
) {
    val formattedDuration: String get() = duration.formatDuration()
    val bitrateKbps: String get() = if (bitrate > 0) "${bitrate / 1000} kbps" else "Unknown"
    val sampleRateKhz: String get() = if (sampleRate > 0) "${sampleRate / 1000} kHz" else "Unknown"
    val channelInfo: String get() = when (channels) {
        1 -> "Mono"
        2 -> "Stereo"
        else -> if (channels > 0) "${channels}ch" else "Unknown"
    }
}

/**
 * Extension function for Uri
 */
private fun Uri.getFileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) cursor.getString(nameIndex) else null
        } else null
    }
}

/**
 * Extension function for duration formatting
 */
private fun Long.formatDuration(): String {
    val hours = this / 3600000
    val minutes = (this % 3600000) / 60000
    val seconds = (this % 60000) / 1000
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
