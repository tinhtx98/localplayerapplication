package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.navigation.TopLevelDestination

@Composable
fun MusicNavigationRail(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = containerColor,
        contentColor = contentColor,
        header = header
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            
            NavigationRailItem(
                icon = {
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            scaleIn(initialScale = 0.8f) + fadeIn() with 
                            scaleOut(targetScale = 0.8f) + fadeOut()
                        },
                        label = "nav_rail_icon"
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(destination.titleResId),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = contentColor.copy(alpha = 0.6f),
                    unselectedTextColor = contentColor.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CompactNavigationRail(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = stringResource(destination.titleResId)
                    )
                },
                label = if (showLabels) {
                    {
                        Text(
                            text = stringResource(destination.titleResId),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                } else null,
                selected = selected,
                onClick = { onNavigateToDestination(destination) }
            )
        }
    }
}

@Composable
fun FloatingNavigationRail(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    fabContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FAB at top if provided
            fabContent?.let { fab ->
                fab()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Navigation items
            destinations.forEach { destination ->
                val selected = currentDestination == destination.route
                
                NavigationRailIconButton(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = stringResource(destination.titleResId)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavigationRailIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
