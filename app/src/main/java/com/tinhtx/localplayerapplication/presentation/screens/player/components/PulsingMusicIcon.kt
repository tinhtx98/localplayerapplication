package com.tinhtx.localplayerapplication.presentation.screens.player.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PulsingMusicIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MusicNote,
    color: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = color.copy(alpha = alpha * 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Playing",
            tint = color.copy(alpha = alpha),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun WaveformPulsingIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    isPlaying: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            WaveformBar(
                index = index,
                color = color,
                isPlaying = isPlaying
            )
        }
    }
}

@Composable
private fun WaveformBar(
    index: Int,
    color: Color,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_animation")
    
    val height by infiniteTransition.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 16.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600 + (index * 100),
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "height_animation_$index"
    )

    Box(
        modifier = Modifier
            .width(3.dp)
            .height(if (isPlaying) height.dp else 4.dp)
            .background(
                color = color,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)
            )
    )
}
