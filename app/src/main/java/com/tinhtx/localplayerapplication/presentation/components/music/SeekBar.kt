package com.tinhtx.localplayerapplication.presentation.components.music

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun MusicSeekBar(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    currentTime: String = "0:00",
    totalTime: String = "0:00",
    showTimeLabels: Boolean = true,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    thumbSize: androidx.compose.ui.unit.Dp = 20.dp,
    trackHeight: androidx.compose.ui.unit.Dp = 4.dp
) {
    Column(modifier = modifier) {
        // Seek bar
        CustomSeekBar(
            progress = progress,
            onProgressChanged = onProgressChanged,
            enabled = enabled,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            thumbColor = thumbColor,
            thumbSize = thumbSize,
            trackHeight = trackHeight,
            modifier = Modifier.fillMaxWidth()
        )

        // Time labels
        if (showTimeLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start
                )

                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun CustomSeekBar(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    thumbSize: androidx.compose.ui.unit.Dp = 20.dp,
    trackHeight: androidx.compose.ui.unit.Dp = 4.dp
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    var thumbPressed by remember { mutableStateOf(false) }

    // Animation for thumb scale when pressed
    val thumbScale by animateFloatAsState(
        targetValue = if (thumbPressed) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "thumb_scale"
    )

    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }

    Canvas(
        modifier = modifier
            .height(thumbSize + 8.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectTapGestures(
                    onPress = { offset ->
                        thumbPressed = true
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onProgressChanged(newProgress)
                        
                        tryAwaitRelease()
                        thumbPressed = false
                    }
                )
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        thumbPressed = true
                    },
                    onDragEnd = { 
                        isDragging = false
                        thumbPressed = false
                    }
                ) { _, _ ->
                    // Drag logic handled in onPress for better UX
                }
            }
    ) {
        val centerY = size.height / 2f
        val trackWidth = size.width - thumbSizePx
        val activeWidth = trackWidth * progress.coerceIn(0f, 1f)
        val thumbX = thumbSizePx / 2f + activeWidth

        // Draw inactive track
        drawLine(
            color = inactiveColor,
            start = Offset(thumbSizePx / 2f, centerY),
            end = Offset(size.width - thumbSizePx / 2f, centerY),
            strokeWidth = trackHeightPx,
            cap = StrokeCap.Round
        )

        // Draw active track
        if (activeWidth > 0f) {
            drawLine(
                color = activeColor,
                start = Offset(thumbSizePx / 2f, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )
        }

        // Draw thumb
        drawCircle(
            color = thumbColor,
            radius = (thumbSizePx / 2f) * thumbScale,
            center = Offset(thumbX, centerY)
        )

        // Draw thumb shadow/glow when pressed
        if (thumbPressed) {
            drawCircle(
                color = thumbColor.copy(alpha = 0.2f),
                radius = (thumbSizePx / 2f) * thumbScale * 1.5f,
                center = Offset(thumbX, centerY)
            )
        }
    }
}

@Composable
fun CompactSeekBar(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(CircleShape)
            .background(inactiveColor)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChanged(newProgress)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(activeColor, CircleShape)
        )
    }
}

@Composable
fun WaveformSeekBar(
    progress: Float,
    waveformData: List<Float>,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    barWidth: androidx.compose.ui.unit.Dp = 2.dp,
    maxHeight: androidx.compose.ui.unit.Dp = 40.dp
) {
    val density = LocalDensity.current
    val barWidthPx = with(density) { barWidth.toPx() }
    val maxHeightPx = with(density) { maxHeight.toPx() }

    Canvas(
        modifier = modifier
            .height(maxHeight)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChanged(newProgress)
                }
            }
    ) {
        val barCount = (size.width / (barWidthPx + 2.dp.toPx())).toInt()
        val progressBarIndex = (barCount * progress).toInt()

        repeat(barCount) { index ->
            val x = index * (barWidthPx + 2.dp.toPx()) + barWidthPx / 2f
            val dataIndex = (index.toFloat() / barCount * waveformData.size).toInt()
                .coerceIn(0, waveformData.size - 1)
            
            val barHeight = if (waveformData.isNotEmpty()) {
                waveformData[dataIndex] * maxHeightPx
            } else {
                maxHeightPx * 0.3f // Default height if no data
            }

            val color = if (index <= progressBarIndex) activeColor else inactiveColor

            drawLine(
                color = color,
                start = Offset(x, size.height / 2f - barHeight / 2f),
                end = Offset(x, size.height / 2f + barHeight / 2f),
                strokeWidth = barWidthPx,
                cap = StrokeCap.Round
            )
        }
    }
}
