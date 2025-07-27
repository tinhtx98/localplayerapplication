package com.tinhtx.localplayerapplication.presentation.components.music

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.queue.RepeatMode

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    hasPrevious: Boolean = true,
    hasNext: Boolean = true,
    modifier: Modifier = Modifier,
    size: PlayerControlsSize = PlayerControlsSize.Medium
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPrevious()
            },
            enabled = hasPrevious,
            modifier = Modifier.size(size.skipButtonSize)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Skip previous",
                modifier = Modifier.size(size.skipIconSize),
                tint = if (hasPrevious) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }

        // Play/Pause button
        FilledIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayPause()
            },
            modifier = Modifier.size(size.playButtonSize),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    scaleIn(animationSpec = tween(150)) + fadeIn() with 
                    scaleOut(animationSpec = tween(150)) + fadeOut()
                }
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    modifier = Modifier.size(size.playIconSize)
                )
            }
        }

        // Next button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            enabled = hasNext,
            modifier = Modifier.size(size.skipButtonSize)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip next",
                modifier = Modifier.size(size.skipIconSize),
                tint = if (hasNext) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

@Composable
fun ExtendedPlayerControls(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    hasPrevious: Boolean = true,
    hasNext: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleShuffle()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Toggle shuffle",
                tint = if (isShuffleEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(24.dp)
            )
        }

        // Previous button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPrevious()
            },
            enabled = hasPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Skip previous",
                modifier = Modifier.size(32.dp),
                tint = if (hasPrevious) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }

        // Play/Pause button
        FilledIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayPause()
            },
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    scaleIn(animationSpec = tween(200)) + fadeIn() with 
                    scaleOut(animationSpec = tween(200)) + fadeOut()
                }
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Next button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            enabled = hasNext,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip next",
                modifier = Modifier.size(32.dp),
                tint = if (hasNext) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }

        // Repeat button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleRepeat()
            },
            modifier = Modifier.size(48.dp)
        ) {
            val icon = when (repeatMode) {
                RepeatMode.OFF -> Icons.Default.Repeat
                RepeatMode.ALL -> Icons.Default.Repeat
                RepeatMode.ONE -> Icons.Default.RepeatOne
            }
            
            Icon(
                imageVector = icon,
                contentDescription = "Toggle repeat",
                tint = if (repeatMode != RepeatMode.OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CompactPlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPlayPause()
            },
            modifier = Modifier.size(40.dp)
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                }
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Next button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip next",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LoadingPlayerControls(
    modifier: Modifier = Modifier,
    size: PlayerControlsSize = PlayerControlsSize.Medium
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button skeleton
        Box(
            modifier = Modifier
                .size(size.skipButtonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        // Play button skeleton
        Box(
            modifier = Modifier
                .size(size.playButtonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        // Next button skeleton
        Box(
            modifier = Modifier
                .size(size.skipButtonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun FloatingPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val infiniteTransition = rememberInfiniteTransition()
    val haptic = LocalHapticFeedback.current
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Background circle animation when playing
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(rotation)
                        .background(
                            contentColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                )
            }
            
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                }
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

enum class PlayerControlsSize(
    val playButtonSize: androidx.compose.ui.unit.Dp,
    val skipButtonSize: androidx.compose.ui.unit.Dp,
    val playIconSize: androidx.compose.ui.unit.Dp,
    val skipIconSize: androidx.compose.ui.unit.Dp,
    val spacing: androidx.compose.ui.unit.Dp
) {
    Small(
        playButtonSize = 48.dp,
        skipButtonSize = 40.dp,
        playIconSize = 24.dp,
        skipIconSize = 20.dp,
        spacing = 8.dp
    ),
    Medium(
        playButtonSize = 64.dp,
        skipButtonSize = 48.dp,
        playIconSize = 32.dp,
        skipIconSize = 24.dp,
        spacing = 12.dp
    ),
    Large(
        playButtonSize = 80.dp,
        skipButtonSize = 56.dp,
        playIconSize = 40.dp,
        skipIconSize = 28.dp,
        spacing = 16.dp
    )
}
