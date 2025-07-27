package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Helper utilities for queue reordering functionality
 */
object QueueReorderHelper {
    
    /**
     * Calculate target index for drop based on drag position
     */
    fun calculateDropIndex(
        dragOffset: Float,
        itemHeight: Float,
        totalItems: Int,
        currentIndex: Int
    ): Int {
        val targetIndex = (dragOffset / itemHeight).roundToInt()
        return targetIndex.coerceIn(0, totalItems - 1)
    }
    
    /**
     * Determine if reorder is valid
     */
    fun isValidReorder(
        fromIndex: Int,
        toIndex: Int,
        totalItems: Int
    ): Boolean {
        return fromIndex != toIndex &&
                fromIndex in 0 until totalItems &&
                toIndex in 0 until totalItems
    }
    
    /**
     * Calculate visual offset for drag animation
     */
    fun calculateDragOffset(
        dragAmount: Float,
        maxOffset: Float
    ): Float {
        return dragAmount.coerceIn(-maxOffset, maxOffset)
    }
}

@Composable
fun DraggableItemModifier(
    index: Int,
    isDragging: Boolean,
    dragOffset: Float,
    onStartDrag: (Int) -> Unit,
    onDrag: (Float) -> Unit,
    onEndDrag: () -> Unit,
    modifier: Modifier = Modifier
): Modifier {
    val haptic = LocalHapticFeedback.current
    
    return modifier
        .graphicsLayer {
            translationY = if (isDragging) dragOffset else 0f
            alpha = if (isDragging) 0.8f else 1f
            scaleX = if (isDragging) 1.05f else 1f
            scaleY = if (isDragging) 1.05f else 1f
        }
        .pointerInput(index) {
            detectDragGestures(
                onDragStart = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartDrag(index) 
                },
                onDrag = { change, _ ->
                    onDrag(change.position.y)
                },
                onDragEnd = { onEndDrag() }
            )
        }
}

@Composable
fun ReorderInstructions(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Drag songs to reorder your queue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { /* TODO: Hide instructions */ }
                ) {
                    Text(
                        text = "Got it",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DropIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        Divider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 3.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ReorderFloatingActions(
    onMoveToTop: () -> Unit,
    onMoveToBottom: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Move to top
        SmallFloatingActionButton(
            onClick = onMoveToTop,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Move to top"
            )
        }

        // Move to bottom
        SmallFloatingActionButton(
            onClick = onMoveToBottom,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Move to bottom"
            )
        }

        // Cancel reorder
        SmallFloatingActionButton(
            onClick = onCancel,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel reorder"
            )
        }
    }
}

@Composable
fun ReorderPreview(
    fromIndex: Int,
    toIndex: Int,
    songTitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Moving \"$songTitle\" from position ${fromIndex + 1} to ${toIndex + 1}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Custom drag and drop state management
 */
@Composable
fun rememberReorderState(): ReorderState {
    return remember { ReorderState() }
}

class ReorderState {
    var isDragging by mutableStateOf(false)
        private set
    
    var draggedIndex by mutableStateOf<Int?>(null)
        private set
    
    var dropTargetIndex by mutableStateOf<Int?>(null)
        private set
    
    var dragOffset by mutableStateOf(0f)
        private set
    
    fun startDragging(index: Int) {
        isDragging = true
        draggedIndex = index
        dropTargetIndex = null
        dragOffset = 0f
    }
    
    fun updateDrag(offset: Float, targetIndex: Int?) {
        dragOffset = offset
        dropTargetIndex = targetIndex
    }
    
    fun endDragging(): Pair<Int?, Int?> {
        val result = draggedIndex to dropTargetIndex
        
        isDragging = false
        draggedIndex = null
        dropTargetIndex = null
        dragOffset = 0f
        
        return result
    }
    
    fun cancelDragging() {
        isDragging = false
        draggedIndex = null
        dropTargetIndex = null
        dragOffset = 0f
    }
}

/**
 * Modifier for drag handle visual feedback
 */
@Composable
fun Modifier.dragHandle(
    isDraggable: Boolean,
    onLongPress: () -> Unit = {}
): Modifier {
    return if (isDraggable) {
        this.pointerInput(Unit) {
            // TODO: Implement long press detection for drag handle
        }
    } else {
        this
    }
}
