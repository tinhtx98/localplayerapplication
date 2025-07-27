package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.tinhtx.localplayerapplication.presentation.components.common.ScreenSize
import com.tinhtx.localplayerapplication.presentation.components.common.rememberResponsiveInfo

@Composable
fun LocalPlayerNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = StartDestinations.DEFAULT,
    deepLinkHandler: DeepLinkHandler,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    // Handle deep links
    LaunchedEffect(navController) {
        // Set up deep link handling if needed
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { 
            RouteTransitions.getEnterTransition(targetState.destination.route ?: "", false)
        },
        exitTransition = { 
            RouteTransitions.getExitTransition(initialState.destination.route ?: "", false)
        },
        popEnterTransition = { 
            RouteTransitions.getEnterTransition(targetState.destination.route ?: "", true)
        },
        popExitTransition = { 
            RouteTransitions.getExitTransition(initialState.destination.route ?: "", true)
        }
    ) {
        // Build navigation graph
        buildNavGraph(responsiveInfo.screenSize)
    }
}

/**
 * Navigation state holder
 */
@Stable
class LocalPlayerNavigationState(
    val navController: NavHostController,
    private val deepLinkHandler: DeepLinkHandler
) {
    
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination
    
    val currentRoute: String?
        @Composable get() = currentDestination?.route
    
    /**
     * Navigate to a destination
     */
    fun navigateTo(route: String, popUpTo: String? = null, inclusive: Boolean = false) {
        navController.navigate(route) {
            popUpTo?.let {
                popUpTo(it) { this.inclusive = inclusive }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    
    /**
     * Navigate back
     */
    fun navigateBack(): Boolean {
        return navController.popBackStack()
    }
    
    /**
     * Navigate to home and clear back stack
     */
    fun navigateToHome() {
        navController.navigate(NavDestinations.HOME) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    
    /**
     * Navigate to search with query
     */
    fun navigateToSearch(query: String? = null) {
        val route = if (query != null) {
            RouteBuilders.searchWithQuery(query)
        } else {
            NavDestinations.SEARCH
        }
        navigateTo(route)
    }
    
    /**
     * Navigate to album detail
     */
    fun navigateToAlbumDetail(albumId: String) {
        navigateTo(RouteBuilders.albumDetail(albumId))
    }
    
    /**
     * Navigate to artist detail
     */
    fun navigateToArtistDetail(artistId: String) {
        navigateTo(RouteBuilders.artistDetail(artistId))
    }
    
    /**
     * Navigate to playlist detail
     */
    fun navigateToPlaylistDetail(playlistId: String) {
        navigateTo(RouteBuilders.playlistDetail(playlistId))
    }
    
    /**
     * Navigate to player
     */
    fun navigateToPlayer() {
        navigateTo(NavDestinations.PLAYER)
    }
    
    /**
     * Handle deep link
     */
    fun handleDeepLink(uri: android.net.Uri): Boolean {
        return deepLinkHandler.handleDeepLink(uri, navController)
    }
    
    /**
     * Check if can navigate back
     */
    val canNavigateBack: Boolean
        @Composable get() = navController.previousBackStackEntry != null
    
    /**
     * Get current back stack
     */
    val backStack: List<NavBackStackEntry>
        @Composable get() = navController.currentBackStack.collectAsState().value
    
    /**
     * Clear back stack to specific destination
     */
    fun clearBackStackTo(destination: String) {
        navController.popBackStack(destination, false)
    }
    
    /**
     * Navigate up in hierarchy
     */
    fun navigateUp(): Boolean {
        return navController.navigateUp()
    }
    
    /**
     * Check if current destination is main destination
     */
    val isMainDestination: Boolean
        @Composable get() {
            val route = currentRoute ?: return false
            return listOf(
                NavDestinations.HOME,
                NavDestinations.SEARCH,
                NavDestinations.LIBRARY,
                NavDestinations.QUEUE,
                NavDestinations.PROFILE
            ).any { route.startsWith(it) }
        }
    
    /**
     * Get navigation level (for UI adjustments)
     */
    val navigationLevel: Int
        @Composable get() = backStack.size
        
    /**
     * Check if destination is accessible
     */
    fun isDestinationAccessible(route: String): Boolean {
        return try {
            navController.graph.findNode(route) != null
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Remember navigation state
 */
@Composable
fun rememberLocalPlayerNavigationState(
    navController: NavHostController = rememberNavController(),
    deepLinkHandler: DeepLinkHandler
): LocalPlayerNavigationState {
    return remember(navController, deepLinkHandler) {
        LocalPlayerNavigationState(navController, deepLinkHandler)
    }
}

/**
 * Navigation event system
 */
sealed class NavigationEvent {
    data class NavigateTo(val route: String, val popUpTo: String? = null) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object NavigateUp : NavigationEvent()
    data class DeepLink(val uri: android.net.Uri) : NavigationEvent()
    data class SearchQuery(val query: String) : NavigationEvent()
}

/**
 * Navigation event handler
 */
class NavigationEventHandler(
    private val navigationState: LocalPlayerNavigationState
) {
    
    fun handleEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateTo -> {
                navigationState.navigateTo(event.route, event.popUpTo)
            }
            NavigationEvent.NavigateBack -> {
                navigationState.navigateBack()
            }
            NavigationEvent.NavigateUp -> {
                navigationState.navigateUp()
            }
            is NavigationEvent.DeepLink -> {
                navigationState.handleDeepLink(event.uri)
            }
            is NavigationEvent.SearchQuery -> {
                navigationState.navigateToSearch(event.query)
            }
        }
    }
}

/**
 * Navigation analytics
 */
data class NavigationAnalytics(
    val fromRoute: String?,
    val toRoute: String,
    val timestamp: Long = System.currentTimeMillis(),
    val navigationType: NavigationType,
    val duration: Long? = null
)

enum class NavigationType {
    FORWARD,
    BACK,
    DEEP_LINK,
    TAB_SWITCH,
    DIRECT
}

/**
 * Navigation observer for analytics
 */
@Composable
fun NavigationObserver(
    navController: NavHostController,
    onNavigationEvent: (NavigationAnalytics) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val previousRoute = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.let { entry ->
            val currentRoute = entry.destination.route
            val fromRoute = previousRoute.value
            
            if (currentRoute != null && currentRoute != fromRoute) {
                val analytics = NavigationAnalytics(
                    fromRoute = fromRoute,
                    toRoute = currentRoute,
                    navigationType = determineNavigationType(fromRoute, currentRoute)
                )
                onNavigationEvent(analytics)
                previousRoute.value = currentRoute
            }
        }
    }
}

private fun determineNavigationType(fromRoute: String?, toRoute: String): NavigationType {
    return when {
        fromRoute == null -> NavigationType.DIRECT
        isTabSwitch(fromRoute, toRoute) -> NavigationType.TAB_SWITCH
        isForwardNavigation(fromRoute, toRoute) -> NavigationType.FORWARD
        else -> NavigationType.BACK
    }
}

private fun isTabSwitch(fromRoute: String, toRoute: String): Boolean {
    val mainRoutes = listOf(
        NavDestinations.HOME,
        NavDestinations.SEARCH,
        NavDestinations.LIBRARY,
        NavDestinations.QUEUE
    )
    return mainRoutes.contains(fromRoute) && mainRoutes.contains(toRoute)
}

private fun isForwardNavigation(fromRoute: String, toRoute: String): Boolean {
    // Implement logic to determine if this is forward navigation
    // For example, navigating from library to album detail
    return when {
        fromRoute.startsWith(NavDestinations.LIBRARY) && toRoute.contains("detail") -> true
        fromRoute.startsWith(NavDestinations.SEARCH) && toRoute.contains("detail") -> true
        else -> false
    }
}

/**
 * Navigation performance monitor
 */
@Composable
fun NavigationPerformanceMonitor(
    navController: NavHostController,
    onPerformanceData: (NavigationPerformanceData) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val navigationStartTime = remember { mutableStateOf<Long?>(null) }
    
    // Monitor navigation start
    LaunchedEffect(navController.currentDestination) {
        navigationStartTime.value = System.currentTimeMillis()
    }
    
    // Monitor navigation completion
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.let { entry ->
            navigationStartTime.value?.let { startTime ->
                val duration = System.currentTimeMillis() - startTime
                val performanceData = NavigationPerformanceData(
                    route = entry.destination.route ?: "",
                    navigationDuration = duration,
                    timestamp = System.currentTimeMillis()
                )
                onPerformanceData(performanceData)
                navigationStartTime.value = null
            }
        }
    }
}

data class NavigationPerformanceData(
    val route: String,
    val navigationDuration: Long,
    val timestamp: Long
)

/**
 * Conditional navigation based on screen size
 */
@Composable
fun ResponsiveNavigationWrapper(
    screenSize: ScreenSize,
    content: @Composable (isCompact: Boolean) -> Unit
) {
    val isCompact = screenSize == ScreenSize.Compact
    content(isCompact)
}

/**
 * Navigation memory manager
 */
class NavigationMemoryManager(
    private val navController: NavHostController
) {
    
    private val maxBackStackSize = 20
    
    fun trimBackStack() {
        val backStack = navController.currentBackStack.value
        if (backStack.size > maxBackStackSize) {
            // Keep only recent entries
            val entriesToRemove = backStack.size - maxBackStackSize
            repeat(entriesToRemove) {
                // Implementation would need custom NavController extension
                // This is a conceptual example
            }
        }
    }
    
    fun clearNonEssentialEntries() {
        // Clear temporary or cached navigation entries
        // Implementation depends on specific needs
    }
}
