package com.tinhtx.localplayerapplication.domain.usecase.favorites

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return playlistRepository.getFavoriteSongs()
    }
    
    suspend fun isFavorite(songId: Long): Boolean {
        return playlistRepository.isSongFavorite(songId)
    }
}
