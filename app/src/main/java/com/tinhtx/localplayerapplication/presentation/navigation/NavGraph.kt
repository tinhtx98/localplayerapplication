package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tinhtx.localplayerapplication.presentation.screens.favorites.FavoritesScreen
import com.tinhtx.localplayerapplication.presentation.screens.home.HomeScreen
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryScreen
import com.tinhtx.localplayerapplication.presentation.screens.player.PlayerScreen
import com.tinhtx.localplayerapplication.presentation.screens.playlist.PlaylistScreen
import com.tinhtx.localplayerapplication.presentation.screens.queue.QueueScreen
import com.tinhtx.localplayerapplication.presentation.screens.search.SearchScreen
import com.tinhtx.localplayerapplication.presentation.screens.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LocalPlayerNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { NavigationTransitions.defaultEnterTransition() },
        exitTransition = { NavigationTransitions.defaultExitTransition() },
        popEnterTransition = { NavigationTransitions.defaultPopEnterTransition() },
        popExitTransition = { NavigationTransitions.defaultPopExitTransition() }
    ) {
        // Main tabs navigation graph
        mainTabsNavGraph(navController, windowSizeClass)
        
        // Modal screens navigation graph
        modalScreensNavGraph(navController, windowSizeClass)
        
        // Detail screens navigation graph
        detailScreensNavGraph(navController, windowSizeClass)
    }
}

private fun NavGraphBuilder.mainTabsNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    // Home Screen
    composable(
        route = Screen.Home.route,
        enterTransition = { homeEnterTransition() },
        exitTransition = { homeExitTransition() }
    ) {
        HomeScreen(
            onNavigateToPlayer = {
                navController.navigateToPlayer()
            },
            onNavigateToPlaylist = { playlistId ->
                navController.navigateToPlaylist(playlistId)
            },
            onNavigateToSearch = {
                navController.navigateToSearch()
            },
            onNavigateToLibrary = {
                navController.navigateToLibrary()
            },
            windowSizeClass = windowSizeClass
        )
    }

    // Library Screen
    composable(
        route = Screen.Library.route,
        enterTransition = { libraryEnterTransition() },
        exitTransition = { libraryExitTransition() }
    ) {
        LibraryScreen(
            onNavigateToPlayer = {
                navController.navigateToPlayer()
            },
            onNavigateToPlaylist = { playlistId ->
                navController.navigateToPlaylist(playlistId)
            },
            onNavigateToFavorites = {
                navController.navigateToFavorites()
            },
            onNavigateToSearch = {
                navController.navigateToSearch()
            },
            windowSizeClass = windowSizeClass
        )
    }

    // Search Screen
    composable(
        route = Screen.Search.route,
        enterTransition = { searchEnterTransition() },
        exitTransition = { searchExitTransition() }
    ) {
        SearchScreen(
            onNavigateToPlayer = {
                navController.navigateToPlayer()
            },
            onNavigateToPlaylist = { playlistId ->
                navController.navigateToPlaylist(playlistId)
            },
            onNavigateBack = {
                navController.popBackStack()
            },
            windowSizeClass = windowSizeClass
        )
    }

    // Settings Screen
    composable(
        route = Screen.Settings.route,
        enterTransition = { settingsEnterTransition() },
        exitTransition = { settingsExitTransition() }
    ) {
        SettingsScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToLibrary = {
                navController.navigateToLibrary()
            },
            windowSizeClass = windowSizeClass
        )
    }
}

private fun NavGraphBuilder.modalScreensNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    // Player Screen (Full Screen Modal)
    composable(
        route = Screen.Player.route,
        enterTransition = { playerEnterTransition() },
        exitTransition = { playerExitTransition() }
    ) {
        PlayerScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToQueue = {
                navController.navigateToQueue()
            },
            onNavigateToLibrary = {
                navController.navigateToLibrary()
            },
            windowSizeClass = windowSizeClass
        )
    }

    // Queue Screen (Modal)
    composable(
        route = Screen.Queue.route,
        enterTransition = { queueEnterTransition() },
        exitTransition = { queueExitTransition() }
    ) {
        QueueScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPlayer = {
                navController.popBackStack()
            },
            onClearQueue = {
                // Handle clear queue
            },
            windowSizeClass = windowSizeClass
        )
    }
}

