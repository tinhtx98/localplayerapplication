package com.tinhtx.localplayerapplication.data.local.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans MediaStore for audio files
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "MediaStoreScanner"
    }
    
    private val contentResolver: ContentResolver = context.contentResolver
    
    /**
     * Get all songs from MediaStore
     */
    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                "${MediaStore.Audio.Media.IS_MUSIC} = 1",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
            Timber.d("$TAG - Found ${songs.size} songs in MediaStore")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying MediaStore for songs")
        }
        
        songs
    }
    
    /**
     * Get all albums from MediaStore
     */
    suspend fun getAllAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        
        try {
            val cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                MediaConstants.ALBUM_PROJECTION,
                null,
                null,
                "${MediaStore.Audio.Albums.ALBUM} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val album = createAlbumFromCursor(it)
                        album?.let { validAlbum ->
                            albums.add(validAlbum)
                        }
                    } while (it.moveToNext())
                }
            }
            
            Timber.d("$TAG - Found ${albums.size} albums in MediaStore")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying MediaStore for albums")
        }
        
        albums
    }
    
    /**
     * Get all artists from MediaStore
     */
    suspend fun getAllArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        
        try {
            val cursor = contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                MediaConstants.ARTIST_PROJECTION,
                null,
                null,
                "${MediaStore.Audio.Artists.ARTIST} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val artist = createArtistFromCursor(it)
                        artist?.let { validArtist ->
                            artists.add(validArtist)
                        }
                    } while (it.moveToNext())
                }
            }
            
            Timber.d("$TAG - Found ${artists.size} artists in MediaStore")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying MediaStore for artists")
        }
        
        artists
    }
    
    /**
     * Get songs modified since timestamp
     */
    suspend fun getModifiedSongs(since: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                           "${MediaStore.Audio.Media.DATE_MODIFIED} > ?"
            val selectionArgs = arrayOf((since / 1000).toString()) // MediaStore uses seconds
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
            Timber.d("$TAG - Found ${songs.size} modified songs since $since")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying modified songs")
        }
        
        songs
    }
    
    /**
     * Get songs in specific folder
     */
    suspend fun getSongsInFolder(folderPath: String): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                           "${MediaStore.Audio.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("$folderPath%")
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
            Timber.d("$TAG - Found ${songs.size} songs in folder: $folderPath")
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying songs in folder: $folderPath")
        }
        
        songs
    }
    
    /**
     * Get songs by album ID
     */
    suspend fun getSongsByAlbumId(albumId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                           "${MediaStore.Audio.Media.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(albumId.toString())
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TRACK} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying songs by album ID: $albumId")
        }
        
        songs
    }
    
    /**
     * Get songs by artist
     */
    suspend fun getSongsByArtist(artist: String): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                           "${MediaStore.Audio.Media.ARTIST} = ?"
            val selectionArgs = arrayOf(artist)
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.ALBUM} ASC, ${MediaStore.Audio.Media.TRACK} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error querying songs by artist: $artist")
        }
        
        songs
    }
    
    /**
     * Search songs by query
     */
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        try {
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND (" +
                           "${MediaStore.Audio.Media.TITLE} LIKE ? OR " +
                           "${MediaStore.Audio.Media.ARTIST} LIKE ? OR " +
                           "${MediaStore.Audio.Media.ALBUM} LIKE ?)"
            val searchTerm = "%$query%"
            val selectionArgs = arrayOf(searchTerm, searchTerm, searchTerm)
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                        }
                    } while (it.moveToNext())
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error searching songs with query: $query")
        }
        
        songs
    }
    
    /**
     * Get song by MediaStore ID
     */
    suspend fun getSongById(mediaStoreId: Long): Song? = withContext(Dispatchers.IO) {
        try {
            val selection = "${MediaStore.Audio.Media._ID} = ?"
            val selectionArgs = arrayOf(mediaStoreId.toString())
            
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return@withContext createSongFromCursor(it)
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting song by ID: $mediaStoreId")
        }
        
        null
    }
    
    /**
     * Get songs with progress flow
     */
    fun getAllSongsWithProgress(): Flow<MediaStoreProgress> = flow {
        emit(MediaStoreProgress.Started)
        
        try {
            val songs = mutableListOf<Song>()
            var totalCount = 0
            var processedCount = 0
            
            // First get total count
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf("COUNT(*)"),
                "${MediaStore.Audio.Media.IS_MUSIC} = 1",
                null,
                null
            )?.use { countCursor ->
                if (countCursor.moveToFirst()) {
                    totalCount = countCursor.getInt(0)
                }
            }
            
            emit(MediaStoreProgress.TotalCount(totalCount))
            
            // Then get actual songs
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaConstants.SONG_PROJECTION,
                "${MediaStore.Audio.Media.IS_MUSIC} = 1",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val song = createSongFromCursor(it)
                        song?.let { validSong ->
                            songs.add(validSong)
                            processedCount++
                            
                            if (processedCount % 10 == 0 || processedCount == totalCount) {
                                emit(MediaStoreProgress.Progress(processedCount, totalCount))
                            }
                        }
                    } while (it.moveToNext())
                }
            }
            
            emit(MediaStoreProgress.Completed(songs))
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error in progress scan")
            emit(MediaStoreProgress.Error(exception))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check if MediaStore has music files
     */
    suspend fun hasMusicFiles(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Media._ID),
                "${MediaStore.Audio.Media.IS_MUSIC} = 1",
                null,
                null
            )
            
            cursor?.use {
                return@withContext it.count > 0
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error checking if MediaStore has music files")
        }
        
        false
    }
    
    /**
     * Get MediaStore statistics
     */
    suspend fun getMediaStoreStatistics(): MediaStoreStatistics = withContext(Dispatchers.IO) {
        var songCount = 0
        var albumCount = 0
        var artistCount = 0
        var totalDuration = 0L
        var totalSize = 0L
        
        try {
            // Song statistics
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    "COUNT(*)",
                    "SUM(${MediaStore.Audio.Media.DURATION})",
                    "SUM(${MediaStore.Audio.Media.SIZE})"
                ),
                "${MediaStore.Audio.Media.IS_MUSIC} = 1",
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    songCount = cursor.getInt(0)
                    totalDuration = cursor.getLong(1)
                    totalSize = cursor.getLong(2)
                }
            }
            
            // Album count
            contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf("COUNT(*)"),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    albumCount = cursor.getInt(0)
                }
            }
            
            // Artist count
            contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                arrayOf("COUNT(*)"),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    artistCount = cursor.getInt(0)
                }
            }
            
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error getting MediaStore statistics")
        }
        
        MediaStoreStatistics(
            songCount = songCount,
            albumCount = albumCount,
            artistCount = artistCount,
            totalDuration = totalDuration,
            totalSize = totalSize
        )
    }
    
    // Private helper methods
    private fun createSongFromCursor(cursor: Cursor): Song? {
        return try {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            
            Song(
                id = 0L, // Will be set by Room
                mediaStoreId = cursor.getLong(idIndex),
                title = cursor.getString(titleIndex) ?: "Unknown Title",
                artist = cursor.getString(artistIndex) ?: "Unknown Artist",
                album = cursor.getString(albumIndex) ?: "Unknown Album",
                albumId = cursor.getLong(albumIdIndex),
                duration = cursor.getLong(durationIndex),
                path = cursor.getString(dataIndex) ?: "",
                size = cursor.getLong(sizeIndex),
                mimeType = cursor.getString(mimeTypeIndex) ?: "audio/mpeg",
                dateAdded = cursor.getLong(dateAddedIndex) * 1000, // Convert to milliseconds
                dateModified = cursor.getLong(dateModifiedIndex) * 1000, // Convert to milliseconds
                year = cursor.getInt(yearIndex),
                trackNumber = parseTrackNumber(cursor.getInt(trackIndex))
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error creating song from cursor")
            null
        }
    }
    
    private fun createAlbumFromCursor(cursor: Cursor): Album? {
        return try {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val firstYearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)
            val numSongsIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            
            Album(
                id = 0L, // Will be set by Room
                mediaStoreId = cursor.getLong(idIndex),
                name = cursor.getString(albumIndex) ?: "Unknown Album",
                artist = cursor.getString(artistIndex) ?: "Unknown Artist",
                artistId = 0L, // Will be resolved later
                year = cursor.getInt(firstYearIndex),
                songCount = cursor.getInt(numSongsIndex)
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error creating album from cursor")
            null
        }
    }
    
    private fun createArtistFromCursor(cursor: Cursor): Artist? {
        return try {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val numAlbumsIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            
            Artist(
                id = 0L, // Will be set by Room
                name = cursor.getString(artistIndex) ?: "Unknown Artist",
                albumCount = cursor.getInt(numAlbumsIndex),
                songCount = cursor.getInt(numTracksIndex)
            )
        } catch (exception: Exception) {
            Timber.e(exception, "$TAG - Error creating artist from cursor")
            null
        }
    }
    
    private fun parseTrackNumber(trackInfo: Int): Int {
        return try {
            // MediaStore sometimes stores track number with disc info (e.g., 1001 for disc 1, track 1)
            trackInfo % 1000
        } catch (exception: Exception) {
            0
        }
    }
}

/**
 * MediaStore scan progress
 */
sealed class MediaStoreProgress {
    object Started : MediaStoreProgress()
    data class TotalCount(val count: Int) : MediaStoreProgress()
    data class Progress(val processed: Int, val total: Int) : MediaStoreProgress()
    data class Completed(val songs: List<Song>) : MediaStoreProgress()
    data class Error(val exception: Throwable) : MediaStoreProgress()
    
    val progressPercentage: Float
        get() = when (this) {
            is Progress -> if (total > 0) (processed.toFloat() / total) * 100f else 0f
            is Completed -> 100f
            else -> 0f
        }
}

/**
 * MediaStore statistics
 */
data class MediaStoreStatistics(
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val totalDuration: Long = 0L,
    val totalSize: Long = 0L
) {
    val averageSongDuration: Long
        get() = if (songCount > 0) totalDuration / songCount else 0L
    
    val averageSongSize: Long
        get() = if (songCount > 0) totalSize / songCount else 0L
    
    val totalSizeMB: Double
        get() = totalSize / (1024.0 * 1024.0)
    
    val totalDurationHours: Double
        get() = totalDuration / (1000.0 * 60.0 * 60.0)
}
