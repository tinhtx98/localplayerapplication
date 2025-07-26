package com.tinhtx.localplayerapplication.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions

class DeepLinkHandler {
    
    companion object {
        const val BASE_URI = "localplayer://"
        const val SONG_URI = "${BASE_URI}song/"
        const val ALBUM_URI = "${BASE_URI}album/"
        const val ARTIST_URI = "${BASE_URI}artist/"
        const val PLAYLIST_URI = "${BASE_URI}playlist/"
        const val PLAYER_URI = "${BASE_URI}player"
        const val SEARCH_URI = "${BASE_URI}search"
    }
    
    fun handleDeepLink(
        navController: NavController,
        intent: Intent
    ): Boolean {
        val uri = intent.data ?: return false
        
        return when {
            uri.toString().startsWith(SONG_URI) -> {
                handleSongDeepLink(navController, uri)
            }
            uri.toString().startsWith(ALBUM_URI) -> {
                handleAlbumDeepLink(navController, uri)
            }
            uri.toString().startsWith(ARTIST_URI) -> {
                handleArtistDeepLink(navController, uri)
            }
            uri.toString().startsWith(PLAYLIST_URI) -> {
                handlePlaylistDeepLink(navController, uri)
            }
            uri.toString().startsWith(PLAYER_URI) -> {
                handlePlayerDeepLink(navController)
            }
            uri.toString().startsWith(SEARCH_URI) -> {
                handleSearchDeepLink(navController, uri)
            }
            else -> false
        }
    }
    
    private fun handleSongDeepLink(navController: NavController, uri: Uri): Boolean {
        val songId = uri.lastPathSegment?.toLongOrNull() ?: return false
        
        // Navigate to player with specific song
        navController.navigate(Screen.Player.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        // TODO: Notify player to play specific song
        return true
    }
    
    private fun handleAlbumDeepLink(navController: NavController, uri: Uri): Boolean {
        val albumId = uri.lastPathSegment?.toLongOrNull() ?: return false
        
        // Navigate to library and show album
        navController.navigate(Screen.Library.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        // TODO: Notify library to show specific album
        return true
    }
    
    private fun handleArtistDeepLink(navController: NavController, uri: Uri): Boolean {
        val artistId = uri.lastPathSegment?.toLongOrNull() ?: return false
        
        // Navigate to library and show artist
        navController.navigate(Screen.Library.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        // TODO: Notify library to show specific artist
        return true
    }
    
    private fun handlePlaylistDeepLink(navController: NavController, uri: Uri): Boolean {
        val playlistId = uri.lastPathSegment?.toLongOrNull() ?: return false
        
        navController.navigate(Screen.Playlist.createRoute(playlistId)) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        return true
    }
    
    private fun handlePlayerDeepLink(navController: NavController): Boolean {
        navController.navigate(Screen.Player.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        return true
    }
    
    private fun handleSearchDeepLink(navController: NavController, uri: Uri): Boolean {
        val query = uri.getQueryParameter("q")
        
        navController.navigate(Screen.Search.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        
        // TODO: Notify search screen to use specific query
        return true
    }
    
    // Helper functions to create deep link URIs
    fun createSongDeepLink(songId: Long): Uri {
        return Uri.parse("$SONG_URI$songId")
    }
    
    fun createAlbumDeepLink(albumId: Long): Uri {
        return Uri.parse("$ALBUM_URI$albumId")
    }
    
    fun createArtistDeepLink(artistId: Long): Uri {
        return Uri.parse("$ARTIST_URI$artistId")
    }
    
    fun createPlaylistDeepLink(playlistId: Long): Uri {
        return Uri.parse("$PLAYLIST_URI$playlistId")
    }
    
    fun createPlayerDeepLink(): Uri {
        return Uri.parse(PLAYER_URI)
    }
    
    fun createSearchDeepLink(query: String? = null): Uri {
        return if (query != null) {
            Uri.parse("$SEARCH_URI?q=${Uri.encode(query)}")
        } else {
            Uri.parse(SEARCH_URI)
        }
    }
}
