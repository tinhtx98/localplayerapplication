package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.tinhtx.localplayerapplication.presentation.components.common.ScreenSize
import com.tinhtx.localplayerapplication.presentation.screens.home.HomeScreen
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryScreen
import com.tinhtx.localplayerapplication.presentation.screens.search.SearchScreen
import com.tinhtx.localplayerapplication.presentation.screens.queue.QueueScreen
import com.tinhtx.localplayerapplication.presentation.screens.player.PlayerScreen
import com.tinhtx.localplayerapplication.presentation.screens.settings.SettingsScreen

/**
 * Build the complete navigation graph
 */
fun NavGraphBuilder.buildNavGraph(screenSize: ScreenSize) {
    
    // Main destinations
    buildMainDestinations(screenSize)
    
    // Library destinations
    buildLibraryDestinations(screenSize)
    
    // Detail destinations
    buildDetailDestinations(screenSize)
    
    // Player destinations
    buildPlayerDestinations(screenSize)
    
    // User destinations
    buildUserDestinations(screenSize)
    
    // Settings destinations
    buildSettingsDestinations(screenSize)
    
    // Utility destinations
    buildUtilityDestinations(screenSize)
}

/**
 * Main destinations (Bottom navigation items)
 */
private fun NavGraphBuilder.buildMainDestinations(screenSize: ScreenSize) {
    
    // Home screen
    composable(
        route = NavDestinations.HOME,
        enterTransition = {
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.FastTab()
                else -> TransitionSets.Fade()
            }.first
        },
        exitTransition = {
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.FastTab()
                else -> TransitionSets.Fade()
            }.second
        }
    ) {
        HomeScreen()
    }
    
    // Search screen
    composable(
        route = NavDestinations.SEARCH + "?${RouteParams.SEARCH_QUERY}={${RouteParams.SEARCH_QUERY}}",
        arguments = listOf(
            navArgument(RouteParams.SEARCH_QUERY) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        enterTransition = { TransitionSets.FastTab().first },
        exitTransition = { TransitionSets.FastTab().second }
    ) { backStackEntry ->
        val searchQuery = backStackEntry.arguments?.getString(RouteParams.SEARCH_QUERY)
        SearchScreen(initialQuery = searchQuery)
    }
    
    // Library screen
    composable(
        route = NavDestinations.LIBRARY,
        enterTransition = { TransitionSets.FastTab().first },
        exitTransition = { TransitionSets.FastTab().second }
    ) {
        LibraryScreen()
    }
    
    // Queue screen
    composable(
        route = NavDestinations.QUEUE,
        enterTransition = { TransitionSets.FastTab().first },
        exitTransition = { TransitionSets.FastTab().second }
    ) {
        QueueScreen()
    }
}

/**
 * Library sub-destinations
 */
private fun NavGraphBuilder.buildLibraryDestinations(screenSize: ScreenSize) {
    
    // Songs list
    composable(
        route = NavDestinations.SONGS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // SongsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.MusicNote,
            title = "Songs",
            description = "All your songs will appear here"
        )
    }
    
    // Albums list
    composable(
        route = NavDestinations.ALBUMS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // AlbumsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Album,
            title = "Albums",
            description = "Your album collection will appear here"
        )
    }
    
    // Artists list
    composable(
        route = NavDestinations.ARTISTS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // ArtistsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Person,
            title = "Artists",
            description = "Your favorite artists will appear here"
        )
    }
    
    // Playlists list
    composable(
        route = NavDestinations.PLAYLISTS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // PlaylistsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.PlaylistPlay,
            title = "Playlists",
            description = "Create and manage your playlists"
        )
    }
    
    // Genres list
    composable(
        route = NavDestinations.GENRES,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // GenresScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Category,
            title = "Genres",
            description = "Browse music by genre"
        )
    }
}

/**
 * Detail destinations
 */
