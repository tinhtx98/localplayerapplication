package com.tinhtx.localplayerapplication.presentation.screens.queue.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.queue.QueueSource
import com.tinhtx.localplayerapplication.presentation.components.image.CoilAsyncImage

@Composable
fun QueueHeaderCard(
    queueTitle: String,
    queueSubtitle: String,
    queueSource: QueueSource?,
    totalDuration: String,
    remainingDuration: String,
    progressPercentage: Float,
    showStats: Boolean,
    onToggleStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Queue source icon/image
                Box(
                    modifier = Modifier.size(48.dp)
                ) {
                    when (queueSource) {
                        is QueueSource.Album -> {
                            if (queueSource.album.artworkPath != null) {
                                CoilAsyncImage(
                                    imageUrl = queueSource.album.artworkPath,
                                    contentDescription = "Album artwork",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                QueueSourceIcon(
                                    icon = Icons.Default.Album,
                                    contentDescription = "Album"
                                )
                            }
                        }
                        is QueueSource.Artist -> {
                            if (queueSource.artist.imagePath != null) {
                                CoilAsyncImage(
                                    imageUrl = queueSource.artist.imagePath,
                                    contentDescription = "Artist image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                QueueSourceIcon(
                                    icon = Icons.Default.Person,
                                    contentDescription = "Artist"
                                )
                            }
                        }
                        is QueueSource.Playlist -> {
                            if (queueSource.playlist.imagePath != null) {
                                CoilAsyncImage(
                                    imageUrl = queueSource.playlist.imagePath,
                                    contentDescription = "Playlist image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                QueueSourceIcon(
                                    icon = Icons.Default.PlaylistPlay,
                                    contentDescription = "Playlist"
                                )
                            }
                        }
                        is QueueSource.Search -> {
                            QueueSourceIcon(
                                icon = Icons.Default.Search,
                                contentDescription = "Search results"
                            )
                        }
                        is QueueSource.Shuffle -> {
                            QueueSourceIcon(
                                icon = Icons.Default.Shuffle,
                                contentDescription = "Shuffle"
                            )
                        }
                        is QueueSource.Radio -> {
                            QueueSourceIcon(
                                icon = Icons.Default.Radio,
                                contentDescription = "Radio"
                            )
                        }
                        else -> {
                            QueueSourceIcon(
                                icon = Icons.Default.QueueMusic,
                                contentDescription = "Queue"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = queueTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = queueSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Queue source description
                    if (queueSource != null && queueSource !is QueueSource.Manual) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = queueSource.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stats toggle button
                IconButton(onClick = onToggleStats) {
                    Icon(
                        imageVector = if (showStats) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showStats) "Hide stats" else "Show stats",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress section
            QueueProgressSection(
                totalDuration = totalDuration,
                remainingDuration = remainingDuration,
                progressPercentage = progressPercentage
            )

            // Expandable stats section
            AnimatedVisibility(
                visible = showStats,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    QueueStatsExpanded(
                        totalDuration = totalDuration,
                        remainingDuration = remainingDuration,
                        progressPercentage = progressPercentage
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueSourceIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun QueueProgressSection(
    totalDuration: String,
    remainingDuration: String,
    progressPercentage: Float
) {
    Column {
        // Duration info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: $totalDuration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Text(
                text = "Remaining: $remainingDuration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = progressPercentage / 100f,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${progressPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QueueStatsExpanded(
    totalDuration: String,
    remainingDuration: String,
    progressPercentage: Float
) {
    Column {
        Divider(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Additional stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QueueStatItem(
                label = "Played",
                value = "${progressPercentage.toInt()}%",
                icon = Icons.Default.PlayArrow
            )

            QueueStatItem(
                label = "Remaining",
                value = remainingDuration,
                icon = Icons.Default.Schedule
            )

            QueueStatItem(
                label = "Total",
                value = totalDuration,
                icon = Icons.Default.Timer
            )
        }
    }
}

@Composable
private fun QueueStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun QueueHeaderCompact(
    queueTitle: String,
    songCount: Int,
    totalDuration: String,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = queueTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$songCount songs • $totalDuration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
