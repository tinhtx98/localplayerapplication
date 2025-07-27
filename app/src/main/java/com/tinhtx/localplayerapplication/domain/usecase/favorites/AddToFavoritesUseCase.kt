package com.tinhtx.localplayerapplication.domain.usecase.favorites

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case for adding songs to favorites
 */
class AddToFavoritesUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Add a single song to favorites
     */
    suspend fun execute(songId: Long): Result<Unit> {
        return try {
            musicRepository.updateFavoriteStatus(songId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add a song object to favorites
     */
    suspend fun execute(song: Song): Result<Unit> {
        return execute(song.id)
    }
    
    /**
     * Add multiple songs to favorites
     */
    suspend fun execute(songIds: List<Long>): Result<Unit> {
        return try {
            musicRepository.markSongsAsFavorite(songIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add multiple song objects to favorites
     */
    suspend fun execute(songs: List<Song>): Result<Unit> {
        return execute(songs.map { it.id })
    }
    
    /**
     * Toggle favorite status of a song
     */
    suspend fun toggle(songId: Long): Result<Boolean> {
        return try {
            val song = musicRepository.getSongById(songId)
            if (song != null) {
                val newFavoriteStatus = !song.isFavorite
                musicRepository.updateFavoriteStatus(songId, newFavoriteStatus)
                Result.success(newFavoriteStatus)
            } else {
                Result.failure(Exception("Song not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if song is already in favorites
     */
    suspend fun isFavorite(songId: Long): Boolean {
        return try {
            val song = musicRepository.getSongById(songId)
            song?.isFavorite ?: false
        } catch (e: Exception) {
            false
        }
    }
}
