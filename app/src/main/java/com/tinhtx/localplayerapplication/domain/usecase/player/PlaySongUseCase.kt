package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MediaRepository
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import javax.inject.Inject

class PlaySongUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(song: Song, source: String? = null): Result<Unit> {
        return try {
            // Update play count
            musicRepository.updatePlayCount(song.id)
            
            // Record play history
            mediaRepository.recordPlayHistory(
                songId = song.id,
                playDuration = 0L, // Will be updated when song completes/stops
                completionPercentage = 0f,
                source = source
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
