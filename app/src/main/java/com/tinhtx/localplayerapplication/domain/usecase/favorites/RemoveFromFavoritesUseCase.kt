package com.tinhtx.localplayerapplication.domain.usecase.favorites

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for removing songs from favorites
 */
class RemoveFromFavoritesUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Remove a single song from favorites
     */
    suspend fun execute(songId: Long): Result<Unit> {
        return try {
            musicRepository.updateFavoriteStatus(songId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a song object from favorites
     */
    suspend fun execute(song: Song): Result<Unit> {
        return execute(song.id)
    }
    
    /**
     * Remove multiple songs from favorites
     */
    suspend fun execute(songIds: List<Long>): Result<Unit> {
        return try {
            musicRepository.unmarkSongsAsFavorite(songIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove multiple song objects from favorites
     */
    suspend fun execute(songs: List<Song>): Result<Unit> {
        return execute(songs.map { it.id })
    }
    
    /**
     * Remove all favorites (clear favorites)
     */
    suspend fun clearAllFavorites(): Result<Unit> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val favoriteIds = allFavorites.map { it.id }
            musicRepository.unmarkSongsAsFavorite(favoriteIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove favorites by artist
     */
    suspend fun removeByArtist(artist: String): Result<Int> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val artistFavorites = allFavorites.filter { it.artist == artist }
            val favoriteIds = artistFavorites.map { it.id }
            
            if (favoriteIds.isNotEmpty()) {
                musicRepository.unmarkSongsAsFavorite(favoriteIds)
            }
            
            Result.success(favoriteIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove favorites by album
     */
    suspend fun removeByAlbum(album: String): Result<Int> {
        return try {
            val allFavorites = musicRepository.getFavoriteSongs()
            val albumFavorites = allFavorites.filter { it.album == album }
            val favoriteIds = albumFavorites.map { it.id }
            
            if (favoriteIds.isNotEmpty()) {
                musicRepository.unmarkSongsAsFavorite(favoriteIds)
            }
            
            Result.success(favoriteIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove old favorites (older than specified days)
     */
    suspend fun removeOldFavorites(olderThanDays: Int): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            val allFavorites = musicRepository.getFavoriteSongs()
            val oldFavorites = allFavorites.filter { it.dateAdded < cutoffTime }
            val favoriteIds = oldFavorites.map { it.id }
            
            if (favoriteIds.isNotEmpty()) {
                musicRepository.unmarkSongsAsFavorite(favoriteIds)
            }
            
            Result.success(favoriteIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
