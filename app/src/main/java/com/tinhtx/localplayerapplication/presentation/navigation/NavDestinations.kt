package com.tinhtx.localplayerapplication.presentation.navigation

object NavDestinations {
    
    // Main destinations
    const val HOME = "/home"
    const val LIBRARY = "/library"
    const val SEARCH = "/search"
    const val QUEUE = "/queue"
    const val PROFILE = "/profile"
    
    // Discovery destinations
    const val DISCOVER = "/discover"
    const val TRENDING = "/trending"
    const val NEW_RELEASES = "/new-releases"
    const val CHARTS = "/charts"
    
    // Library sub-destinations
    const val SONGS = "/library/songs"
    const val ALBUMS = "/library/albums" 
    const val ARTISTS = "/library/artists"
    const val PLAYLISTS = "/library/playlists"
    const val GENRES = "/library/genres"
    const val FOLDERS = "/library/folders"
    
    // User destinations
    const val FAVORITES = "/favorites"
    const val RECENTLY_PLAYED = "/recent"
    const val DOWNLOADS = "/downloads"
    const val LISTENING_HISTORY = "/history"
    
    // Player destinations
    const val PLAYER = "/player"
    const val MINI_PLAYER = "/mini-player"
    const val EQUALIZER = "/equalizer"
    const val SLEEP_TIMER = "/sleep-timer"
    
    // Content detail destinations
    const val ALBUM_DETAIL = "/album/{albumId}"
    const val ARTIST_DETAIL = "/artist/{artistId}"
    const val PLAYLIST_DETAIL = "/playlist/{playlistId}"
    const val GENRE_DETAIL = "/genre/{genreId}"
    
    // Settings destinations
    const val SETTINGS = "/settings"
    const val THEME_SETTINGS = "/settings/theme"
    const val AUDIO_SETTINGS = "/settings/audio"
    const val LIBRARY_SETTINGS = "/settings/library"
    const val ABOUT = "/settings/about"
    
    // Auth destinations (if needed)
    const val LOGIN = "/auth/login"
    const val REGISTER = "/auth/register"
    const val FORGOT_PASSWORD = "/auth/forgot-password"
    
    // Onboarding destinations
    const val WELCOME = "/onboarding/welcome"
    const val PERMISSIONS = "/onboarding/permissions"
    const val SETUP = "/onboarding/setup"
    
    // Utility destinations
    const val SPLASH = "/splash"
    const val ERROR = "/error"
    const val NOT_FOUND = "/404"
}

// Route parameters
object RouteParams {
    const val ALBUM_ID = "albumId"
    const val ARTIST_ID = "artistId"
    const val PLAYLIST_ID = "playlistId"
    const val GENRE_ID = "genreId"
    const val SONG_ID = "songId"
    const val SEARCH_QUERY = "query"
    const val ERROR_MESSAGE = "message"
}

// Route builders for parameterized destinations
object RouteBuilders {
    
    fun albumDetail(albumId: String): String {
        return NavDestinations.ALBUM_DETAIL.replace("{${RouteParams.ALBUM_ID}}", albumId)
    }
    
    fun artistDetail(artistId: String): String {
        return NavDestinations.ARTIST_DETAIL.replace("{${RouteParams.ARTIST_ID}}", artistId)
    }
    
    fun playlistDetail(playlistId: String): String {
        return NavDestinations.PLAYLIST_DETAIL.replace("{${RouteParams.PLAYLIST_ID}}", playlistId)
    }
    
    fun genreDetail(genreId: String): String {
        return NavDestinations.GENRE_DETAIL.replace("{${RouteParams.GENRE_ID}}", genreId)
    }
    
    fun searchWithQuery(query: String): String {
        return "${NavDestinations.SEARCH}?${RouteParams.SEARCH_QUERY}=$query"
    }
    
    fun errorWithMessage(message: String): String {
        return "${NavDestinations.ERROR}?${RouteParams.ERROR_MESSAGE}=$message"
    }
}

// Navigation groups for better organization
enum class NavGroup {
    MAIN,           // Primary navigation items
    LIBRARY,        // Library-related destinations  
    DISCOVERY,      // Content discovery
    USER,           // User profile and preferences
    PLAYER,         // Playback related
    SETTINGS,       // Configuration
    AUTH,           // Authentication
    ONBOARDING,     // First-time user experience
    UTILITY         // System/utility screens
}

// Navigation destination metadata
data class NavDestination(
    val route: String,
    val group: NavGroup,
    val requiresAuth: Boolean = false,
    val isBottomNavItem: Boolean = false,
    val parentRoute: String? = null,
    val deepLinkPatterns: List<String> = emptyList()
)

object NavDestinationRegistry {
    
