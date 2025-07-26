package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long): Result<Unit> {
        return try {
            playlistRepository.addSongToPlaylist(playlistId, songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addMultipleSongs(playlistId: Long, songIds: List<Long>): Result<Unit> {
        return try {
            songIds.forEach { songId ->
                playlistRepository.addSongToPlaylist(playlistId, songId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
