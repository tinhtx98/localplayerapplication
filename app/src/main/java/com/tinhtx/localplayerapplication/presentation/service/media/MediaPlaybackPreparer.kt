package com.tinhtx.localplayerapplication.data.service.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaSessionManager
import kotlinx.coroutines.*
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.usecase.GetSongByIdUseCase
import com.tinhtx.localplayerapplication.domain.usecase.SearchSongsUseCase
import com.tinhtx.localplayerapplication.data.service.components.ServiceUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlaybackPreparer @Inject constructor(
    private val getSongByIdUseCase: GetSongByIdUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val exoPlayerManager: ExoPlayerManager
) : androidx.media.session.MediaSessionManager.OnActiveSessionsChangedListener {
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var preparerCallback: MediaPlaybackPreparerCallback? = null
    
    interface MediaPlaybackPreparerCallback {
        fun onPrepared(songs: List<Song>, startIndex: Int)
        fun onError(error: String)
    }
    
    fun setCallback(callback: MediaPlaybackPreparerCallback) {
        this.preparerCallback = callback
    }
    
    /**
     * Prepare playback from media ID
     */
    fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Preparing from media ID: $mediaId"
        )
        
        coroutineScope.launch {
            try {
                val result = getSongByIdUseCase(mediaId)
                result.fold(
                    onSuccess = { song ->
                        if (song != null) {
                            if (playWhenReady) {
                                exoPlayerManager.playSong(song)
                            } else {
                                preparerCallback?.onPrepared(listOf(song), 0)
                            }
                            
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                song, 
                                "Prepared from media ID successfully"
                            )
                        } else {
                            val error = "Song not found: $mediaId"
                            preparerCallback?.onError(error)
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                null, 
                                error
                            )
                        }
                    },
                    onFailure = { exception ->
                        val error = "Failed to get song: ${exception.message}"
                        preparerCallback?.onError(error)
                        ServiceUtils.logPlaybackEvent(
                            "MediaPreparer", 
                            null, 
                            error
                        )
                    }
                )
            } catch (e: Exception) {
                val error = "Exception preparing from media ID: ${e.message}"
                preparerCallback?.onError(error)
                ServiceUtils.logPlaybackEvent(
                    "MediaPreparer", 
                    null, 
                    error
                )
            }
        }
    }
    
    /**
     * Prepare playback from search query
     */
    fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Preparing from search: $query"
        )
        
        if (query.isEmpty()) {
            preparerCallback?.onError("Empty search query")
            return
        }
        
        coroutineScope.launch {
            try {
                val result = searchSongsUseCase(query)
                result.fold(
                    onSuccess = { songs ->
                        if (songs.isNotEmpty()) {
                            if (playWhenReady) {
                                exoPlayerManager.playQueue(songs, 0)
                            } else {
                                preparerCallback?.onPrepared(songs, 0)
                            }
                            
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                null, 
                                "Prepared ${songs.size} songs from search"
                            )
                        } else {
                            val error = "No songs found for query: $query"
                            preparerCallback?.onError(error)
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                null, 
                                error
                            )
                        }
                    },
                    onFailure = { exception ->
                        val error = "Search failed: ${exception.message}"
                        preparerCallback?.onError(error)
                        ServiceUtils.logPlaybackEvent(
                            "MediaPreparer", 
                            null, 
                            error
                        )
                    }
                )
            } catch (e: Exception) {
                val error = "Exception preparing from search: ${e.message}"
                preparerCallback?.onError(error)
                ServiceUtils.logPlaybackEvent(
                    "MediaPreparer", 
                    null, 
                    error
                )
            }
        }
    }
    
    /**
     * Prepare playback from URI
     */
    fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Preparing from URI: $uri"
        )
        
        coroutineScope.launch {
            try {
                // Extract media ID from URI if possible
                val mediaId = extractMediaIdFromUri(uri)
                if (mediaId != null) {
                    onPrepareFromMediaId(mediaId, playWhenReady, extras)
                } else {
                    // Try to create a song from URI directly
                    val song = createSongFromUri(uri)
                    if (song != null) {
                        if (playWhenReady) {
                            exoPlayerManager.playSong(song)
                        } else {
                            preparerCallback?.onPrepared(listOf(song), 0)
                        }
                        
                        ServiceUtils.logPlaybackEvent(
                            "MediaPreparer", 
                            song, 
                            "Prepared from URI successfully"
                        )
                    } else {
                        val error = "Cannot create song from URI: $uri"
                        preparerCallback?.onError(error)
                        ServiceUtils.logPlaybackEvent(
                            "MediaPreparer", 
                            null, 
                            error
                        )
                    }
                }
            } catch (e: Exception) {
                val error = "Exception preparing from URI: ${e.message}"
                preparerCallback?.onError(error)
                ServiceUtils.logPlaybackEvent(
                    "MediaPreparer", 
                    null, 
                    error
                )
            }
        }
    }
    
    /**
     * Prepare default playback (recent or popular songs)
     */
    fun onPrepare(playWhenReady: Boolean) {
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Preparing default playback"
        )
        
        coroutineScope.launch {
            try {
                // Get recent or popular songs as default
                val result = searchSongsUseCase("") // Empty query might return all songs
                result.fold(
                    onSuccess = { songs ->
                        if (songs.isNotEmpty()) {
                            val defaultSongs = songs.take(50) // Limit to 50 songs
                            if (playWhenReady) {
                                exoPlayerManager.playQueue(defaultSongs, 0)
                            } else {
                                preparerCallback?.onPrepared(defaultSongs, 0)
                            }
                            
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                null, 
                                "Prepared ${defaultSongs.size} default songs"
                            )
                        } else {
                            val error = "No songs available for default playback"
                            preparerCallback?.onError(error)
                            ServiceUtils.logPlaybackEvent(
                                "MediaPreparer", 
                                null, 
                                error
                            )
                        }
                    },
                    onFailure = { exception ->
                        val error = "Failed to prepare default playback: ${exception.message}"
                        preparerCallback?.onError(error)
                        ServiceUtils.logPlaybackEvent(
                            "MediaPreparer", 
                            null, 
                            error
                        )
                    }
                )
            } catch (e: Exception) {
                val error = "Exception preparing default playback: ${e.message}"
                preparerCallback?.onError(error)
                ServiceUtils.logPlaybackEvent(
                    "MediaPreparer", 
                    null, 
                    error
                )
            }
        }
    }
    
    /**
     * Get supported prepare actions
     */
    fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE or
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                PlaybackStateCompat.ACTION_PREPARE_FROM_URI
    }
    
    private fun extractMediaIdFromUri(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                // Extract media ID from content URI
                uri.lastPathSegment
            }
            "localplayer" -> {
                // Extract from custom scheme (localplayer://song/123)
                if (uri.pathSegments.size >= 2 && uri.pathSegments[0] == "song") {
                    uri.pathSegments[1]
                } else {
                    null
                }
            }
            else -> null
        }
    }
    
    private fun createSongFromUri(uri: Uri): Song? {
        return try {
            // This is a simplified implementation
            // In a real app, you'd extract metadata from the file
            val path = uri.path ?: return null
            val fileName = path.substringAfterLast("/")
            val title = fileName.substringBeforeLast(".")
            
            Song(
                id = uri.hashCode().toString(),
                title = title,
                artist = "Unknown Artist",
                album = "Unknown Album",
                genre = "",
                duration = 0L,
                path = path,
                artworkPath = "",
                trackNumber = 0,
                year = 0,
                size = 0L,
                mimeType = "",
                dateAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ServiceUtils.logPlaybackEvent(
                "MediaPreparer", 
                null, 
                "Failed to create song from URI: ${e.message}"
            )
            null
        }
    }
    
    /**
     * Handle smart search queries
     */
    private fun handleSmartSearch(query: String, extras: Bundle?): String {
        val lowerQuery = query.lowercase()
        
        return when {
            lowerQuery.startsWith("play ") -> {
                lowerQuery.removePrefix("play ").trim()
            }
            lowerQuery.startsWith("artist:") -> {
                // Handle artist-specific search
                lowerQuery.removePrefix("artist:").trim()
            }
            lowerQuery.startsWith("album:") -> {
                // Handle album-specific search
                lowerQuery.removePrefix("album:").trim()
            }
            lowerQuery.startsWith("genre:") -> {
                // Handle genre-specific search
                lowerQuery.removePrefix("genre:").trim()
            }
            else -> query
        }
    }
    
    /**
     * Create search query from voice command
     */
    private fun createVoiceQuery(extras: Bundle?): String? {
        return extras?.let { bundle ->
            val artist = bundle.getString(android.provider.MediaStore.EXTRA_MEDIA_ARTIST)
            val album = bundle.getString(android.provider.MediaStore.EXTRA_MEDIA_ALBUM)
            val title = bundle.getString(android.provider.MediaStore.EXTRA_MEDIA_TITLE)
            val genre = bundle.getString(android.provider.MediaStore.EXTRA_MEDIA_GENRE)
            
            buildString {
                title?.let { append("$it ") }
                artist?.let { append("$it ") }
                album?.let { append("$it ") }
                genre?.let { append("$it ") }
            }.takeIf { it.isNotBlank() }?.trim()
        }
    }
    
    override fun onActiveSessionsChanged(controllers: MutableList<android.media.session.MediaController>?) {
        // Handle active sessions changed if needed
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Active sessions changed: ${controllers?.size ?: 0}"
        )
    }
    
    /**
     * Release resources
     */
    fun release() {
        coroutineScope.cancel()
        preparerCallback = null
        
        ServiceUtils.logPlaybackEvent(
            "MediaPreparer", 
            null, 
            "Media preparer released"
        )
    }
}
