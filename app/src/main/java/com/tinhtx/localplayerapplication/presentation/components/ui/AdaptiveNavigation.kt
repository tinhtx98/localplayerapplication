package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.navigation.TopLevelDestination

@Composable
fun AdaptiveNavigation(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            BottomNavigationBar(
                destinations = destinations,
                currentDestination = currentDestination,
                onNavigateToDestination = onNavigateToDestination,
                modifier = modifier
            )
        }
        WindowWidthSizeClass.Medium -> {
            NavigationRail(
                destinations = destinations,
                currentDestination = currentDestination,
                onNavigateToDestination = onNavigateToDestination,
                modifier = modifier
            )
        }
        WindowWidthSizeClass.Expanded -> {
            NavigationDrawer(
                destinations = destinations,
                currentDestination = currentDestination,
                onNavigateToDestination = onNavigateToDestination,
                modifier = modifier
            )
        }
        else -> {
            BottomNavigationBar(
                destinations = destinations,
                currentDestination = currentDestination,
                onNavigateToDestination = onNavigateToDestination,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ResponsiveNavigation(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Bottom navigation for compact screens
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        destinations = destinations,
                        currentDestination = currentDestination,
                        onNavigateToDestination = onNavigateToDestination
                    )
                },
                modifier = modifier
            ) { paddingValues ->
                content(paddingValues)
            }
        }
        WindowWidthSizeClass.Medium -> {
            // Navigation rail for medium screens
            Row(modifier = modifier) {
                NavigationRail(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = onNavigateToDestination
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues())
                }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Navigation drawer for expanded screens
            PermanentNavigationDrawer(
                drawerContent = {
                    NavigationDrawerContent(
                        destinations = destinations,
                        currentDestination = currentDestination,
                        onNavigateToDestination = onNavigateToDestination
                    )
                },
                modifier = modifier
            ) {
                content(PaddingValues())
            }
        }
        else -> {
            // Fallback to bottom navigation
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        destinations = destinations,
                        currentDestination = currentDestination,
                        onNavigateToDestination = onNavigateToDestination
                    )
                },
                modifier = modifier
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}

@Composable
fun AnimatedAdaptiveNavigation(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    animationDuration: Int = 300
) {
    AnimatedContent(
        targetState = windowSizeClass.widthSizeClass,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(animationDuration)
            ) with slideOutHorizontally(
                animationSpec = tween(animationDuration)
            )
        },
        modifier = modifier,
        label = "adaptive_navigation"
    ) { widthSizeClass ->
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                BottomNavigationBar(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = onNavigateToDestination
                )
            }
            WindowWidthSizeClass.Medium -> {
                NavigationRail(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = onNavigateToDestination
                )
            }
            WindowWidthSizeClass.Expanded -> {
                NavigationDrawerContent(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = onNavigateToDestination
                )
            }
            else -> {
                BottomNavigationBar(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = onNavigateToDestination
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawer(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    PermanentNavigationDrawer(
        drawerContent = {
            NavigationDrawerContent(
                destinations = destinations,
                currentDestination = currentDestination,
                onNavigateToDestination = onNavigateToDestination
            )
        },
        modifier = modifier
    ) {
        // Content will be provided by parent
    }
}

@Composable
private fun NavigationDrawerContent(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier.width(240.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            // App logo/title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Retro Music",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Navigation items
            destinations.forEach { destination ->
                val selected = currentDestination == destination.route
                
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(text = stringResource(destination.titleResId))
                    },
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
