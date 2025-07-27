package com.tinhtx.localplayerapplication.presentation.components.music

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.image.AlbumArtwork

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean = false,
    isFavorite: Boolean = false,
    isCurrentlyPlaying: Boolean = false,
    isPlaying: Boolean = false,
    showAlbumArt: Boolean = true,
    showDuration: Boolean = true,
    showTrackNumber: Boolean = false,
    trackNumber: Int? = null,
    isSelectionMode: Boolean = false,
    compactMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                else -> 1.dp
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactMode) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading content: album art, track number, or selection checkbox
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null, // Handled by parent onClick
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (showTrackNumber && trackNumber != null) {
                Box(
                    modifier = Modifier.size(if (compactMode) 32.dp else 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentlyPlaying) {
                        PlayingIndicator(
                            isPlaying = isPlaying,
                            size = if (compactMode) 16.dp else 20.dp
                        )
                    } else {
                        Text(
                            text = trackNumber.toString(),
                            style = if (compactMode) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (showAlbumArt) {
                Box(
                    modifier = Modifier.size(if (compactMode) 40.dp else 48.dp)
                ) {
                    AlbumArtwork(
                        artworkUrl = song.artworkPath,
                        albumName = song.album,
                        artistName = song.artist,
                        modifier = Modifier.fillMaxSize(),
                        size = if (compactMode) 40.dp else 48.dp,
                        cornerRadius = if (compactMode) 6.dp else 8.dp
                    )
                    
                    // Playing indicator overlay
                    if (isCurrentlyPlaying) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.Pause,
                                contentDescription = if (isPlaying) "Playing" else "Paused",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }

            if (showAlbumArt || showTrackNumber || isSelectionMode) {
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = song.title,
                    style = if (compactMode) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!compactMode) {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Artist and album info
                if (compactMode) {
                    Text(
                        text = "${song.artist} • ${if (showDuration) formatDuration(song.duration) else song.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (song.album.isNotEmpty()) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )

                            Text(
                                text = song.album,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                // Additional info (genre, year, etc.)
                if (!compactMode && (song.genre.isNotEmpty() || song.year > 0)) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (song.genre.isNotEmpty()) {
                            Text(
                                text = song.genre,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        
                        if (song.year > 0) {
                            if (song.genre.isNotEmpty()) {
                                Text(
                                    text = " • ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = song.year.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Trailing content
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration
                if (showDuration && !compactMode) {
                    Text(
                        text = formatDuration(song.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Favorite button
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(if (compactMode) 32.dp else 40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (compactMode) 16.dp else 20.dp)
                        )
                    }
                }

                // More options button
                if (onMoreClick != null && !isSelectionMode) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(if (compactMode) 32.dp else 40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (compactMode) 16.dp else 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongItemMinimal(
    song: Song,
    isCurrentlyPlaying: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                fontWeight = if (isCurrentlyPlaying) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${song.artist} • ${formatDuration(song.duration)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            if (isCurrentlyPlaying) {
                PlayingIndicator(
                    isPlaying = isPlaying,
                    size = 20.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
fun PlayingIndicator(
    isPlaying: Boolean,
    size: androidx.compose.ui.unit.Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            scaleIn() + fadeIn() with scaleOut() + fadeOut()
        },
        modifier = modifier
    ) { playing ->
        if (playing) {
            // Animated equalizer bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.size(size)
            ) {
                repeat(3) { index ->
                    AnimatedEqualizerBar(
                        delay = index * 150,
                        modifier = Modifier
                            .width(3.dp)
                            .height(size)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Paused",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
private fun AnimatedEqualizerBar(
    delay: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val height by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600 + delay),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxHeight(height)
            .background(
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(1.dp)
            )
    )
}

@Composable
fun SongItemGrid(
    song: Song,
    isSelected: Boolean = false,
    isFavorite: Boolean = false,
    isCurrentlyPlaying: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCurrentlyPlaying -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Album artwork
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AlbumArtwork(
                    artworkUrl = song.artworkPath,
                    albumName = song.album,
                    artistName = song.artist,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 8.dp
                )
                
                // Favorite button
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Playing indicator
                if (isCurrentlyPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        PlayingIndicator(
                            isPlaying = true,
                            size = 16.dp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Song info
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
