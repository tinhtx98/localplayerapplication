package com.tinhtx.localplayerapplication.domain.usecase.history

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for clearing play history
 */
class ClearHistoryUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Clear all play history
     */
    suspend fun execute(): Result<Unit> {
        return try {
            // Reset all play counts and last played timestamps
            val allSongs = musicRepository.getAllSongs()
            allSongs.forEach { song ->
                val clearedSong = song.copy(
                    playCount = 0,
                    lastPlayed = 0L
                )
                musicRepository.updateSong(clearedSong)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history for specific song
     */
    suspend fun clearSongHistory(songId: Long): Result<Unit> {
        return try {
            val song = musicRepository.getSongById(songId)
            if (song == null) {
                return Result.failure(Exception("Song not found"))
            }
            
            val clearedSong = song.copy(
                playCount = 0,
                lastPlayed = 0L
            )
            musicRepository.updateSong(clearedSong)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history for multiple songs
     */
    suspend fun clearSongsHistory(songIds: List<Long>): Result<Int> {
        return try {
            var clearedCount = 0
            
            songIds.forEach { songId ->
                val song = musicRepository.getSongById(songId)
                if (song != null) {
                    val clearedSong = song.copy(
                        playCount = 0,
                        lastPlayed = 0L
                    )
                    musicRepository.updateSong(clearedSong)
                    clearedCount++
                }
            }
            
            Result.success(clearedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history for songs by artist
     */
    suspend fun clearHistoryByArtist(artistName: String): Result<Int> {
        return try {
            val artistSongs = musicRepository.getSongsByArtist(artistName)
            val songIds = artistSongs.map { it.id }
            clearSongsHistory(songIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history for songs by album
     */
    suspend fun clearHistoryByAlbum(albumName: String): Result<Int> {
        return try {
            val albumSongs = musicRepository.getSongsByAlbum(albumName)
            val songIds = albumSongs.map { it.id }
            clearSongsHistory(songIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history older than specified days
     */
    suspend fun clearOldHistory(olderThanDays: Int): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            val allSongs = musicRepository.getAllSongs()
            
            val oldHistorySongs = allSongs.filter { song ->
                song.lastPlayed > 0 && song.lastPlayed < cutoffTime
            }
            
            var clearedCount = 0
            oldHistorySongs.forEach { song ->
                val clearedSong = song.copy(
                    playCount = 0,
                    lastPlayed = 0L
                )
                musicRepository.updateSong(clearedSong)
                clearedCount++
            }
            
            Result.success(clearedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear history for songs with low play count
     */
    suspend fun clearLowPlayCountHistory(maxPlayCount: Int): Result<Int> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val lowPlayCountSongs = allSongs.filter { song ->
                song.playCount > 0 && song.playCount <= maxPlayCount
            }
            
            var clearedCount = 0
            lowPlayCountSongs.forEach { song ->
                val clearedSong = song.copy(
                    playCount = 0,
                    lastPlayed = 0L
                )
                musicRepository.updateSong(clearedSong)
                clearedCount++
            }
            
            Result.success(clearedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset play counts only (keep last played timestamps)
     */
    suspend fun resetPlayCounts(): Result<Int> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val songsWithPlayCount = allSongs.filter { it.playCount > 0 }
            
            var resetCount = 0
            songsWithPlayCount.forEach { song ->
                val resetSong = song.copy(playCount = 0)
                musicRepository.updateSong(resetSong)
                resetCount++
            }
            
            Result.success(resetCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear recently played list (keep play counts)
     */
    suspend fun clearRecentlyPlayed(): Result<Int> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val recentlyPlayedSongs = allSongs.filter { it.lastPlayed > 0 }
            
            var clearedCount = 0
            recentlyPlayedSongs.forEach { song ->
                val clearedSong = song.copy(lastPlayed = 0L)
                musicRepository.updateSong(clearedSong)
                clearedCount++
            }
            
            Result.success(clearedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get count of songs with history
     */
    suspend fun getHistoryCount(): Result<Int> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val songsWithHistory = allSongs.count { it.playCount > 0 || it.lastPlayed > 0 }
            Result.success(songsWithHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if can clear history
     */
    suspend fun canClearHistory(): Result<Boolean> {
        return try {
            val historyCount = getHistoryCount().getOrNull() ?: 0
            Result.success(historyCount > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get history summary before clearing
     */
    suspend fun getHistorySummary(): Result<HistoryClearSummary> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val totalSongs = allSongs.size
            val songsWithHistory = allSongs.count { it.playCount > 0 || it.lastPlayed > 0 }
            val totalPlayCount = allSongs.sumOf { it.playCount }
            val oldestEntry = allSongs.filter { it.lastPlayed > 0 }.minByOrNull { it.lastPlayed }
            val newestEntry = allSongs.filter { it.lastPlayed > 0 }.maxByOrNull { it.lastPlayed }
            
            val summary = HistoryClearSummary(
                totalSongs = totalSongs,
                songsWithHistory = songsWithHistory,
                totalPlayCount = totalPlayCount,
                oldestEntryTime = oldestEntry?.lastPlayed ?: 0L,
                newestEntryTime = newestEntry?.lastPlayed ?: 0L
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for history clear summary
 */
data class HistoryClearSummary(
    val totalSongs: Int,
    val songsWithHistory: Int,
    val totalPlayCount: Int,
    val oldestEntryTime: Long,
    val newestEntryTime: Long
) {
    val historyPercentage: Float
        get() = if (totalSongs > 0) (songsWithHistory.toFloat() / totalSongs) * 100f else 0f
    
    val averagePlayCount: Float
        get() = if (songsWithHistory > 0) totalPlayCount.toFloat() / songsWithHistory else 0f
    
    val hasHistory: Boolean
        get() = songsWithHistory > 0
}
