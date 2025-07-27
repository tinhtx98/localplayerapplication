package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen size breakpoints for responsive design
 */
object Breakpoints {
    val Compact = 0.dp
    val Medium = 600.dp
    val Expanded = 840.dp
    val Large = 1200.dp
    val ExtraLarge = 1600.dp
}

/**
 * Screen size classification
 */
enum class ScreenSize {
    Compact,    // Phones in portrait
    Medium,     // Phones in landscape, small tablets
    Expanded,   // Large tablets
    Large,      // Desktop, large tablets in landscape
    ExtraLarge  // Large desktop screens
}

/**
 * Device orientation
 */
enum class DeviceOrientation {
    Portrait,
    Landscape
}

/**
 * Device type classification
 */
enum class DeviceType {
    Phone,
    Tablet,
    Desktop
}

/**
 * Responsive layout information
 */
@Stable
data class ResponsiveInfo(
    val screenSize: ScreenSize,
    val orientation: DeviceOrientation,
    val deviceType: DeviceType,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val windowSizeClass: WindowSizeClass?
)

/**
 * Provides responsive layout information
 */
@Composable
fun rememberResponsiveInfo(): ResponsiveInfo {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidth = with(density) { configuration.screenWidthDp.dp }
    val screenHeight = with(density) { configuration.screenHeightDp.dp }
    
    val screenSize = when {
        screenWidth < Breakpoints.Medium -> ScreenSize.Compact
        screenWidth < Breakpoints.Expanded -> ScreenSize.Medium
        screenWidth < Breakpoints.Large -> ScreenSize.Expanded
        screenWidth < Breakpoints.ExtraLarge -> ScreenSize.Large
        else -> ScreenSize.ExtraLarge
    }
    
    val orientation = if (screenWidth > screenHeight) {
        DeviceOrientation.Landscape
    } else {
        DeviceOrientation.Portrait
    }
    
    val deviceType = when {
        screenWidth < Breakpoints.Medium -> DeviceType.Phone
        screenWidth < Breakpoints.Large -> DeviceType.Tablet
        else -> DeviceType.Desktop
    }
    
    return ResponsiveInfo(
        screenSize = screenSize,
        orientation = orientation,
        deviceType = deviceType,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        windowSizeClass = null // Can be provided if needed
    )
}

/**
 * Adaptive layout based on screen size
 */
@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null,
    largeContent: (@Composable () -> Unit)? = null
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    Box(modifier = modifier) {
        when (responsiveInfo.screenSize) {
            ScreenSize.Compact -> compactContent()
            ScreenSize.Medium -> mediumContent?.invoke() ?: compactContent()
            ScreenSize.Expanded -> expandedContent?.invoke() ?: mediumContent?.invoke() ?: compactContent()
            ScreenSize.Large, ScreenSize.ExtraLarge -> 
                largeContent?.invoke() ?: expandedContent?.invoke() ?: mediumContent?.invoke() ?: compactContent()
        }
    }
}

/**
 * Responsive column count for grids
 */
@Composable
fun rememberResponsiveColumnCount(
    compactColumns: Int = 1,
    mediumColumns: Int = 2,
    expandedColumns: Int = 3,
    largeColumns: Int = 4
): Int {
    val responsiveInfo = rememberResponsiveInfo()
    
    return when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compactColumns
        ScreenSize.Medium -> mediumColumns
        ScreenSize.Expanded -> expandedColumns
        ScreenSize.Large, ScreenSize.ExtraLarge -> largeColumns
    }
}

/**
 * Responsive padding values
 */
@Composable
fun rememberResponsivePadding(
    compactPadding: Dp = 8.dp,
    mediumPadding: Dp = 16.dp,
    expandedPadding: Dp = 24.dp,
    largePadding: Dp = 32.dp
): Dp {
    val responsiveInfo = rememberResponsiveInfo()
    
    return when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compactPadding
        ScreenSize.Medium -> mediumPadding
        ScreenSize.Expanded -> expandedPadding
        ScreenSize.Large, ScreenSize.ExtraLarge -> largePadding
    }
}

/**
 * Responsive content width
 */
@Composable
fun rememberResponsiveContentWidth(
    maxWidth: Dp = 1200.dp
): Dp {
    val responsiveInfo = rememberResponsiveInfo()
    
    return when (responsiveInfo.screenSize) {
        ScreenSize.Compact, ScreenSize.Medium -> responsiveInfo.screenWidth
        ScreenSize.Expanded -> minOf(responsiveInfo.screenWidth, 800.dp)
        ScreenSize.Large, ScreenSize.ExtraLarge -> minOf(responsiveInfo.screenWidth, maxWidth)
    }
}

/**
 * Responsive navigation type
 */
enum class NavigationType {
    BottomNavigation,
    NavigationRail,
    NavigationDrawer
}

@Composable
fun rememberNavigationType(): NavigationType {
    val responsiveInfo = rememberResponsiveInfo()
    
    return when {
        responsiveInfo.screenSize == ScreenSize.Compact -> NavigationType.BottomNavigation
        responsiveInfo.screenSize == ScreenSize.Medium && 
        responsiveInfo.orientation == DeviceOrientation.Portrait -> NavigationType.BottomNavigation
        responsiveInfo.screenSize in listOf(ScreenSize.Medium, ScreenSize.Expanded) -> NavigationType.NavigationRail
        else -> NavigationType.NavigationDrawer
    }
}

/**
 * Responsive modifier extensions
 */
