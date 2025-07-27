package com.tinhtx.localplayerapplication.domain.usecase.music

import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching songs, albums, and artists
 */
class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    
    /**
     * Search songs by query
     */
    suspend fun searchSongs(query: String): Result<List<Song>> {
        return try {
            if (query.isBlank()) {
                Result.success(emptyList())
            } else {
                val songs = musicRepository.searchSongs(query)
                Result.success(songs)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search songs with Flow for reactive UI
     */
    fun searchSongsFlow(query: String): Flow<List<Song>> {
        return musicRepository.searchSongsFlow(query)
    }
    
    /**
     * Search albums by query
     */
    suspend fun searchAlbums(query: String): Result<List<Album>> {
        return try {
            if (query.isBlank()) {
                Result.success(emptyList())
            } else {
                val albums = musicRepository.searchAlbums(query)
                Result.success(albums)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search artists by query
     */
    suspend fun searchArtists(query: String): Result<List<Artist>> {
        return try {
            if (query.isBlank()) {
                Result.success(emptyList())
            } else {
                val artists = musicRepository.searchArtists(query)
                Result.success(artists)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Comprehensive search (songs, albums, artists)
     */
    suspend fun searchAll(query: String): Result<SearchResults> {
        return try {
            if (query.isBlank()) {
                Result.success(SearchResults())
            } else {
                val songs = musicRepository.searchSongs(query)
                val albums = musicRepository.searchAlbums(query)
                val artists = musicRepository.searchArtists(query)
                
                val results = SearchResults(
                    songs = songs,
                    albums = albums,
                    artists = artists,
                    query = query
                )
                
                Result.success(results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search with filters
     */
    suspend fun searchWithFilters(
        query: String,
        filters: SearchFilters
    ): Result<SearchResults> {
        return try {
            if (query.isBlank()) {
                Result.success(SearchResults())
            } else {
                val allSongs = musicRepository.searchSongs(query)
                val allAlbums = musicRepository.searchAlbums(query)
                val allArtists = musicRepository.searchArtists(query)
                
                val filteredSongs = applyFiltersToSongs(allSongs, filters)
                val filteredAlbums = applyFiltersToAlbums(allAlbums, filters)
                val filteredArtists = applyFiltersToArtists(allArtists, filters)
                
                val results = SearchResults(
                    songs = filteredSongs,
                    albums = filteredAlbums,
                    artists = filteredArtists,
                    query = query
                )
                
                Result.success(results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get search suggestions based on input
     */
    suspend fun getSearchSuggestions(input: String, limit: Int = 10): Result<List<String>> {
        return try {
            if (input.length < 2) {
                Result.success(emptyList())
            } else {
                val songs = musicRepository.getAllSongs()
                val suggestions = mutableSetOf<String>()
                
                songs.forEach { song ->
                    if (song.title.contains(input, ignoreCase = true)) {
                        suggestions.add(song.title)
                    }
                    if (song.artist.contains(input, ignoreCase = true)) {
                        suggestions.add(song.artist)
                    }
                    if (song.album.contains(input, ignoreCase = true)) {
                        suggestions.add(song.album)
                    }
                }
                
                val sortedSuggestions = suggestions
                    .sortedBy { it.lowercase() }
                    .take(limit)
                
                Result.success(sortedSuggestions)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Advanced search with multiple criteria
     */
    suspend fun advancedSearch(criteria: SearchCriteria): Result<List<Song>> {
        return try {
            val allSongs = musicRepository.getAllSongs()
            
            val filteredSongs = allSongs.filter { song ->
                var matches = true
                
                criteria.title?.let { title ->
                    matches = matches && song.title.contains(title, ignoreCase = true)
                }
                
                criteria.artist?.let { artist ->
                    matches = matches && song.artist.contains(artist, ignoreCase = true)
                }
                
                criteria.album?.let { album ->
                    matches = matches && song.album.contains(album, ignoreCase = true)
                }
                
                criteria.genre?.let { genre ->
                    matches = matches && (song.genre?.contains(genre, ignoreCase = true) ?: false)
                }
                
                criteria.yearRange?.let { (startYear, endYear) ->
                    matches = matches && song.year in startYear..endYear
                }
                
                criteria.durationRange?.let { (minDuration, maxDuration) ->
                    matches = matches && song.duration in minDuration..maxDuration
                }
                
                criteria.favoritesOnly?.let { favoritesOnly ->
                    if (favoritesOnly) {
                        matches = matches && song.isFavorite
                    }
                }
                
                matches
            }
            
            Result.success(filteredSongs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    private fun applyFiltersToSongs(songs: List<Song>, filters: SearchFilters): List<Song> {
        return songs.filter { song ->
            var matches = true
            
            filters.yearRange?.let { (startYear, endYear) ->
                matches = matches && song.year in startYear..endYear
            }
            
            filters.durationRange?.let { (minDuration, maxDuration) ->
                matches = matches && song.duration in minDuration..maxDuration
            }
            
            filters.favoritesOnly?.let { favoritesOnly ->
                if (favoritesOnly) {
                    matches = matches && song.isFavorite
                }
            }
            
            filters.genre?.let { genre ->
                matches = matches && (song.genre?.equals(genre, ignoreCase = true) ?: false)
            }
            
            matches
        }
    }
    
    private fun applyFiltersToAlbums(albums: List<Album>, filters: SearchFilters): List<Album> {
        return albums.filter { album ->
            var matches = true
            
            filters.yearRange?.let { (startYear, endYear) ->
                matches = matches && album.year in startYear..endYear
            }
            
            matches
        }
    }
    
    private fun applyFiltersToArtists(artists: List<Artist>, filters: SearchFilters): List<Artist> {
        return artists.filter { artist ->
            var matches = true
            
            filters.minSongCount?.let { minCount ->
                matches = matches && artist.songCount >= minCount
            }
            
            filters.minAlbumCount?.let { minCount ->
                matches = matches && artist.albumCount >= minCount
            }
            
            matches
        }
    }
}

/**
 * Data classes for search functionality
 */
data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val query: String = ""
) {
    val totalResults: Int
        get() = songs.size + albums.size + artists.size
    
    val isEmpty: Boolean
        get() = totalResults == 0
}

data class SearchFilters(
    val yearRange: Pair<Int, Int>? = null,
    val durationRange: Pair<Long, Long>? = null,
    val favoritesOnly: Boolean? = null,
    val genre: String? = null,
    val minSongCount: Int? = null,
    val minAlbumCount: Int? = null
)

data class SearchCriteria(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val yearRange: Pair<Int, Int>? = null,
    val durationRange: Pair<Long, Long>? = null,
    val favoritesOnly: Boolean? = null
)
