package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.image.AlbumArtImage

@Composable
fun QueueSongItem(
    song: Song,
    position: Int,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    isDraggable: Boolean = true
) {
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentSong -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentSong) 4.dp else 2.dp
        ),
        border = if (isCurrentSong) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        } else null
    ) {
        Box {
            // Current song indicator stripe
            if (isCurrentSong) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position number or play indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isCurrentSong) {
                                if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCurrentSong && isPlaying -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Now playing",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        isCurrentSong -> {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Paused",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = position.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Album art
                AlbumArtImage(
                    albumId = song.albumId,
                    contentDescription = "Album art for ${song.title}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Song information
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isCurrentSong) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.displayArtist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (song.displayAlbum.isNotBlank() && song.displayAlbum != "Unknown Album") {
                            Text(
                                text = " • ${song.displayAlbum}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Queue position info for current song
                    if (isCurrentSong) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Now playing • Position $position",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Duration
                Text(
                    text = song.formattedDuration,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Favorite button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        AnimatedContent(
                            targetState = song.isFavorite,
                            transitionSpec = {
                                scaleIn() + fadeIn() with scaleOut() + fadeOut()
                            },
                            label = "favorite_animation"
                        ) { isFavorite ->
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                modifier = Modifier.size(18.dp),
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Remove from queue button
                    IconButton(
                        onClick = { showRemoveConfirmation = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Remove from queue",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }

                    // Drag handle (if draggable)
                    if (isDraggable) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { _, _ ->
                                        // Handle drag gestures for reordering
                                    }
                                },
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = {
                Text("Remove from queue?")
            },
            text = {
                Text("\"${song.title}\" will be removed from the queue.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFromQueue()
                        showRemoveConfirmation = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
