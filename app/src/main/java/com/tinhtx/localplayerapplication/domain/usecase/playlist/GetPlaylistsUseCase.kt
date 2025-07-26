package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists().map { playlists ->
            playlists.sortedBy { it.name.lowercase() }
        }
    }
    
    fun getFavoritePlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getFavoritePlaylists()
    }
    
    fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistRepository.searchPlaylists(query)
    }
}