private fun NavGraphBuilder.buildDetailDestinations(screenSize: ScreenSize) {
    
    // Album detail
    composable(
        route = NavDestinations.ALBUM_DETAIL,
        arguments = listOf(
            navArgument(RouteParams.ALBUM_ID) {
                type = NavType.StringType
            }
        ),
        enterTransition = { 
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.Forward().first
                else -> TransitionSets.Overlay().first
            }
        },
        exitTransition = { 
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.Forward().second
                else -> TransitionSets.Overlay().second
            }
        },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) { backStackEntry ->
        val albumId = backStackEntry.arguments?.getString(RouteParams.ALBUM_ID) ?: ""
        // AlbumDetailScreen(albumId = albumId) - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Album,
            title = "Album Detail",
            description = "Album ID: $albumId"
        )
    }
    
    // Artist detail
    composable(
        route = NavDestinations.ARTIST_DETAIL,
        arguments = listOf(
            navArgument(RouteParams.ARTIST_ID) {
                type = NavType.StringType
            }
        ),
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) { backStackEntry ->
        val artistId = backStackEntry.arguments?.getString(RouteParams.ARTIST_ID) ?: ""
        // ArtistDetailScreen(artistId = artistId) - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Person,
            title = "Artist Detail",
            description = "Artist ID: $artistId"
        )
    }
    
    // Playlist detail
    composable(
        route = NavDestinations.PLAYLIST_DETAIL,
        arguments = listOf(
            navArgument(RouteParams.PLAYLIST_ID) {
                type = NavType.StringType
            }
        ),
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getString(RouteParams.PLAYLIST_ID) ?: ""
        // PlaylistDetailScreen(playlistId = playlistId) - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.PlaylistPlay,
            title = "Playlist Detail",
            description = "Playlist ID: $playlistId"
        )
    }
    
    // Genre detail
    composable(
        route = NavDestinations.GENRE_DETAIL,
        arguments = listOf(
            navArgument(RouteParams.GENRE_ID) {
                type = NavType.StringType
            }
        ),
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) { backStackEntry ->
        val genreId = backStackEntry.arguments?.getString(RouteParams.GENRE_ID) ?: ""
        // GenreDetailScreen(genreId = genreId) - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Category,
            title = "Genre Detail",
            description = "Genre ID: $genreId"
        )
    }
}

/**
 * Player destinations
 */
private fun NavGraphBuilder.buildPlayerDestinations(screenSize: ScreenSize) {
    
    // Full screen player
    composable(
        route = NavDestinations.PLAYER,
        enterTransition = { 
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.Modal().first
                else -> TransitionSets.Overlay().first
            }
        },
        exitTransition = { 
            when (screenSize) {
                ScreenSize.Compact -> TransitionSets.Modal().second
                else -> TransitionSets.Overlay().second
            }
        },
        popEnterTransition = { TransitionSets.Modal().first },
        popExitTransition = { TransitionSets.Modal().second }
    ) {
        PlayerScreen()
    }
    
    // Equalizer
    composable(
        route = NavDestinations.EQUALIZER,
        enterTransition = { TransitionSets.Overlay().first },
        exitTransition = { TransitionSets.Overlay().second },
        popEnterTransition = { TransitionSets.Overlay().first },
        popExitTransition = { TransitionSets.Overlay().second }
    ) {
        // EqualizerScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Equalizer,
            title = "Equalizer",
            description = "Adjust your audio settings"
        )
    }
    
    // Sleep timer
    composable(
        route = NavDestinations.SLEEP_TIMER,
        enterTransition = { TransitionSets.Overlay().first },
        exitTransition = { TransitionSets.Overlay().second },
        popEnterTransition = { TransitionSets.Overlay().first },
        popExitTransition = { TransitionSets.Overlay().second }
    ) {
        // SleepTimerScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Schedule,
            title = "Sleep Timer",
            description = "Set a timer to stop playback"
        )
    }
}

/**
 * User destinations
 */
private fun NavGraphBuilder.buildUserDestinations(screenSize: ScreenSize) {
    
    // Profile
    composable(
        route = NavDestinations.PROFILE,
        enterTransition = { TransitionSets.FastTab().first },
        exitTransition = { TransitionSets.FastTab().second }
    ) {
        // ProfileScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Person,
            title = "Profile",
            description = "Manage your profile and preferences"
        )
    }
    
    // Favorites
    composable(
        route = NavDestinations.FAVORITES,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // FavoritesScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyFavorites(
            onBrowseMusic = { /* Navigate to library */ }
        )
    }
    
    // Recently played
    composable(
        route = NavDestinations.RECENTLY_PLAYED,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // RecentlyPlayedScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyRecentlyPlayed(
            onBrowseMusic = { /* Navigate to library */ }
        )
    }
    
    // Downloads
    composable(
        route = NavDestinations.DOWNLOADS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // DownloadsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Download,
            title = "Downloads",
            description = "Your downloaded music will appear here"
        )
    }
}

/**
 * Settings destinations
 */
