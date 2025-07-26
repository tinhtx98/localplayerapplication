package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Stable
data class NavigationBarConfig(
    val containerColor: Color,
    val contentColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val indicatorColor: Color,
    val tonalElevation: Dp,
    val showLabels: Boolean,
    val animationDuration: Int,
    val style: NavigationBarStyle
)

enum class NavigationBarStyle {
    STANDARD,
    FLOATING,
    MINIMAL,
    TAB_BAR
}

@Composable
fun rememberNavigationBarConfig(
    style: NavigationBarStyle = NavigationBarStyle.STANDARD,
    showLabels: Boolean = true,
    animationDuration: Int = 300,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    tonalElevation: Dp = 3.dp
): NavigationBarConfig {
    return remember(
        style,
        showLabels,
        animationDuration,
        containerColor,
        contentColor,
        selectedContentColor,
        unselectedContentColor,
        indicatorColor,
        tonalElevation
    ) {
        NavigationBarConfig(
            containerColor = containerColor,
            contentColor = contentColor,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor,
            indicatorColor = indicatorColor,
            tonalElevation = tonalElevation,
            showLabels = showLabels,
            animationDuration = animationDuration,
            style = style
        )
    }
}

@Stable
data class NavigationRailConfig(
    val containerColor: Color,
    val contentColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val indicatorColor: Color,
    val showLabels: Boolean,
    val animationDuration: Int,
    val style: NavigationRailStyle
)

enum class NavigationRailStyle {
    STANDARD,
    COMPACT,
    FLOATING
}

@Composable
fun rememberNavigationRailConfig(
    style: NavigationRailStyle = NavigationRailStyle.STANDARD,
    showLabels: Boolean = true,
    animationDuration: Int = 300,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer
): NavigationRailConfig {
    return remember(
        style,
        showLabels,
        animationDuration,
        containerColor,
        contentColor,
        selectedContentColor,
        unselectedContentColor,
        indicatorColor
    ) {
        NavigationRailConfig(
            containerColor = containerColor,
            contentColor = contentColor,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor,
            indicatorColor = indicatorColor,
            showLabels = showLabels,
            animationDuration = animationDuration,
            style = style
        )
    }
}
