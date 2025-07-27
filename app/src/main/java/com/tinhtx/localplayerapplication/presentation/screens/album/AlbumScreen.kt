package com.tinhtx.localplayerapplication.presentation.screens.album

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.screens.album.components.*

/**
 * Album Screen - Complete album detail interface
 * Maps 100% với AlbumViewModel và AlbumUiState
 * Uses correct components: AlbumHeaderCard, AlbumInfoCard, AlbumSearchBar, 
 * AlbumSongItem, AlbumSortDialog, AddToPlaylistDialog, EmptyAlbumState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: Long? = null,
    albumName: String? = null,
    artistName: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Dialog states
    var showSortDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showMoreOptionsDialog by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Load album data on first composition
    LaunchedEffect(albumId, albumName, artistName) {
        when {
            albumId != null -> viewModel.loadAlbum(albumId)
            albumName != null && artistName != null -> viewModel.loadAlbum(albumName, artistName)
        }
    }

    // Handle navigation side effects
    LaunchedEffect(uiState.selectedSongForPlayback) {
        uiState.selectedSongForPlayback?.let { song ->
            onNavigateToPlayer(song)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.navigateToArtist) {
        uiState.navigateToArtist?.let { artist ->
            onNavigateToArtist(artist)
            viewModel.clearNavigationStates()
        }
    }

    // Handle dialog states from ViewModel
    LaunchedEffect(uiState.showSortOptions) {
        showSortDialog = uiState.showSortOptions
    }

    LaunchedEffect(uiState.showAddToPlaylist) {
        showAddToPlaylistDialog = uiState.showAddToPlaylist
    }

    LaunchedEffect(uiState.showMoreOptions) {
        showMoreOptionsDialog = uiState.showMoreOptions
    }

    // Handle error auto-dismiss
    LaunchedEffect(uiState.hasError) {
        if (uiState.hasError) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    // Handle selection mode back gesture
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.clearSelection()
    }

    // Filtered songs based on search
    val filteredSongs = remember(uiState.sortedSongs, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.sortedSongs
        } else {
            uiState.sortedSongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshAlbum,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    AlbumLoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasError -> {
                    AlbumErrorState(
                        error = uiState.currentError ?: "Unknown error",
                        onRetry = viewModel::refreshAlbum,
                        onDismiss = viewModel::clearError,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.isEmpty -> {
                    EmptyAlbumState(
                        albumName = uiState.album?.name ?: "Unknown Album",
                        artistName = uiState.album?.artist ?: "Unknown Artist",
                        onRefresh = viewModel::refreshAlbum,
                        onNavigateBack = onNavigateBack,
                        onScanLibrary = { /* TODO: Implement library scan */ },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasData -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // Space for mini player
                    ) {
                        // Album header card
                        item {
                            AlbumHeaderCard(
                                album = uiState.album!!,
                                isAlbumFavorite = uiState.isAlbumFavorite,
                                isPlaying = uiState.isPlaying,
                                onNavigateBack = onNavigateBack,
                                onToggleAlbumFavorite = viewModel::toggleAlbumFavorite,
                                onNavigateToArtist = viewModel::navigateToArtist,
                                onMoreClick = viewModel::toggleMoreOptions,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Album info card with statistics and action buttons
                        item {
                            AlbumInfoCard(
                                albumStats = uiState.albumStats,
                                isPlaying = uiState.isPlaying,
                                shuffleMode = uiState.shuffleMode,
                                onPlayAlbum = viewModel::playAlbum,
                                onShufflePlay = viewModel::shufflePlayAlbum,
                                onToggleSelectionMode = viewModel::toggleSelectionMode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Search bar (only show if not in selection mode)
                        if (!uiState.isSelectionMode) {
                            item {
                                AlbumSearchBar(
                                    searchQuery = searchQuery,
                                    onSearchQueryChanged = { searchQuery = it },
                                    isSearchActive = isSearchActive,
                                    onActiveChanged = { isSearchActive = it },
                                    songCount = uiState.songs.size,
                                    filteredCount = filteredSongs.size,
                                    onSortClick = { showSortDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }

                        // Selection mode toolbar
                        if (uiState.isSelectionMode) {
                            item {
                                SelectionModeToolbar(
                                    selectedCount = uiState.selectedSongsCount,
                                    canSelectAll = uiState.canSelectAll,
                                    allSelected = uiState.allSongsSelected,
                                    onSelectAll = viewModel::selectAllSongs,
                                    onClearSelection = viewModel::clearSelection,
                                    onAddToFavorites = viewModel::addSelectedToFavorites,
                                    onRemoveFromFavorites = viewModel::removeSelectedFromFavorites,
                                    onAddToPlaylist = { showAddToPlaylistDialog = true },
                                    onPlaySelected = viewModel::playSelectedSongs,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Songs list using AlbumSongItem
                        itemsIndexed(
                            items = filteredSongs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            AlbumSongItem(
                                song = song,
                                trackNumber = song.trackNumber.takeIf { it > 0 } ?: (index + 1),
                                isSelected = uiState.isSongSelected(song.id),
                                isFavorite = uiState.isSongFavorite(song),
                                isCurrentlyPlaying = uiState.isCurrentlyPlaying(song),
                                isPlaying = uiState.isPlaying,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSongSelection(song.id)
                                    } else {
                                        viewModel.playSong(song)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleSongSelection(song.id)
                                },
                                onToggleSelection = {
                                    viewModel.toggleSongSelection(song.id)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleSongFavorite(song)
                                },
                                onMoreClick = {
                                    selectedSongForOptions = song
                                    showMoreOptionsDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp)
                                    .animateItemPlacement()
                            )
                        }

                        // Empty search results
                        if (searchQuery.isNotBlank() && filteredSongs.isEmpty()) {
                            item {
                                EmptySearchResultsState(
                                    searchQuery = searchQuery,
                                    onClearSearch = { searchQuery = "" },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        AlbumDialogs(
            showSortDialog = showSortDialog,
            onDismissSortDialog = { showSortDialog = false },
            showAddToPlaylistDialog = showAddToPlaylistDialog,
            onDismissAddToPlaylistDialog = { showAddToPlaylistDialog = false },
            showMoreOptionsDialog = showMoreOptionsDialog,
            onDismissMoreOptionsDialog = { 
                showMoreOptionsDialog = false
                selectedSongForOptions = null
            },
            selectedSongForOptions = selectedSongForOptions,
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToPlaylist = onNavigateToPlaylist
        )

        // Error snackbar
        AnimatedVisibility(
            visible = uiState.hasError,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ErrorSnackbar(
                error = uiState.currentError ?: "",
                onDismiss = viewModel::clearError,
                onRetry = viewModel::refreshAlbum,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun SelectionModeToolbar(
    selectedCount: Int,
    canSelectAll: Boolean,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onAddToFavorites: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onPlaySelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear selection",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Select all button
                if (canSelectAll && !allSelected) {
                    TextButton(onClick = onSelectAll) {
                        Text(
                            text = "Select All",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play selected
                OutlinedButton(
                    onClick = onPlaySelected,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play")
                }

                // Add to favorites
                OutlinedButton(
                    onClick = onAddToFavorites,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Favorite")
                }

                // Add to playlist
                OutlinedButton(
                    onClick = onAddToPlaylist,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Playlist")
                }
            }
        }
    }
}

@Composable
private fun AlbumLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading album...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AlbumErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to load album",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onNavigateBack) {
                Text("Go Back")
            }

            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }

            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptySearchResultsState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No songs found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "No songs match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onClearSearch) {
            Text("Clear Search")
        }
    }
}

@Composable
private fun ErrorSnackbar(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onRetry) {
                Text("Retry")
            }

            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun AlbumDialogs(
    showSortDialog: Boolean,
    onDismissSortDialog: () -> Unit,
    showAddToPlaylistDialog: Boolean,
    onDismissAddToPlaylistDialog: () -> Unit,
    showMoreOptionsDialog: Boolean,
    onDismissMoreOptionsDialog: () -> Unit,
    selectedSongForOptions: Song?,
    uiState: AlbumUiState,
    viewModel: AlbumViewModel,
    onNavigateToPlaylist: () -> Unit
) {
    // Sort dialog using AlbumSortDialog component
    if (showSortDialog) {
        AlbumSortDialog(
            currentSortOrder = uiState.sortOrder,
            currentAscending = uiState.sortAscending,
            onDismiss = onDismissSortDialog,
            onApply = { sortOrder, ascending ->
                viewModel.updateSortOrder(sortOrder, ascending)
                onDismissSortDialog()
            }
        )
    }

    // Add to playlist dialog using AddToPlaylistDialog component
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            selectedSongsCount = uiState.selectedSongsCount,
            playlists = emptyList(), // TODO: Get playlists from ViewModel
            onDismiss = onDismissAddToPlaylistDialog,
            onCreateNewPlaylist = {
                onNavigateToPlaylist()
                onDismissAddToPlaylistDialog()
            },
            onAddToExistingPlaylist = { playlistId ->
                viewModel.addSelectedToPlaylist(playlistId)
                onDismissAddToPlaylistDialog()
            }
        )
    }

    // More options dialog for individual songs
    if (showMoreOptionsDialog && selectedSongForOptions != null) {
        SongMoreOptionsDialog(
            song = selectedSongForOptions,
            onDismiss = onDismissMoreOptionsDialog,
            onAddToFavorite = { viewModel.toggleSongFavorite(selectedSongForOptions) },
            onAddToPlaylist = { 
                // TODO: Show add single song to playlist
            },
            onShareSong = {
                // TODO: Implement sharing
            },
            onViewSongInfo = {
                // TODO: Show song info
            }
        )
    }
}

@Composable
private fun SongMoreOptionsDialog(
    song: Song,
    onDismiss: () -> Unit,
    onAddToFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShareSong: () -> Unit,
    onViewSongInfo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onAddToFavorite()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Favorites")
                }

                TextButton(
                    onClick = {
                        onAddToPlaylist()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Playlist")
                }

                TextButton(
                    onClick = {
                        onShareSong()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }

                TextButton(
                    onClick = {
                        onViewSongInfo()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Song Info")
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

// BackHandler import
@Composable
private fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
