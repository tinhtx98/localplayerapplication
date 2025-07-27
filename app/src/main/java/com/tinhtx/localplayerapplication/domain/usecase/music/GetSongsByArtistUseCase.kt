package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting songs by artist
 */
class GetSongsByArtistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get songs by artist name
     */
    suspend fun execute(artistName: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by artist with sorting
     */
    suspend fun execute(artistName: String, sortOrder: SortOrder): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val sortedSongs = when (sortOrder) {
                SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
                SortOrder.ALBUM -> songs.sortedBy { it.album.lowercase() }
                SortOrder.YEAR -> songs.sortedByDescending { it.year }
                SortOrder.DURATION -> songs.sortedByDescending { it.duration }
                SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED -> songs.sortedByDescending { it.dateModified }
                SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> songs.sortedByDescending { it.lastPlayed }
                else -> songs.sortedBy { it.album.lowercase() }.thenBy { it.trackNumber }
            }
            Result.success(sortedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs by artist
     */
    suspend fun getFavoriteSongsByArtist(artistName: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val favoriteSongs = songs.filter { it.isFavorite }
            Result.success(favoriteSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artist song statistics
     */
    suspend fun getArtistSongStatistics(artistName: String): Result<ArtistSongStatistics> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val albums = songs.map { it.album }.distinct()
            val totalDuration = songs.sumOf { it.duration }
            val totalSize = songs.sumOf { it.size }
            val totalPlayCount = songs.sumOf { it.playCount }
            val favoriteCount = songs.count { it.isFavorite }
            val averageDuration = if (songs.isNotEmpty()) totalDuration / songs.size else 0L
            val earliestYear = songs.minOfOrNull { it.year } ?: 0
            val latestYear = songs.maxOfOrNull { it.year } ?: 0
            val longestSong = songs.maxByOrNull { it.duration }
            val shortestSong = songs.minByOrNull { it.duration }
            val mostPlayedSong = songs.maxByOrNull { it.playCount }
            
            val statistics = ArtistSongStatistics(
                artistName = artistName,
                songCount = songs.size,
                albumCount = albums.size,
                totalDuration = totalDuration,
                totalSize = totalSize,
                totalPlayCount = totalPlayCount,
                favoriteCount = favoriteCount,
                averageDuration = averageDuration,
                earliestYear = earliestYear,
                latestYear = latestYear,
                longestSong = longestSong,
                shortestSong = shortestSong,
                mostPlayedSong = mostPlayedSong
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search songs by artist
     */
    suspend fun searchSongsByArtist(artistName: String, query: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val filteredSongs = songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artist's top songs by play count
     */
    suspend fun getArtistTopSongs(artistName: String, limit: Int = 10): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val topSongs = songs
                .sortedByDescending { it.playCount }
                .take(limit)
            Result.success(topSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by artist grouped by album
     */
    suspend fun getSongsByArtistGroupedByAlbum(artistName: String): Result<Map<String, List<Song>>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val groupedSongs = songs.groupBy { it.album }
                .mapValues { (_, albumSongs) ->
                    albumSongs.sortedBy { it.trackNumber }
                }
            Result.success(groupedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artist's albums with song counts
     */
    suspend fun getArtistAlbumsWithCounts(artistName: String): Result<List<ArtistAlbumInfo>> {
        return try {
            val songs = musicRepository.getSongsByArtist(artistName)
            val albumInfo = songs.groupBy { it.album }
                .map { (albumName, albumSongs) ->
                    ArtistAlbumInfo(
                        albumName = albumName,
                        songCount = albumSongs.size,
                        totalDuration = albumSongs.sumOf { it.duration },
                        year = albumSongs.maxOfOrNull { it.year } ?: 0
                    )
                }
                .sortedBy { it.year }
            
            Result.success(albumInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for artist song statistics
 */
data class ArtistSongStatistics(
    val artistName: String,
    val songCount: Int,
    val albumCount: Int,
    val totalDuration: Long,
    val totalSize: Long,
    val totalPlayCount: Int,
    val favoriteCount: Int,
    val averageDuration: Long,
    val earliestYear: Int,
    val latestYear: Int,
    val longestSong: Song?,
    val shortestSong: Song?,
    val mostPlayedSong: Song?
) {
    val totalDurationHours: Double
        get() = totalDuration / (1000.0 * 60.0 * 60.0)
    
    val totalSizeMB: Double
        get() = totalSize / (1024.0 * 1024.0)
    
    val favoritePercentage: Double
        get() = if (songCount > 0) (favoriteCount.toDouble() / songCount) * 100.0 else 0.0
    
    val yearSpan: Int
        get() = if (latestYear > 0 && earliestYear > 0) latestYear - earliestYear else 0
}

/**
 * Data class for artist album information
 */
data class ArtistAlbumInfo(
    val albumName: String,
    val songCount: Int,
    val totalDuration: Long,
    val year: Int
) {
    val totalDurationMinutes: Double
        get() = totalDuration / (1000.0 * 60.0)
}
