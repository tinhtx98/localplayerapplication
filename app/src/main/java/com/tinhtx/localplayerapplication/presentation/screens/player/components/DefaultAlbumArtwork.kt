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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DefaultAlbumArtwork(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "album_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 10000 else 0,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )

    Box(
        modifier = modifier
            .rotate(rotation)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl record effect
        Box(
            modifier = Modifier
                .fillMaxSize(0.3f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Concentric circles for vinyl effect
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize(0.9f - (index * 0.15f))
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
                    .padding(2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun GradientAlbumArtwork(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = "Album",
                modifier = Modifier.size(48.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No Cover Art",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
