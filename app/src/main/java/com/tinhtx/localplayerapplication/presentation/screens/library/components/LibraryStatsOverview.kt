package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryStatType
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryStats

@Composable
fun LibraryStatsOverview(
    stats: LibraryStats,
    onStatsClick: (LibraryStatType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Music Library Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // First row of stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LibraryStatItem(
                        icon = Icons.Default.MusicNote,
                        count = stats.totalSongs,
                        label = "Songs",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onStatsClick(LibraryStatType.SONGS) }
                    )
                    
                    LibraryStatItem(
                        icon = Icons.Default.Album,
                        count = stats.totalAlbums,
                        label = "Albums",
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onStatsClick(LibraryStatType.ALBUMS) }
                    )
                    
                    LibraryStatItem(
                        icon = Icons.Default.Person,
                        count = stats.totalArtists,
                        label = "Artists",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { onStatsClick(LibraryStatType.ARTISTS) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Second row of stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LibraryStatItem(
                        icon = Icons.Default.QueueMusic,
                        count = stats.totalPlaylists,
                        label = "Playlists",
                        color = Color(0xFF4CAF50),
                        onClick = { onStatsClick(LibraryStatType.PLAYLISTS) }
                    )
                    
                    LibraryStatItem(
                        icon = Icons.Default.Favorite,
                        count = stats.totalFavorites,
                        label = "Favorites",
                        color = Color.Red,
                        onClick = { onStatsClick(LibraryStatType.FAVORITES) }
                    )
                    
                    LibraryDurationItem(
                        duration = stats.totalDuration,
                        onClick = { onStatsClick(LibraryStatType.DURATION) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryStatItem(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f))
                .animateContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                slideInVertically { it } + fadeIn() with
                slideOutVertically { -it } + fadeOut()
            },
            label = "count_animation"
        ) { animatedCount ->
            Text(
                text = formatCount(animatedCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LibraryDurationItem(
    duration: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF9800).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFF9800)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = duration,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Duration",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}
