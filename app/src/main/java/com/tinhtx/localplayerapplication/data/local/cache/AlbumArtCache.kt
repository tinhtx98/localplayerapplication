package com.tinhtx.localplayerapplication.data.local.cache

import android.graphics.Bitmap
import androidx.collection.LruCache
import com.tinhtx.localplayerapplication.core.constants.MediaConstants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumArtCache @Inject constructor() {
    
    private val memoryCache: LruCache<String, Bitmap>
    
    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // Use 1/8th of available memory for cache
        
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
    
    fun put(key: String, bitmap: Bitmap) {
        if (get(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }
    
    fun get(key: String): Bitmap? {
        return memoryCache.get(key)
    }
    
    fun putAlbumArt(albumId: Long, bitmap: Bitmap) {
        put("album_$albumId", bitmap)
    }
    
    fun getAlbumArt(albumId: Long): Bitmap? {
        return get("album_$albumId")
    }
    
    fun putSongArt(songId: Long, bitmap: Bitmap) {
        put("song_$songId", bitmap)
    }
    
    fun getSongArt(songId: Long): Bitmap? {
        return get("song_$songId")
    }
    
    fun putArtistArt(artistId: Long, bitmap: Bitmap) {
        put("artist_$artistId", bitmap)
    }
    
    fun getArtistArt(artistId: Long): Bitmap? {
        return get("artist_$artistId")
    }
    
    fun remove(key: String) {
        memoryCache.remove(key)
    }
    
    fun clear() {
        memoryCache.evictAll()
    }
    
    fun size(): Int {
        return memoryCache.size()
    }
    
    fun maxSize(): Int {
        return memoryCache.maxSize()
    }
    
    fun hitCount(): Long {
        return memoryCache.hitCount()
    }
    
    fun missCount(): Long {
        return memoryCache.missCount()
    }
    
    fun evictionCount(): Long {
        return memoryCache.evictionCount()
    }
    
    companion object {
        const val DEFAULT_CACHE_SIZE = 1024 * 1024 * 4 // 4MB
    }
}
