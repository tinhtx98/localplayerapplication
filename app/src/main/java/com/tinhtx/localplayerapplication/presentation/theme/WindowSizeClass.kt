package com.tinhtx.localplayerapplication.presentation.theme

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WindowSizeClass.shouldUseBottomNavigation(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

@Composable
fun WindowSizeClass.shouldUseNavigationRail(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded
}

@Composable
fun WindowSizeClass.getHorizontalPadding(): Dp {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp
        WindowWidthSizeClass.Medium -> 24.dp
        WindowWidthSizeClass.Expanded -> 32.dp
        else -> 16.dp
    }
}

@Composable
fun WindowSizeClass.getContentMaxWidth(): Dp {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Dp.Unspecified
        WindowWidthSizeClass.Medium -> 840.dp
        WindowWidthSizeClass.Expanded -> 1200.dp
        else -> Dp.Unspecified
    }
}

@Composable
fun WindowSizeClass.getGridColumns(): Int {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
    }
}
