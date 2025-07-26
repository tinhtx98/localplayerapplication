package com.tinhtx.localplayerapplication.presentation.shared.extension

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Media and audio related extension functions
 */

// Time formatting extensions
fun Long.formatDuration(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}

fun Int.formatDuration(): String = toLong().formatDuration()

fun Long.formatDurationCompact(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

fun Long.formatDurationWords(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    
    return when {
        hours > 0 && minutes > 0 -> "$hours hour${if (hours != 1L) "s" else ""} $minutes minute${if (minutes != 1L) "s" else ""}"
        hours > 0 -> "$hours hour${if (hours != 1L) "s" else ""}"
        minutes > 0 -> "$minutes minute${if (minutes != 1L) "s" else ""}"
        else -> "Less than a minute"
    }
}

// File size formatting
fun Long.formatFileSize(): String {
    if (this <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()
    
    val size = this / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", size, units[digitGroups])
}

fun Int.formatFileSize(): String = toLong().formatFileSize()

// Bitrate formatting
fun Int.formatBitrate(): String {
    return when {
        this >= 1000 -> "${this / 1000} kbps"
        else -> "$this bps"
    }
}

// Sample rate formatting
fun Int.formatSampleRate(): String {
    return when {
        this >= 1000 -> "${this / 1000} kHz"
        else -> "$this Hz"
    }
}

// Album art utilities
fun Long.getAlbumArtUri(): Uri {
    return ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        this
    )
}

fun Context.getAlbumArt(albumId: Long): Bitmap? {
    return try {
        val uri = albumId.getAlbumArtUri()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (exception: Exception) {
        null
    }
}

fun Context.getAlbumArtFromFile(filePath: String): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val art = retriever.embeddedPicture
        retriever.release()
        
        art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    } catch (exception: Exception) {
        null
    }
}

// Audio metadata extraction
fun Context.getAudioMetadata(filePath: String): AudioMetadata? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        
        val metadata = AudioMetadata(
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0,
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0,
            sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: 0,
            trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull() ?: 0
        )
        
        retriever.release()
        metadata
    } catch (exception: Exception) {
        null
    }
}

data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val year: Int,
    val genre: String?,
    val bitrate: Int,
    val sampleRate: Int,
    val trackNumber: Int
)

// Audio format detection
fun String.getAudioFormat(): AudioFormat {
    val extension = this.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "mp3" -> AudioFormat.MP3
        "flac" -> AudioFormat.FLAC
        "wav" -> AudioFormat.WAV
        "aac" -> AudioFormat.AAC
        "ogg" -> AudioFormat.OGG
        "m4a" -> AudioFormat.M4A
        "wma" -> AudioFormat.WMA
        else -> AudioFormat.UNKNOWN
    }
}

enum class AudioFormat(val displayName: String, val isLossless: Boolean) {
    MP3("MP3", false),
    FLAC("FLAC", true),
    WAV("WAV", true),
    AAC("AAC", false),
    OGG("OGG Vorbis", false),
    M4A("M4A", false),
    WMA("WMA", false),
    UNKNOWN("Unknown", false)
}

// Playlist utilities
fun List<String>.createM3UPlaylist(playlistName: String): String {
    val builder = StringBuilder()
    builder.appendLine("#EXTM3U")
    builder.appendLine("#PLAYLIST:$playlistName")
    
    forEach { filePath ->
        // Extract basic info for M3U
        val file = File(filePath)
        builder.appendLine("#EXTINF:-1,${file.nameWithoutExtension}")
        builder.appendLine(filePath)
    }
    
    return builder.toString()
}

fun Context.savePlaylistToFile(content: String, fileName: String): File? {
    return try {
        val playlistsDir = File(filesDir, "playlists")
        if (!playlistsDir.exists()) {
            playlistsDir.mkdirs()
        }
        
        val file = File(playlistsDir, "$fileName.m3u")
        FileOutputStream(file).use { output ->
            output.write(content.toByteArray())
        }
        file
    } catch (exception: Exception) {
        null
    }
}

// Image processing for album art
fun Bitmap.resizeForAlbumArt(maxSize: Int = 512): Bitmap {
    val ratio = Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height)
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()
    
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}

fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 90): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(format, quality, stream)
    return stream.toByteArray()
}

fun Bitmap.saveToFile(file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 90): Boolean {
    return try {
        FileOutputStream(file).use { output ->
            compress(format, quality, output)
        }
        true
    } catch (exception: Exception) {
        false
    }
}

// MediaStore utilities
fun Context.queryAudioFiles(): List<AudioFileInfo> {
    val audioFiles = mutableListOf<AudioFileInfo>()
    
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.TRACK
    )
    
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
    val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
    
    contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
        val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
        
        while (cursor.moveToNext()) {
            val audioFile = AudioFileInfo(
                id = cursor.getLong(idColumn),
                title = cursor.getString(titleColumn) ?: "Unknown",
                artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                album = cursor.getString(albumColumn) ?: "Unknown Album",
                duration = cursor.getLong(durationColumn),
                path = cursor.getString(dataColumn) ?: "",
                albumId = cursor.getLong(albumIdColumn),
                year = cursor.getInt(yearColumn),
                trackNumber = cursor.getInt(trackColumn)
            )
            audioFiles.add(audioFile)
        }
    }
    
    return audioFiles
}

data class AudioFileInfo(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumId: Long,
    val year: Int,
    val trackNumber: Int
)

// Audio quality assessment
fun Int.getAudioQuality(): AudioQuality {
    return when {
        this >= 320000 -> AudioQuality.VERY_HIGH
        this >= 256000 -> AudioQuality.HIGH
        this >= 192000 -> AudioQuality.MEDIUM
        this >= 128000 -> AudioQuality.LOW
        else -> AudioQuality.VERY_LOW
    }
}

enum class AudioQuality(val displayName: String, val description: String) {
    VERY_LOW("Very Low", "< 128 kbps"),
    LOW("Low", "128 kbps"),
    MEDIUM("Medium", "192 kbps"),
    HIGH("High", "256 kbps"),
    VERY_HIGH("Very High", "320+ kbps")
}

// Date formatting for media
fun Long.formatMediaDate(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.formatMediaDateDetailed(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

// Audio file validation
fun String.isValidAudioFile(): Boolean {
    val audioExtensions = setOf("mp3", "flac", "wav", "aac", "ogg", "m4a", "wma")
    val extension = this.substringAfterLast('.', "").lowercase()
    return audioExtensions.contains(extension)
}

fun File.isValidAudioFile(): Boolean = 
    exists() && isFile && name.isValidAudioFile()

fun String.sanitizeForFilename(): String {
    return this.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(Regex("\\s+"), " ")
        .trim()
}
