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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.RepeatMode
import com.tinhtx.localplayerapplication.domain.model.ShuffleMode

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    hasPrevious: Boolean = true,
    hasNext: Boolean = true,
    shuffleMode: ShuffleMode = ShuffleMode.OFF,
    repeatMode: RepeatMode = RepeatMode.OFF,
    onShuffleClick: (() -> Unit)? = null,
    onRepeatClick: (() -> Unit)? = null,
    controlsSize: PlayerControlsSize = PlayerControlsSize.LARGE
) {
    val (buttonSize, playButtonSize, iconSize, playIconSize) = when (controlsSize) {
        PlayerControlsSize.SMALL -> PlayerControlsSizes(40.dp, 56.dp, 20.dp, 28.dp)
        PlayerControlsSize.MEDIUM -> PlayerControlsSizes(48.dp, 64.dp, 24.dp, 32.dp)
        PlayerControlsSize.LARGE -> PlayerControlsSizes(56.dp, 72.dp, 28.dp, 36.dp)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle button
        if (onShuffleClick != null) {
            PlayerButton(
                icon = Icons.Default.Shuffle,
                onClick = onShuffleClick,
                size = buttonSize,
                iconSize = iconSize,
                isActive = shuffleMode.isEnabled,
                contentDescription = "Shuffle ${if (shuffleMode.isEnabled) "on" else "off"}"
            )
        }

        // Previous button
        PlayerButton(
            icon = Icons.Default.SkipPrevious,
            onClick = onPrevious,
            size = buttonSize,
            iconSize = iconSize,
            enabled = hasPrevious,
            contentDescription = "Previous song"
        )

        // Play/Pause button (larger)
        PlayerButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPlayPause,
            size = playButtonSize,
            iconSize = playIconSize,
            isPrimary = true,
            contentDescription = if (isPlaying) "Pause" else "Play"
        )

        // Next button
        PlayerButton(
            icon = Icons.Default.SkipNext,
            onClick = onNext,
            size = buttonSize,
            iconSize = iconSize,
            enabled = hasNext,
            contentDescription = "Next song"
        )

        // Repeat button
        if (onRepeatClick != null) {
            PlayerButton(
                icon = when (repeatMode) {
                    RepeatMode.OFF -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    RepeatMode.ALL -> Icons.Default.Repeat
                },
                onClick = onRepeatClick,
                size = buttonSize,
                iconSize = iconSize,
                isActive = repeatMode != RepeatMode.OFF,
                contentDescription = repeatMode.displayName
            )
        }
    }
}

@Composable
fun MiniPlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    hasNext: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPlayPause,
            size = 40.dp,
            iconSize = 20.dp,
            isPrimary = false,
            contentDescription = if (isPlaying) "Pause" else "Play"
        )

        PlayerButton(
            icon = Icons.Default.SkipNext,
            onClick = onNext,
            size = 40.dp,
            iconSize = 20.dp,
            enabled = hasNext,
            contentDescription = "Next song"
        )
    }
}

@Composable
private fun PlayerButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    isActive: Boolean = false,
    contentDescription: String? = null
) {
    val containerColor = when {
        isPrimary -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape)
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            },
            label = "icon_change"
        ) { targetIcon ->
            Icon(
                imageVector = targetIcon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
        }
    }
}

enum class PlayerControlsSize {
    SMALL, MEDIUM, LARGE
}

private data class PlayerControlsSizes(
    val buttonSize: androidx.compose.ui.unit.Dp,
    val playButtonSize: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp,
    val playIconSize: androidx.compose.ui.unit.Dp
)
