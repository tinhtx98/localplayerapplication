package com.tinhtx.localplayerapplication.data.local.media

import android.content.Context
import com.tinhtx.localplayerapplication.core.di.IoDispatcher
import com.tinhtx.localplayerapplication.data.local.database.dao.AlbumDao
import com.tinhtx.localplayerapplication.data.local.database.dao.ArtistDao
import com.tinhtx.localplayerapplication.data.local.database.dao.SongDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    private val context: Context,
    private val mediaStoreScanner: MediaStoreScanner,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    private val _scanProgress = MutableSharedFlow<ScanProgress>()
    val scanProgress: SharedFlow<ScanProgress> = _scanProgress.asSharedFlow()
    
    private val _scanComplete = MutableSharedFlow<ScanResult>()
    val scanComplete: SharedFlow<ScanResult> = _scanComplete.asSharedFlow()
    
    suspend fun performFullScan() = withContext(ioDispatcher) {
        try {
            _scanProgress.emit(ScanProgress("Starting scan...", 0))
            
            // Clear existing data
            _scanProgress.emit(ScanProgress("Clearing old data...", 10))
            songDao.deleteAllSongs()
            albumDao.deleteAllAlbums()
            artistDao.deleteAllArtists()
            
            // Scan for songs
            _scanProgress.emit(ScanProgress("Scanning songs...", 30))
            val songs = mediaStoreScanner.scanForSongs()
            songDao.insertSongs(songs)
            
            // Scan for albums
            _scanProgress.emit(ScanProgress("Scanning albums...", 60))
            val albums = mediaStoreScanner.scanForAlbums()
            albumDao.insertAlbums(albums)
            
            // Scan for artists
            _scanProgress.emit(ScanProgress("Scanning artists...", 80))
            val artists = mediaStoreScanner.scanForArtists()
            artistDao.insertArtists(artists)
            
            _scanProgress.emit(ScanProgress("Scan complete!", 100))
            
            val result = ScanResult(
                songsFound = songs.size,
                albumsFound = albums.size,
                artistsFound = artists.size,
                isSuccess = true,
                errorMessage = null
            )
            
            _scanComplete.emit(result)
            
        } catch (e: Exception) {
            val errorResult = ScanResult(
                songsFound = 0,
                albumsFound = 0,
                artistsFound = 0,
                isSuccess = false,
                errorMessage = e.message ?: "Unknown error occurred"
            )
            
            _scanComplete.emit(errorResult)
        }
    }
    
    suspend fun performIncrementalScan() = withContext(ioDispatcher) {
        try {
            _scanProgress.emit(ScanProgress("Checking for new files...", 0))
            
            val existingSongCount = songDao.getSongCount()
            val mediaSongCount = mediaStoreScanner.getSongCount()
            
            if (mediaSongCount > existingSongCount) {
                _scanProgress.emit(ScanProgress("Found new files, updating...", 50))
                
                // Scan and add only new songs
                val allSongs = mediaStoreScanner.scanForSongs()
                val newSongs = allSongs.filter { newSong ->
                    songDao.getSongByMediaStoreId(newSong.mediaStoreId) == null
                }
                
                if (newSongs.isNotEmpty()) {
                    songDao.insertSongs(newSongs)
                    
                    // Update albums and artists as well
                    val albums = mediaStoreScanner.scanForAlbums()
                    albumDao.insertAlbums(albums)
                    
                    val artists = mediaStoreScanner.scanForArtists()
                    artistDao.insertArtists(artists)
                }
                
                _scanProgress.emit(ScanProgress("Incremental scan complete!", 100))
                
                val result = ScanResult(
                    songsFound = newSongs.size,
                    albumsFound = 0, // Not tracking new albums/artists separately
                    artistsFound = 0,
                    isSuccess = true,
                    errorMessage = null
                )
                
                _scanComplete.emit(result)
            } else {
                _scanProgress.emit(ScanProgress("No new files found", 100))
                
                val result = ScanResult(
                    songsFound = 0,
                    albumsFound = 0,
                    artistsFound = 0,
                    isSuccess = true,
                    errorMessage = null
                )
                
                _scanComplete.emit(result)
            }
            
        } catch (e: Exception) {
            val errorResult = ScanResult(
                songsFound = 0,
                albumsFound = 0,
                artistsFound = 0,
                isSuccess = false,
                errorMessage = e.message ?: "Unknown error occurred"
            )
            
            _scanComplete.emit(errorResult)
        }
    }
    
    data class ScanProgress(
        val message: String,
        val percentage: Int
    )
    
    data class ScanResult(
        val songsFound: Int,
        val albumsFound: Int,
        val artistsFound: Int,
        val isSuccess: Boolean,
        val errorMessage: String?
    )
}
