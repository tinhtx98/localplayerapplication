package com.tinhtx.localplayerapplication.domain.usecase.history

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for getting play history
 */
class GetPlayHistoryUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
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
     * Get all songs with play history
     */
    suspend fun getSongsWithHistory(): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val songsWithHistory = allSongs.filter { it.playCount > 0 || it.lastPlayed > 0 }
            Result.success(songsWithHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs with history sorted by criteria
     */
    suspend fun getSongsWithHistory(sortOrder: SortOrder): Result<List<Song>> {
        return try {
            val songsWithHistory = getSongsWithHistory().getOrNull() ?: emptyList()
            
            val sortedSongs = when (sortOrder) {
                SortOrder.PLAY_COUNT -> songsWithHistory.sortedByDescending { it.playCount }
                SortOrder.LAST_PLAYED -> songsWithHistory.sortedByDescending { it.lastPlayed }
                SortOrder.TITLE -> songsWithHistory.sortedBy { it.title.lowercase() }
                SortOrder.ARTIST -> songsWithHistory.sortedBy { it.artist.lowercase() }
                SortOrder.ALBUM -> songsWithHistory.sortedBy { it.album.lowercase() }
                SortOrder.DATE_ADDED -> songsWithHistory.sortedByDescending { it.dateAdded }
                SortOrder.DURATION -> songsWithHistory.sortedByDescending { it.duration }
                else -> songsWithHistory.sortedByDescending { it.lastPlayed }
            }
            
            Result.success(sortedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs never played
     */
    suspend fun getNeverPlayedSongs(): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val neverPlayedSongs = allSongs.filter { it.playCount == 0 && it.lastPlayed == 0L }
            Result.success(neverPlayedSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get play history for specific time period
     */
    suspend fun getHistoryForPeriod(startTime: Long, endTime: Long): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val periodSongs = allSongs.filter { song ->
                song.lastPlayed in startTime..endTime
            }.sortedByDescending { it.lastPlayed }
            
            Result.success(periodSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get today's played songs
     */
    suspend fun getTodayPlayedSongs(): Result<List<Song>> {
        return try {
            val todayStart = getTodayStartTime()
            val todayEnd = System.currentTimeMillis()
            getHistoryForPeriod(todayStart, todayEnd)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get this week's played songs
     */
    suspend fun getThisWeekPlayedSongs(): Result<List<Song>> {
        return try {
            val weekStart = getWeekStartTime()
            val weekEnd = System.currentTimeMillis()
            getHistoryForPeriod(weekStart, weekEnd)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get this month's played songs
     */
    suspend fun getThisMonthPlayedSongs(): Result<List<Song>> {
        return try {
            val monthStart = getMonthStartTime()
            val monthEnd = System.currentTimeMillis()
            getHistoryForPeriod(monthStart, monthEnd)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs played more than X times
     */
    suspend fun getSongsPlayedMoreThan(minPlayCount: Int): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val frequentSongs = allSongs.filter { it.playCount > minPlayCount }
                .sortedByDescending { it.playCount }
            Result.success(frequentSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get songs played only once
     */
    suspend fun getSongsPlayedOnce(): Result<List<Song>> {
        return getSongsPlayedMoreThan(0).let { result ->
            result.map { songs -> songs.filter { it.playCount == 1 } }
        }
    }
    
    /**
     * Get play history statistics
     */
    suspend fun getPlayHistoryStatistics(): Result<PlayHistoryStatistics> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val songsWithHistory = allSongs.filter { it.playCount > 0 || it.lastPlayed > 0 }
            val neverPlayedSongs = allSongs.filter { it.playCount == 0 && it.lastPlayed == 0L }
            
            val totalPlayCount = allSongs.sumOf { it.playCount }
            val averagePlayCount = if (songsWithHistory.isNotEmpty()) {
                totalPlayCount.toDouble() / songsWithHistory.size
            } else 0.0
            
            val mostPlayedSong = allSongs.maxByOrNull { it.playCount }
            val recentlyPlayedSong = allSongs.filter { it.lastPlayed > 0 }
                .maxByOrNull { it.lastPlayed }
            
            val oldestHistoryEntry = allSongs.filter { it.lastPlayed > 0 }
                .minByOrNull { it.lastPlayed }
            
            // Get listening time estimates
            val estimatedListeningTime = calculateEstimatedListeningTime(allSongs)
            
            val statistics = PlayHistoryStatistics(
                totalSongs = allSongs.size,
                songsWithHistory = songsWithHistory.size,
                neverPlayedSongs = neverPlayedSongs.size,
                totalPlayCount = totalPlayCount,
                averagePlayCount = averagePlayCount,
                mostPlayedSong = mostPlayedSong,
                recentlyPlayedSong = recentlyPlayedSong,
                oldestHistoryEntry = oldestHistoryEntry,
                estimatedListeningTimeMs = estimatedListeningTime
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get listening habits analysis
     */
    suspend fun getListeningHabitsAnalysis(): Result<ListeningHabitsAnalysis> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            val songsWithHistory = allSongs.filter { it.playCount > 0 }
            
            // Analyze by artist
            val artistPlayCounts = songsWithHistory.groupBy { it.artist }
                .mapValues { (_, songs) -> songs.sumOf { it.playCount } }
                .toList()
                .sortedByDescending { it.second }
            
            // Analyze by album
            val albumPlayCounts = songsWithHistory.groupBy { it.album }
                .mapValues { (_, songs) -> songs.sumOf { it.playCount } }
                .toList()
                .sortedByDescending { it.second }
            
            // Analyze by genre
            val genrePlayCounts = songsWithHistory.groupBy { it.genre ?: "Unknown" }
                .mapValues { (_, songs) -> songs.sumOf { it.playCount } }
                .toList()
                .sortedByDescending { it.second }
            
            // Analyze by year
            val yearPlayCounts = songsWithHistory.groupBy { it.year }
                .mapValues { (_, songs) -> songs.sumOf { it.playCount } }
                .toList()
                .sortedByDescending { it.second }
            
            val analysis = ListeningHabitsAnalysis(
                topArtists = artistPlayCounts.take(10),
                topAlbums = albumPlayCounts.take(10),
                topGenres = genrePlayCounts.take(10),
                topYears = yearPlayCounts.take(10),
                repeatListenerPercentage = calculateRepeatListenerPercentage(songsWithHistory),
                explorationRate = calculateExplorationRate(allSongs, songsWithHistory)
            )
            
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search in play history
     */
    suspend fun searchHistory(query: String): Result<List<Song>> {
        return try {
            val songsWithHistory = getSongsWithHistory().getOrNull() ?: emptyList()
            val filteredSongs = songsWithHistory.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }.sortedByDescending { it.lastPlayed }
            
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    private fun getTodayStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getWeekStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getMonthStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun calculateEstimatedListeningTime(songs: List<Song>): Long {
        return songs.sumOf { song ->
            (song.duration * song.playCount).toLong()
        }
    }
    
    private fun calculateRepeatListenerPercentage(songsWithHistory: List<Song>): Float {
        if (songsWithHistory.isEmpty()) return 0f
        val repeatSongs = songsWithHistory.count { it.playCount > 1 }
        return (repeatSongs.toFloat() / songsWithHistory.size) * 100f
    }
    
    private fun calculateExplorationRate(allSongs: List<Song>, songsWithHistory: List<Song>): Float {
        if (allSongs.isEmpty()) return 0f
        return (songsWithHistory.size.toFloat() / allSongs.size) * 100f
    }
}

/**
 * Data class for play history statistics
 */
data class PlayHistoryStatistics(
    val totalSongs: Int,
    val songsWithHistory: Int,
    val neverPlayedSongs: Int,
    val totalPlayCount: Int,
    val averagePlayCount: Double,
    val mostPlayedSong: Song?,
    val recentlyPlayedSong: Song?,
    val oldestHistoryEntry: Song?,
    val estimatedListeningTimeMs: Long
) {
    val historyPercentage: Float
        get() = if (totalSongs > 0) (songsWithHistory.toFloat() / totalSongs) * 100f else 0f
    
    val neverPlayedPercentage: Float
        get() = if (totalSongs > 0) (neverPlayedSongs.toFloat() / totalSongs) * 100f else 0f
    
    val estimatedListeningTimeHours: Double
        get() = estimatedListeningTimeMs / (1000.0 * 60.0 * 60.0)
    
    val formattedListeningTime: String
        get() {
            val hours = (estimatedListeningTimeHours).toInt()
            val minutes = ((estimatedListeningTimeHours % 1) * 60).toInt()
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

/**
 * Data class for listening habits analysis
 */
data class ListeningHabitsAnalysis(
    val topArtists: List<Pair<String, Int>>,
    val topAlbums: List<Pair<String, Int>>,
    val topGenres: List<Pair<String, Int>>,
    val topYears: List<Pair<Int, Int>>,
    val repeatListenerPercentage: Float,
    val explorationRate: Float
) {
    val favoriteArtist: String?
        get() = topArtists.firstOrNull()?.first
    
    val favoriteGenre: String?
        get() = topGenres.firstOrNull()?.first
    
    val favoriteDecade: String?
        get() {
            val topYear = topYears.firstOrNull()?.first ?: return null
            val decade = (topYear / 10) * 10
            return "${decade}s"
        }
}
