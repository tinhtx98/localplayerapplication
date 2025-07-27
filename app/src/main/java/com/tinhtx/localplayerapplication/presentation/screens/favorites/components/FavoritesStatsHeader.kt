package com.tinhtx.localplayerapplication.presentation.screens.favorites.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.favorites.FavoritesStatistics

@Composable
fun FavoritesStatsHeader(
    favoritesStats: FavoritesStatistics,
    isLoading: Boolean,
    onPlayAllFavorites: () -> Unit,
    onShufflePlayFavorites: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and favorite icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Favorites",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Loading state
                repeat(2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) {
                            FavoriteStatSkeleton()
                        }
                    }
                }
            } else {
                // Statistics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FavoriteStatItem(
                        title = "Songs",
                        value = favoritesStats.totalFavoriteSongs.toString(),
                        icon = Icons.Default.MusicNote
                    )

                    FavoriteStatItem(
                        title = "Albums",
                        value = favoritesStats.totalFavoriteAlbums.toString(),
                        icon = Icons.Default.Album
                    )

                    FavoriteStatItem(
                        title = "Artists",
                        value = favoritesStats.totalFavoriteArtists.toString(),
                        icon = Icons.Default.Person
                    )

                    FavoriteStatItem(
                        title = "Playlists",
                        value = favoritesStats.totalFavoritePlaylists.toString(),
                        icon = Icons.Default.PlaylistPlay
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Additional stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FavoriteStatItem(
                        title = "Duration",
                        value = favoritesStats.formattedTotalDuration,
                        icon = Icons.Default.Schedule
                    )

                    FavoriteStatItem(
                        title = "Total Plays",
                        value = favoritesStats.favoritesPlayCount.toString(),
                        icon = Icons.Default.PlayArrow
                    )

                    FavoriteStatItem(
                        title = "Top Genre",
                        value = favoritesStats.topGenre,
                        icon = Icons.Default.Category
                    )

                    FavoriteStatItem(
                        title = "Avg Duration",
                        value = favoritesStats.formattedAverageDuration,
                        icon = Icons.Default.Timer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            if (!isLoading && favoritesStats.totalFavoriteSongs > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play all favorites
                    Button(
                        onClick = onPlayAllFavorites,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play All")
                    }

                    // Shuffle favorites
                    OutlinedButton(
                        onClick = onShufflePlayFavorites,
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

                Spacer(modifier = Modifier.height(8.dp))

                // Select button
                OutlinedButton(
                    onClick = onToggleSelectionMode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Favorites")
                }
            }

            // Most played favorite (if available)
            if (!isLoading && favoritesStats.mostPlayedFavorite != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Column {
                    Text(
                        text = "Most Played Favorite",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = favoritesStats.mostPlayedFavorite.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "${favoritesStats.mostPlayedFavorite.artist} • ${favoritesStats.mostPlayedFavorite.playCount} plays",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteStatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun FavoriteStatSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(16.dp)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(50.dp)
                .height(12.dp)
                .shimmerEffect()
        )
    }
}

// Shimmer effect extension (placeholder implementation)
private fun Modifier.shimmerEffect() = this
