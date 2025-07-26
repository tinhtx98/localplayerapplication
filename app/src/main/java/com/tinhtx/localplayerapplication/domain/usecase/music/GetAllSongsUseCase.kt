package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(sortOrder: AppConstants.SortOrder = AppConstants.SortOrder.TITLE_ASC): Flow<List<Song>> {
        return musicRepository.getAllSongs().map { songs ->
            when (sortOrder) {
                AppConstants.SortOrder.TITLE_ASC -> songs.sortedBy { it.title.lowercase() }
                AppConstants.SortOrder.TITLE_DESC -> songs.sortedByDescending { it.title.lowercase() }
                AppConstants.SortOrder.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
                AppConstants.SortOrder.ARTIST_DESC -> songs.sortedByDescending { it.artist.lowercase() }
                AppConstants.SortOrder.ALBUM_ASC -> songs.sortedBy { it.album.lowercase() }
                AppConstants.SortOrder.ALBUM_DESC -> songs.sortedByDescending { it.album.lowercase() }
                AppConstants.SortOrder.DURATION_ASC -> songs.sortedBy { it.duration }
                AppConstants.SortOrder.DURATION_DESC -> songs.sortedByDescending { it.duration }
                AppConstants.SortOrder.DATE_ADDED_ASC -> songs.sortedBy { it.dateAdded }
                AppConstants.SortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
            }
        }
    }
}
