package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSongsByAlbumUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(albumId: Long): Flow<List<Song>> {
        return musicRepository.getSongsByAlbum(albumId).map { songs ->
            songs.sortedBy { it.track }
        }
    }
}
