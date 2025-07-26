package com.tinhtx.localplayerapplication.data.local.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioMetadataExtractor @Inject constructor(
    private val context: Context
) {
    
    suspend fun extractMetadata(filePath: String): ExtractedMetadata? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        
        try {
            retriever.setDataSource(filePath)
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: File(filePath).nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Unknown Album"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0
            val track = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull() ?: 0
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            
            // Extract album art
            val albumArt = try {
                val artBytes = retriever.embeddedPicture
                artBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } catch (e: Exception) {
                null
            }
            
            ExtractedMetadata(
                title = title,
                artist = MediaUtils.cleanArtistName(artist),
                album = MediaUtils.cleanAlbumName(album),
                duration = duration,
                year = year,
                track = track,
                genre = genre,
                bitrate = bitrate,
                albumArt = albumArt
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
    
    suspend fun extractAlbumArt(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        
        try {
            retriever.setDataSource(filePath)
            val artBytes = retriever.embeddedPicture
            artBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
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
    
    suspend fun getDuration(filePath: String): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        
        try {
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    data class ExtractedMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val year: Int,
        val track: Int,
        val genre: String?,
        val bitrate: Int,
        val albumArt: Bitmap?
    )
}