    val destinations = mapOf(
        // Main destinations
        NavDestinations.HOME to NavDestination(
            route = NavDestinations.HOME,
            group = NavGroup.MAIN,
            isBottomNavItem = true,
            deepLinkPatterns = listOf("localplayer://home", "https://localplayer.app/home")
        ),
        
        NavDestinations.LIBRARY to NavDestination(
            route = NavDestinations.LIBRARY,
            group = NavGroup.MAIN,
            isBottomNavItem = true,
            deepLinkPatterns = listOf("localplayer://library")
        ),
        
        NavDestinations.SEARCH to NavDestination(
            route = NavDestinations.SEARCH,
            group = NavGroup.MAIN,
            isBottomNavItem = true,
            deepLinkPatterns = listOf("localplayer://search")
        ),
        
        NavDestinations.QUEUE to NavDestination(
            route = NavDestinations.QUEUE,
            group = NavGroup.PLAYER,
            isBottomNavItem = true,
            deepLinkPatterns = listOf("localplayer://queue")
        ),
        
        // Library sub-destinations
        NavDestinations.SONGS to NavDestination(
            route = NavDestinations.SONGS,
            group = NavGroup.LIBRARY,
            parentRoute = NavDestinations.LIBRARY
        ),
        
        NavDestinations.ALBUMS to NavDestination(
            route = NavDestinations.ALBUMS,
            group = NavGroup.LIBRARY,
            parentRoute = NavDestinations.LIBRARY,
            deepLinkPatterns = listOf("localplayer://albums")
        ),
        
        NavDestinations.ARTISTS to NavDestination(
            route = NavDestinations.ARTISTS,
            group = NavGroup.LIBRARY,
            parentRoute = NavDestinations.LIBRARY,
            deepLinkPatterns = listOf("localplayer://artists")
        ),
        
        NavDestinations.PLAYLISTS to NavDestination(
            route = NavDestinations.PLAYLISTS,
            group = NavGroup.LIBRARY,
            parentRoute = NavDestinations.LIBRARY,
            deepLinkPatterns = listOf("localplayer://playlists")
        ),
        
        // Detail destinations
        NavDestinations.ALBUM_DETAIL to NavDestination(
            route = NavDestinations.ALBUM_DETAIL,
            group = NavGroup.LIBRARY,
            deepLinkPatterns = listOf("localplayer://album/{albumId}", "https://localplayer.app/album/{albumId}")
        ),
        
        NavDestinations.ARTIST_DETAIL to NavDestination(
            route = NavDestinations.ARTIST_DETAIL,
            group = NavGroup.LIBRARY,
            deepLinkPatterns = listOf("localplayer://artist/{artistId}")
        ),
        
        NavDestinations.PLAYLIST_DETAIL to NavDestination(
            route = NavDestinations.PLAYLIST_DETAIL,
            group = NavGroup.LIBRARY,
            deepLinkPatterns = listOf("localplayer://playlist/{playlistId}")
        ),
        
        // Player destinations
        NavDestinations.PLAYER to NavDestination(
            route = NavDestinations.PLAYER,
            group = NavGroup.PLAYER,
            deepLinkPatterns = listOf("localplayer://player")
        ),
        
        NavDestinations.EQUALIZER to NavDestination(
            route = NavDestinations.EQUALIZER,
            group = NavGroup.PLAYER,
            parentRoute = NavDestinations.SETTINGS
        ),
        
        // User destinations
        NavDestinations.PROFILE to NavDestination(
            route = NavDestinations.PROFILE,
            group = NavGroup.USER,
            isBottomNavItem = true
        ),
        
        NavDestinations.FAVORITES to NavDestination(
            route = NavDestinations.FAVORITES,
            group = NavGroup.USER,
            deepLinkPatterns = listOf("localplayer://favorites")
        ),
        
        NavDestinations.RECENTLY_PLAYED to NavDestination(
            route = NavDestinations.RECENTLY_PLAYED,
            group = NavGroup.USER,
            deepLinkPatterns = listOf("localplayer://recent")
        ),
        
        // Settings destinations
        NavDestinations.SETTINGS to NavDestination(
            route = NavDestinations.SETTINGS,
            group = NavGroup.SETTINGS,
            deepLinkPatterns = listOf("localplayer://settings")
        ),
        
        NavDestinations.THEME_SETTINGS to NavDestination(
            route = NavDestinations.THEME_SETTINGS,
            group = NavGroup.SETTINGS,
            parentRoute = NavDestinations.SETTINGS
        ),
        
        NavDestinations.AUDIO_SETTINGS to NavDestination(
            route = NavDestinations.AUDIO_SETTINGS,
            group = NavGroup.SETTINGS,
            parentRoute = NavDestinations.SETTINGS
        ),
        
        // Discovery destinations
        NavDestinations.DISCOVER to NavDestination(
            route = NavDestinations.DISCOVER,
            group = NavGroup.DISCOVERY,
            deepLinkPatterns = listOf("localplayer://discover")
        ),
        
        // Utility destinations
        NavDestinations.SPLASH to NavDestination(
            route = NavDestinations.SPLASH,
            group = NavGroup.UTILITY
        ),
        
        NavDestinations.ERROR to NavDestination(
            route = NavDestinations.ERROR,
            group = NavGroup.UTILITY
        )
    )
    
    fun getDestination(route: String): NavDestination? {
        return destinations[route]
    }
    
    fun getDestinationsByGroup(group: NavGroup): List<NavDestination> {
        return destinations.values.filter { it.group == group }
    }
    
    fun getBottomNavDestinations(): List<NavDestination> {
        return destinations.values.filter { it.isBottomNavItem }
    }
    
    fun getAllDeepLinkPatterns(): List<String> {
        return destinations.values.flatMap { it.deepLinkPatterns }
    }
}

// Start destinations for different scenarios
object StartDestinations {
    const val DEFAULT = NavDestinations.HOME
    const val FIRST_TIME_USER = NavDestinations.WELCOME
    const val NO_PERMISSIONS = NavDestinations.PERMISSIONS
    const val ERROR_STATE = NavDestinations.ERROR
}

// Navigation constants
object NavConstants {
    const val BOTTOM_NAV_ANIMATION_DURATION = 300
    const val SCREEN_TRANSITION_DURATION = 400
    const val DEEP_LINK_TIMEOUT = 5000L
    const val MAX_NAVIGATION_STACK_SIZE = 50
}
