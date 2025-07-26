package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveLayout(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    compactContent: @Composable BoxScope.() -> Unit,
    mediumContent: @Composable BoxScope.() -> Unit = compactContent,
    expandedContent: @Composable BoxScope.() -> Unit = mediumContent
) {
    Box(modifier = modifier) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> compactContent()
            WindowWidthSizeClass.Medium -> mediumContent()
            WindowWidthSizeClass.Expanded -> expandedContent()
            else -> compactContent()
        }
    }
}

@Composable
fun ResponsiveRow(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
    val arrangement = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Arrangement.spacedBy(spacing)
        WindowWidthSizeClass.Medium -> Arrangement.spacedBy(spacing * 1.5f)
        WindowWidthSizeClass.Expanded -> Arrangement.spacedBy(spacing * 2f)
        else -> Arrangement.spacedBy(spacing)
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        content = content
    )
}

@Composable
fun ResponsiveColumn(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrangement = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Arrangement.spacedBy(spacing)
        WindowWidthSizeClass.Medium -> Arrangement.spacedBy(spacing * 1.25f)
        WindowWidthSizeClass.Expanded -> Arrangement.spacedBy(spacing * 1.5f)
        else -> Arrangement.spacedBy(spacing)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = arrangement,
        content = content
    )
}

@Composable
fun ResponsivePadding(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val padding = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp
        WindowWidthSizeClass.Medium -> 24.dp
        WindowWidthSizeClass.Expanded -> 32.dp
        else -> 16.dp
    }
    
    Box(
        modifier = modifier.padding(padding),
        content = content
    )
}

@Composable
fun MaxWidthContainer(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val maxWidth = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> androidx.compose.ui.unit.Dp.Unspecified
        WindowWidthSizeClass.Medium -> 840.dp
        WindowWidthSizeClass.Expanded -> 1200.dp
        else -> androidx.compose.ui.unit.Dp.Unspecified
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .let { if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified) it.widthIn(max = maxWidth) else it },
        content = content
    )
}

@Composable
fun AdaptiveGrid(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    minItemWidth: androidx.compose.ui.unit.Dp = 160.dp,
    content: @Composable () -> Unit
) {
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
    }
    
    // Implementation sẽ sử dụng LazyVerticalGrid với columns đã tính toán
    // Đây là wrapper component để chuẩn bị cho việc sử dụng trong các screen
}
