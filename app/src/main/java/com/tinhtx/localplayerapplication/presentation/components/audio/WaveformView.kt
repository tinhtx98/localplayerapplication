package com.tinhtx.localplayerapplication.presentation.components.audio

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun InteractiveWaveformView(
    waveformData: List<Float>,
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    barWidth: Dp = 2.dp,
    barSpacing: Dp = 1.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    enabled: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Canvas(
        modifier = modifier
            .height(height)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val seekPosition = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(seekPosition)
                        
                        tryAwaitRelease()
                        isDragging = false
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        
        val totalBars = ((canvasWidth + barSpacingPx) / (barWidthPx + barSpacingPx)).toInt()
        val progressBarIndex = (totalBars * progress).toInt()
        
        repeat(totalBars) { index ->
            val x = index * (barWidthPx + barSpacingPx)
            val dataIndex = (index.toFloat() / totalBars * waveformData.size).toInt()
                .coerceIn(0, waveformData.size - 1)
            
            val barHeight = if (waveformData.isNotEmpty()) {
                waveformData[dataIndex] * canvasHeight * 0.8f
            } else {
                canvasHeight * 0.3f
            }
            
            val color = if (index <= progressBarIndex) activeColor else inactiveColor
            val y = (canvasHeight - barHeight) / 2f
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2f)
            )
        }
        
        // Draw progress indicator
        if (isDragging) {
            val progressX = canvasWidth * progress
            drawLine(
                color = activeColor,
                start = Offset(progressX, 0f),
                end = Offset(progressX, canvasHeight),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StaticWaveformView(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    style: WaveformStyle = WaveformStyle.BARS
) {
    Canvas(
        modifier = modifier.height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        when (style) {
            WaveformStyle.BARS -> {
                drawBarsWaveform(
                    waveformData = waveformData,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    color = color
                )
            }
            WaveformStyle.LINE -> {
                drawLineWaveform(
                    waveformData = waveformData,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    color = color
                )
            }
            WaveformStyle.FILLED -> {
                drawFilledWaveform(
                    waveformData = waveformData,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    color = color
                )
            }
        }
    }
}

private fun DrawScope.drawBarsWaveform(
    waveformData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
    color: Color
) {
    val barWidth = 2.dp.toPx()
    val spacing = 1.dp.toPx()
    val totalBars = ((canvasWidth + spacing) / (barWidth + spacing)).toInt()
    
    repeat(totalBars) { index ->
        val x = index * (barWidth + spacing)
        val dataIndex = (index.toFloat() / totalBars * waveformData.size).toInt()
            .coerceIn(0, waveformData.size - 1)
        
        val barHeight = if (waveformData.isNotEmpty()) {
            waveformData[dataIndex] * canvasHeight * 0.8f
        } else {
            canvasHeight * 0.3f
        }
        
        val y = (canvasHeight - barHeight) / 2f
        
        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f)
        )
    }
}

private fun DrawScope.drawLineWaveform(
    waveformData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
    color: Color
) {
    if (waveformData.isEmpty()) return
    
    val path = androidx.compose.ui.graphics.Path()
    val centerY = canvasHeight / 2f
    val amplitude = canvasHeight * 0.4f
    
    waveformData.forEachIndexed { index, value ->
        val x = (index.toFloat() / (waveformData.size - 1)) * canvasWidth
        val y = centerY + (value - 0.5f) * 2f * amplitude
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawFilledWaveform(
    waveformData: List<Float>,
    canvasWidth: Float,
    canvasHeight: Float,
    color: Color
) {
    if (waveformData.isEmpty()) return
    
    val path = androidx.compose.ui.graphics.Path()
    val centerY = canvasHeight / 2f
    val amplitude = canvasHeight * 0.4f
    
    // Start from bottom left
    path.moveTo(0f, centerY)
    
    // Draw the waveform
    waveformData.forEachIndexed { index, value ->
        val x = (index.toFloat() / (waveformData.size - 1)) * canvasWidth
        val y = centerY + (value - 0.5f) * 2f * amplitude
        path.lineTo(x, y)
    }
    
    // Close the path
    path.lineTo(canvasWidth, centerY)
    path.close()
    
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.6f),
                color.copy(alpha = 0.2f)
            )
        )
    )
    
    // Draw the outline
    val outlinePath = androidx.compose.ui.graphics.Path()
    outlinePath.moveTo(0f, centerY)
    
    waveformData.forEachIndexed { index, value ->
        val x = (index.toFloat() / (waveformData.size - 1)) * canvasWidth
        val y = centerY + (value - 0.5f) * 2f * amplitude
        outlinePath.lineTo(x, y)
    }
    
    drawPath(
        path = outlinePath,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

enum class WaveformStyle {
    BARS,
    LINE,
    FILLED
}
