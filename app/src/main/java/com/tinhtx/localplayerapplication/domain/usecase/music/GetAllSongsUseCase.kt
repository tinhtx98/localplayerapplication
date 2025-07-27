package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all songs
 */
class GetAllSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get all songs
     */
    suspend fun execute(): Result<List<Song>> {
        return try {
            val songs = musicRepository.getAllSongs()
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all songs as Flow for reactive UI
     */
    fun executeFlow(): Flow<List<Song>> {
        return musicRepository.getAllSongsFlow()
    }
    
    /**
     * Get songs sorted by specified order
     */
    suspend fun execute(sortOrder: SortOrder): Result<List<Song>> {
        return try {
            val songs = musicRepository.getAllSongs()
            val sortedSongs = when (sortOrder) {
                SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
                SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
                SortOrder.ALBUM -> songs.sortedBy { it.album.lowercase() }
                SortOrder.YEAR -> songs.sortedByDescending { it.year }
                SortOrder.DURATION -> songs.sortedByDescending { it.duration }
                SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
                SortOrder.DATE_MODIFIED -> songs.sortedByDescending { it.dateModified }
                SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> songs.sortedByDescending { it.lastPlayed }
            }
            Result.success(sortedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently added songs
     */
    suspend fun getRecentlyAddedSongs(limit: Int = 50): Result<List<Song>> {
        return try {
            val songs = musicRepository.getRecentlyAddedSongs(limit)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently played songs
     */
    suspend fun getRecentlyPlayedSongs(limit: Int = 50): Result<List<Song>> {
        return try {
            val songs = musicRepository.getRecentlyPlayedSongs(limit)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get most played songs
     */
    suspend fun getMostPlayedSongs(limit: Int = 50): Result<List<Song>> {
        return try {
            val songs = musicRepository.getMostPlayedSongs(limit)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by duration range
     */
    suspend fun getSongsByDurationRange(minDuration: Long, maxDuration: Long): Result<List<Song>> {
        return try {
            val songs = musicRepository.getAllSongs()
            val filteredSongs = songs.filter { song ->
                song.duration in minDuration..maxDuration
            }
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by year range
     */
    suspend fun getSongsByYearRange(startYear: Int, endYear: Int): Result<List<Song>> {
        return try {
            val songs = musicRepository.getAllSongs()
            val filteredSongs = songs.filter { song ->
                song.year in startYear..endYear
            }
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs by genre
     */
    suspend fun getSongsByGenre(genre: String): Result<List<Song>> {
        return try {
            val songs = musicRepository.getSongsByGenre(genre)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get random songs
     */
    suspend fun getRandomSongs(count: Int): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val randomSongs = allSongs.shuffled().take(count)
            Result.success(randomSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get song statistics
     */
    suspend fun getSongStatistics(): Result<SongStatistics> {
        return try {
            val songs = musicRepository.getAllSongs()
            val totalSongs = songs.size
            val totalDuration = songs.sumOf { it.duration }
            val totalSize = songs.sumOf { it.size }
            val averageDuration = if (totalSongs > 0) totalDuration / totalSongs else 0L
            val averageSize = if (totalSongs > 0) totalSize / totalSongs else 0L
            val totalPlayCount = songs.sumOf { it.playCount }
            val favoriteCount = songs.count { it.isFavorite }
            val oldestSong = songs.minByOrNull { it.dateAdded }
            val newestSong = songs.maxByOrNull { it.dateAdded }
            val mostPlayedSong = songs.maxByOrNull { it.playCount }
            
            val statistics = SongStatistics(
                totalSongs = totalSongs,
                totalDuration = totalDuration,
                totalSize = totalSize,
                averageDuration = averageDuration,
                averageSize = averageSize,
                totalPlayCount = totalPlayCount,
                favoriteCount = favoriteCount,
                oldestSong = oldestSong,
                newestSong = newestSong,
                mostPlayedSong = mostPlayedSong
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for song statistics
 */
data class SongStatistics(
    val totalSongs: Int,
    val totalDuration: Long,
    val totalSize: Long,
    val averageDuration: Long,
    val averageSize: Long,
    val totalPlayCount: Int,
    val favoriteCount: Int,
    val oldestSong: Song?,
    val newestSong: Song?,
    val mostPlayedSong: Song?
) {
    val totalDurationHours: Double
        get() = totalDuration / (1000.0 * 60.0 * 60.0)
    
    val totalSizeMB: Double
        get() = totalSize / (1024.0 * 1024.0)
    
    val favoritePercentage: Double
        get() = if (totalSongs > 0) (favoriteCount.toDouble() / totalSongs) * 100.0 else 0.0
}
