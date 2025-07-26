package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.di.IoDispatcher
import com.tinhtx.localplayerapplication.data.local.database.dao.AlbumDao
import com.tinhtx.localplayerapplication.data.local.database.dao.ArtistDao
import com.tinhtx.localplayerapplication.data.local.database.dao.SongDao
import com.tinhtx.localplayerapplication.data.local.media.MediaScanner
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val mediaScanner: MediaScanner,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MusicRepository {
    
    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getSongById(id: Long): Song? = withContext(ioDispatcher) {
        songDao.getSongById(id)?.toDomain()
    }
    
    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return songDao.getSongsByAlbum(albumId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getSongsByArtist(artistId: Long): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentlyPlayedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyPlayedSongs(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMostPlayedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getMostPlayedSongs(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentlyAddedSongs(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyAddedSongs(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllAlbums(): Flow<List<Album>> {
        return albumDao.getAllAlbums().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getAlbumById(id: Long): Album? = withContext(ioDispatcher) {
        albumDao.getAlbumById(id)?.toDomain()
    }
    
    override fun getAlbumsByArtist(artistId: Long): Flow<List<Album>> {
        return albumDao.getAlbumsByArtist(artistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchAlbums(query: String): Flow<List<Album>> {
        return albumDao.searchAlbums(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getArtistById(id: Long): Artist? = withContext(ioDispatcher) {
        artistDao.getArtistById(id)?.toDomain()
    }
    
    override fun searchArtists(query: String): Flow<List<Artist>> {
        return artistDao.searchArtists(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun updatePlayCount(songId: Long) = withContext(ioDispatcher) {
        songDao.incrementPlayCount(songId)
    }
    
    override suspend fun scanMediaLibrary() = withContext(ioDispatcher) {
        mediaScanner.performFullScan()
    }
    
    override suspend fun incrementalScan() = withContext(ioDispatcher) {
        mediaScanner.performIncrementalScan()
    }
    
    override fun getScanProgress(): Flow<MediaScanner.ScanProgress> {
        return mediaScanner.scanProgress
    }
    
    override fun getScanComplete(): Flow<MediaScanner.ScanResult> {
        return mediaScanner.scanComplete
    }
    
    override suspend fun getSongCount(): Int = withContext(ioDispatcher) {
        songDao.getSongCount()
    }
    
    override suspend fun getAlbumCount(): Int = withContext(ioDispatcher) {
        albumDao.getAlbumCount()
    }
    
    override suspend fun getArtistCount(): Int = withContext(ioDispatcher) {
        artistDao.getArtistCount()
    }
    
    override suspend fun getTotalDuration(): Long = withContext(ioDispatcher) {
        songDao.getTotalDuration()
    }
    
    override fun getSongsByYear(year: Int): Flow<List<Song>> {
        return songDao.getSongsByYear(year).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAlbumsByYear(year: Int): Flow<List<Album>> {
        return albumDao.getAlbumsByYear(year).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getAllYears(): List<Int> = withContext(ioDispatcher) {
        songDao.getAllYears()
    }
}