fun Modifier.responsiveWidth(
    compactFraction: Float = 1f,
    mediumFraction: Float = 1f,
    expandedFraction: Float = 0.8f,
    largeFraction: Float = 0.7f
): Modifier = composed {
    val responsiveInfo = rememberResponsiveInfo()
    
    val fraction = when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compactFraction
        ScreenSize.Medium -> mediumFraction
        ScreenSize.Expanded -> expandedFraction
        ScreenSize.Large, ScreenSize.ExtraLarge -> largeFraction
    }
    
    fillMaxWidth(fraction)
}

fun Modifier.responsivePadding(
    compact: Dp = 8.dp,
    medium: Dp = 16.dp,
    expanded: Dp = 24.dp,
    large: Dp = 32.dp
): Modifier = composed {
    val responsiveInfo = rememberResponsiveInfo()
    
    val paddingValue = when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compact
        ScreenSize.Medium -> medium
        ScreenSize.Expanded -> expanded
        ScreenSize.Large, ScreenSize.ExtraLarge -> large
    }
    
    padding(paddingValue)
}

fun Modifier.responsiveHorizontalPadding(
    compact: Dp = 16.dp,
    medium: Dp = 24.dp,
    expanded: Dp = 32.dp,
    large: Dp = 48.dp
): Modifier = composed {
    val responsiveInfo = rememberResponsiveInfo()
    
    val paddingValue = when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compact
        ScreenSize.Medium -> medium
        ScreenSize.Expanded -> expanded
        ScreenSize.Large, ScreenSize.ExtraLarge -> large
    }
    
    padding(horizontal = paddingValue)
}

/**
 * Responsive grid layout
 */
@Composable
fun ResponsiveGrid(
    items: List<Any>,
    modifier: Modifier = Modifier,
    compactColumns: Int = 1,
    mediumColumns: Int = 2,
    expandedColumns: Int = 3,
    largeColumns: Int = 4,
    verticalSpacing: Dp = 8.dp,
    horizontalSpacing: Dp = 8.dp,
    content: @Composable (item: Any, index: Int) -> Unit
) {
    val columns = rememberResponsiveColumnCount(
        compactColumns = compactColumns,
        mediumColumns = mediumColumns,
        expandedColumns = expandedColumns,
        largeColumns = largeColumns
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(rememberResponsivePadding()),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            content(item, index)
        }
    }
}

/**
 * Responsive row layout
 */
@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Convert row content to column for compact screens
                content(object : RowScope {
                    @Stable
                    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
                })
            }
        }
        else -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spacing),
                content = content
            )
        }
    }
}

/**
 * Responsive side panel layout
 */
@Composable
fun ResponsiveSidePanel(
    mainContent: @Composable () -> Unit,
    sideContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showSidePanel: Boolean = true,
    sidePanelWidth: Dp = 300.dp
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    when {
        !showSidePanel || responsiveInfo.screenSize == ScreenSize.Compact -> {
            mainContent()
        }
        responsiveInfo.screenSize in listOf(ScreenSize.Medium, ScreenSize.Expanded) -> {
            Row(modifier = modifier) {
                Box(modifier = Modifier.weight(1f)) {
                    mainContent()
                }
                Box(modifier = Modifier.width(sidePanelWidth)) {
                    sideContent()
                }
            }
        }
        else -> {
            Row(modifier = modifier) {
                Box(modifier = Modifier.width(sidePanelWidth)) {
                    sideContent()
                }
                Box(modifier = Modifier.weight(1f)) {
                    mainContent()
                }
            }
        }
    }
}

/**
 * Window size class utilities
 */
@Composable
fun calculateWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp
    
    val widthSizeClass = when {
        widthDp < 600.dp -> WindowWidthSizeClass.Compact
        widthDp < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    
    val heightSizeClass = when {
        heightDp < 480.dp -> WindowHeightSizeClass.Compact
        heightDp < 900.dp -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Expanded
    }
    
    return WindowSizeClass(
        widthSizeClass = widthSizeClass,
        heightSizeClass = heightSizeClass
    )
}

/**
 * Responsive text size
 */
@Composable
fun rememberResponsiveTextScale(
    compactScale: Float = 0.9f,
    mediumScale: Float = 1f,
    expandedScale: Float = 1.1f,
    largeScale: Float = 1.2f
): Float {
    val responsiveInfo = rememberResponsiveInfo()
    
    return when (responsiveInfo.screenSize) {
        ScreenSize.Compact -> compactScale
        ScreenSize.Medium -> mediumScale
        ScreenSize.Expanded -> expandedScale
        ScreenSize.Large, ScreenSize.ExtraLarge -> largeScale
    }
}

/**
 * Responsive safe area padding
 */
@Composable
fun Modifier.responsiveSafeAreaPadding(): Modifier = composed {
    val responsiveInfo = rememberResponsiveInfo()
    
    when (responsiveInfo.deviceType) {
        DeviceType.Phone -> systemBarsPadding()
        DeviceType.Tablet -> padding(8.dp)
        DeviceType.Desktop -> padding(16.dp)
    }
}

/**
 * Check if device is foldable or has specific form factor
 */
@Composable
fun rememberIsFoldable(): Boolean {
    val configuration = LocalConfiguration.current
    // Basic detection - can be enhanced with WindowManager APIs
    return configuration.screenWidthDp > 600 && configuration.screenHeightDp > 600
}

/**
 * Responsive content arrangement
 */
@Composable
fun ResponsiveContentArrangement(
    compactContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val responsiveInfo = rememberResponsiveInfo()
    
    Box(modifier = modifier) {
        when (responsiveInfo.screenSize) {
            ScreenSize.Compact, ScreenSize.Medium -> compactContent()
            else -> expandedContent()
        }
    }
}
