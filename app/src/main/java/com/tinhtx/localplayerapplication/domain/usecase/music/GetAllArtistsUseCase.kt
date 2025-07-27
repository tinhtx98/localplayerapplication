package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all artists
 */
class GetAllArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get all artists
     */
    suspend fun execute(): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            Result.success(artists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all artists as Flow for reactive UI
     */
    fun executeFlow(): Flow<List<Artist>> {
        return musicRepository.getAllArtistsFlow()
    }
    
    /**
     * Get artists sorted by specified order
     */
    suspend fun execute(sortOrder: SortOrder): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val sortedArtists = when (sortOrder) {
                SortOrder.TITLE -> artists.sortedBy { it.name.lowercase() }
                SortOrder.ALBUM -> artists.sortedByDescending { it.albumCount }
                SortOrder.DATE_ADDED -> artists.sortedByDescending { it.id }
                SortOrder.DATE_MODIFIED -> artists.sortedByDescending { it.id }
                else -> artists.sortedBy { it.name.lowercase() }
            }
            Result.success(sortedArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search artists by query
     */
    suspend fun searchArtists(query: String): Result<List<Artist>> {
        return try {
            val artists = musicRepository.searchArtists(query)
            Result.success(artists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artists with minimum album count
     */
    suspend fun getArtistsWithMinAlbums(minAlbums: Int): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val filteredArtists = artists.filter { it.albumCount >= minAlbums }
            Result.success(filteredArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artists with minimum song count
     */
    suspend fun getArtistsWithMinSongs(minSongs: Int): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val filteredArtists = artists.filter { it.songCount >= minSongs }
            Result.success(filteredArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get top artists by song count
     */
    suspend fun getTopArtistsBySongCount(limit: Int = 10): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val topArtists = artists
                .sortedByDescending { it.songCount }
                .take(limit)
            Result.success(topArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get top artists by album count
     */
    suspend fun getTopArtistsByAlbumCount(limit: Int = 10): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val topArtists = artists
                .sortedByDescending { it.albumCount }
                .take(limit)
            Result.success(topArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artists by name pattern
     */
    suspend fun getArtistsByPattern(pattern: String): Result<List<Artist>> {
        return try {
            val artists = musicRepository.getAllArtists()
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            val matchingArtists = artists.filter { artist ->
                regex.containsMatchIn(artist.name)
            }
            Result.success(matchingArtists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get artist statistics
     */
    suspend fun getArtistStatistics(): Result<ArtistStatistics> {
        return try {
            val artists = musicRepository.getAllArtists()
            val totalArtists = artists.size
            val totalAlbums = artists.sumOf { it.albumCount }
            val totalSongs = artists.sumOf { it.songCount }
            val averageAlbumsPerArtist = if (totalArtists > 0) totalAlbums.toDouble() / totalArtists else 0.0
            val averageSongsPerArtist = if (totalArtists > 0) totalSongs.toDouble() / totalArtists else 0.0
            val mostProductiveArtist = artists.maxByOrNull { it.songCount }
            val mostAlbumsArtist = artists.maxByOrNull { it.albumCount }
            
            val statistics = ArtistStatistics(
                totalArtists = totalArtists,
                totalAlbums = totalAlbums,
                totalSongs = totalSongs,
                averageAlbumsPerArtist = averageAlbumsPerArtist,
                averageSongsPerArtist = averageSongsPerArtist,
                mostProductiveArtist = mostProductiveArtist?.name ?: "Unknown",
                mostAlbumsArtist = mostAlbumsArtist?.name ?: "Unknown"
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for artist statistics
 */
data class ArtistStatistics(
    val totalArtists: Int,
    val totalAlbums: Int,
    val totalSongs: Int,
    val averageAlbumsPerArtist: Double,
    val averageSongsPerArtist: Double,
    val mostProductiveArtist: String,
    val mostAlbumsArtist: String
)
