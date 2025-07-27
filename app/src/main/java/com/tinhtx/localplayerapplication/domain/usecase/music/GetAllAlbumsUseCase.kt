package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    operator fun invoke(): Flow<List<Album>> = flow {
        try {
            val albums = musicRepository.getAllAlbums()
            emit(albums)
        } catch (exception: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun execute(): List<Album> {
        return try {
            musicRepository.getAllAlbums()
        } catch (exception: Exception) {
            emptyList()
        }
    }
}
