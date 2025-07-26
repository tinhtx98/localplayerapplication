package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.data.local.media.MediaScanner
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanMediaLibraryUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun performFullScan() {
        musicRepository.scanMediaLibrary()
        userPreferencesRepository.updateLastScanTime(System.currentTimeMillis())
    }
    
    suspend fun performIncrementalScan() {
        musicRepository.incrementalScan()
        userPreferencesRepository.updateLastScanTime(System.currentTimeMillis())
    }
    
    fun getScanProgress(): Flow<MediaScanner.ScanProgress> {
        return musicRepository.getScanProgress()
    }
    
    fun getScanComplete(): Flow<MediaScanner.ScanResult> {
        return musicRepository.getScanComplete()
    }
}
