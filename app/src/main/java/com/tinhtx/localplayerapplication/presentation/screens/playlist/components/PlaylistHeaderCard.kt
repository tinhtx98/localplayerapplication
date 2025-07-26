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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tinhtx.localplayerapplication.domain.model.Playlist

@Composable
fun PlaylistHeaderCard(
    playlist: Playlist,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playlist cover art
                    PlaylistCoverArt(
                        coverArtPath = playlist.coverArtPath,
                        playlistName = playlist.name,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Playlist info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = playlist.songCountText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        if (playlist.description?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = playlist.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Created date
                        Text(
                            text = "Created ${formatDate(playlist.dateCreated)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                if (playlist.songCount > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play")
                        }
                        
                        OutlinedButton(
                            onClick = onShuffleClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistCoverArt(
    coverArtPath: String?,
    playlistName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (coverArtPath != null) {
            AsyncImage(
                model = coverArtPath,
                contentDescription = "Playlist cover for $playlistName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    DefaultPlaylistCover(playlistName = playlistName)
                }
            )
        } else {
            DefaultPlaylistCover(playlistName = playlistName)
        }
    }
}

@Composable
private fun DefaultPlaylistCover(
    playlistName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            
            if (playlistName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = playlistName.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> "today"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        diff < 30 * 24 * 60 * 60 * 1000 -> "${diff / (7 * 24 * 60 * 60 * 1000)} weeks ago"
        else -> "a while ago"
    }
}
