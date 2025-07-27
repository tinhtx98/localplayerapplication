package com.tinhtx.localplayerapplication.domain.usecase.history

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for updating play history
 */
class UpdatePlayHistoryUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Record song play (increment play count and update last played)
     */
    suspend fun recordSongPlay(songId: Long): Result<Unit> {
        return try {
            musicRepository.incrementPlayCount(songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Record multiple song plays
     */
    suspend fun recordSongPlays(songIds: List<Long>): Result<Int> {
        return try {
            var recordedCount = 0
            
            songIds.forEach { songId ->
                try {
                    musicRepository.incrementPlayCount(songId)
                    recordedCount++
                } catch (e: Exception) {
                    // Continue with other songs even if one fails
                }
            }
            
            Result.success(recordedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update last played timestamp for song
     */
    suspend fun updateLastPlayed(songId: Long, timestamp: Long = System.currentTimeMillis()): Result<Unit> {
        return try {
            val song = musicRepository.getSongById(songId)
            if (song == null) {
                return Result.failure(Exception("Song not found"))
            }
            
            val updatedSong = song.copy(lastPlayed = timestamp)
            musicRepository.updateSong(updatedSong)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set specific play count for song
     */
    suspend fun setPlayCount(songId: Long, playCount: Int): Result<Unit> {
        return try {
            if (playCount < 0) {
                return Result.failure(Exception("Play count cannot be negative"))
            }
            
            val song = musicRepository.getSongById(songId)
            if (song == null) {
                return Result.failure(Exception("Song not found"))
            }
            
            val updatedSong = song.copy(playCount = playCount)
            musicRepository.updateSong(updatedSong)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Record session play history
     */
    suspend fun recordSessionHistory(sessionData: SessionPlayData): Result<Unit> {
        return try {
            sessionData.songsPlayed.forEach { songPlay ->
                val song = musicRepository.getSongById(songPlay.songId)
                if (song != null) {
                    val updatedSong = song.copy(
                        playCount = song.playCount + songPlay.playCount,
                        lastPlayed = maxOf(song.lastPlayed, songPlay.lastPlayed)
                    )
                    musicRepository.updateSong(updatedSong)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update listening session statistics
     */
    suspend fun updateSessionStatistics(
        sessionDuration: Long,
        songsCompleted: Int,
        songsSkipped: Int
    ): Result<Unit> {
        return try {
            // This would typically update user statistics in UserPreferences
            // For now, we'll store basic session info
            // TODO: Integrate with user statistics tracking
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Record skip action (song was skipped before completion)
     */
    suspend fun recordSongSkip(songId: Long, positionWhenSkipped: Long): Result<Unit> {
        return try {
            val song = musicRepository.getSongById(songId)
            if (song == null) {
                return Result.failure(Exception("Song not found"))
            }
            
            // Only increment play count if song was played for more than 30 seconds
            // or more than 50% of duration (whichever is smaller)
            val minPlayTime = minOf(30000L, song.duration / 2)
            
            if (positionWhenSkipped >= minPlayTime) {
                musicRepository.incrementPlayCount(songId)
            }
            
            // Always update last played timestamp
            val updatedSong = song.copy(lastPlayed = System.currentTimeMillis())
            musicRepository.updateSong(updatedSong)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Record song completion (song played to the end)
     */
    suspend fun recordSongCompletion(songId: Long): Result<Unit> {
        return try {
            // Always increment play count for completed songs
            musicRepository.incrementPlayCount(songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Bulk update play history from import
     */
    suspend fun importPlayHistory(historyData: List<SongHistoryData>): Result<ImportResult> {
        return try {
            var successCount = 0
            var failureCount = 0
            val errors = mutableListOf<String>()
            
            historyData.forEach { data ->
                try {
                    val song = musicRepository.getSongById(data.songId)
                    if (song != null) {
                        val updatedSong = song.copy(
                            playCount = data.playCount,
                            lastPlayed = data.lastPlayed
                        )
                        musicRepository.updateSong(updatedSong)
                        successCount++
                    } else {
                        failureCount++
                        errors.add("Song not found: ${data.songId}")
                    }
                } catch (e: Exception) {
                    failureCount++
                    errors.add("Error updating song ${data.songId}: ${e.message}")
                }
            }
            
            val result = ImportResult(
                totalRecords = historyData.size,
                successCount = successCount,
                failureCount = failureCount,
                errors = errors
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Merge play history from different sources
     */
    suspend fun mergePlayHistory(
        existingSongId: Long,
        importedHistory: SongHistoryData,
        mergeStrategy: MergeStrategy = MergeStrategy.ADD_PLAY_COUNTS
    ): Result<Unit> {
        return try {
            val existingSong = musicRepository.getSongById(existingSongId)
            if (existingSong == null) {
                return Result.failure(Exception("Existing song not found"))
            }
            
            val updatedSong = when (mergeStrategy) {
                MergeStrategy.ADD_PLAY_COUNTS -> {
                    existingSong.copy(
                        playCount = existingSong.playCount + importedHistory.playCount,
                        lastPlayed = maxOf(existingSong.lastPlayed, importedHistory.lastPlayed)
                    )
                }
                MergeStrategy.KEEP_HIGHEST_COUNT -> {
                    existingSong.copy(
                        playCount = maxOf(existingSong.playCount, importedHistory.playCount),
                        lastPlayed = maxOf(existingSong.lastPlayed, importedHistory.lastPlayed)
                    )
                }
                MergeStrategy.REPLACE_EXISTING -> {
                    existingSong.copy(
                        playCount = importedHistory.playCount,
                        lastPlayed = importedHistory.lastPlayed
                    )
                }
                MergeStrategy.KEEP_MOST_RECENT -> {
                    if (importedHistory.lastPlayed > existingSong.lastPlayed) {
                        existingSong.copy(
                            playCount = importedHistory.playCount,
                            lastPlayed = importedHistory.lastPlayed
                        )
                    } else {
                        existingSong
                    }
                }
            }
            
            musicRepository.updateSong(updatedSong)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate and fix inconsistent play history
     */
    suspend fun validateAndFixHistory(): Result<ValidationResult> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            var fixedCount = 0
            val issues = mutableListOf<String>()
            
            allSongs.forEach { song ->
                var needsUpdate = false
                var updatedSong = song
                
                // Fix: Play count > 0 but lastPlayed = 0
                if (song.playCount > 0 && song.lastPlayed == 0L) {
                    updatedSong = updatedSong.copy(lastPlayed = System.currentTimeMillis())
                    needsUpdate = true
                    issues.add("Fixed missing lastPlayed for song: ${song.title}")
                }
                
                // Fix: Play count = 0 but lastPlayed > 0
                if (song.playCount == 0 && song.lastPlayed > 0L) {
                    updatedSong = updatedSong.copy(playCount = 1)
                    needsUpdate = true
                    issues.add("Fixed missing playCount for song: ${song.title}")
                }
                
                // Fix: Negative play count
                if (song.playCount < 0) {
                    updatedSong = updatedSong.copy(playCount = 0)
                    needsUpdate = true
                    issues.add("Fixed negative playCount for song: ${song.title}")
                }
                
                // Fix: Future lastPlayed timestamp
                if (song.lastPlayed > System.currentTimeMillis()) {
                    updatedSong = updatedSong.copy(lastPlayed = System.currentTimeMillis())
                    needsUpdate = true
                    issues.add("Fixed future lastPlayed for song: ${song.title}")
                }
                
                if (needsUpdate) {
                    musicRepository.updateSong(updatedSong)
                    fixedCount++
                }
            }
            
            val validationResult = ValidationResult(
                totalSongs = allSongs.size,
                issuesFound = issues.size,
                fixedCount = fixedCount,
                issues = issues
            )
            
            Result.success(validationResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export play history data
     */
    suspend fun exportPlayHistory(): Result<List<SongHistoryData>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val historyData = allSongs.filter { it.playCount > 0 || it.lastPlayed > 0L }
                .map { song ->
                    SongHistoryData(
                        songId = song.id,
                        playCount = song.playCount,
                        lastPlayed = song.lastPlayed,
                        songTitle = song.title,
                        artistName = song.artist
                    )
                }
            
            Result.success(historyData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for session play data
 */
data class SessionPlayData(
    val sessionId: String,
    val sessionStartTime: Long,
    val sessionEndTime: Long,
    val songsPlayed: List<SongPlayData>
)

/**
 * Data class for individual song play data
 */
data class SongPlayData(
    val songId: Long,
    val playCount: Int,
    val lastPlayed: Long,
    val playDuration: Long? = null,
    val completionPercentage: Float? = null
)

/**
 * Data class for song history data
 */
data class SongHistoryData(
    val songId: Long,
    val playCount: Int,
    val lastPlayed: Long,
    val songTitle: String? = null,
    val artistName: String? = null
)

/**
 * Data class for import results
 */
data class ImportResult(
    val totalRecords: Int,
    val successCount: Int,
    val failureCount: Int,
    val errors: List<String>
) {
    val successRate: Float
        get() = if (totalRecords > 0) (successCount.toFloat() / totalRecords) * 100f else 0f
    
    val hasErrors: Boolean
        get() = failureCount > 0
}

/**
 * Data class for validation results
 */
data class ValidationResult(
    val totalSongs: Int,
    val issuesFound: Int,
    val fixedCount: Int,
    val issues: List<String>
) {
    val hadIssues: Boolean
        get() = issuesFound > 0
    
    val allIssuesFixed: Boolean
        get() = issuesFound == fixedCount
}

/**
 * Enum for merge strategies
 */
enum class MergeStrategy {
    ADD_PLAY_COUNTS,      // Add imported play count to existing
    KEEP_HIGHEST_COUNT,   // Keep the higher play count
    REPLACE_EXISTING,     // Replace existing with imported data
    KEEP_MOST_RECENT      // Keep data from most recent timestamp
}
