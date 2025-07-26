package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSongsByArtistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(artistId: Long): Flow<List<Song>> {
        return musicRepository.getSongsByArtist(artistId).map { songs ->
            songs.sortedWith(compareBy({ it.album }, { it.track }, { it.title }))
        }
    }
}
