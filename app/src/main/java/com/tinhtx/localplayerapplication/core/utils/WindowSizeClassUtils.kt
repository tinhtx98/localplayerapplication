package com.tinhtx.localplayerapplication.core.utils

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utility functions for handling different window size classes
 */
object WindowSizeClassUtils {

    /**
     * Breakpoints for window width (following Material Design 3 guidelines)
     */
    object WindowWidthBreakpoints {
        val Compact = 0.dp
        val Medium = 600.dp
        val Expanded = 840.dp
    }

    /**
     * Breakpoints for window height
     */
    object WindowHeightBreakpoints {
        val Compact = 0.dp
        val Medium = 480.dp
        val Expanded = 900.dp
    }

    /**
     * Get current window width size class
     */
    @Composable
    fun getCurrentWidthSizeClass(): WindowWidthSizeClass {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < WindowWidthBreakpoints.Medium -> WindowWidthSizeClass.Compact
            screenWidth < WindowWidthBreakpoints.Expanded -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }

    /**
     * Get current window height size class
     */
    @Composable
    fun getCurrentHeightSizeClass(): WindowHeightSizeClass {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        
        return when {
            screenHeight < WindowHeightBreakpoints.Medium -> WindowHeightSizeClass.Compact
            screenHeight < WindowHeightBreakpoints.Expanded -> WindowHeightSizeClass.Medium
            else -> WindowHeightSizeClass.Expanded
        }
    }

    /**
     * Check if current window size is compact
     */
    fun WindowSizeClass.isCompact(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Compact
    }

    /**
     * Check if current window size is medium
     */
    fun WindowSizeClass.isMedium(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Medium
    }

    /**
     * Check if current window size is expanded
     */
    fun WindowSizeClass.isExpanded(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Expanded
    }

    /**
     * Check if should use navigation rail instead of bottom navigation
     */
    fun WindowSizeClass.shouldUseNavigationRail(): Boolean {
        return widthSizeClass != WindowWidthSizeClass.Compact
    }

    /**
     * Check if should use dual pane layout
     */
    fun WindowSizeClass.shouldUseDualPane(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Expanded
    }

    /**
     * Get number of columns for grid layout based on window size
     */
    fun WindowSizeClass.getGridColumns(): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 2
            WindowWidthSizeClass.Medium -> 3
            WindowWidthSizeClass.Expanded -> 4
        }
    }

    /**
     * Get content padding based on window size
     */
    fun WindowSizeClass.getContentPadding(): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 16.dp
            WindowWidthSizeClass.Medium -> 24.dp
            WindowWidthSizeClass.Expanded -> 32.dp
        }
    }

    /**
     * Get sidebar width for dual pane layout
     */
    fun WindowSizeClass.getSidebarWidth(): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 0.dp
            WindowWidthSizeClass.Medium -> 256.dp
            WindowWidthSizeClass.Expanded -> 320.dp
        }
    }

    /**
     * Check if should show extended player controls
     */
    fun WindowSizeClass.shouldShowExtendedPlayerControls(): Boolean {
        return widthSizeClass != WindowWidthSizeClass.Compact && 
               heightSizeClass != WindowHeightSizeClass.Compact
    }

    /**
     * Get player layout type based on window size
     */
    fun WindowSizeClass.getPlayerLayoutType(): PlayerLayoutType {
        return when {
            widthSizeClass == WindowWidthSizeClass.Compact -> PlayerLayoutType.COMPACT
            widthSizeClass == WindowWidthSizeClass.Medium -> PlayerLayoutType.MEDIUM
            else -> PlayerLayoutType.EXPANDED
        }
    }

    /**
     * Get album grid item size based on window size
     */
    fun WindowSizeClass.getAlbumGridItemSize(): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 160.dp
            WindowWidthSizeClass.Medium -> 180.dp
            WindowWidthSizeClass.Expanded -> 200.dp
        }
    }

    /**
     * Get list item height based on window size
     */
    fun WindowSizeClass.getListItemHeight(): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 56.dp
            WindowWidthSizeClass.Medium -> 64.dp
            WindowWidthSizeClass.Expanded -> 72.dp
        }
    }

    /**
     * Check if should use adaptive navigation
     */
    fun WindowSizeClass.shouldUseAdaptiveNavigation(): Boolean {
        return widthSizeClass != WindowWidthSizeClass.Compact
    }

    /**
     * Get bottom sheet peek height based on window size
     */
    fun WindowSizeClass.getBottomSheetPeekHeight(): Dp {
        return when (heightSizeClass) {
            WindowHeightSizeClass.Compact -> 56.dp
            WindowHeightSizeClass.Medium -> 64.dp
            WindowHeightSizeClass.Expanded -> 72.dp
        }
    }

    /**
     * Player layout types
     */
    enum class PlayerLayoutType {
        COMPACT,    // Single column, minimal controls
        MEDIUM,     // Single column, standard controls
        EXPANDED    // Dual column or expanded controls
    }

    /**
     * Navigation type based on window size
     */
    sealed class NavigationType {
        object BottomNavigation : NavigationType()
        object NavigationRail : NavigationType()
        object NavigationDrawer : NavigationType()
    }

    /**
     * Get appropriate navigation type for window size
     */
    fun WindowSizeClass.getNavigationType(): NavigationType {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> NavigationType.BottomNavigation
            WindowWidthSizeClass.Medium -> NavigationType.NavigationRail
            WindowWidthSizeClass.Expanded -> NavigationType.NavigationDrawer
        }
    }

    /**
     * Layout configuration for different screen sizes
     */
    data class LayoutConfiguration(
        val gridColumns: Int,
        val contentPadding: Dp,
        val sidebarWidth: Dp,
        val albumItemSize: Dp,
        val listItemHeight: Dp,
        val shouldUseDualPane: Boolean,
        val shouldUseNavigationRail: Boolean,
        val navigationType: NavigationType,
        val playerLayoutType: PlayerLayoutType
    )

    /**
     * Get complete layout configuration for window size
     */
    fun WindowSizeClass.getLayoutConfiguration(): LayoutConfiguration {
        return LayoutConfiguration(
            gridColumns = getGridColumns(),
            contentPadding = getContentPadding(),
            sidebarWidth = getSidebarWidth(),
            albumItemSize = getAlbumGridItemSize(),
            listItemHeight = getListItemHeight(),
            shouldUseDualPane = shouldUseDualPane(),
            shouldUseNavigationRail = shouldUseNavigationRail(),
            navigationType = getNavigationType(),
            playerLayoutType = getPlayerLayoutType()
        )
    }
}
