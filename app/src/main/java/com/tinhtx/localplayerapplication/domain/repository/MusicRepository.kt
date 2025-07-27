package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for music-related operations (SIMPLIFIED)
 */
interface MusicRepository {
    
    // Basic CRUD Operations
    suspend fun getAllSongs(): List<Song>
    fun getAllSongsFlow(): Flow<List<Song>>
    suspend fun getAllAlbums(): List<Album>
    fun getAllAlbumsFlow(): Flow<List<Album>>
    suspend fun getAllArtists(): List<Artist>
    fun getAllArtistsFlow(): Flow<List<Artist>>
    
    // Single item retrieval
    suspend fun getSongById(id: Long): Song?
    suspend fun getAlbumById(id: Long): Album?
    suspend fun getArtistById(id: Long): Artist?
    suspend fun getSongByPath(path: String): Song?
    
    // Album and Artist relationships
    suspend fun getSongsByAlbum(album: String): List<Song>
    suspend fun getSongsByAlbumId(albumId: Long): List<Song>
    suspend fun getSongsByArtist(artist: String): List<Song>
    suspend fun getAlbumsByArtist(artist: String): List<Album>
    suspend fun getSongsByGenre(genre: String): List<Song>
    
    // Search functionality
    suspend fun searchSongs(query: String): List<Song>
    fun searchSongsFlow(query: String): Flow<List<Song>>
    suspend fun searchAlbums(query: String): List<Album>
    suspend fun searchArtists(query: String): List<Artist>
    
    // Recently played and popular
    suspend fun getRecentlyAddedSongs(limit: Int): List<Song>
    suspend fun getRecentlyPlayedSongs(limit: Int): List<Song>
    suspend fun getMostPlayedSongs(limit: Int): List<Song>
    
    // Favorites management
    suspend fun getFavoriteSongs(): List<Song>
    fun getFavoriteSongsFlow(): Flow<List<Song>>
    suspend fun getFavoriteCount(): Int
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    suspend fun markSongsAsFavorite(songIds: List<Long>)
    suspend fun unmarkSongsAsFavorite(songIds: List<Long>)
    suspend fun toggleFavoriteStatus(songId: Long): Boolean
    suspend fun isSongFavorite(songId: Long): Boolean
    
    // Song management
    suspend fun insertSong(song: Song): Long
    suspend fun insertSongs(songs: List<Song>): List<Long>
    suspend fun updateSong(song: Song)
    suspend fun updateSongs(songs: List<Song>)
    suspend fun deleteSong(song: Song)
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun deleteSongById(id: Long)
    suspend fun deleteSongsByIds(ids: List<Long>)
    
    // Play count and history management
    suspend fun incrementPlayCount(songId: Long)
    suspend fun updatePlayCount(songId: Long, playCount: Int)
    suspend fun updateLastPlayed(songId: Long, timestamp: Long)
    suspend fun resetPlayCount(songId: Long)
    suspend fun resetAllPlayCounts()
    suspend fun clearPlayHistory()
    
    // Basic maintenance operations
    suspend fun updateArtistStatistics()
    suspend fun updateAlbumStatistics()
    suspend fun clearAllData()
    suspend fun validateAudioFile(path: String): Boolean
    suspend fun removeInvalidSongs(): Int
    suspend fun fixInconsistentData(): Int
    
    // Batch operations
    suspend fun getSongsByIds(ids: List<Long>): List<Song>
    suspend fun getSongsInPath(path: String): List<Song>
    suspend fun getSongsModifiedAfter(timestamp: Long): List<Song>
    suspend fun updateSongPaths(pathMappings: Map<String, String>): Int
    suspend fun bulkUpdateFavorites(songIds: List<Long>, isFavorite: Boolean): Int
    
    // Filtering operations
    suspend fun getSongsByYear(year: Int): List<Song>
    suspend fun getSongsByYearRange(startYear: Int, endYear: Int): List<Song>
    suspend fun getSongsByDurationRange(minDuration: Long, maxDuration: Long): List<Song>
    suspend fun getSongsWithMinPlayCount(minCount: Int): List<Song>
    suspend fun getNeverPlayedSongs(): List<Song>
    suspend fun getRecentlyPlayedSongs(since: Long): List<Song>
    
    // Utility operations
    suspend fun getAllGenres(): List<String>
    suspend fun getAllYears(): List<Int>
    suspend fun getDistinctAlbumNames(): List<String>
    suspend fun getDistinctArtistNames(): List<String>
    suspend fun getAllFormats(): List<String>
    suspend fun getSongCount(): Int
    suspend fun getAlbumCount(): Int
    suspend fun getArtistCount(): Int
    suspend fun getTotalDuration(): Long
    suspend fun getTotalSize(): Long
    
    // Advanced queries
    suspend fun getRandomSongs(count: Int): List<Song>
    suspend fun getSimilarSongs(songId: Long, limit: Int): List<Song>
    suspend fun getTopSongs(limit: Int): List<Song>
    suspend fun getRecommendedSongs(limit: Int): List<Song>
    
    // Data integrity
    suspend fun validateMusicLibrary(): List<String>
    suspend fun findDuplicateSongs(): List<List<Song>>
    suspend fun findMissingSongs(): List<Song>
    suspend fun repairDatabase(): Boolean
    suspend fun optimizeDatabase(): Boolean
    
    // Cache management
    suspend fun clearMetadataCache()
    suspend fun refreshMetadataCache()
    suspend fun preloadFrequentlyAccessedData()
    
    // Import/Export
    suspend fun exportLibraryData(): String
    suspend fun importLibraryData( String): Boolean
    suspend fun backupLibrary(): String
    suspend fun restoreLibrary(backupData: String): Boolean
}
