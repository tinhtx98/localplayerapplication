package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for removing songs from playlists
 */
class RemoveFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    
    /**
     * Remove a single song from playlist
     */
    suspend fun execute(playlistId: Long, songId: Long): Result<Unit> {
        return try {
            // Check if playlist exists
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            // Check if song exists in playlist
            val songExists = playlistRepository.checkIfSongInPlaylist(playlistId, songId)
            if (!songExists) {
                return Result.failure(Exception("Song not found in playlist"))
            }
            
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove multiple songs from playlist
     */
    suspend fun execute(playlistId: Long, songIds: List<Long>): Result<Int> {
        return try {
            if (songIds.isEmpty()) {
                return Result.failure(Exception("Song list is empty"))
            }
            
            // Check if playlist exists
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            // Filter songs that actually exist in playlist
            val existingSongs = songIds.filter { songId ->
                playlistRepository.checkIfSongInPlaylist(playlistId, songId)
            }
            
            if (existingSongs.isEmpty()) {
                return Result.failure(Exception("None of the songs exist in the playlist"))
            }
            
            playlistRepository.removeSongsFromPlaylist(playlistId, existingSongs)
            Result.success(existingSongs.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove song objects from playlist
     */
    suspend fun execute(playlistId: Long, songs: List<Song>): Result<Int> {
        return execute(playlistId, songs.map { it.id })
    }
    
    /**
     * Remove all songs from playlist (clear playlist)
     */
    suspend fun clearPlaylist(playlistId: Long): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                return Result.failure(Exception("Playlist not found"))
            }
            
            playlistRepository.removeAllSongsFromPlaylist(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove song from playlist by position
     */
    suspend fun removeByPosition(playlistId: Long, position: Int): Result<Unit> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            
            if (position < 0 || position >= songs.size) {
                return Result.failure(Exception("Invalid position: $position"))
            }
            
            val songToRemove = songs[position]
            execute(playlistId, songToRemove.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove song from multiple playlists
     */
    suspend fun removeSongFromMultiplePlaylists(
        songId: Long, 
        playlistIds: List<Long>
    ): Result<Map<Long, Boolean>> {
        val results = mutableMapOf<Long, Boolean>()
        
        playlistIds.forEach { playlistId ->
            try {
                val result = execute(playlistId, songId)
                results[playlistId] = result.isSuccess
            } catch (e: Exception) {
                results[playlistId] = false
            }
        }
        
        return Result.success(results)
    }
    
    /**
     * Remove duplicates from playlist
     */
    suspend fun removeDuplicates(playlistId: Long): Result<Int> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            
            // Find duplicates
            val seenSongIds = mutableSetOf<Long>()
            val duplicatePositions = mutableListOf<Int>()
            
            songs.forEachIndexed { index, song ->
                if (seenSongIds.contains(song.id)) {
                    duplicatePositions.add(index)
                } else {
                    seenSongIds.add(song.id)
                }
            }
            
            if (duplicatePositions.isEmpty()) {
                return Result.success(0)
            }
            
            // Remove duplicates (in reverse order to maintain positions)
            duplicatePositions.reversed().forEach { position ->
                removeByPosition(playlistId, position)
            }
            
            Result.success(duplicatePositions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs by artist from playlist
     */
    suspend fun removeSongsByArtist(playlistId: Long, artistName: String): Result<Int> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val artistSongs = songs.filter { it.artist.equals(artistName, ignoreCase = true) }
            
            if (artistSongs.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, artistSongs)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs by album from playlist
     */
    suspend fun removeSongsByAlbum(playlistId: Long, albumName: String): Result<Int> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val albumSongs = songs.filter { it.album.equals(albumName, ignoreCase = true) }
            
            if (albumSongs.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, albumSongs)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs shorter than specified duration
     */
    suspend fun removeShortSongs(playlistId: Long, minDurationSeconds: Int): Result<Int> {
        return try {
            val minDurationMs = minDurationSeconds * 1000L
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val shortSongs = songs.filter { it.duration < minDurationMs }
            
            if (shortSongs.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, shortSongs)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs longer than specified duration
     */
    suspend fun removeLongSongs(playlistId: Long, maxDurationSeconds: Int): Result<Int> {
        return try {
            val maxDurationMs = maxDurationSeconds * 1000L
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val longSongs = songs.filter { it.duration > maxDurationMs }
            
            if (longSongs.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, longSongs)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs with low play count
     */
    suspend fun removeUnplayedSongs(playlistId: Long): Result<Int> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val unplayedSongs = songs.filter { it.playCount == 0 }
            
            if (unplayedSongs.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, unplayedSongs)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove songs based on custom filter
     */
    suspend fun removeWithFilter(
        playlistId: Long,
        filter: (Song) -> Boolean
    ): Result<Int> {
        return try {
            val songs = playlistRepository.getSongsInPlaylist(playlistId)
            val songsToRemove = songs.filter(filter)
            
            if (songsToRemove.isEmpty()) {
                return Result.success(0)
            }
            
            val result = execute(playlistId, songsToRemove)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if can remove song from playlist
     */
    suspend fun canRemoveSong(playlistId: Long, songId: Long): Result<Boolean> {
        return try {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            if (playlist == null) {
                Result.success(false)
            } else {
                val songExists = playlistRepository.checkIfSongInPlaylist(playlistId, songId)
                Result.success(songExists)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
