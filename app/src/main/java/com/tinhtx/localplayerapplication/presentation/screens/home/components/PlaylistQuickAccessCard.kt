package com.tinhtx.localplayerapplication.presentation.screens.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.presentation.components.image.PlaylistCoverImage

@Composable
fun PlaylistQuickAccessCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showPlayButton: Boolean = true
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        Box {
            // Background pattern
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header with icon and play button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPlaylistIcon(playlist),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (showPlayButton && playlist.songCount > 0) {
                        AnimatedVisibility(
                            visible = isHovered,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            FloatingActionButton(
                                onClick = onClick,
                                modifier = Modifier.size(36.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play playlist",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playlist info
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = playlist.songCountText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (playlist.duration > 0) {
                        Text(
                            text = " • ${playlist.formattedDuration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Progress indicator for non-empty playlists
                if (playlist.songCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = 1f, // Could be actual progress if we track listening progress
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistQuickAccessGrid(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = playlists,
            key = { it.id }
        ) { playlist ->
            PlaylistQuickAccessCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
fun PlaylistHorizontalCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist cover or icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.coverArtPath != null) {
                    PlaylistCoverImage(
                        playlistCoverUrl = playlist.coverArtPath,
                        contentDescription = "Playlist cover",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = getPlaylistIcon(playlist),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Playlist info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = playlist.summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Play button
            if (playlist.songCount > 0) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play playlist",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun getPlaylistIcon(playlist: Playlist): ImageVector {
    return when {
        playlist.id == -1L -> Icons.Default.Favorite // Favorites
        playlist.id == -2L -> Icons.Default.History // Recently played
        playlist.id == -3L -> Icons.Default.TrendingUp // Most played
        playlist.isFavorite -> Icons.Default.Star
        else -> Icons.Default.QueueMusic
    }
}
