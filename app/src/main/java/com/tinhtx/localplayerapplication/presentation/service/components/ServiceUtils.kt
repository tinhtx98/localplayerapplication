package com.tinhtx.localplayerapplication.data.service.components

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.data.service.RepeatMode
import java.util.concurrent.TimeUnit

object ServiceUtils {
    
    const val SERVICE_TAG = "MusicService"
    const val MEDIA_SESSION_TAG = "LocalPlayerMediaSession"
    const val NOTIFICATION_CHANNEL_ID = "music_playback_channel"
    const val NOTIFICATION_ID = 1001
    
    // Intent Actions
    const val ACTION_PLAY = "com.tinhtx.localplayerapplication.PLAY"
    const val ACTION_PAUSE = "com.tinhtx.localplayerapplication.PAUSE"
    const val ACTION_SKIP_NEXT = "com.tinhtx.localplayerapplication.SKIP_NEXT"
    const val ACTION_SKIP_PREVIOUS = "com.tinhtx.localplayerapplication.SKIP_PREVIOUS"
    const val ACTION_STOP = "com.tinhtx.localplayerapplication.STOP"
    const val ACTION_TOGGLE_REPEAT = "com.tinhtx.localplayerapplication.TOGGLE_REPEAT"
    const val ACTION_TOGGLE_SHUFFLE = "com.tinhtx.localplayerapplication.TOGGLE_SHUFFLE"
    const val ACTION_SEEK_TO = "com.tinhtx.localplayerapplication.SEEK_TO"
    
    // Intent Extras
    const val EXTRA_SONG = "extra_song"
    const val EXTRA_QUEUE = "extra_queue"
    const val EXTRA_POSITION = "extra_position"
    const val EXTRA_SEEK_POSITION = "extra_seek_position"
    
    // Playback State Actions
    const val PLAYBACK_ACTIONS = (
        PlaybackStateCompat.ACTION_PLAY or
        PlaybackStateCompat.ACTION_PAUSE or
        PlaybackStateCompat.ACTION_PLAY_PAUSE or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
        PlaybackStateCompat.ACTION_STOP or
        PlaybackStateCompat.ACTION_SEEK_TO or
        PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
        PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
    )
    
    /**
     * Convert Song to MediaMetadataCompat
     */
    fun Song.toMediaMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artworkPath)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, year.toLong())
            .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "$artist • $album")
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
            .build()
    }
    
    /**
     * Convert RepeatMode to PlaybackStateCompat repeat mode
     */
    fun RepeatMode.toPlaybackStateRepeatMode(): Int {
        return when (this) {
            RepeatMode.OFF -> PlaybackStateCompat.REPEAT_MODE_NONE
            RepeatMode.ALL -> PlaybackStateCompat.REPEAT_MODE_ALL
            RepeatMode.ONE -> PlaybackStateCompat.REPEAT_MODE_ONE
        }
    }
    
    /**
     * Convert PlaybackStateCompat repeat mode to RepeatMode
     */
    fun Int.toRepeatMode(): RepeatMode {
        return when (this) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> RepeatMode.OFF
            PlaybackStateCompat.REPEAT_MODE_ALL -> RepeatMode.ALL
            PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    }
    
    /**
     * Format duration to mm:ss format
     */
    fun formatDuration(durationMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Check if device supports audio focus
     */
    fun Context.supportsAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.isAudioFocusRestricted.not()
        } else {
            true
        }
    }
    
    /**
     * Create service intent with action
     */
    fun Context.createServiceIntent(action: String): Intent {
        return Intent(this, com.tinhtx.localplayerapplication.data.service.MusicService::class.java).apply {
            this.action = action
        }
    }
    
    /**
     * Create play intent with song
     */
    fun Context.createPlayIntent(song: Song): Intent {
        return createServiceIntent(ACTION_PLAY).apply {
            putExtra(EXTRA_SONG, song)
        }
    }
    
    /**
     * Create queue intent
     */
    fun Context.createQueueIntent(
        songs: ArrayList<Song>, 
        position: Int = 0
    ): Intent {
        return createServiceIntent(ACTION_PLAY).apply {
            putParcelableArrayListExtra(EXTRA_QUEUE, songs)
            putExtra(EXTRA_POSITION, position)
        }
    }
    
    /**
     * Create seek intent
     */
    fun Context.createSeekIntent(position: Long): Intent {
        return createServiceIntent(ACTION_SEEK_TO).apply {
            putExtra(EXTRA_SEEK_POSITION, position)
        }
    }
    
    /**
     * Check if service is running
     */
    fun Context.isMusicServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) 
            as android.app.ActivityManager
        
        return activityManager.getRunningServices(Integer.MAX_VALUE)
            .any { 
                it.service.className == com.tinhtx.localplayerapplication.data.service.MusicService::class.java.name 
            }
    }
    
    /**
     * Get audio session id from bundle
     */
    fun Bundle?.getAudioSessionId(): Int {
        return this?.getInt("android.media.AudioManager.EXTRA_AUDIO_SESSION_ID", 0) ?: 0
    }
    
    /**
     * Create bundle with audio session id
     */
    fun createAudioSessionBundle(audioSessionId: Int): Bundle {
        return Bundle().apply {
            putInt("android.media.AudioManager.EXTRA_AUDIO_SESSION_ID", audioSessionId)
        }
    }
    
    /**
     * Validate song for playback
     */
    fun Song.isValidForPlayback(): Boolean {
        return path.isNotEmpty() && 
               java.io.File(path).exists() && 
               duration > 0
    }
    
    /**
     * Create playback position update bundle
     */
    fun createPositionUpdateBundle(
        position: Long,
        duration: Long,
        isPlaying: Boolean
    ): Bundle {
        return Bundle().apply {
            putLong("position", position)
            putLong("duration", duration)
            putBoolean("isPlaying", isPlaying)
        }
    }
    
    /**
     * Safe string truncation for notification
     */
    fun String.truncateForNotification(maxLength: Int = 50): String {
        return if (length > maxLength) {
            "${take(maxLength - 3)}..."
        } else {
            this
        }
    }
    
    /**
     * Convert file path to content URI
     */
    fun String.toContentUri(): android.net.Uri {
        return if (startsWith("content://")) {
            toUri()
        } else {
            java.io.File(this).toUri()
        }
    }
    
    /**
     * Calculate buffer percentage
     */
    fun calculateBufferPercentage(
        bufferedPosition: Long,
        duration: Long
    ): Int {
        return if (duration > 0) {
            ((bufferedPosition * 100) / duration).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }
    
    /**
     * Generate unique request code
     */
    fun generateRequestCode(): Int {
        return (System.currentTimeMillis() and 0xfffffff).toInt()
    }
    
    /**
     * Log playback event
     */
    fun logPlaybackEvent(
        event: String,
        song: Song?,
        additionalInfo: String = ""
    ) {
        val songInfo = song?.let { "${it.title} by ${it.artist}" } ?: "Unknown"
        android.util.Log.d(
            SERVICE_TAG, 
            "Playback Event: $event | Song: $songInfo | Info: $additionalInfo"
        )
    }
    
    /**
     * Create error bundle
     */
    fun createErrorBundle(
        errorCode: Int,
        errorMessage: String
    ): Bundle {
        return Bundle().apply {
            putInt("error_code", errorCode)
            putString("error_message", errorMessage)
        }
    }
    
    /**
     * Check if device is in low memory state
     */
    fun Context.isLowMemory(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) 
            as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }
}
