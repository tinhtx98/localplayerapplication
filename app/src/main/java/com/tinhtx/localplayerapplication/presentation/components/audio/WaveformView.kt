package com.tinhtx.localplayerapplication.presentation.components.audio

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun WaveformView(
    waveformData: List<Float>,
    progress: Float = 0f,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    style: WaveformStyle = WaveformStyle.Bars,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    unplayedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    backgroundColor: Color = Color.Transparent,
    strokeWidth: Dp = 2.dp,
    barWidth: Dp = 2.dp,
    barSpacing: Dp = 1.dp,
    isInteractive: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    val currentProgress = if (isDragging) dragProgress else progress
    
    when (style) {
        WaveformStyle.Bars -> WaveformBars(
            waveformData = waveformData,
            progress = currentProgress,
            playedColor = playedColor,
            unplayedColor = unplayedColor,
            backgroundColor = backgroundColor,
            barWidth = barWidth,
            barSpacing = barSpacing,
            isInteractive = isInteractive,
            onSeek = onSeek,
            onDragStart = { isDragging = true },
            onDragEnd = { isDragging = false },
            onDragProgress = { dragProgress = it },
            modifier = modifier
        )
        WaveformStyle.Line -> WaveformLine(
            waveformData = waveformData,
            progress = currentProgress,
            playedColor = playedColor,
            unplayedColor = unplayedColor,
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth,
            isInteractive = isInteractive,
            onSeek = onSeek,
            onDragStart = { isDragging = true },
            onDragEnd = { isDragging = false },
            onDragProgress = { dragProgress = it },
            modifier = modifier
        )
        WaveformStyle.Filled -> WaveformFilled(
            waveformData = waveformData,
            progress = currentProgress,
            playedColor = playedColor,
            unplayedColor = unplayedColor,
            backgroundColor = backgroundColor,
            isInteractive = isInteractive,
            onSeek = onSeek,
            onDragStart = { isDragging = true },
            onDragEnd = { isDragging = false },
            onDragProgress = { dragProgress = it },
            modifier = modifier
        )
    }
}

@Composable
private fun WaveformBars(
    waveformData: List<Float>,
    progress: Float,
    playedColor: Color,
    unplayedColor: Color,
    backgroundColor: Color,
    barWidth: Dp,
    barSpacing: Dp,
    isInteractive: Boolean,
    onSeek: ((Float) -> Unit)?,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragProgress: (Float) -> Unit,
    modifier: Modifier
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isInteractive && onSeek != null) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                onDragStart()
                                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                onDragProgress(newProgress)
                            },
                            onDragEnd = {
                                onSeek(progress)
                                onDragEnd()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newProgress = progress + (dragAmount / size.width)
                                onDragProgress(newProgress.coerceIn(0f, 1f))
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val totalBarWidth = with(density) { barWidth.toPx() }
        val totalSpacing = with(density) { barSpacing.toPx() }
        val availableWidth = size.width
        val maxBars = (availableWidth / (totalBarWidth + totalSpacing)).toInt()
        
        val displayData = if (waveformData.size > maxBars) {
            waveformData.downsample(maxBars)
        } else {
            waveformData
        }
        
        val actualBarWidth = availableWidth / displayData.size - totalSpacing
        val maxBarHeight = size.height * 0.8f
        val progressIndex = (progress * displayData.size).toInt()
        
        displayData.forEachIndexed { index, amplitude ->
            val x = index * (actualBarWidth + totalSpacing)
            val barHeight = (maxBarHeight * amplitude.absoluteValue).coerceAtLeast(2f)
            val y = (size.height - barHeight) / 2
            
            val color = if (index <= progressIndex) playedColor else unplayedColor
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2, actualBarWidth / 2)
            )
        }
        
        // Progress indicator line
        if (progress > 0f) {
            val progressX = size.width * progress
            drawLine(
                color = playedColor,
                start = Offset(progressX, 0f),
                end = Offset(progressX, size.height),
                strokeWidth = 2.dp.toPx(),
                alpha = 0.8f
            )
        }
    }
}

