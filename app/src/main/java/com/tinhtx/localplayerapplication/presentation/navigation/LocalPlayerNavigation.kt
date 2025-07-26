package com.tinhtx.localplayerapplication.presentation.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tinhtx.localplayerapplication.presentation.components.ui.ResponsiveNavigation
import kotlinx.coroutines.flow.map

@Composable
fun LocalPlayerNavigation(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route,
    onDeepLinkReceived: ((Intent) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Current destination tracking
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    // Top level destinations for navigation
    val topLevelDestinations = remember {
        listOf(
            TopLevelDestination.HOME,
            TopLevelDestination.LIBRARY,
            TopLevelDestination.SEARCH,
            TopLevelDestination.SETTINGS
        )
    }
    
    // Deep link handler
    val deepLinkHandler = remember { DeepLinkHandler() }
    
    // Handle deep links on launch
    LaunchedEffect(navController, activity) {
        activity?.intent?.let { intent ->
            if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
                val handled = deepLinkHandler.handleDeepLink(navController, intent)
                if (handled) {
                    onDeepLinkReceived?.invoke(intent)
                }
            }
        }
    }
    
    // Handle new intents for deep links
    DisposableEffect(navController, activity) {
        val handleNewIntent: (Intent) -> Unit = { intent ->
            if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
                deepLinkHandler.handleDeepLink(navController, intent)
                onDeepLinkReceived?.invoke(intent)
            }
        }
        
        activity?.let { act ->
            if (act.intent != null) {
                handleNewIntent(act.intent)
            }
        }
        
        onDispose {
            // Cleanup handled automatically by DisposableEffect
        }
    }

    // Handle back press for nested navigation
    BackHandler(
        enabled = navController.previousBackStackEntry != null
    ) {
        if (!navController.popBackStack()) {
            activity?.finish()
        }
    }

    ResponsiveNavigation(
        destinations = topLevelDestinations,
        currentDestination = currentDestination,
        onNavigateToDestination = { destination ->
            navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        windowSizeClass = windowSizeClass,
        modifier = modifier
    ) { paddingValues ->
        LocalPlayerNavGraph(
            navController = navController,
            windowSizeClass = windowSizeClass,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun LocalPlayerNavigationWithState(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
    onNavigationEvent: ((NavigationEvent) -> Unit)? = null
) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(startDestination) }
    
    // Track navigation events
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            if (route != null && route != currentRoute) {
                val previousRoute = currentRoute
                currentRoute = route
                onNavigationEvent?.invoke(
                    NavigationEvent.RouteChanged(
                        from = previousRoute,
                        to = route
                    )
                )
            }
        }
    }
    
    LocalPlayerNavigation(
        windowSizeClass = windowSizeClass,
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        onDeepLinkReceived = { intent ->
            onNavigationEvent?.invoke(
                NavigationEvent.DeepLinkReceived(intent)
            )
        }
    )
}

@Composable
fun LocalPlayerNavigationContainer(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    navigationBarConfig: NavigationBarConfig = rememberNavigationBarConfig(),
    startDestination: String = Screen.Home.route,
    enableDeepLinks: Boolean = true,
    onNavigationReady: ((NavHostController) -> Unit)? = null
) {
    val navController = rememberNavController()
    
    LaunchedEffect(navController) {
        onNavigationReady?.invoke(navController)
    }
    
    LocalPlayerNavigation(
        windowSizeClass = windowSizeClass,
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        onDeepLinkReceived = if (enableDeepLinks) {
            { intent ->
                val uri = intent.data
                val source = intent.getStringExtra("source") ?: "unknown"
                android.util.Log.d("Navigation", "Deep link received: $uri from $source")
            }
        } else null
    )
}

sealed class NavigationEvent {
    data class RouteChanged(val from: String, val to: String) : NavigationEvent()
    data class DeepLinkReceived(val intent: Intent) : NavigationEvent()
    data class NavigationError(val error: Throwable) : NavigationEvent()
}

fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateToPlayerFromAny() {
    navigate(Screen.Player.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToPlaylistFromAny(playlistId: Long) {
    navigate(Screen.Playlist.createRoute(playlistId)) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSearchFromAny(query: String? = null) {
    val route = if (query != null) {
        "${Screen.Search.route}?query=${android.net.Uri.encode(query)}"
    } else {
        Screen.Search.route
    }
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.isOnTopLevelDestination(): Boolean {
    val currentRoute = currentBackStackEntry?.destination?.route
    return TopLevelDestination.values().any { it.route == currentRoute }
}

fun NavHostController.getCurrentTopLevelDestination(): TopLevelDestination? {
    val currentRoute = currentBackStackEntry?.destination?.route
    return TopLevelDestination.values().find { it.route == currentRoute }
}

@Composable
fun HandleSystemBackPress(
    navController: NavHostController,
    onBackPressed: (() -> Boolean)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    BackHandler {
        val handled = onBackPressed?.invoke() ?: false
        if (!handled) {
            if (!navController.popBackStack()) {
                activity?.finish()
            }
        }
    }
}

@Composable
fun LocalPlayerNavigationPreview(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    LocalPlayerNavigation(
        windowSizeClass = windowSizeClass,
        modifier = modifier
    )
}

@Composable
fun LocalPlayerNavigationDebug(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    showDebugInfo: Boolean = false
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val backStackCount = navController.backQueue.size
    
    Column(modifier = modifier) {
        if (showDebugInfo) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Debug Navigation Info:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Route: ${currentRoute ?: "Unknown"}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Back Stack Count: $backStackCount",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Is Top Level: ${navController.isOnTopLevelDestination()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        
        LocalPlayerNavigation(
            windowSizeClass = windowSizeClass,
            navController = navController,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NavigationErrorBoundary(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onError: ((Throwable) -> Unit)? = null,
    fallbackContent: @Composable (() -> Unit)? = null
) {
    var hasError by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    
    if (hasError) {
        if (fallbackContent != null) {
            fallbackContent()
        } else {
            Card(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Navigation Error",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error?.message ?: "Unknown navigation error occurred",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            hasError = false
                            error = null
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    } else {
        try {
            LocalPlayerNavigation(
                windowSizeClass = windowSizeClass,
                modifier = modifier
            )
        } catch (e: Exception) {
            LaunchedEffect(e) {
                hasError = true
                error = e
                onError?.invoke(e)
                android.util.Log.e("NavigationError", "Navigation error occurred", e)
            }
        }
    }
}
