package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

class DeletePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long): Result<Unit> {
        return try {
            playlistRepository.deletePlaylist(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
