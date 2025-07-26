package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.common.FullScreenLoadingIndicator
import com.tinhtx.localplayerapplication.presentation.components.music.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsBottomSheet(
    currentPlaylistSongs: List<Song>,
    onSongsSelected: (List<Song>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddSongsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var selectedSongs by remember { mutableStateOf<Set<Song>>(emptySet()) }
    
    val availableSongs = uiState.allSongs.filter { song ->
        currentPlaylistSongs.none { it.id == song.id }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllSongs()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (selectedSongs.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = selectedSongs.size.toString(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FullScreenLoadingIndicator(
                            message = "Loading songs...",
                            showBackground = false
                        )
                    }
                }
                
                availableSongs.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All songs are already in this playlist",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                else -> {
                    val filteredSongs = if (searchQuery.isBlank()) {
                        availableSongs
                    } else {
                        availableSongs.filter { song ->
                            song.title.contains(searchQuery, ignoreCase = true) ||
                            song.displayArtist.contains(searchQuery, ignoreCase = true) ||
                            song.displayAlbum.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    // Songs count
                    Text(
                        text = "${filteredSongs.size} available songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Songs list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = filteredSongs,
                            key = { it.id }
                        ) { song ->
                            AddSongItem(
                                song = song,
                                isSelected = selectedSongs.contains(song),
                                onSelectionChange = { isSelected ->
                                    selectedSongs = if (isSelected) {
                                        selectedSongs + song
                                    } else {
                                        selectedSongs - song
                                    }
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
            
            // Action buttons
            if (availableSongs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            onSongsSelected(selectedSongs.toList())
                        },
                        enabled = selectedSongs.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add ${selectedSongs.size} songs")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSongItem(
    song: Song,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onSelectionChange(!isSelected) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            SongItem(
                song = song,
                onClick = { onSelectionChange(!isSelected) },
                onFavoriteClick = { /* Handle favorite in parent */ },
                onMoreClick = { /* Handle more options */ },
                modifier = Modifier.weight(1f),
                showMoreButton = false
            )
        }
    }
}
