package com.tinhtx.localplayerapplication.domain.usecase.favorites

import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddToFavoritesUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(songId: Long): Result<Unit> {
        return try {
            playlistRepository.addToFavorites(songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
