package com.tinhtx.localplayerapplication.domain.repository

import android.graphics.Bitmap
import com.tinhtx.localplayerapplication.data.local.media.AudioMetadataExtractor
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    
    // Album Art
    suspend fun getAlbumArt(albumId: Long, filePath: String? = null): Bitmap?
    suspend fun preloadAlbumArt(albumIds: List<Long>)
    
    // Metadata
    suspend fun extractAudioMetadata(filePath: String): AudioMetadataExtractor.ExtractedMetadata?
    
    // Play History
    suspend fun recordPlayHistory(
        songId: Long,
        playDuration: Long,
        completionPercentage: Float,
        source: String? = null
    )
    fun getPlayHistory(limit: Int = 50): Flow<List<Song>>
    suspend fun clearPlayHistory()
    suspend fun getAverageCompletionRate(songId: Long): Float
    suspend fun getPlayCountSince(songId: Long, since: Long): Int
    
    // Cache Management
    suspend fun clearImageCache()
    suspend fun getCacheSize(): Long
}
