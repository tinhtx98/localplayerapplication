package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for deleting playlists
 */
class DeletePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    
    /**
     * Delete playlist by ID
     */
    suspend fun execute(playlistId: Long): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            // Check if it's a system playlist
            if (playlist.isSystemPlaylist) {
                return Result.failure(Exception("Cannot delete system playlist"))
            }
            
            playlistRepository.deletePlaylistById(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete playlist object
     */
    suspend fun execute(playlist: Playlist): Result<Unit> {
        return execute(playlist.id)
    }
    
    /**
     * Delete playlist by name
     */
    suspend fun deleteByName(playlistName: String): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistByName(playlistName)
            if (playlist == null) {
                return Result.failure(Exception("Playlist '$playlistName' not found"))
            }
            
            execute(playlist.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete multiple playlists
     */
    suspend fun execute(playlistIds: List<Long>): Result<DeleteResult> {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()
        
        playlistIds.forEach { playlistId ->
            try {
                val result = execute(playlistId)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failureCount++
                    errors.add("Playlist $playlistId: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("Playlist $playlistId: ${e.message}")
            }
        }
        
        val deleteResult = DeleteResult(
            totalRequested = playlistIds.size,
            successCount = successCount,
            failureCount = failureCount,
            errors = errors
        )
        
        return Result.success(deleteResult)
    }
    
    /**
     * Delete multiple playlist objects
     */
    suspend fun execute(playlists: List<Playlist>): Result<DeleteResult> {
        return execute(playlists.map { it.id })
    }
    
    /**
     * Delete empty playlists
     */
    suspend fun deleteEmptyPlaylists(): Result<Int> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val emptyPlaylists = allPlaylists.filter { it.songCount == 0 && !it.isSystemPlaylist }
            
            if (emptyPlaylists.isEmpty()) {
                return Result.success(0)
            }
            
            val deleteResult = execute(emptyPlaylists)
            if (deleteResult.isSuccess) {
                Result.success(deleteResult.getOrNull()?.successCount ?: 0)
            } else {
                Result.failure(deleteResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete old playlists (not accessed for specified days)
     */
    suspend fun deleteOldPlaylists(olderThanDays: Int): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            val allPlaylists = playlistRepository.getAllPlaylists()
            
            val oldPlaylists = allPlaylists.filter { playlist ->
                !playlist.isSystemPlaylist && playlist.updatedAt < cutoffTime
            }
            
            if (oldPlaylists.isEmpty()) {
                return Result.success(0)
            }
            
            val deleteResult = execute(oldPlaylists)
            if (deleteResult.isSuccess) {
                Result.success(deleteResult.getOrNull()?.successCount ?: 0)
            } else {
                Result.failure(deleteResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Soft delete (mark as deleted but don't remove)
     */
    suspend fun softDelete(playlistId: Long): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            if (playlist.isSystemPlaylist) {
                return Result.failure(Exception("Cannot delete system playlist"))
            }
            
            // TODO: Implement soft delete functionality
            // This would require adding a 'deleted' field to playlist entity
            // For now, just perform hard delete
            execute(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if playlist can be deleted
     */
    suspend fun canDelete(playlistId: Long): Result<Boolean> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            when {
                playlist == null -> Result.success(false)
                playlist.isSystemPlaylist -> Result.success(false)
                else -> Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get deletable playlists
     */
    suspend fun getDeletablePlaylists(): Result<List<Playlist>> {
        return try {
            val allPlaylists = playlistRepository.getAllPlaylists()
            val deletablePlaylists = allPlaylists.filter { !it.isSystemPlaylist }
            Result.success(deletablePlaylists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete with confirmation
     */
    suspend fun deleteWithConfirmation(
        playlistId: Long,
        confirmationCallback: suspend (Playlist) -> Boolean
    ): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            val confirmed = confirmationCallback(playlist)
            if (!confirmed) {
                return Result.failure(Exception("Deletion cancelled by user"))
            }
            
            execute(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cleanup orphaned references after deletion
     */
    suspend fun cleanupOrphanedReferences(): Result<Unit> {
        return try {
            playlistRepository.removeOrphanedPlaylistSongReferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for batch delete results
 */
data class DeleteResult(
    val totalRequested: Int,
    val successCount: Int,
    val failureCount: Int,
    val errors: List<String>
) {
    val isCompleteSuccess: Boolean
        get() = failureCount == 0
    
    val hasPartialSuccess: Boolean
        get() = successCount > 0 && failureCount > 0
    
    val successRate: Float
        get() = if (totalRequested > 0) (successCount.toFloat() / totalRequested) * 100f else 0f
}