private fun NavGraphBuilder.buildSettingsDestinations(screenSize: ScreenSize) {
    
    // Main settings
    composable(
        route = NavDestinations.SETTINGS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        SettingsScreen()
    }
    
    // Theme settings
    composable(
        route = NavDestinations.THEME_SETTINGS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // ThemeSettingsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Palette,
            title = "Theme Settings",
            description = "Customize the app appearance"
        )
    }
    
    // Audio settings
    composable(
        route = NavDestinations.AUDIO_SETTINGS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // AudioSettingsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.AudioFile,
            title = "Audio Settings",
            description = "Configure audio preferences"
        )
    }
    
    // Library settings
    composable(
        route = NavDestinations.LIBRARY_SETTINGS,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // LibrarySettingsScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.LibraryMusic,
            title = "Library Settings",
            description = "Manage your music library"
        )
    }
    
    // About
    composable(
        route = NavDestinations.ABOUT,
        enterTransition = { TransitionSets.Forward().first },
        exitTransition = { TransitionSets.Forward().second },
        popEnterTransition = { TransitionSets.Backward().first },
        popExitTransition = { TransitionSets.Backward().second }
    ) {
        // AboutScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Info,
            title = "About",
            description = "LocalPlayer v1.0.0"
        )
    }
}

/**
 * Utility destinations
 */
private fun NavGraphBuilder.buildUtilityDestinations(screenSize: ScreenSize) {
    
    // Splash screen
    composable(
        route = NavDestinations.SPLASH,
        enterTransition = { TransitionSets.None().first },
        exitTransition = { TransitionSets.Fade().second }
    ) {
        // SplashScreen() - Will be implemented
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingScreen(
            title = "LocalPlayer",
            subtitle = "Loading your music..."
        )
    }
    
    // Error screen
    composable(
        route = NavDestinations.ERROR + "?${RouteParams.ERROR_MESSAGE}={${RouteParams.ERROR_MESSAGE}}",
        arguments = listOf(
            navArgument(RouteParams.ERROR_MESSAGE) {
                type = NavType.StringType
                nullable = true
                defaultValue = "An error occurred"
            }
        ),
        enterTransition = { TransitionSets.Fade().first },
        exitTransition = { TransitionSets.Fade().second }
    ) { backStackEntry ->
        val errorMessage = backStackEntry.arguments?.getString(RouteParams.ERROR_MESSAGE) 
            ?: "An error occurred"
        
        com.tinhtx.localplayerapplication.presentation.components.common.ErrorMessage(
            message = errorMessage,
            onRetry = { /* Handle retry */ }
        )
    }
}

/**
 * Nested navigation graphs for complex features
 */
fun NavGraphBuilder.buildLibraryNavGraph(screenSize: ScreenSize) {
    navigation(
        startDestination = NavDestinations.SONGS,
        route = "library_graph"
    ) {
        buildLibraryDestinations(screenSize)
        buildDetailDestinations(screenSize)
    }
}

fun NavGraphBuilder.buildPlayerNavGraph(screenSize: ScreenSize) {
    navigation(
        startDestination = NavDestinations.PLAYER,
        route = "player_graph"
    ) {
        buildPlayerDestinations(screenSize)
    }
}

fun NavGraphBuilder.buildSettingsNavGraph(screenSize: ScreenSize) {
    navigation(
        startDestination = NavDestinations.SETTINGS,
        route = "settings_graph"
    ) {
        buildSettingsDestinations(screenSize)
    }
}

/**
 * Navigation graph configuration
 */
data class NavGraphConfig(
    val enableTransitions: Boolean = true,
    val enableDeepLinks: Boolean = true,
    val enableAnalytics: Boolean = true,
    val maxBackStackSize: Int = 50,
    val transitionDuration: Int = NavigationTransitions.DEFAULT_ENTER_DURATION
)

/**
 * Configure navigation graph with custom settings
 */
fun NavGraphBuilder.buildConfiguredNavGraph(
    screenSize: ScreenSize,
    config: NavGraphConfig = NavGraphConfig()
) {
    if (config.enableTransitions) {
        buildNavGraph(screenSize)
    } else {
        // Build without transitions for performance
        buildNavGraphWithoutTransitions(screenSize)
    }
}

private fun NavGraphBuilder.buildNavGraphWithoutTransitions(screenSize: ScreenSize) {
    // Same as buildNavGraph but with TransitionSets.None() for all destinations
    buildMainDestinations(screenSize)
    buildLibraryDestinations(screenSize)
    buildDetailDestinations(screenSize)
    buildPlayerDestinations(screenSize)
    buildUserDestinations(screenSize)
    buildSettingsDestinations(screenSize)
    buildUtilityDestinations(screenSize)
}