@Composable
private fun WaveformLine(
    waveformData: List<Float>,
    progress: Float,
    playedColor: Color,
    unplayedColor: Color,
    backgroundColor: Color,
    strokeWidth: Dp,
    isInteractive: Boolean,
    onSeek: ((Float) -> Unit)?,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragProgress: (Float) -> Unit,
    modifier: Modifier
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isInteractive && onSeek != null) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                onDragStart()
                                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                onDragProgress(newProgress)
                            },
                            onDragEnd = {
                                onSeek(progress)
                                onDragEnd()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newProgress = progress + (dragAmount / size.width)
                                onDragProgress(newProgress.coerceIn(0f, 1f))
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val centerY = size.height / 2
        val maxAmplitude = size.height * 0.4f
        val stepX = size.width / (waveformData.size - 1)
        val progressX = size.width * progress
        val stroke = with(density) { strokeWidth.toPx() }
        
        // Draw unplayed waveform
        val unplayedPath = Path()
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * stepX
            val y = centerY + amplitude * maxAmplitude
            
            if (index == 0) {
                unplayedPath.moveTo(x, y)
            } else {
                unplayedPath.lineTo(x, y)
            }
        }
        
        drawPath(
            path = unplayedPath,
            color = unplayedColor,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        
        // Draw played waveform
        if (progress > 0f) {
            val playedPath = Path()
            val progressIndex = (progress * waveformData.size).toInt()
            
            waveformData.take(progressIndex + 1).forEachIndexed { index, amplitude ->
                val x = index * stepX
                val y = centerY + amplitude * maxAmplitude
                
                if (index == 0) {
                    playedPath.moveTo(x, y)
                } else {
                    playedPath.lineTo(x, y)
                }
            }
            
            drawPath(
                path = playedPath,
                color = playedColor,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        
        // Progress indicator
        drawLine(
            color = playedColor,
            start = Offset(progressX, 0f),
            end = Offset(progressX, size.height),
            strokeWidth = 2.dp.toPx(),
            alpha = 0.6f
        )
        
        // Center line
        drawLine(
            color = unplayedColor.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun WaveformFilled(
    waveformData: List<Float>,
    progress: Float,
    playedColor: Color,
    unplayedColor: Color,
    backgroundColor: Color,
    isInteractive: Boolean,
    onSeek: ((Float) -> Unit)?,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragProgress: (Float) -> Unit,
    modifier: Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isInteractive && onSeek != null) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                onDragStart()
                                val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                onDragProgress(newProgress)
                            },
                            onDragEnd = {
                                onSeek(progress)
                                onDragEnd()
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newProgress = progress + (dragAmount / size.width)
                                onDragProgress(newProgress.coerceIn(0f, 1f))
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val centerY = size.height / 2
        val maxAmplitude = size.height * 0.4f
        val stepX = size.width / (waveformData.size - 1)
        val progressX = size.width * progress
        
        // Create filled paths
        val unplayedTopPath = Path()
        val unplayedBottomPath = Path()
        val playedTopPath = Path()
        val playedBottomPath = Path()
        
        // Build unplayed paths
        unplayedTopPath.moveTo(0f, centerY)
        unplayedBottomPath.moveTo(0f, centerY)
        
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * stepX
            val topY = centerY - amplitude.absoluteValue * maxAmplitude
            val bottomY = centerY + amplitude.absoluteValue * maxAmplitude
            
            unplayedTopPath.lineTo(x, topY)
            unplayedBottomPath.lineTo(x, bottomY)
        }
        
        unplayedTopPath.lineTo(size.width, centerY)
        unplayedBottomPath.lineTo(size.width, centerY)
        unplayedTopPath.close()
        unplayedBottomPath.close()
        
        // Draw unplayed waveform
        drawPath(
            path = unplayedTopPath,
            color = unplayedColor.copy(alpha = 0.3f)
        )
        drawPath(
            path = unplayedBottomPath,
            color = unplayedColor.copy(alpha = 0.3f)
        )
        
        // Build and draw played paths
        if (progress > 0f) {
            val progressIndex = (progress * waveformData.size).toInt()
            val playedData = waveformData.take(progressIndex + 1)
            
            playedTopPath.moveTo(0f, centerY)
            playedBottomPath.moveTo(0f, centerY)
            
            playedData.forEachIndexed { index, amplitude ->
                val x = index * stepX
                val topY = centerY - amplitude.absoluteValue * maxAmplitude
                val bottomY = centerY + amplitude.absoluteValue * maxAmplitude
                
                playedTopPath.lineTo(x, topY)
                playedBottomPath.lineTo(x, bottomY)
            }
            
            playedTopPath.lineTo(progressX, centerY)
            playedBottomPath.lineTo(progressX, centerY)
            playedTopPath.close()
            playedBottomPath.close()
            
            // Create gradient
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    playedColor.copy(alpha = 0.8f),
                    playedColor.copy(alpha = 0.3f)
                )
            )
            
            drawPath(path = playedTopPath, brush = gradient)
            drawPath(path = playedBottomPath, brush = gradient)
        }
        
        // Progress indicator
        drawLine(
            color = playedColor,
            start = Offset(progressX, 0f),
            end = Offset(progressX, size.height),
            strokeWidth = 3.dp.toPx(),
            alpha = 0.8f
        )
    }
}

