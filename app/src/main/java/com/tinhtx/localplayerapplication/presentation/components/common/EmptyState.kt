package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
            
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(actionText)
                }
                
                if (secondaryActionText != null && onSecondaryActionClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onSecondaryActionClick,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(secondaryActionText)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMusicLibrary(
    onScanMusic: () -> Unit,
    onImportMusic: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.LibraryMusic,
        title = "No Music Found",
        description = "Your music library is empty. Scan your device for music files or import music from other sources.",
        actionText = "Scan for Music",
        onActionClick = onScanMusic,
        secondaryActionText = if (onImportMusic != null) "Import Music" else null,
        onSecondaryActionClick = onImportMusic,
        modifier = modifier
    )
}

@Composable
fun EmptyPlaylist(
    playlistName: String,
    onAddSongs: () -> Unit,
    onBrowseMusic: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.PlaylistAdd,
        title = "Empty Playlist",
        description = "\"$playlistName\" doesn't have any songs yet. Add some songs to get started.",
        actionText = "Add Songs",
        onActionClick = onAddSongs,
        secondaryActionText = if (onBrowseMusic != null) "Browse Music" else null,
        onSecondaryActionClick = onBrowseMusic,
        modifier = modifier
    )
}

@Composable
fun EmptySearchResults(
    searchQuery: String,
    onClearSearch: () -> Unit,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Results Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No results for \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Try:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                suggestions.take(3).forEach { suggestion ->
                    TextButton(
                        onClick = { onSuggestionClick?.invoke(suggestion) }
                    ) {
                        Text("• $suggestion")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onClearSearch) {
                Text("Clear Search")
            }
        }
    }
}

@Composable
fun EmptyFavorites(
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.FavoriteBorder,
        title = "No Favorites Yet",
        description = "Songs you mark as favorites will appear here. Start exploring your music and add some favorites!",
        actionText = "Browse Music",
        onActionClick = onBrowseMusic,
        modifier = modifier
    )
}

@Composable
fun EmptyRecentlyPlayed(
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.History,
        title = "No Recent Activity",
        description = "Songs you've played recently will appear here. Start listening to music to see your history.",
        actionText = "Browse Music",
        onActionClick = onBrowseMusic,
        modifier = modifier
    )
}

@Composable
fun EmptyArtists(
    onScanMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Person,
        title = "No Artists Found",
        description = "No artists were found in your music library. Make sure your music files have proper metadata.",
        actionText = "Scan Music",
        onActionClick = onScanMusic,
        modifier = modifier
    )
}

@Composable
fun EmptyAlbums(
    onScanMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Album,
        title = "No Albums Found",
        description = "No albums were found in your music library. Make sure your music files have proper metadata.",
        actionText = "Scan Music",
        onActionClick = onScanMusic,
        modifier = modifier
    )
}

@Composable
fun EmptyGenres(
    onScanMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Category,
        title = "No Genres Found",
        description = "No genres were found in your music library. Make sure your music files have proper metadata.",
        actionText = "Scan Music",
        onActionClick = onScanMusic,
        modifier = modifier
    )
}

@Composable
fun NoPermissionState(
    permission: String,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Lock,
        title = "Permission Required",
        description = "This app needs $permission permission to access your music files. Please grant the permission to continue.",
        actionText = "Grant Permission",
        onActionClick = onGrantPermission,
        secondaryActionText = "Open Settings",
        onSecondaryActionClick = onOpenSettings,
        modifier = modifier
    )
}

@Composable
fun NoInternetState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.WifiOff,
        title = "No Internet Connection",
        description = "Please check your internet connection and try again.",
        actionText = "Retry",
        onActionClick = onRetry,
        modifier = modifier
    )
}

@Composable
fun MaintenanceState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Build,
        title = "Under Maintenance",
        description = "This feature is currently under maintenance. Please try again later.",
        modifier = modifier
    )
}

@Composable
fun ComingSoonState(
    featureName: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Schedule,
        title = "Coming Soon",
        description = "$featureName is coming soon! Stay tuned for updates.",
        modifier = modifier
    )
}
