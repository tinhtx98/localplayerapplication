package com.tinhtx.localplayerapplication.presentation.components.audio

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    audioData: List<Float> = emptyList(),
    modifier: Modifier = Modifier,
    style: VisualizerStyle = VisualizerStyle.Bars,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.Transparent,
    barCount: Int = 32,
    animationDuration: Int = 300
) {
    when (style) {
        VisualizerStyle.Bars -> BarVisualizer(
            isPlaying = isPlaying,
            audioData = audioData,
            modifier = modifier,
            color = color,
            backgroundColor = backgroundColor,
            barCount = barCount,
            animationDuration = animationDuration
        )
        VisualizerStyle.Circle -> CircleVisualizer(
            isPlaying = isPlaying,
            audioData = audioData,
            modifier = modifier,
            color = color,
            backgroundColor = backgroundColor,
            barCount = barCount,
            animationDuration = animationDuration
        )
        VisualizerStyle.Wave -> WaveVisualizer(
            isPlaying = isPlaying,
            audioData = audioData,
            modifier = modifier,
            color = color,
            backgroundColor = backgroundColor,
            animationDuration = animationDuration
        )
        VisualizerStyle.Spectrum -> SpectrumVisualizer(
            isPlaying = isPlaying,
            audioData = audioData,
            modifier = modifier,
            color = color,
            backgroundColor = backgroundColor,
            barCount = barCount,
            animationDuration = animationDuration
        )
    }
}

@Composable
private fun BarVisualizer(
    isPlaying: Boolean,
    audioData: List<Float>,
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    barCount: Int,
    animationDuration: Int
) {
    val density = LocalDensity.current
    val bars = remember { mutableStateListOf<Float>() }
    val infiniteTransition = rememberInfiniteTransition()
    
    // Initialize bars
    LaunchedEffect(barCount) {
        bars.clear()
        repeat(barCount) { bars.add(0.1f) }
    }
    
    // Animation for bars
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                if (audioData.isNotEmpty()) {
                    // Use real audio data
                    val stepSize = audioData.size / barCount
                    repeat(barCount) { index ->
                        val dataIndex = (index * stepSize).coerceAtMost(audioData.size - 1)
                        bars[index] = audioData[dataIndex].coerceIn(0.1f, 1f)
                    }
                } else {
                    // Generate random animation
                    repeat(barCount) { index ->
                        bars[index] = Random.nextFloat().coerceIn(0.1f, 1f)
                    }
                }
                kotlinx.coroutines.delay(animationDuration.toLong())
            }
        } else {
            // Animate to zero when not playing
            repeat(barCount) { index ->
                bars[index] = 0.1f
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        val barWidth = size.width / barCount
        val maxBarHeight = size.height * 0.8f
        
        bars.forEachIndexed { index, amplitude ->
            val barHeight = maxBarHeight * amplitude
            val x = index * barWidth + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barActualWidth, barHeight),
                cornerRadius = CornerRadius(barActualWidth / 2, barActualWidth / 2)
            )
        }
    }
}

@Composable
private fun CircleVisualizer(
    isPlaying: Boolean,
    audioData: List<Float>,
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    barCount: Int,
    animationDuration: Int
) {
    val bars = remember { mutableStateListOf<Float>() }
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Initialize bars
    LaunchedEffect(barCount) {
        bars.clear()
        repeat(barCount) { bars.add(0.2f) }
    }
    
    // Animation for bars
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                if (audioData.isNotEmpty()) {
                    val stepSize = audioData.size / barCount
                    repeat(barCount) { index ->
                        val dataIndex = (index * stepSize).coerceAtMost(audioData.size - 1)
                        bars[index] = audioData[dataIndex].coerceIn(0.2f, 1f)
                    }
                } else {
                    repeat(barCount) { index ->
                        bars[index] = Random.nextFloat().coerceIn(0.2f, 1f)
                    }
                }
                kotlinx.coroutines.delay(animationDuration.toLong())
            }
        } else {
            repeat(barCount) { index ->
                bars[index] = 0.2f
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.3f
        val maxBarLength = size.minDimension / 2 * 0.4f
        
        bars.forEachIndexed { index, amplitude ->
            val angle = (360f / barCount * index + rotation) * PI / 180f
            val barLength = maxBarLength * amplitude
            
            val startX = center.x + cos(angle).toFloat() * radius
            val startY = center.y + sin(angle).toFloat() * radius
            val endX = center.x + cos(angle).toFloat() * (radius + barLength)
            val endY = center.y + sin(angle).toFloat() * (radius + barLength)
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Center circle
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 0.8f,
            center = center
        )
    }
}

