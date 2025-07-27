package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting songs by album
 */
class GetSongsByAlbumUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get songs by album name
     */
    suspend fun execute(albumName: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by album ID
     */
    suspend fun execute(albumId: Long): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbumId(albumId)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by album with sorting
     */
    suspend fun execute(albumName: String, sortOrder: SortOrder): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            val sortedSongs = when (sortOrder) {
                SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
                SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
                SortOrder.DURATION -> songs.sortedByDescending { it.duration }
                SortOrder.YEAR -> songs.sortedByDescending { it.year }
                SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED -> songs.sortedByDescending { it.dateModified }
                SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> songs.sortedByDescending { it.lastPlayed }
                else -> songs.sortedBy { it.trackNumber }
            }
            Result.success(sortedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by album ID with sorting
     */
    suspend fun execute(albumId: Long, sortOrder: SortOrder): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbumId(albumId)
            val sortedSongs = when (sortOrder) {
                SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
                SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
                SortOrder.DURATION -> songs.sortedByDescending { it.duration }
                SortOrder.YEAR -> songs.sortedByDescending { it.year }
                SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED -> songs.sortedByDescending { it.dateModified }
                SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> songs.sortedByDescending { it.lastPlayed }
                else -> songs.sortedBy { it.trackNumber }
            }
            Result.success(sortedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs from album
     */
    suspend fun getFavoriteSongsFromAlbum(albumName: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            val favoriteSongs = songs.filter { it.isFavorite }
            Result.success(favoriteSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get album duration and statistics
     */
    suspend fun getAlbumStatistics(albumName: String): Result<AlbumSongStatistics> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            val totalDuration = songs.sumOf { it.duration }
            val totalSize = songs.sumOf { it.size }
            val totalPlayCount = songs.sumOf { it.playCount }
            val favoriteCount = songs.count { it.isFavorite }
            val averageDuration = if (songs.isNotEmpty()) totalDuration / songs.size else 0L
            val longestSong = songs.maxByOrNull { it.duration }
            val shortestSong = songs.minByOrNull { it.duration }
            val mostPlayedSong = songs.maxByOrNull { it.playCount }
            
            val statistics = AlbumSongStatistics(
                albumName = albumName,
                songCount = songs.size,
                totalDuration = totalDuration,
                totalSize = totalSize,
                totalPlayCount = totalPlayCount,
                favoriteCount = favoriteCount,
                averageDuration = averageDuration,
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
     * Search songs within album
     */
    suspend fun searchSongsInAlbum(albumName: String, query: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            val filteredSongs = songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true)
            }
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get album track listing (sorted by track number)
     */
    suspend fun getAlbumTrackListing(albumName: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByAlbum(albumName)
            val trackListing = songs.sortedBy { it.trackNumber }
            Result.success(trackListing)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for album song statistics
 */
data class AlbumSongStatistics(
    val albumName: String,
    val songCount: Int,
    val totalDuration: Long,
    val totalSize: Long,
    val totalPlayCount: Int,
    val favoriteCount: Int,
    val averageDuration: Long,
    val longestSong: Song?,
    val shortestSong: Song?,
    val mostPlayedSong: Song?
) {
    val totalDurationMinutes: Double
        get() = totalDuration / (1000.0 * 60.0)
    
    val totalSizeMB: Double
        get() = totalSize / (1024.0 * 1024.0)
    
    val favoritePercentage: Double
        get() = if (songCount > 0) (favoriteCount.toDouble() / songCount) * 100.0 else 0.0
}
