package com.tinhtx.localplayerapplication.presentation.components.music

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun SeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bufferedProgress: Float = 0f,
    showBuffered: Boolean = false,
    colors: SeekBarColors = SeekBarDefaults.colors()
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    val currentProgress = if (isDragging) dragProgress else progress
    
    Slider(
        value = currentProgress,
        onValueChange = { newValue ->
            dragProgress = newValue
            if (!isDragging) {
                isDragging = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        },
        onValueChangeFinished = {
            onSeek(dragProgress)
            isDragging = false
        },
        modifier = modifier,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = colors.thumbColor,
            activeTrackColor = colors.activeTrackColor,
            inactiveTrackColor = colors.inactiveTrackColor,
            disabledThumbColor = colors.thumbColor.copy(alpha = 0.38f),
            disabledActiveTrackColor = colors.activeTrackColor.copy(alpha = 0.38f),
            disabledInactiveTrackColor = colors.inactiveTrackColor.copy(alpha = 0.38f)
        )
    )
}

@Composable
fun SeekBarWithTime(
    progress: Float,
    currentTime: Long,
    totalTime: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bufferedProgress: Float = 0f,
    showBuffered: Boolean = false
) {
    Column(
        modifier = modifier
    ) {
        SeekBar(
            progress = progress,
            onSeek = onSeek,
            enabled = enabled,
            bufferedProgress = bufferedProgress,
            showBuffered = showBuffered,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatTime(totalTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomSeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    bufferedProgress: Float = 0f,
    showBuffered: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 4.dp,
    thumbSize: androidx.compose.ui.unit.Dp = 20.dp,
    colors: SeekBarColors = SeekBarDefaults.colors()
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    val currentProgress = if (isDragging) dragProgress else progress
    
    BoxWithConstraints(
        modifier = modifier
            .height(maxOf(height, thumbSize))
            .pointerInput(enabled) {
                if (enabled) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            dragProgress = newProgress
                        },
                        onDragEnd = {
                            onSeek(dragProgress)
                            isDragging = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newProgress = dragProgress + (dragAmount / size.width)
                            dragProgress = newProgress.coerceIn(0f, 1f)
                        }
                    )
                }
            }
    ) {
        val trackWidth = maxWidth
        val trackHeight = height
        val progressWidth = trackWidth * currentProgress
        val bufferedWidth = if (showBuffered) trackWidth * bufferedProgress else 0.dp
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(colors.inactiveTrackColor)
            )
            
            // Buffered track
            if (showBuffered && bufferedWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .width(bufferedWidth)
                        .height(trackHeight)
                        .clip(RoundedCornerShape(trackHeight / 2))
                        .background(colors.bufferedTrackColor)
                )
            }
            
            // Progress track
            Box(
                modifier = Modifier
                    .width(progressWidth)
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(
                        if (colors.activeTrackColor is Color) {
                            colors.activeTrackColor
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    )
            )
            
            // Thumb
            Box(
                modifier = Modifier
                    .offset(x = progressWidth - thumbSize / 2)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(colors.thumbColor),
                contentAlignment = Alignment.Center
            ) {
                // Inner circle for visual feedback when dragging
                AnimatedVisibility(
                    visible = isDragging,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(thumbSize * 0.6f)
                            .background(
                                colors.thumbColor.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformSeekBar(
    progress: Float,
    waveformData: List<Float>,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SeekBarColors = SeekBarDefaults.colors()
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    val currentProgress = if (isDragging) dragProgress else progress
    
    BoxWithConstraints(
        modifier = modifier
            .height(60.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            dragProgress = newProgress
                        },
                        onDragEnd = {
                            onSeek(dragProgress)
                            isDragging = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newProgress = dragProgress + (dragAmount / size.width)
                            dragProgress = newProgress.coerceIn(0f, 1f)
                        }
                    )
                }
            }
    ) {
        val barWidth = maxWidth / waveformData.size
        val progressIndex = (currentProgress * waveformData.size).toInt()
        
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            waveformData.forEachIndexed { index, amplitude ->
                val isPlayed = index <= progressIndex
                val barHeight = (amplitude * 50.dp).coerceAtLeast(2.dp)
                
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight)
                        .background(
                            if (isPlayed) colors.activeTrackColor else colors.inactiveTrackColor,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun CircularSeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    strokeWidth: androidx.compose.ui.unit.Dp = 8.dp,
    colors: SeekBarColors = SeekBarDefaults.colors()
) {
    // TODO: Implement circular seek bar
    // This would require custom drawing using Canvas
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MinimalSeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    )
}

// Colors and defaults
data class SeekBarColors(
    val thumbColor: Color,
    val activeTrackColor: Color,
    val inactiveTrackColor: Color,
    val bufferedTrackColor: Color = inactiveTrackColor.copy(alpha = 0.5f)
)

object SeekBarDefaults {
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        activeTrackColor: Color = MaterialTheme.colorScheme.primary,
        inactiveTrackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        bufferedTrackColor: Color = inactiveTrackColor.copy(alpha = 0.5f)
    ) = SeekBarColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor,
        bufferedTrackColor = bufferedTrackColor
    )
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
