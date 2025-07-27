package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: BottomNavStyle = BottomNavStyle.Default,
    showLabels: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    hasNotification: Map<String, Boolean> = emptyMap()
) {
    when (style) {
        BottomNavStyle.Default -> DefaultBottomNav(
            items = items,
            selectedItem = selectedItem,
            onItemClick = onItemClick,
            showLabels = showLabels,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedColor = selectedColor,
            hasNotification = hasNotification,
            modifier = modifier
        )
        BottomNavStyle.Floating -> FloatingBottomNav(
            items = items,
            selectedItem = selectedItem,
            onItemClick = onItemClick,
            showLabels = showLabels,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedColor = selectedColor,
            hasNotification = hasNotification,
            modifier = modifier
        )
        BottomNavStyle.Pills -> PillsBottomNav(
            items = items,
            selectedItem = selectedItem,
            onItemClick = onItemClick,
            showLabels = showLabels,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedColor = selectedColor,
            hasNotification = hasNotification,
            modifier = modifier
        )
    }
}

@Composable
private fun DefaultBottomNav(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    showLabels: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    selectedColor: Color,
    hasNotification: Map<String, Boolean>,
    modifier: Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item.route
            val hasNotif = hasNotification[item.route] == true
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    BottomNavIcon(
                        icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        hasNotification = hasNotif,
                        isSelected = isSelected,
                        selectedColor = selectedColor
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = contentColor.copy(alpha = 0.7f),
                    unselectedTextColor = contentColor.copy(alpha = 0.7f),
                    indicatorColor = selectedColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
private fun FloatingBottomNav(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    showLabels: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    selectedColor: Color,
    hasNotification: Map<String, Boolean>,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        shadowElevation = 8.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item.route
                val hasNotif = hasNotification[item.route] == true
                
                FloatingNavItem(
                    item = item,
                    isSelected = isSelected,
                    hasNotification = hasNotif,
                    showLabel = showLabels,
                    onClick = { onItemClick(item.route) },
                    selectedColor = selectedColor,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun PillsBottomNav(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    showLabels: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    selectedColor: Color,
    hasNotification: Map<String, Boolean>,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItem == item.route
                val hasNotif = hasNotification[item.route] == true
                
                PillNavItem(
                    item = item,
                    isSelected = isSelected,
                    hasNotification = hasNotif,
                    showLabel = showLabels,
                    onClick = { onItemClick(item.route) },
                    selectedColor = selectedColor,
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavIcon(
    icon: ImageVector,
    hasNotification: Boolean,
    isSelected: Boolean,
    selectedColor: Color,
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
                modifier = Modifier.size(if (selected) 26.dp else 24.dp)
            )
        }
        
        // Notification badge
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    hasNotification: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    contentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        color = if (isSelected) selectedColor.copy(alpha = 0.12f) else Color.Transparent,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BottomNavIcon(
                icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                hasNotification = hasNotification,
                isSelected = isSelected,
                selectedColor = selectedColor
            )
            
            if (showLabel) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = selectedColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PillNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    hasNotification: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) selectedColor else contentColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BottomNavIcon(
                icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                hasNotification = hasNotification,
                isSelected = isSelected,
                selectedColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else contentColor
            )
            
            AnimatedVisibility(
                visible = isSelected && showLabel,
                enter = slideInHorizontally(initialOffsetX = { -it / 2 }) + 
                        expandHorizontally() + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it / 2 }) + 
                       shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AdaptiveBottomNavigation(
    items: List<BottomNavItem>,
    selectedItem: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Map<String, Boolean> = emptyMap()
) {
    val responsiveInfo = com.tinhtx.localplayerapplication.presentation.components.common.rememberResponsiveInfo()
    
    when (responsiveInfo.screenSize) {
        com.tinhtx.localplayerapplication.presentation.components.common.ScreenSize.Compact -> {
            BottomNavigationBar(
                items = items,
                selectedItem = selectedItem,
                onItemClick = onItemClick,
                style = BottomNavStyle.Default,
                hasNotification = hasNotification,
                modifier = modifier
            )
        }
        com.tinhtx.localplayerapplication.presentation.components.common.ScreenSize.Medium -> {
            BottomNavigationBar(
                items = items,
                selectedItem = selectedItem,
                onItemClick = onItemClick,
                style = BottomNavStyle.Floating,
                hasNotification = hasNotification,
                modifier = modifier
            )
        }
        else -> {
            BottomNavigationBar(
                items = items,
                selectedItem = selectedItem,
                onItemClick = onItemClick,
                style = BottomNavStyle.Pills,
                hasNotification = hasNotification,
                modifier = modifier
            )
        }
    }
}

enum class BottomNavStyle {
    Default,
    Floating,
    Pills
}
