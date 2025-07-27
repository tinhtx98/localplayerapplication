package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.SortOrder
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all albums
 */
class GetAllAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get all albums
     */
    suspend fun execute(): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAllAlbums()
            Result.success(albums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all albums as Flow for reactive UI
     */
    fun executeFlow(): Flow<List<Album>> {
        return musicRepository.getAllAlbumsFlow()
    }
    
    /**
     * Get albums sorted by specified order
     */
    suspend fun execute(sortOrder: SortOrder): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAllAlbums()
            val sortedAlbums = when (sortOrder) {
                SortOrder.TITLE -> albums.sortedBy { it.name.lowercase() }
                SortOrder.ARTIST -> albums.sortedBy { it.artist.lowercase() }
                SortOrder.YEAR -> albums.sortedByDescending { it.year }
                SortOrder.DATE_ADDED -> albums.sortedByDescending { it.id }
                SortOrder.DATE_MODIFIED -> albums.sortedByDescending { it.id }
                else -> albums.sortedBy { it.name.lowercase() }
            }
            Result.success(sortedAlbums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get albums by specific artist
     */
    suspend fun getAlbumsByArtist(artist: String): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAlbumsByArtist(artist)
            Result.success(albums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search albums by query
     */
    suspend fun searchAlbums(query: String): Result<List<Album>> {
        return try {
            val albums = musicRepository.searchAlbums(query)
            Result.success(albums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get albums by year range
     */
    suspend fun getAlbumsByYearRange(startYear: Int, endYear: Int): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAllAlbums()
            val filteredAlbums = albums.filter { album ->
                album.year in startYear..endYear
            }
            Result.success(filteredAlbums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get albums with minimum song count
     */
    suspend fun getAlbumsWithMinSongs(minSongs: Int): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAllAlbums()
            val filteredAlbums = albums.filter { it.songCount >= minSongs }
            Result.success(filteredAlbums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently added albums
     */
    suspend fun getRecentlyAddedAlbums(limit: Int = 20): Result<List<Album>> {
        return try {
            val albums = musicRepository.getAllAlbums()
            val recentAlbums = albums
                .sortedByDescending { it.id }
                .take(limit)
            Result.success(recentAlbums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get album statistics
     */
    suspend fun getAlbumStatistics(): Result<AlbumStatistics> {
        return try {
            val albums = musicRepository.getAllAlbums()
            val totalAlbums = albums.size
            val totalSongs = albums.sumOf { it.songCount }
            val averageSongsPerAlbum = if (totalAlbums > 0) totalSongs.toDouble() / totalAlbums else 0.0
            val oldestYear = albums.minOfOrNull { it.year } ?: 0
            val newestYear = albums.maxOfOrNull { it.year } ?: 0
            val mostPopularArtist = albums.groupingBy { it.artist }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "Unknown"
            
            val statistics = AlbumStatistics(
                totalAlbums = totalAlbums,
                totalSongs = totalSongs,
                averageSongsPerAlbum = averageSongsPerAlbum,
                oldestYear = oldestYear,
                newestYear = newestYear,
                mostPopularArtist = mostPopularArtist
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for album statistics
 */
data class AlbumStatistics(
    val totalAlbums: Int,
    val totalSongs: Int,
    val averageSongsPerAlbum: Double,
    val oldestYear: Int,
    val newestYear: Int,
    val mostPopularArtist: String
)
