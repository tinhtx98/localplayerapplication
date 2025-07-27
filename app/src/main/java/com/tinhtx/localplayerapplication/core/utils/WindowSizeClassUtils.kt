package com.tinhtx.localplayerapplication.core.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WindowSizeClassUtils {
    
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
     * Should use bottom navigation
     */
    fun WindowSizeClass.shouldUseBottomNavigation(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Compact
    }
    
    /**
     * Should use navigation rail
     */
    fun WindowSizeClass.shouldUseNavigationRail(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Expanded
    }
    
    /**
     * Get appropriate column count for grid layouts
     */
    fun WindowSizeClass.getGridColumnCount(): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 2
            WindowWidthSizeClass.Medium -> 3
            WindowWidthSizeClass.Expanded -> 4
            else -> 2
        }
    }
    
    /**
     * Get content padding based on window size
     */
    fun WindowSizeClass.getContentPadding() = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp
        WindowWidthSizeClass.Medium -> 24.dp
        WindowWidthSizeClass.Expanded -> 32.dp
        else -> 16.dp
    }
    
    /**
     * Calculate dynamic sizes based on screen width
     */
    @Composable
    fun calculateDynamicSizes(): DynamicSizes {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < 600.dp -> DynamicSizes.Compact
            screenWidth < 840.dp -> DynamicSizes.Medium
            else -> DynamicSizes.Expanded
        }
    }
    
    data class DynamicSizes(
        val cardWidth: Dp,
        val imageSize: Dp,
        val titleSize: TextUnit,
        val subtitleSize: TextUnit
    ) {
        companion object {
            val Compact = DynamicSizes(
                cardWidth = 160.dp,
                imageSize = 140.dp,
                titleSize = 14.sp,
                subtitleSize = 12.sp
            )
            
            val Medium = DynamicSizes(
                cardWidth = 180.dp,
                imageSize = 160.dp,
                titleSize = 16.sp,
                subtitleSize = 14.sp
            )
            
            val Expanded = DynamicSizes(
                cardWidth = 200.dp,
                imageSize = 180.dp,
                titleSize = 18.sp,
                subtitleSize = 16.sp
            )
        }
    }
}
