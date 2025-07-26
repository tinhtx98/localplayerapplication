package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(
                width = this.size.width - strokeWidthPx,
                height = this.size.height - strokeWidthPx
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                x = strokeWidthPx / 2,
                y = strokeWidthPx / 2
            )
        )
    }
}

@Composable
fun MusicLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String = "Loading...",
    size: androidx.compose.ui.unit.Dp = 60.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_loading")
    
    // Multiple rotating circles for vinyl effect
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )
    
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Outer vinyl ring
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation1)
            ) {
                val strokeWidth = 3.dp.toPx()
                drawCircle(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    radius = size.toPx() / 2 - strokeWidth,
                    style = Stroke(width = strokeWidth)
                )
            }
            
            // Inner vinyl ring
            Canvas(
                modifier = Modifier
                    .size(size * 0.7f)
                    .rotate(rotation2)
            ) {
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    radius = this.size.width / 2 - strokeWidth,
                    style = Stroke(width = strokeWidth)
                )
            }
            
            // Center dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WaveformLoadingIndicator(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val animationDelay = index * 100
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = animationDelay, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp * scale)
                    .background(
                        color = color,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun FullScreenLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading your music...",
    showBackground: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .let { mod ->
                if (showBackground) {
                    mod.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                } else {
                    mod
                }
            },
        contentAlignment = Alignment.Center
    ) {
        MusicLoadingIndicator(
            text = message,
            size = 80.dp
        )
    }
}
