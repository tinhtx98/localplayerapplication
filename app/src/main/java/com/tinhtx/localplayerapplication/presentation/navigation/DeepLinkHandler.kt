package com.tinhtx.localplayerapplication.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkHandler @Inject constructor() {
    
    private val supportedSchemes = listOf("localplayer", "https")
    private val supportedHosts = listOf("localplayer.app", "music.localplayer.app")
    
    /**
     * Handle deep link from Intent
     */
    fun handleDeepLink(intent: Intent, navController: NavController): Boolean {
        val uri = intent.data ?: return false
        return handleDeepLink(uri, navController)
    }
    
    /**
     * Handle deep link from Uri
     */
    fun handleDeepLink(uri: Uri, navController: NavController): Boolean {
        if (!isValidDeepLink(uri)) return false
        
        val route = parseDeepLink(uri) ?: return false
        
        try {
            navController.navigate(route) {
                // Clear back stack for certain deep links
                if (shouldClearBackStack(route)) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
                
                // Single top for main destinations
                if (isMainDestination(route)) {
                    launchSingleTop = true
                }
            }
            return true
        } catch (e: Exception) {
            // Log error and fallback
            handleDeepLinkError(uri, e)
            return false
        }
    }
    
    /**
     * Parse deep link URI to navigation route
     */
    private fun parseDeepLink(uri: Uri): String? {
        return when (uri.scheme?.lowercase()) {
            "localplayer" -> parseLocalPlayerScheme(uri)
            "https" -> parseHttpsScheme(uri)
            else -> null
        }
    }
    
    /**
     * Parse localplayer:// scheme
     */
    private fun parseLocalPlayerScheme(uri: Uri): String? {
        val host = uri.host?.lowercase() ?: return null
        val pathSegments = uri.pathSegments
        val queryParams = uri.queryParameterNames
        
        return when (host) {
            "home" -> NavDestinations.HOME
            "search" -> {
                val query = uri.getQueryParameter("q") ?: uri.getQueryParameter("query")
                if (query != null) {
                    RouteBuilders.searchWithQuery(query)
                } else {
                    NavDestinations.SEARCH
                }
            }
            "library" -> NavDestinations.LIBRARY
            "queue" -> NavDestinations.QUEUE
            "player" -> NavDestinations.PLAYER
            "albums" -> NavDestinations.ALBUMS
            "artists" -> NavDestinations.ARTISTS
            "playlists" -> NavDestinations.PLAYLISTS
            "favorites" -> NavDestinations.FAVORITES
            "recent" -> NavDestinations.RECENTLY_PLAYED
            "settings" -> NavDestinations.SETTINGS
            "discover" -> NavDestinations.DISCOVER
            
            // Parameterized routes
            "album" -> {
                val albumId = pathSegments.firstOrNull()
                if (albumId != null) {
                    RouteBuilders.albumDetail(albumId)
                } else null
            }
            "artist" -> {
                val artistId = pathSegments.firstOrNull()
                if (artistId != null) {
                    RouteBuilders.artistDetail(artistId)
                } else null
            }
            "playlist" -> {
                val playlistId = pathSegments.firstOrNull()
                if (playlistId != null) {
                    RouteBuilders.playlistDetail(playlistId)
                } else null
            }
            
            else -> null
        }
    }
    
    /**
     * Parse https:// scheme
     */
    private fun parseHttpsScheme(uri: Uri): String? {
        val host = uri.host?.lowercase() ?: return null
        if (!supportedHosts.contains(host)) return null
        
        val pathSegments = uri.pathSegments
        if (pathSegments.isEmpty()) return NavDestinations.HOME
        
        return when (pathSegments[0]) {
            "home" -> NavDestinations.HOME
            "search" -> {
                val query = uri.getQueryParameter("q") ?: uri.getQueryParameter("query")
                if (query != null) {
                    RouteBuilders.searchWithQuery(query)
                } else {
                    NavDestinations.SEARCH
                }
            }
            "library" -> NavDestinations.LIBRARY
            "album" -> {
                val albumId = pathSegments.getOrNull(1)
                if (albumId != null) {
                    RouteBuilders.albumDetail(albumId)
                } else null
            }
            "artist" -> {
                val artistId = pathSegments.getOrNull(1)
                if (artistId != null) {
                    RouteBuilders.artistDetail(artistId)
                } else null
            }
            "playlist" -> {
                val playlistId = pathSegments.getOrNull(1)
                if (playlistId != null) {
                    RouteBuilders.playlistDetail(playlistId)
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Validate if URI is a supported deep link
     */
    private fun isValidDeepLink(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        
        if (!supportedSchemes.contains(scheme)) return false
        
        if (scheme == "https") {
            val host = uri.host?.lowercase() ?: return false
            return supportedHosts.contains(host)
        }
        
        return true
    }
    
    /**
     * Check if route should clear back stack
     */
    private fun shouldClearBackStack(route: String): Boolean {
        return when {
            route.startsWith(NavDestinations.HOME) -> true
            route.startsWith(NavDestinations.SEARCH) -> true
            route.startsWith(NavDestinations.LIBRARY) -> true
            else -> false
        }
    }
    
    /**
     * Check if route is a main destination
     */
    private fun isMainDestination(route: String): Boolean {
        val mainDestinations = listOf(
            NavDestinations.HOME,
            NavDestinations.SEARCH,
            NavDestinations.LIBRARY,
            NavDestinations.QUEUE,
            NavDestinations.PROFILE
        )
        return mainDestinations.any { route.startsWith(it) }
    }
    
    /**
     * Handle deep link parsing errors
     */
    private fun handleDeepLinkError(uri: Uri, error: Exception) {
        // Log error for debugging
        println("Deep link error for URI: $uri, Error: ${error.message}")
        
        // Could implement analytics reporting here
        // Analytics.reportDeepLinkError(uri.toString(), error.message)
    }
    
    /**
     * Generate deep link for a route
     */
    fun generateDeepLink(route: String, scheme: String = "localplayer"): String? {
        return when {
            route == NavDestinations.HOME -> "$scheme://home"
            route == NavDestinations.SEARCH -> "$scheme://search"
            route == NavDestinations.LIBRARY -> "$scheme://library"
            route == NavDestinations.QUEUE -> "$scheme://queue"
            route == NavDestinations.FAVORITES -> "$scheme://favorites"
            route == NavDestinations.RECENTLY_PLAYED -> "$scheme://recent"
            route == NavDestinations.SETTINGS -> "$scheme://settings"
            
            route.startsWith(NavDestinations.ALBUM_DETAIL.substringBefore("{")) -> {
                val albumId = extractParamFromRoute(route, RouteParams.ALBUM_ID)
                if (albumId != null) "$scheme://album/$albumId" else null
            }
            
            route.startsWith(NavDestinations.ARTIST_DETAIL.substringBefore("{")) -> {
                val artistId = extractParamFromRoute(route, RouteParams.ARTIST_ID)
                if (artistId != null) "$scheme://artist/$artistId" else null
            }
            
            route.startsWith(NavDestinations.PLAYLIST_DETAIL.substringBefore("{")) -> {
                val playlistId = extractParamFromRoute(route, RouteParams.PLAYLIST_ID)
                if (playlistId != null) "$scheme://playlist/$playlistId" else null
            }
            
            else -> null
        }
    }
    
    /**
     * Extract parameter from parameterized route
     */
    private fun extractParamFromRoute(route: String, paramName: String): String? {
        // Simple parameter extraction - can be enhanced
        val regex = Regex("/$paramName/([^/?]+)")
        return regex.find(route)?.groupValues?.get(1)
    }
    
    /**
     * Generate shareable deep link (HTTPS)
     */
    fun generateShareableLink(route: String): String? {
        return generateDeepLink(route, "https")?.replace("https://", "https://localplayer.app/")
    }
    
    /**
     * Validate deep link before navigation
     */
    fun validateDeepLink(uri: Uri): DeepLinkValidationResult {
        if (!isValidDeepLink(uri)) {
            return DeepLinkValidationResult.Invalid("Unsupported scheme or host")
        }
        
        val route = parseDeepLink(uri)
        if (route == null) {
            return DeepLinkValidationResult.Invalid("Cannot parse route from URI")
        }
        
        return DeepLinkValidationResult.Valid(route)
    }
}

/**
 * Deep link validation result
 */
sealed class DeepLinkValidationResult {
    data class Valid(val route: String) : DeepLinkValidationResult()
    data class Invalid(val reason: String) : DeepLinkValidationResult()
}

/**
 * Deep link analytics data
 */
data class DeepLinkAnalytics(
    val uri: String,
    val source: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean,
    val route: String?,
    val error: String? = null
)

/**
 * Deep link configuration
 */
data class DeepLinkConfig(
    val supportedSchemes: List<String> = listOf("localplayer", "https"),
    val supportedHosts: List<String> = listOf("localplayer.app"),
    val enableAnalytics: Boolean = true,
    val fallbackRoute: String = NavDestinations.HOME
)

/**
 * Deep link intent filters helper
 */
object DeepLinkIntentFilters {
    
    /**
     * Generate intent filter XML for manifest
     */
    fun generateManifestFilters(): String {
        return """
            <!-- Custom scheme deep links -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="localplayer" />
            </intent-filter>
            
            <!-- HTTPS deep links -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="https"
                      android:host="localplayer.app" />
            </intent-filter>
        """.trimIndent()
    }
    
    /**
     * Common deep link patterns for testing
     */
    val testPatterns = listOf(
        "localplayer://home",
        "localplayer://search?q=test",
        "localplayer://album/123",
        "localplayer://artist/456",
        "localplayer://playlist/789",
        "https://localplayer.app/home",
        "https://localplayer.app/album/123"
    )
}
