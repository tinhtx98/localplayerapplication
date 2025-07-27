package com.tinhtx.localplayerapplication.domain.repository

import com.tinhtx.localplayerapplication.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Existing methods...
    suspend fun getAllSongs(): List<Song>
    suspend fun getAllAlbums(): List<Album>
    suspend fun getAllArtists(): List<Artist>

    // Add missing statistics methods
    suspend fun getSongCount(): Int
    suspend fun getAlbumCount(): Int
    suspend fun getArtistCount(): Int
    suspend fun getPlaylistCount(): Int
    suspend fun getFavoriteSongCount(): Int

    // Playtime methods
    suspend fun getTotalPlaytimeMs(): Long
    suspend fun getTotalPlaytimeHours(): Double

    // Library size methods
    suspend fun getTotalLibrarySize(): Long // bytes
    suspend fun getTotalLibrarySizeMB(): Long // MB

    // Additional analytics methods
    suspend fun getAverageSongDuration(): Long
    suspend fun getRecentlyAddedCount(days: Int): Int
    suspend fun getLibraryScanCount(): Int
    suspend fun getPlaybackErrorCount(): Int
    suspend fun getNetworkErrorCount(): Int
    suspend fun getPermissionErrorCount(): Int
    suspend fun getSessionCount(since: Long): Int
    suspend fun getPlaytimeHours(since: Long): Double
    suspend fun getMostPlayedSongs(limit: Int): List<Song>
    suspend fun getMostPlayedArtists(limit: Int): List<Artist>
    suspend fun getShuffleUsagePercentage(): Float
    suspend fun getRepeatUsagePercentage(): Float
    suspend fun getAverageSessionDurationMinutes(): Double
    suspend fun getSkipRatePercentage(): Float

    // Update statistics methods
    suspend fun updateArtistStatistics()
    suspend fun updateAlbumStatistics()
    suspend fun updateGenreStatistics()

    // Existing methods continue...
    suspend fun insertSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSongByPath(path: String)
    suspend fun insertArtist(artist: Artist)
    suspend fun insertAlbum(album: Album)
    suspend fun getArtistByName(name: String): Artist?
    suspend fun getAlbumByNameAndArtist(name: String, artist: String): Album?
}
