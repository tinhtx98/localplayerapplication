package com.tinhtx.localplayerapplication.presentation.components.music

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.RepeatMode
import com.tinhtx.localplayerapplication.domain.model.ShuffleMode

@Composable
fun ShuffleButton(
    shuffleMode: ShuffleMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp
) {
    val isActive = shuffleMode.isEnabled
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
    ) {
        AnimatedContent(
            targetState = isActive,
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            },
            label = "shuffle_animation"
        ) { active ->
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = shuffleMode.displayName,
                modifier = Modifier.size(iconSize),
                tint = if (active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
fun RepeatButton(
    repeatMode: RepeatMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp
) {
    val isActive = repeatMode != RepeatMode.OFF
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
    ) {
        AnimatedContent(
            targetState = repeatMode,
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            },
            label = "repeat_animation"
        ) { mode ->
            Icon(
                imageVector = when (mode) {
                    RepeatMode.OFF -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    RepeatMode.ALL -> Icons.Default.Repeat
                },
                contentDescription = mode.displayName,
                modifier = Modifier.size(iconSize),
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
fun ShuffleRepeatRow(
    shuffleMode: ShuffleMode,
    repeatMode: RepeatMode,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShuffleButton(
            shuffleMode = shuffleMode,
            onClick = onShuffleClick
        )
        
        RepeatButton(
            repeatMode = repeatMode,
            onClick = onRepeatClick
        )
    }
}
