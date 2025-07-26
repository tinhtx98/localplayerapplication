package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.navigation.BottomNavItem
import com.tinhtx.localplayerapplication.presentation.navigation.LocalPlayerNavigationBarItem
import com.tinhtx.localplayerapplication.presentation.navigation.TopLevelDestination

@Composable
fun MusicBottomNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: androidx.compose.ui.unit.Dp = 3.dp
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            
            LocalPlayerNavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                destination = destination
            )
        }
    }
}

@Composable
fun FloatingBottomNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val selected = currentDestination == destination.route
                
                FloatingNavItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    label = if (showLabels) stringResource(destination.titleResId) else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    scaleIn(initialScale = 0.8f) + fadeIn() with 
                    scaleOut(targetScale = 0.8f) + fadeOut()
                },
                label = "nav_icon"
            ) { isSelected ->
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
        
        if (label != null) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MinimalBottomNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            
            IconButton(
                onClick = { onNavigateToDestination(destination) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = stringResource(destination.titleResId),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

@Composable
fun AnimatedBottomNavigationBar(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = 300
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            
            NavigationBarItem(
                icon = {
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            (scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(animationDuration)
                            ) + fadeIn(animationSpec = tween(animationDuration))) with
                            (scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(animationDuration)
                            ) + fadeOut(animationSpec = tween(animationDuration)))
                        },
                        label = "nav_bar_icon"
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    AnimatedContent(
                        targetState = stringResource(destination.titleResId),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(animationDuration)) with
                            fadeOut(animationSpec = tween(animationDuration))
                        },
                        label = "nav_bar_label"
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun TabBarBottomNavigation(
    destinations: List<TopLevelDestination>,
    currentDestination: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            destinations.forEach { destination ->
                val selected = currentDestination == destination.route
                
                TabBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    label = stringResource(destination.titleResId),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(2.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}
