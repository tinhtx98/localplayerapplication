package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.image.AlbumArtImage

@Composable
fun PlaylistSongItem(
    song: Song,
    position: Int,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRemoveFromPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
    showPosition: Boolean = true,
    isPlaying: Boolean = false
) {
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number or play indicator
            if (showPosition) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.primary else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Now playing",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

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
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
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
                    onClick = onFavoriteClick,
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

                // Remove from playlist button
                IconButton(
                    onClick = { showRemoveConfirmation = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "Remove from playlist",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = {
                Text("Remove from playlist?")
            },
            text = {
                Text("\"${song.title}\" will be removed from this playlist.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFromPlaylistClick()
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
