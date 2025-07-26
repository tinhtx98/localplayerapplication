package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Result<Long> {
        return try {
            if (name.isBlank()) {
                Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
            } else {
                val playlistId = playlistRepository.createPlaylist(name.trim(), description?.trim())
                Result.success(playlistId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
