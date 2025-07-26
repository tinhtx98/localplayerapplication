package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.data.local.media.MediaScanner
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    
    // Songs
    fun getAllSongs(): Flow<List<Song>>
    suspend fun getSongById(id: Long): Song?
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artistId: Long): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun getRecentlyPlayedSongs(limit: Int = 20): Flow<List<Song>>
    fun getMostPlayedSongs(limit: Int = 20): Flow<List<Song>>
    fun getRecentlyAddedSongs(limit: Int = 20): Flow<List<Song>>
    fun getSongsByYear(year: Int): Flow<List<Song>>
    
    // Albums
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun getAlbumById(id: Long): Album?
    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>>
    fun searchAlbums(query: String): Flow<List<Album>>
    fun getAlbumsByYear(year: Int): Flow<List<Album>>
    
    // Artists
    fun getAllArtists(): Flow<List<Artist>>
    suspend fun getArtistById(id: Long): Artist?
    fun searchArtists(query: String): Flow<List<Artist>>
    
    // Playback tracking
    suspend fun updatePlayCount(songId: Long)
    
    // Media scanning
    suspend fun scanMediaLibrary()
    suspend fun incrementalScan()
    fun getScanProgress(): Flow<MediaScanner.ScanProgress>
    fun getScanComplete(): Flow<MediaScanner.ScanResult>
    
    // Statistics
    suspend fun getSongCount(): Int
    suspend fun getAlbumCount(): Int
    suspend fun getArtistCount(): Int
    suspend fun getTotalDuration(): Long
    suspend fun getAllYears(): List<Int>
}
