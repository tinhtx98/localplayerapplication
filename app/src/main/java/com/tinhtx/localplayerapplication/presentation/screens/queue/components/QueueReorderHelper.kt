package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun <T> DraggableItem(
    item: T,
    key: Any,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "drag_elevation"
    )

    Box(
        modifier = modifier
            .zIndex(if (isDragging) 1f else 0f)
            .shadow(elevation)
            .pointerInput(key) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { _, _ -> }
                )
            }
    ) {
        content(item)
    }
}

data class DragDropState(
    val draggedIndex: Int? = null,
    val targetIndex: Int? = null
) {
    val isDragging: Boolean get() = draggedIndex != null
}

@Composable
fun rememberDragDropState(): MutableState<DragDropState> {
    return remember { mutableStateOf(DragDropState()) }
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(absoluteIndex - this.firstVisibleItemIndex)
}
