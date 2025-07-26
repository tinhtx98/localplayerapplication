package com.tinhtx.localplayerapplication.presentation.components.audio

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    barWidth: Dp = 3.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 4.dp,
    spacing: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 150,
    style: VisualizerStyle = VisualizerStyle.BARS
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    val animatedBars = remember { mutableStateListOf<Float>() }
    
    // Initialize bars
    LaunchedEffect(barCount) {
        animatedBars.clear()
        repeat(barCount) {
            animatedBars.add(Random.nextFloat())
        }
    }
    
    // Animate bars when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                animatedBars.forEachIndexed { index, _ ->
                    animatedBars[index] = Random.nextFloat()
                }
                kotlinx.coroutines.delay(animationDuration.toLong())
            }
        }
    }
    
    when (style) {
        VisualizerStyle.BARS -> {
            BarVisualizer(
                isPlaying = isPlaying,
                barHeights = animatedBars,
                barWidth = barWidth,
                maxBarHeight = maxBarHeight,
                minBarHeight = minBarHeight,
                spacing = spacing,
                color = color,
                modifier = modifier
            )
        }
        VisualizerStyle.CIRCULAR -> {
            CircularVisualizer(
                isPlaying = isPlaying,
                barHeights = animatedBars,
                color = color,
                modifier = modifier
            )
        }
        VisualizerStyle.WAVEFORM -> {
            WaveformVisualizer(
                isPlaying = isPlaying,
                waveData = animatedBars,
                color = color,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BarVisualizer(
    isPlaying: Boolean,
    barHeights: List<Float>,
    barWidth: Dp,
    maxBarHeight: Dp,
    minBarHeight: Dp,
    spacing: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barWidthPx = barWidth.toPx()
        val spacingPx = spacing.toPx()
        val maxHeightPx = maxBarHeight.toPx()
        val minHeightPx = minBarHeight.toPx()
        val totalWidth = size.width
        
        val availableWidth = totalWidth - (barHeights.size - 1) * spacingPx
        val actualBarWidth = minOf(barWidthPx, availableWidth / barHeights.size)
        
        barHeights.forEachIndexed { index, heightRatio ->
            val barHeight = if (isPlaying) {
                minHeightPx + (maxHeightPx - minHeightPx) * heightRatio
            } else {
                minHeightPx
            }
            
            val x = index * (actualBarWidth + spacingPx)
            val y = (size.height - barHeight) / 2f
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2f)
            )
        }
    }
}

@Composable
private fun CircularVisualizer(
    isPlaying: Boolean,
    barHeights: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.8f
        val barCount = barHeights.size
        val angleStep = 2f * PI / barCount
        
        barHeights.forEachIndexed { index, heightRatio ->
            val angle = index * angleStep
            val barLength = if (isPlaying) {
                radius * 0.2f + radius * 0.3f * heightRatio
            } else {
                radius * 0.1f
            }
            
            val startX = center.x + cos(angle).toFloat() * (radius - barLength)
            val startY = center.y + sin(angle).toFloat() * (radius - barLength)
            val endX = center.x + cos(angle).toFloat() * radius
            val endY = center.y + sin(angle).toFloat() * radius
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun WaveformVisualizer(
    isPlaying: Boolean,
    waveData: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (waveData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val amplitude = height * 0.4f
        
        val path = androidx.compose.ui.graphics.Path()
        
        waveData.forEachIndexed { index, value ->
            val x = (index.toFloat() / (waveData.size - 1)) * width
            val y = if (isPlaying) {
                centerY + sin(value * 4 * PI).toFloat() * amplitude * value
            } else {
                centerY
            }
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    color.copy(alpha = 0.8f),
                    color,
                    color.copy(alpha = 0.8f)
                )
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

enum class VisualizerStyle {
    BARS,
    CIRCULAR,
    WAVEFORM
}
