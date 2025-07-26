package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long): Result<Unit> {
        return try {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
