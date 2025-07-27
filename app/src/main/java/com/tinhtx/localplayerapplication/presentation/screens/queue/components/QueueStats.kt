package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.queue.QueueStats as QueueStatsData

@Composable
fun QueueStats(
    stats: QueueStatsData,
    showExtendedStats: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Queue Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stats.totalSongs == 0) {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No songs in queue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Basic stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QueueStatItem(
                        icon = Icons.Default.QueueMusic,
                        label = "Songs",
                        value = stats.totalSongs.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    QueueStatItem(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = stats.formattedTotalDuration,
                        modifier = Modifier.weight(1f)
                    )

                    QueueStatItem(
                        icon = Icons.Default.Person,
                        label = "Artists",
                        value = stats.uniqueArtists.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showExtendedStats) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Extended stats
                    QueueStatsExtended(stats = stats)
                }
            }
        }
    }
}

@Composable
private fun QueueStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QueueStatsExtended(
    stats: QueueStatsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Second row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QueueStatItem(
                icon = Icons.Default.Album,
                label = "Albums",
                value = stats.uniqueAlbums.toString(),
                modifier = Modifier.weight(1f)
            )

            QueueStatItem(
                icon = Icons.Default.Category,
                label = "Genres",
                value = stats.uniqueGenres.toString(),
                modifier = Modifier.weight(1f)
            )

            QueueStatItem(
                icon = Icons.Default.Favorite,
                label = "Favorites",
                value = stats.favoriteSongsCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Average and totals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QueueStatItem(
                icon = Icons.Default.Timer,
                label = "Avg Duration",
                value = stats.formattedAverageDuration,
                modifier = Modifier.weight(1f)
            )

            QueueStatItem(
                icon = Icons.Default.PlayArrow,
                label = "Total Plays",
                value = stats.totalPlayCount.toString(),
                modifier = Modifier.weight(1f)
            )

            QueueStatItem(
                icon = Icons.Default.Star,
                label = "Avg Rating",
                value = if (stats.averageRating > 0) String.format("%.1f", stats.averageRating) else "—",
                modifier = Modifier.weight(1f)
            )
        }

        // Notable songs section
        if (stats.mostPlayedSong != null || stats.longestSong != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Notable Songs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            stats.mostPlayedSong?.let { song ->
                QueueNotableSongItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Most Played",
                    songTitle = song.title,
                    songArtist = song.artist,
                    detail = "${song.playCount} plays"
                )
            }

            stats.longestSong?.let { song ->
                QueueNotableSongItem(
                    icon = Icons.Default.Timer,
                    label = "Longest",
                    songTitle = song.title,
                    songArtist = song.artist,
                    detail = formatDuration(song.duration)
                )
            }

            stats.shortestSong?.let { song ->
                QueueNotableSongItem(
                    icon = Icons.Default.Schedule,
                    label = "Shortest",
                    songTitle = song.title,
                    songArtist = song.artist,
                    detail = formatDuration(song.duration)
                )
            }
        }

        // Diversity score
        if (stats.diversityScore > 0) {
            Spacer(modifier = Modifier.height(8.dp))

            QueueDiversityIndicator(
                diversityScore = stats.diversityScore,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QueueNotableSongItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    songTitle: String,
    songArtist: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$songTitle • $songArtist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }

        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun QueueDiversityIndicator(
    diversityScore: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Queue Diversity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${(diversityScore * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = diversityScore,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = when {
                diversityScore < 0.3f -> "Low diversity - similar artists and genres"
                diversityScore < 0.7f -> "Moderate diversity - good mix of content"
                else -> "High diversity - wide variety of music"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun QueueStatsDialog(
    stats: QueueStatsData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null
            )
        },
        title = {
            Text("Queue Statistics")
        },
        text = {
            LazyColumn {
                item {
                    QueueStats(
                        stats = stats,
                        showExtendedStats = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