private fun NavGraphBuilder.detailScreensNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    // Playlist Detail Screen
    composable(
        route = Screen.Playlist.route,
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.LongType
                defaultValue = -1L
            }
        ),
        enterTransition = { playlistEnterTransition() },
        exitTransition = { playlistExitTransition() }
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: -1L
        
        PlaylistScreen(
            playlistId = playlistId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPlayer = {
                navController.navigateToPlayer()
            },
            onNavigateToSearch = {
                navController.navigateToSearch()
            },
            windowSizeClass = windowSizeClass
        )
    }

    // Favorites Screen
    composable(
        route = Screen.Favorites.route,
        enterTransition = { favoritesEnterTransition() },
        exitTransition = { favoritesExitTransition() }
    ) {
        FavoritesScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPlayer = {
                navController.navigateToPlayer()
            },
            onNavigateToSearch = {
                navController.navigateToSearch()
            },
            windowSizeClass = windowSizeClass
        )
    }
}

// Custom transition functions for each screen
private fun AnimatedContentTransitionScope<NavBackStackEntry>.homeEnterTransition(): EnterTransition {
    return when (initialState.destination.route) {
        Screen.Library.route, Screen.Search.route, Screen.Settings.route -> {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300))
        }
        else -> fadeIn(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.homeExitTransition(): ExitTransition {
    return when (targetState.destination.route) {
        Screen.Library.route, Screen.Search.route, Screen.Settings.route -> {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        }
        Screen.Player.route -> {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400))
        }
        else -> fadeOut(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.libraryEnterTransition(): EnterTransition {
    return when (initialState.destination.route) {
        Screen.Home.route -> {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300))
        }
        Screen.Search.route, Screen.Settings.route -> {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300))
        }
        else -> fadeIn(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.libraryExitTransition(): ExitTransition {
    return when (targetState.destination.route) {
        Screen.Home.route -> {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        }
        Screen.Search.route, Screen.Settings.route -> {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        }
        Screen.Player.route, Screen.Playlist.route, Screen.Favorites.route -> {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400))
        }
        else -> fadeOut(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.searchEnterTransition(): EnterTransition {
    return when (initialState.destination.route) {
        Screen.Home.route, Screen.Library.route -> {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300))
        }
        Screen.Settings.route -> {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(300))
        }
        else -> fadeIn(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.searchExitTransition(): ExitTransition {
    return when (targetState.destination.route) {
        Screen.Home.route, Screen.Library.route -> {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        }
        Screen.Settings.route -> {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(300))
        }
        Screen.Player.route -> {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = EaseInCubic)
            ) + fadeOut(animationSpec = tween(400))
        }
        else -> fadeOut(animationSpec = tween(300))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.settingsEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.settingsExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.playerEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(500, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(500)) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(500, easing = EaseOutCubic)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.playerExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(400, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(400)) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(400, easing = EaseInCubic)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.queueEnterTransition(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(350, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(350))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.queueExitTransition(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.playlistEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.playlistExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.favoritesEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.favoritesExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

// Navigation extension functions
private fun NavHostController.navigateToPlayer() {
    navigate(Screen.Player.route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToPlaylist(playlistId: Long) {
    navigate(Screen.Playlist.createRoute(playlistId)) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToSearch() {
    navigate(Screen.Search.route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToLibrary() {
    navigate(Screen.Library.route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToQueue() {
    navigate(Screen.Queue.route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToFavorites() {
    navigate(Screen.Favorites.route) {
        launchSingleTop = true
    }
}
