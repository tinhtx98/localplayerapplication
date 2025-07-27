package com.tinhtx.localplayerapplication.domain.usecase.playlist

import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case for creating playlists
 */
class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    
    /**
     * Create a new empty playlist
     */
    suspend fun execute(name: String, description: String? = null): Result<Long> {
        return try {
            if (name.isBlank()) {
                return Result.failure(Exception("Playlist name cannot be empty"))
            }
            
            // Check if playlist with same name already exists
            val existingPlaylist = playlistRepository.getPlaylistByName(name)
            if (existingPlaylist != null) {
                return Result.failure(Exception("Playlist with name '$name' already exists"))
            }
            
            val playlist = Playlist(
                name = name.trim(),
                description = description?.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val playlistId = playlistRepository.insertPlaylist(playlist)
            Result.success(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist with initial songs
     */
    suspend fun execute(name: String, songs: List<Song>, description: String? = null): Result<Long> {
        return try {
            // First create empty playlist
            val createResult = execute(name, description)
            if (createResult.isFailure) {
                return createResult
            }
            
            val playlistId = createResult.getOrNull()!!
            
            // Add songs to playlist
            if (songs.isNotEmpty()) {
                val songIds = songs.map { it.id }
                playlistRepository.addSongsToPlaylist(playlistId, songIds)
            }
            
            Result.success(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist with song IDs
     */
    suspend fun execute(name: String, songIds: List<Long>, description: String? = null): Result<Long> {
        return try {
            // Create empty playlist first
            val createResult = execute(name, description)
            if (createResult.isFailure) {
                return createResult
            }
            
            val playlistId = createResult.getOrNull()!!
            
            // Add songs to playlist
            if (songIds.isNotEmpty()) {
                playlistRepository.addSongsToPlaylist(playlistId, songIds)
            }
            
            Result.success(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist from album
     */
    suspend fun createFromAlbum(albumName: String, albumSongs: List<Song>): Result<Long> {
        return try {
            val playlistName = "Album: $albumName"
            val description = "Created from album '$albumName'"
            
            execute(playlistName, albumSongs, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist from artist
     */
    suspend fun createFromArtist(artistName: String, artistSongs: List<Song>): Result<Long> {
        return try {
            val playlistName = "Artist: $artistName"
            val description = "Created from artist '$artistName'"
            
            execute(playlistName, artistSongs, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist from favorites
     */
    suspend fun createFromFavorites(favoriteSongs: List<Song>): Result<Long> {
        return try {
            val playlistName = "My Favorites"
            val description = "Created from favorite songs"
            
            execute(playlistName, favoriteSongs, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create playlist from current queue
     */
    suspend fun createFromQueue(queueSongs: List<Song>, customName: String? = null): Result<Long> {
        return try {
            val playlistName = customName ?: "Queue - ${getCurrentDateString()}"
            val description = "Created from current playback queue"
            
            execute(playlistName, queueSongs, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate unique playlist name
     */
    suspend fun generateUniqueName(baseName: String): Result<String> {
        return try {
            var counter = 1
            var uniqueName = baseName
            
            while (playlistRepository.getPlaylistByName(uniqueName) != null) {
                uniqueName = "$baseName ($counter)"
                counter++
                
                if (counter > 100) {
                    return Result.failure(Exception("Unable to generate unique name"))
                }
            }
            
            Result.success(uniqueName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create smart playlist (most played, recently added, etc.)
     */
    suspend fun createSmartPlaylist(type: SmartPlaylistType, songs: List<Song>): Result<Long> {
        return try {
            val (name, description) = when (type) {
                SmartPlaylistType.MOST_PLAYED -> "Most Played" to "Your most played songs"
                SmartPlaylistType.RECENTLY_ADDED -> "Recently Added" to "Recently added songs"
                SmartPlaylistType.RECENTLY_PLAYED -> "Recently Played" to "Recently played songs"
                SmartPlaylistType.NEVER_PLAYED -> "Never Played" to "Songs you haven't played yet"
                SmartPlaylistType.RANDOM_MIX -> "Random Mix" to "Random selection of songs"
            }
            
            execute(name, songs, description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Duplicate existing playlist
     */
    suspend fun duplicatePlaylist(sourcePlaylistId: Long, newName: String? = null): Result<Long> {
        return try {
            val sourcePlaylist = playlistRepository.getPlaylistById(sourcePlaylistId)
            if (sourcePlaylist == null) {
                return Result.failure(Exception("Source playlist not found"))
            }
            
            val duplicateName = newName ?: generateUniqueName("${sourcePlaylist.name} - Copy").getOrNull()
            if (duplicateName == null) {
                return Result.failure(Exception("Unable to generate name for duplicate"))
            }
            
            val duplicateId = playlistRepository.duplicatePlaylist(sourcePlaylistId, duplicateName)
            Result.success(duplicateId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate playlist name
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name cannot be empty")
            name.length > 100 -> ValidationResult(false, "Name too long (max 100 characters)")
            name.contains(Regex("[<>:\"/\\\\|?*]")) -> ValidationResult(false, "Name contains invalid characters")
            else -> ValidationResult(true, "Valid name")
        }
    }
    
    // Private helper methods
    private fun getCurrentDateString(): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}

/**
 * Smart playlist types
 */
enum class SmartPlaylistType {
    MOST_PLAYED,
    RECENTLY_ADDED,
    RECENTLY_PLAYED,
    NEVER_PLAYED,
    RANDOM_MIX
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)
