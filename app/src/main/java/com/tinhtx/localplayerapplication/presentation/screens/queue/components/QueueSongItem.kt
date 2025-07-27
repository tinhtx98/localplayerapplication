package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QueueSongItem(
    song: Song,
    index: Int,
    isSelected: Boolean,
    isFavorite: Boolean,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    isSelectionMode: Boolean,
    compactView: Boolean,
    showSongNumber: Boolean,
    canReorder: Boolean,
    isDragging: Boolean,
    isDropTarget: Boolean,
    searchQuery: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = when {
        isDragging -> 0.5f
        isDropTarget -> 0.8f
        else -> 1f
    }

    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCurrentlyPlaying -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected || isCurrentlyPlaying -> 4.dp
                isDragging -> 8.dp
                else -> 1.dp
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactView) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading content: drag handle, selection, or track number
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isSelectionMode -> {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null, // Handled by parent onClick
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    canReorder -> {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    isCurrentlyPlaying -> {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.Pause,
                            contentDescription = if (isPlaying) "Playing" else "Paused",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    showSongNumber -> {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (compactView) {
                    // Compact layout: title and artist in one line
                    Text(
                        text = buildAnnotatedString {
                            append(highlightSearchTerm(song.title, searchQuery))
                            append(" • ")
                            pushStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            append(song.artist)
                            pop()
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    // Full layout
                    Text(
                        text = highlightSearchTerm(song.title, searchQuery),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = highlightSearchTerm(song.artist, searchQuery),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (song.album.isNotEmpty()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )

                            Text(
                                text = highlightSearchTerm(song.album, searchQuery),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Text(
                            text = formatDuration(song.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (compactView) 2.dp else 4.dp)
            ) {
                // Favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(if (compactView) 28.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (compactView) 14.dp else 16.dp)
                    )
                }

                // Remove button (for queue)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(if (compactView) 28.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove from queue",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(if (compactView) 14.dp else 16.dp)
                    )
                }

                // More options button
                if (!compactView || !isSelectionMode) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(if (compactView) 28.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (compactView) 14.dp else 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueSongItemCompact(
    song: Song,
    index: Int,
    isSelected: Boolean,
    isFavorite: Boolean,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = highlightSearchTerm(song.title, searchQuery),
                fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${highlightSearchTerm(song.artist, searchQuery)} • ${formatDuration(song.duration)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCurrentlyPlaying -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.Pause,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        modifier = modifier
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun highlightSearchTerm(text: String, searchTerm: String): AnnotatedString {
    if (searchTerm.isBlank() || text.isBlank()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerSearchTerm = searchTerm.lowercase()
        
        var startIndex = 0
        
        while (startIndex < text.length) {
            val index = lowerText.indexOf(lowerSearchTerm, startIndex)
            
            if (index == -1) {
                // No more matches, append remaining text
                append(text.substring(startIndex))
                break
            }
            
            // Append text before match
            append(text.substring(startIndex, index))
            
            // Append highlighted match
            pushStyle(
                SpanStyle(
                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    fontWeight = FontWeight.SemiBold
                )
            )
            append(text.substring(index, index + searchTerm.length))
            pop()
            
            startIndex = index + searchTerm.length
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
