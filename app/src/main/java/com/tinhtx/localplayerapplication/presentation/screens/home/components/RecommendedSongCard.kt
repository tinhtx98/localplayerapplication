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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.image.AlbumArtImage

@Composable
fun RecommendedSongCard(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    reason: String? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 8.dp else 4.dp
        )
    ) {
        Box {
            // Recommendation badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            bottomEnd = 12.dp
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Recommended",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp)) // Space for badge
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art with play overlay
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        AlbumArtImage(
                            albumId = song.albumId,
                            contentDescription = "Album art for ${song.title}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        // Play button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(alpha = 0.0f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedVisibility(
                                visible = isPressed,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Song info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = song.displayArtist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (reason != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

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
                                modifier = Modifier.size(16.dp),
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // Additional info row
                if (song.playCount > 0 || song.year > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (song.playCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${song.playCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        Text(
                            text = song.formattedDuration,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedSongsList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
    reasons: Map<Long, String> = emptyMap()
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            items = songs,
            key = { it.id }
        ) { song ->
            RecommendedSongCard(
                song = song,
                onClick = { onSongClick(song) },
                onFavoriteClick = { onFavoriteClick(song) },
                reason = reasons[song.id],
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
fun SmartRecommendationCard(
    songs: List<Song>,
    title: String,
    subtitle: String,
    onViewAllClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                TextButton(onClick = onViewAllClick) {
                    Text("View All")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Songs list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                songs.take(3).forEach { song ->
                    RecommendedSongCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
