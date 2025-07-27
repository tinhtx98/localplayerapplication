package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.navigation.BottomNavItem

@Composable
fun NavigationRail(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    showLabels: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    hasNotification: Map<String, Boolean> = emptyMap(),
    isCompact: Boolean = false
) {
    androidx.compose.material3.NavigationRail(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        header = header,
        windowInsets = WindowInsets(0)
    ) {
        // Header content
        header?.invoke()
        
        if (header != null) {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Navigation items
        items.forEach { item ->
            val isSelected = selectedItem == item.route
            val hasNotif = hasNotification[item.route] == true
            
            if (isCompact) {
                CompactRailItem(
                    item = item,
                    isSelected = isSelected,
                    hasNotification = hasNotif,
                    onClick = { onItemClick(item.route) },
                    selectedColor = selectedColor,
                    contentColor = contentColor
                )
            } else {
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onItemClick(item.route) },
                    icon = {
                        RailNavIcon(
                            icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            hasNotification = hasNotif,
                            isSelected = isSelected
                        )
                    },
                    label = if (showLabels) {
                        {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    } else null,
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = contentColor.copy(alpha = 0.7f),
                        unselectedTextColor = contentColor.copy(alpha = 0.7f),
                        indicatorColor = selectedColor.copy(alpha = 0.12f)
                    )
                )
            }
        }
        
        // Footer content
        if (footer != null) {
            Spacer(modifier = Modifier.weight(1f))
            footer()
        }
    }
}

@Composable
private fun CompactRailItem(
    item: BottomNavItem,
    isSelected: Boolean,
    hasNotification: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    contentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.12f) else Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            RailNavIcon(
                icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                hasNotification = hasNotification,
                isSelected = isSelected,
                color = if (isSelected) selectedColor else contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RailNavIcon(
    icon: ImageVector,
    hasNotification: Boolean,
    isSelected: Boolean,
    color: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                scaleIn(animationSpec = tween(200)) + fadeIn() with 
                scaleOut(animationSpec = tween(200)) + fadeOut()
            }
        ) { selected ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (selected) 26.dp else 24.dp),
                tint = color ?: LocalContentColor.current
            )
        }
        
        // Notification badge
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}

@Composable
fun ExpandableNavigationRail(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    hasNotification: Map<String, Boolean> = emptyMap()
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (expanded) -it else it },
                    animationSpec = tween(300)
                ) + fadeIn() with
                slideOutHorizontally(
                    targetOffsetX = { if (expanded) it else -it },
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) { isExpanded ->
            if (isExpanded) {
                // Expanded rail with labels
                Row {
                    NavigationRail(
                        items = items,
                        selectedItem = selectedItem,
                        onItemClick = onItemClick,
                        header = header,
                        footer = footer,
                        showLabels = true,
                        hasNotification = hasNotification,
                        modifier = Modifier.width(120.dp)
                    )
                }
            } else {
                // Compact rail
                NavigationRail(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onItemClick,
                    header = {
                        header?.invoke()
                        
                        // Toggle button
                        IconButton(
                            onClick = onToggleExpanded,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Expand navigation"
                            )
                        }
                    },
                    footer = footer,
                    showLabels = false,
                    hasNotification = hasNotification,
                    isCompact = true,
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

@Composable
fun NavigationRailWithSearch(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap()
) {
    NavigationRail(
        items = items,
        selectedItem = selectedItem,
        onItemClick = onItemClick,
        header = {
            // Search button
            Surface(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        hasNotification = hasNotification,
        modifier = modifier
    )
}

@Composable
fun NavigationRailWithUser(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    userImageUrl: String?,
    userName: String,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap()
) {
    NavigationRail(
        items = items,
        selectedItem = selectedItem,
        onItemClick = onItemClick,
        footer = {
            // User profile section
            Surface(
                onClick = onUserClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User avatar
                    com.tinhtx.localplayerapplication.presentation.components.image.CircularUserAvatar(
                        imageUrl = userImageUrl,
                        userName = userName,
                        size = 32.dp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = userName.take(8),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        hasNotification = hasNotification,
        modifier = modifier
    )
}
