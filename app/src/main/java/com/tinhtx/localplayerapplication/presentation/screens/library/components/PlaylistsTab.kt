package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.presentation.components.music.PlaylistCard
import com.tinhtx.localplayerapplication.presentation.components.music.HorizontalPlaylistCard
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    viewMode: AppConstants.ViewMode,
    gridSize: AppConstants.GridSize,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    
    Box(modifier = modifier.fillMaxSize()) {
        when (viewMode) {
            AppConstants.ViewMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = horizontalPadding,
                        vertical = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create new playlist item
                    item {
                        CreatePlaylistItem(
                            onClick = onCreatePlaylistClick,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    
                    items(
                        items = playlists,
                        key = { it.id }
                    ) { playlist ->
                        HorizontalPlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
            
            AppConstants.ViewMode.GRID -> {
                val columns = when (gridSize) {
                    AppConstants.GridSize.SMALL -> GridCells.Adaptive(140.dp)
                    AppConstants.GridSize.MEDIUM -> GridCells.Adaptive(160.dp)
                    AppConstants.GridSize.LARGE -> GridCells.Adaptive(180.dp)
                }
                
                LazyVerticalGrid(
                    columns = columns,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = horizontalPadding,
                        vertical = 8.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Create new playlist item
                    item {
                        CreatePlaylistGridItem(
                            onClick = onCreatePlaylistClick,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    
                    items(
                        items = playlists,
                        key = { it.id }
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
        
        // Empty state
        if (playlists.isEmpty()) {
            EmptyPlaylistsState(
                onCreatePlaylistClick = onCreatePlaylistClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CreatePlaylistItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Create New Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Add songs to your custom playlist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistGridItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create Playlist",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyPlaylistsState(
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Create your first playlist to organize your favorite songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreatePlaylistClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}