@Composable
fun SimpleWaveform(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.Transparent
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(backgroundColor)
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val centerY = size.height / 2
        val maxAmplitude = size.height * 0.4f
        val stepX = size.width / (waveformData.size - 1)
        
        val path = Path()
        
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * stepX
            val y = centerY + amplitude * maxAmplitude
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MiniWaveform(
    waveformData: List<Float>,
    progress: Float = 0f,
    modifier: Modifier = Modifier,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    unplayedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val centerY = size.height / 2
        val maxAmplitude = size.height * 0.3f
        val barWidth = size.width / waveformData.size
        val progressIndex = (progress * waveformData.size).toInt()
        
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * barWidth
            val barHeight = amplitude.absoluteValue * maxAmplitude
            val color = if (index <= progressIndex) playedColor else unplayedColor
            
            drawRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }
    }
}

enum class WaveformStyle {
    Bars,
    Line,
    Filled
}

// Utility functions for waveform processing
object WaveformProcessor {
    
    fun generateSampleWaveform(size: Int, seed: Long = System.currentTimeMillis()): List<Float> {
        val random = kotlin.random.Random(seed)
        return (0 until size).map {
            val t = it.toFloat() / size
            sin(t * PI * 8).toFloat() * random.nextFloat() * 0.8f
        }
    }
    
    fun smoothWaveform( List<Float>, windowSize: Int = 3): List<Float> {
        if (data.size <= windowSize) return data
        
        return data.mapIndexed { index, _ ->
            val start = maxOf(0, index - windowSize / 2)
            val end = minOf(data.size, index + windowSize / 2 + 1)
            data.subList(start, end).average().toFloat()
        }
    }
    
    fun normalizeWaveform( List<Float>): List<Float> {
        val maxValue = data.maxByOrNull { it.absoluteValue }?.absoluteValue ?: 1f
        return if (maxValue > 0f) {
            data.map { it / maxValue }
        } else {
            data
        }
    }
    
    fun resampleWaveform( List<Float>, targetSize: Int): List<Float> {
        if (data.size == targetSize) return data
        if (data.isEmpty()) return emptyList()
        
        val ratio = data.size.toDouble() / targetSize
        return (0 until targetSize).map { index ->
            val sourceIndex = (index * ratio).toInt().coerceAtMost(data.size - 1)
            data[sourceIndex]
        }
    }
    
    fun extractPeaks( List<Float>, threshold: Float = 0.5f): List<Int> {
        val peaks = mutableListOf<Int>()
        
        for (i in 1 until data.size - 1) {
            val current = data[i].absoluteValue
            val prev = data[i - 1].absoluteValue
            val next = data[i + 1].absoluteValue
            
            if (current > threshold && current > prev && current > next) {
                peaks.add(i)
            }
        }
        
        return peaks
    }
}
