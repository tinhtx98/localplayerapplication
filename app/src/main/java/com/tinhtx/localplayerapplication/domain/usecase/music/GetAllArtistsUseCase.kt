package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    operator fun invoke(): Flow<List<Artist>> = flow {
        try {
            val artists = musicRepository.getAllArtists()
            emit(artists)
        } catch (exception: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun execute(): List<Artist> {
        return try {
            musicRepository.getAllArtists()
        } catch (exception: Exception) {
            emptyList()
        }
    }
}
