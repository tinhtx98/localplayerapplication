package com.tinhtx.localplayerapplication.data.local.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.data.local.database.entities.AlbumEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.ArtistEntity
import com.tinhtx.localplayerapplication.data.local.database.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    private val context: Context
) {
    
    suspend fun scanForSongs(): List<SongEntity> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<SongEntity>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaConstants.SONG_PROJECTION,
            MediaConstants.SONG_SELECTION,
            MediaConstants.SONG_SELECTION_ARGS,
            MediaConstants.SONG_SORT_ORDER
        )
        
        cursor?.use { c ->
            while (c.moveToNext()) {
                try {
                    val song = mapCursorToSongEntity(c)
                    if (MediaUtils.isAudioFileValid(song.data)) {
                        songs.add(song)
                    }
                } catch (e: Exception) {
                    // Log error but continue scanning
                    continue
                }
            }
        }
        
        songs
    }
    
    suspend fun scanForAlbums(): List<AlbumEntity> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<AlbumEntity>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            MediaConstants.ALBUM_PROJECTION,
            MediaConstants.ALBUM_SELECTION,
            null,
            MediaConstants.ALBUM_SORT_ORDER
        )
        
        cursor?.use { c ->
            while (c.moveToNext()) {
                try {
                    albums.add(mapCursorToAlbumEntity(c))
                } catch (e: Exception) {
                    continue
                }
            }
        }
        
        albums
    }
    
    suspend fun scanForArtists(): List<ArtistEntity> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<ArtistEntity>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            MediaConstants.ARTIST_PROJECTION,
            MediaConstants.ARTIST_SELECTION,
            null,
            MediaConstants.ARTIST_SORT_ORDER
        )
        
        cursor?.use { c ->
            while (c.moveToNext()) {
                try {
                    artists.add(mapCursorToArtistEntity(c))
                } catch (e: Exception) {
                    continue
                }
            }
        }
        
        artists
    }
    
    private fun mapCursorToSongEntity(cursor: Cursor): SongEntity {
        return SongEntity(
            mediaStoreId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) 
                ?: "Unknown",
            artist = MediaUtils.cleanArtistName(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
            ),
            album = MediaUtils.cleanAlbumName(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
            ),
            duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
            data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
            dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)),
            albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
            artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)),
            track = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)),
            year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)),
            size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
        )
    }
    
    private fun mapCursorToAlbumEntity(cursor: Cursor): AlbumEntity {
        return AlbumEntity(
            mediaStoreId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)),
            albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
                ?: "Unknown Album",
            artist = MediaUtils.cleanArtistName(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
            ),
            artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST_ID)),
            songCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)),
            firstYear = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)),
            lastYear = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR))
        )
    }
    
    private fun mapCursorToArtistEntity(cursor: Cursor): ArtistEntity {
        return ArtistEntity(
            mediaStoreId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)),
            artistName = MediaUtils.cleanArtistName(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
            ),
            albumCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)),
            trackCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
        )
    }
    
    suspend fun getSongCount(): Int = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf("COUNT(*)"),
            MediaConstants.SONG_SELECTION,
            MediaConstants.SONG_SELECTION_ARGS,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return@withContext it.getInt(0)
            }
        }
        0
    }
}
