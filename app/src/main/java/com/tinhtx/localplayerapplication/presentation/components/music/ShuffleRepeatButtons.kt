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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.queue.RepeatMode

@Composable
fun ShuffleRepeatButtons(
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    size: ShuffleRepeatSize = ShuffleRepeatSize.Medium
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShuffleButton(
            isEnabled = isShuffleEnabled,
            onClick = onToggleShuffle,
            size = size
        )
        
        RepeatButton(
            repeatMode = repeatMode,
            onClick = onToggleRepeat,
            size = size
        )
    }
}

@Composable
fun ShuffleButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ShuffleRepeatSize = ShuffleRepeatSize.Medium
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background circle when enabled
        AnimatedVisibility(
            visible = isEnabled,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(size.backgroundSize)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    )
            )
        }
        
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.size(size.buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = if (isEnabled) "Disable shuffle" else "Enable shuffle",
                modifier = Modifier
                    .size(size.iconSize)
                    .then(
                        if (isEnabled) Modifier.rotate(rotation * 0.1f) else Modifier
                    ),
                tint = if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
        
        // Active indicator
        if (isEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun RepeatButton(
    repeatMode: RepeatMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ShuffleRepeatSize = ShuffleRepeatSize.Medium
) {
    val haptic = LocalHapticFeedback.current
    val isEnabled = repeatMode != RepeatMode.OFF
    
    val icon = when (repeatMode) {
        RepeatMode.OFF -> Icons.Default.Repeat
        RepeatMode.ALL -> Icons.Default.Repeat
        RepeatMode.ONE -> Icons.Default.RepeatOne
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background circle when enabled
        AnimatedVisibility(
            visible = isEnabled,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(size.backgroundSize)
                    .background(
                        when (repeatMode) {
                            RepeatMode.ALL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            RepeatMode.ONE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            else -> Color.Transparent
                        },
                        CircleShape
                    )
            )
        }
        
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.size(size.buttonSize)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Repeat: ${repeatMode.displayName}",
                modifier = Modifier.size(size.iconSize),
                tint = when (repeatMode) {
                    RepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    RepeatMode.ALL -> MaterialTheme.colorScheme.primary
                    RepeatMode.ONE -> MaterialTheme.colorScheme.secondary
                }
            )
        }
        
        // Mode indicator
        when (repeatMode) {
            RepeatMode.ALL -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            RepeatMode.ONE -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ShuffleRepeatCompact(
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Compact shuffle
        TextButton(
            onClick = onToggleShuffle,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isShuffleEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Shuffle",
                style = MaterialTheme.typography.labelSmall
            )
        }
        
        // Compact repeat
        TextButton(
            onClick = onToggleRepeat,
            colors = ButtonDefaults.textButtonColors(
                contentColor = when (repeatMode) {
                    RepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    RepeatMode.ALL -> MaterialTheme.colorScheme.primary
                    RepeatMode.ONE -> MaterialTheme.colorScheme.secondary
                }
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.OFF, RepeatMode.ALL -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = repeatMode.displayName,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AnimatedShuffleButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEnabled) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isEnabled) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Shuffle,
            contentDescription = if (isEnabled) "Disable shuffle" else "Enable shuffle",
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
                .rotate(rotation),
            tint = if (isEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
fun ToggleButtonGroup(
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Shuffle toggle
            FilterChip(
                selected = isShuffleEnabled,
                onClick = onToggleShuffle,
                label = { Text("Shuffle") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            // Repeat toggle
            FilterChip(
                selected = repeatMode != RepeatMode.OFF,
                onClick = onToggleRepeat,
                label = { Text(repeatMode.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.OFF, RepeatMode.ALL -> Icons.Default.Repeat
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

enum class ShuffleRepeatSize(
    val buttonSize: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp,
    val backgroundSize: androidx.compose.ui.unit.Dp,
    val spacing: androidx.compose.ui.unit.Dp
) {
    Small(
        buttonSize = 36.dp,
        iconSize = 18.dp,
        backgroundSize = 32.dp,
        spacing = 8.dp
    ),
    Medium(
        buttonSize = 48.dp,
        iconSize = 24.dp,
        backgroundSize = 40.dp,
        spacing = 12.dp
    ),
    Large(
        buttonSize = 56.dp,
        iconSize = 28.dp,
        backgroundSize = 48.dp,
        spacing = 16.dp
    )
}
