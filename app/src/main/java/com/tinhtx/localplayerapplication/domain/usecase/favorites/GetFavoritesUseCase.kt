package com.tinhtx.localplayerapplication.domain.usecase.favorites

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting favorite songs
 */
class GetFavoritesUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Get all favorite songs
     */
    suspend fun execute(): Result<List<Song>> {
        return try {
            val favorites = musicRepository.getFavoriteSongs()
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs as Flow for reactive UI
     */
    fun executeFlow(): Flow<List<Song>> {
        return musicRepository.getFavoriteSongsFlow()
    }
    
    /**
     * Get favorite count
     */
    suspend fun getFavoriteCount(): Result<Int> {
        return try {
            val count = musicRepository.getFavoriteCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recently added favorites
     */
    suspend fun getRecentlyAddedFavorites(limit: Int = 20): Result<List<Song>> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val recentFavorites = allFavorites
                .sortedByDescending { it.dateAdded }
                .take(limit)
            Result.success(recentFavorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search within favorite songs
     */
    suspend fun searchFavorites(query: String): Result<List<Song>> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val filteredFavorites = allFavorites.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
            Result.success(filteredFavorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs by artist
     */
    suspend fun getFavoritesByArtist(artist: String): Result<List<Song>> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val artistFavorites = allFavorites.filter { it.artist == artist }
            Result.success(artistFavorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs by album
     */
    suspend fun getFavoritesByAlbum(album: String): Result<List<Song>> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val albumFavorites = allFavorites.filter { it.album == album }
            Result.success(albumFavorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get favorite songs duration
     */
    suspend fun getTotalFavoritesDuration(): Result<Long> {
        return try {
            val favorites = musicRepository.getFavoriteSongs()
            val totalDuration = favorites.sumOf { it.duration }
            Result.success(totalDuration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
