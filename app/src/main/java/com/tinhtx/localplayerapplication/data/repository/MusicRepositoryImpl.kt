package com.tinhtx.localplayerapplication.data.repository

import com.tinhtx.localplayerapplication.core.di.IoDispatcher
import com.tinhtx.localplayerapplication.data.local.cache.AlbumArtCache
import com.tinhtx.localplayerapplication.data.local.cache.ImageCacheManager
import com.tinhtx.localplayerapplication.data.local.database.dao.HistoryDao
import com.tinhtx.localplayerapplication.data.local.database.entities.HistoryEntity
import com.tinhtx.localplayerapplication.data.local.media.AudioMetadataExtractor
import com.tinhtx.localplayerapplication.domain.repository.MediaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import com.tinhtx.localplayerapplication.data.local.database.entities.toDomain
import com.tinhtx.localplayerapplication.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val imageCacheManager: ImageCacheManager,
    private val albumArtCache: AlbumArtCache,
    private val audioMetadataExtractor: AudioMetadataExtractor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MediaRepository {

    override suspend fun getAlbumArt(albumId: Long, filePath: String?): Bitmap? = withContext(ioDispatcher) {
        // Try memory cache first
        albumArtCache.getAlbumArt(albumId)?.let { return@withContext it }

        // Try disk cache and MediaStore
        val bitmap = imageCacheManager.getAlbumArt(albumId, filePath)
        bitmap?.let { albumArtCache.putAlbumArt(albumId, it) }

        bitmap
    }

    override suspend fun extractAudioMetadata(filePath: String): AudioMetadataExtractor.ExtractedMetadata? = withContext(ioDispatcher) {
        audioMetadataExtractor.extractMetadata(filePath)
    }

    override suspend fun recordPlayHistory(
        songId: Long,
        playDuration: Long,
        completionPercentage: Float,
        source: String?
    ) = withContext(ioDispatcher) {
        val history = HistoryEntity(
            songId = songId,
            playedAt = System.currentTimeMillis(),
            completionPercentage = completionPercentage,
            playDuration = playDuration,
            source = source
        )
        historyDao.insertHistory(history)
    }

    override fun getPlayHistory(limit: Int): Flow<List<Song>> {
        return historyDao.getRecentlyPlayedSongs(limit).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun clearPlayHistory() = withContext(ioDispatcher) {
        historyDao.deleteAllHistory()
    }

    override suspend fun clearImageCache() = withContext(ioDispatcher) {
        imageCacheManager.clearCache()
        albumArtCache.clear()
    }

    override suspend fun getCacheSize(): Long = withContext(ioDispatcher) {
        imageCacheManager.getCacheSize()
    }

    override suspend fun preloadAlbumArt(albumIds: List<Long>) = withContext(ioDispatcher) {
        imageCacheManager.preloadAlbumArt(albumIds)
    }

    override suspend fun getAverageCompletionRate(songId: Long): Float = withContext(ioDispatcher) {
        historyDao.getAverageCompletionRate(songId)
    }

    override suspend fun getPlayCountSince(songId: Long, since: Long): Int = withContext(ioDispatcher) {
        historyDao.getPlayCountSince(songId, since)
    }
}
