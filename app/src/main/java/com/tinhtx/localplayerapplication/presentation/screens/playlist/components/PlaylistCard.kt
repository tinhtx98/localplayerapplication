package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.presentation.components.image.CoilAsyncImage

enum class PlaylistCardViewMode {
    LIST,
    GRID,
    COMPACT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    viewMode: PlaylistCardViewMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = if (isSelectionMode) onToggleSelection else onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 3.dp
        )
    ) {
        when (viewMode) {
            PlaylistCardViewMode.LIST -> {
                PlaylistCardListContent(
                    playlist = playlist,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode
                )
            }
            PlaylistCardViewMode.GRID -> {
                PlaylistCardGridContent(
                    playlist = playlist,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode
                )
            }
            PlaylistCardViewMode.COMPACT -> {
                PlaylistCardCompactContent(
                    playlist = playlist,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode
                )
            }
        }
    }
}

@Composable
private fun PlaylistCardListContent(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox or playlist icon
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null, // Handled by parent onClick
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Playlist thumbnail
        PlaylistThumbnail(
            playlist = playlist,
            size = 56.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Playlist info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (playlist.description.isNotBlank()) {
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${playlist.songCount} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (playlist.totalDuration > 0) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Text(
                        text = formatDuration(playlist.totalDuration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Play button
        if (!isSelectionMode) {
            IconButton(
                onClick = { /* TODO: Play playlist */ }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play playlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PlaylistCardGridContent(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean
) {
    Column {
        // Playlist thumbnail with selection indicator
        Box {
            PlaylistThumbnail(
                playlist = playlist,
                size = null, // Fill available width
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            // Selection indicator
            if (isSelectionMode && isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }

            // Play button overlay
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { /* TODO: Play playlist */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play playlist",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Playlist info
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (playlist.totalDuration > 0) {
                Text(
                    text = formatDuration(playlist.totalDuration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlaylistCardCompactContent(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox or small thumbnail
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            PlaylistThumbnail(
                playlist = playlist,
                size = 32.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Playlist info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Play button
        if (!isSelectionMode) {
            IconButton(
                onClick = { /* TODO: Play playlist */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play playlist",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaylistThumbnail(
    playlist: Playlist,
    size: dp? = 56.dp,
    modifier: Modifier = Modifier
) {
    val thumbnailModifier = if (size != null) {
        modifier.size(size)
    } else {
        modifier
    }

    if (playlist.imagePath != null) {
        CoilAsyncImage(
            imageUrl = playlist.imagePath,
            contentDescription = "Playlist thumbnail",
            modifier = thumbnailModifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // Default playlist icon
        Box(
            modifier = thumbnailModifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistPlay,
                contentDescription = "Playlist",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(if (size != null) size * 0.6f else 24.dp)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}
