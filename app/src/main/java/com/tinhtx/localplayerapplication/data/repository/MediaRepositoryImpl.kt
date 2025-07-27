package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.data.local.database.dao.*
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) : MusicRepository {

    // Existing implementations...
    override suspend fun getAllSongs(): List<Song> = songDao.getAllSongs().map { it.toDomain() }
    override suspend fun getAllAlbums(): List<Album> = albumDao.getAllAlbums().map { it.toDomain() }
    override suspend fun getAllArtists(): List<Artist> = artistDao.getAllArtists().map { it.toDomain() }

    // Statistics implementations
    override suspend fun getSongCount(): Int = songDao.getSongCount()

    override suspend fun getAlbumCount(): Int = albumDao.getAlbumCount()

    override suspend fun getArtistCount(): Int = artistDao.getArtistCount()

    override suspend fun getPlaylistCount(): Int = playlistDao.getPlaylistCount()

    override suspend fun getFavoriteSongCount(): Int = favoriteDao.getFavoriteCount()

    // Playtime methods
    override suspend fun getTotalPlaytimeMs(): Long = songDao.getTotalDuration()

    override suspend fun getTotalPlaytimeHours(): Double {
        val totalMs = getTotalPlaytimeMs()
        return totalMs / (1000.0 * 60.0 * 60.0) // Convert to hours
    }

    // Library size methods
    override suspend fun getTotalLibrarySize(): Long = songDao.getTotalSize()

    override suspend fun getTotalLibrarySizeMB(): Long {
        val totalBytes = getTotalLibrarySize()
        return totalBytes / (1024 * 1024) // Convert to MB
    }

    // Additional analytics methods
    override suspend fun getAverageSongDuration(): Long {
        val totalDuration = songDao.getTotalDuration()
        val songCount = songDao.getSongCount()
        return if (songCount > 0) totalDuration / songCount else 0L
    }

    override suspend fun getRecentlyAddedCount(days: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return songDao.getRecentlyAddedCount(cutoffTime)
    }

    override suspend fun getLibraryScanCount(): Int = 0 // This would come from settings/preferences

    override suspend fun getPlaybackErrorCount(): Int = 0 // This would come from error tracking

    override suspend fun getNetworkErrorCount(): Int = 0 // This would come from error tracking

    override suspend fun getPermissionErrorCount(): Int = 0 // This would come from error tracking

    override suspend fun getSessionCount(since: Long): Int = historyDao.getSessionCount(since)

    override suspend fun getPlaytimeHours(since: Long): Double {
        val totalMs = historyDao.getTotalPlaytimeSince(since)
        return totalMs / (1000.0 * 60.0 * 60.0)
    }

    override suspend fun getMostPlayedSongs(limit: Int): List<Song> {
        return songDao.getMostPlayedSongs(limit).map { it.toDomain() }
    }

    override suspend fun getMostPlayedArtists(limit: Int): List<Artist> {
        return artistDao.getMostPlayedArtists(limit).map { it.toDomain() }
    }

    override suspend fun getShuffleUsagePercentage(): Float = 0f // This would come from playback history

    override suspend fun getRepeatUsagePercentage(): Float = 0f // This would come from playback history

    override suspend fun getAverageSessionDurationMinutes(): Double = 0.0 // This would come from session tracking

    override suspend fun getSkipRatePercentage(): Float = 0f // This would come from playback history

    // Update statistics methods
    override suspend fun updateArtistStatistics() {
        artistDao.updateStatistics()
    }

    override suspend fun updateAlbumStatistics() {
        albumDao.updateStatistics()
    }

    override suspend fun updateGenreStatistics() {
        // Implementation for genre statistics
    }

    // Existing implementations continue...
    override suspend fun insertSong(song: Song) = songDao.insertSong(song.toEntity())
    override suspend fun updateSong(song: Song) = songDao.updateSong(song.toEntity())
    override suspend fun deleteSongByPath(path: String) = songDao.deleteSongByPath(path)
    override suspend fun insertArtist(artist: Artist) = artistDao.insertArtist(artist.toEntity())
    override suspend fun insertAlbum(album: Album) = albumDao.insertAlbum(album.toEntity())
    override suspend fun getArtistByName(name: String): Artist? = artistDao.getArtistByName(name)?.toDomain()
    override suspend fun getAlbumByNameAndArtist(name: String, artist: String): Album? =
        albumDao.getAlbumByNameAndArtist(name, artist)?.toDomain()
}
