package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(query: String): Flow<List<Song>> {
        return if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            musicRepository.searchSongs(query.trim())
        }
    }
    
    fun searchWithFilters(
        query: String,
        filterByArtist: String? = null,
        filterByAlbum: String? = null,
        minDuration: Long? = null,
        maxDuration: Long? = null
    ): Flow<List<Song>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }
        
        musicRepository.searchSongs(query).collect { songs ->
            val filteredSongs = songs.filter { song ->
                val matchesArtist = filterByArtist?.let { 
                    song.artist.contains(it, ignoreCase = true) 
                } ?: true
                
                val matchesAlbum = filterByAlbum?.let { 
                    song.album.contains(it, ignoreCase = true) 
                } ?: true
                
                val matchesMinDuration = minDuration?.let { 
                    song.duration >= it 
                } ?: true
                
                val matchesMaxDuration = maxDuration?.let { 
                    song.duration <= it 
                } ?: true
                
                matchesArtist && matchesAlbum && matchesMinDuration && matchesMaxDuration
            }
            
            emit(filteredSongs)
        }
    }
}