@Composable
private fun WaveVisualizer(
    isPlaying: Boolean,
    audioData: List<Float>,
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    animationDuration: Int
) {
    val waveData = remember { mutableStateListOf<Float>() }
    val pointCount = 100
    
    // Initialize wave data
    LaunchedEffect(Unit) {
        waveData.clear()
        repeat(pointCount) { waveData.add(0f) }
    }
    
    // Animation for wave
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                if (audioData.isNotEmpty()) {
                    val stepSize = audioData.size / pointCount
                    repeat(pointCount) { index ->
                        val dataIndex = (index * stepSize).coerceAtMost(audioData.size - 1)
                        waveData[index] = audioData[dataIndex] * 0.5f
                    }
                } else {
                    repeat(pointCount) { index ->
                        val t = index.toFloat() / pointCount * 2 * PI
                        waveData[index] = sin(t + System.currentTimeMillis() * 0.005f).toFloat() * 0.3f
                    }
                }
                kotlinx.coroutines.delay(animationDuration.toLong())
            }
        } else {
            repeat(pointCount) { index ->
                waveData[index] = 0f
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        val path = Path()
        val stepX = size.width / (pointCount - 1)
        val centerY = size.height / 2
        val maxAmplitude = size.height * 0.4f
        
        // Create wave path
        waveData.forEachIndexed { index, amplitude ->
            val x = index * stepX
            val y = centerY + amplitude * maxAmplitude
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw wave
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw center line
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun SpectrumVisualizer(
    isPlaying: Boolean,
    audioData: List<Float>,
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    barCount: Int,
    animationDuration: Int
) {
    val bars = remember { mutableStateListOf<Float>() }
    
    // Initialize bars
    LaunchedEffect(barCount) {
        bars.clear()
        repeat(barCount) { bars.add(0.05f) }
    }
    
    // Animation for bars
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                if (audioData.isNotEmpty()) {
                    val stepSize = audioData.size / barCount
                    repeat(barCount) { index ->
                        val dataIndex = (index * stepSize).coerceAtMost(audioData.size - 1)
                        bars[index] = audioData[dataIndex].coerceIn(0.05f, 1f)
                    }
                } else {
                    repeat(barCount) { index ->
                        // Simulate frequency spectrum (higher frequencies have lower amplitude)
                        val baseAmplitude = Random.nextFloat() * (1f - index.toFloat() / barCount)
                        bars[index] = baseAmplitude.coerceIn(0.05f, 1f)
                    }
                }
                kotlinx.coroutines.delay(animationDuration.toLong())
            }
        } else {
            repeat(barCount) { index ->
                bars[index] = 0.05f
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        val barWidth = size.width / barCount
        val maxBarHeight = size.height * 0.9f
        
        bars.forEachIndexed { index, amplitude ->
            val barHeight = maxBarHeight * amplitude
            val x = index * barWidth + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f
            
            // Create gradient for spectrum effect
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    color,
                    color.copy(alpha = 0.6f),
                    color.copy(alpha = 0.3f)
                ),
                startY = size.height - barHeight,
                endY = size.height
            )
            
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barActualWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun MiniAudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 5
) {
    val bars = remember { mutableStateListOf<Float>() }
    
    LaunchedEffect(barCount) {
        bars.clear()
        repeat(barCount) { bars.add(0.1f) }
    }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                repeat(barCount) { index ->
                    bars[index] = Random.nextFloat().coerceIn(0.1f, 1f)
                }
                kotlinx.coroutines.delay(200)
            }
        } else {
            repeat(barCount) { index ->
                bars[index] = 0.1f
            }
        }
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { index, amplitude ->
            val animatedHeight by animateFloatAsState(
                targetValue = amplitude,
                animationSpec = tween(200)
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((20.dp * animatedHeight).coerceAtLeast(2.dp))
                    .background(color, RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun EqualizerVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 3
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            
            val height by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600 + index * 150),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                )
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (isPlaying) (16.dp * height) else 4.dp)
                    .background(
                        if (isPlaying) color else color.copy(alpha = 0.3f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

enum class VisualizerStyle {
    Bars,
    Circle,
    Wave,
    Spectrum
}

// Extension functions for audio data processing
fun List<Float>.normalize(): List<Float> {
    val maxValue = maxOrNull() ?: 1f
    return if (maxValue > 0f) {
        map { it / maxValue }
    } else {
        this
    }
}

fun List<Float>.smooth(windowSize: Int = 3): List<Float> {
    if (size <= windowSize) return this
    
    return mapIndexed { index, _ ->
        val start = maxOf(0, index - windowSize / 2)
        val end = minOf(size, index + windowSize / 2 + 1)
        subList(start, end).average().toFloat()
    }
}

fun List<Float>.downsample(targetSize: Int): List<Float> {
    if (size <= targetSize) return this
    
    val stepSize = size.toFloat() / targetSize
    return (0 until targetSize).map { index ->
        val sourceIndex = (index * stepSize).toInt().coerceAtMost(size - 1)
        this[sourceIndex]
    }
}
