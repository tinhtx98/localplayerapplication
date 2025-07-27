package com.tinhtx.localplayerapplication.presentation.screens.playlist

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.screens.playlist.components.*

/**
 * Playlist Detail Screen - Chi tiết 1 playlist cụ thể
 * Maps với PlaylistViewModel và PlaylistUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Load playlist detail on first composition
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistDetail(playlistId)
    }

    // Handle navigation side effects
    LaunchedEffect(uiState.selectedSongForPlayback) {
        uiState.selectedSongForPlayback?.let { song ->
            onNavigateToPlayer(song)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.navigateToAlbum) {
        uiState.navigateToAlbum?.let { album ->
            onNavigateToAlbum(album)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.navigateToArtist) {
        uiState.navigateToArtist?.let { artist ->
            onNavigateToArtist(artist)
            viewModel.clearNavigationStates()
        }
    }

    // Handle playlist deletion navigation
    LaunchedEffect(uiState.lastDeletedPlaylist) {
        if (uiState.lastDeletedPlaylist?.id == playlistId) {
            onNavigateBack()
        }
    }

    // Handle error auto-dismiss
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    // Handle selection mode back gesture
    BackHandler(enabled = uiState.songsSelectionMode) {
        viewModel.clearSelection()
    }

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshAll,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top app bar
                PlaylistDetailTopBar(
                    playlist = uiState.currentPlaylist,
                    isSelectionMode = uiState.songsSelectionMode,
                    selectedCount = uiState.selectedSongsCount,
                    canEdit = uiState.canEditPlaylist,
                    onNavigateBack = onNavigateBack,
                    onToggleOptions = viewModel::togglePlaylistOptionsMenu,
                    onToggleSelectionMode = { /* Will be handled by individual song items */ },
                    onSelectAll = { /* TODO: Implement select all songs */ },
                    onClearSelection = { /* TODO: Implement clear selection */ }
                )

                when {
                    uiState.playlistDetailLoading -> {
                        PlaylistDetailLoadingState(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.hasPlaylistError -> {
                        PlaylistDetailErrorState(
                            error = uiState.currentPlaylistError ?: "Unknown error",
                            onRetry = { viewModel.loadPlaylistDetail(playlistId) },
                            onDismiss = viewModel::clearError,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.playlistEmpty -> {
                        EmptyPlaylistState(
                            playlistName = uiState.currentPlaylist?.name ?: "Playlist",
                            canAddSongs = uiState.canAddSongs,
                            onAddSongs = viewModel::toggleAddSongsBottomSheet,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.hasPlaylistData -> {
                        Column(modifier = Modifier.weight(1f)) {
                            // Playlist header
                            PlaylistHeaderCard(
                                playlist = uiState.currentPlaylist!!,
                                isPlaying = uiState.isPlaying,
                                currentPlayingSong = uiState.currentPlayingSong,
                                canEdit = uiState.canEditPlaylist,
                                canAddSongs = uiState.canAddSongs,
                                onPlayPlaylist = viewModel::playPlaylist,
                                onShufflePlay = viewModel::shufflePlayPlaylist,
                                onEditPlaylist = viewModel::toggleEditPlaylistDialog,
                                onAddSongs = viewModel::toggleAddSongsBottomSheet,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )

                            // Search bar (only show if not in selection mode)
                            if (!uiState.songsSelectionMode) {
                                PlaylistSearchBar(
                                    searchQuery = uiState.playlistSearchQuery,
                                    onSearchQueryChanged = { /* TODO: viewModel::searchPlaylist */ },
                                    isSearchActive = uiState.playlistSearchActive,
                                    onActiveChanged = { /* TODO: Handle search active state */ },
                                    totalCount = uiState.playlistSongs.size,
                                    filteredCount = uiState.playlistSearchResultsCount,
                                    onSortClick = viewModel::togglePlaylistSortDialog,
                                    onViewModeClick = { /* TODO: Implement view mode */ },
                                    placeholder = "Search in playlist...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            // Songs list - Using DraggableSongList if can reorder, otherwise regular list
                            if (uiState.canReorderSongs && !uiState.songsSelectionMode) {
                                DraggableSongList(
                                    songs = uiState.filteredPlaylistSongs,
                                    currentPlayingSong = uiState.currentPlayingSong,
                                    isPlaying = uiState.isPlaying,
                                    favoriteSongs = uiState.favoriteSongs,
                                    onSongClick = viewModel::playSong,
                                    onSongLongClick = { /* TODO: Long click handler */ },
                                    onToggleFavorite = { /* TODO: viewModel::toggleSongFavorite */ },
                                    onMoreClick = { /* TODO: Show song options */ },
                                    onMoveItem = { fromIndex, toIndex ->
                                        // TODO: viewModel::reorderSongs(fromIndex, toIndex)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            } else {
                                // Regular songs list
                                PlaylistSongsList(
                                    songs = uiState.filteredPlaylistSongs,
                                    isSelectionMode = uiState.songsSelectionMode,
                                    selectedSongs = uiState.selectedSongs,
                                    currentPlayingSong = uiState.currentPlayingSong,
                                    isPlaying = uiState.isPlaying,
                                    favoriteSongs = uiState.favoriteSongs,
                                    onSongClick = { song ->
                                        if (uiState.songsSelectionMode) {
                                            // TODO: viewModel.toggleSongSelection(song.id)
                                        } else {
                                            viewModel.playSong(song)
                                        }
                                    },
                                    onSongLongClick = { song ->
                                        // TODO: Toggle selection mode and select song
                                    },
                                    onToggleSelection = { /* TODO: viewModel::toggleSongSelection */ },
                                    onToggleFavorite = { /* TODO: viewModel::toggleSongFavorite */ },
                                    onMoreClick = { /* TODO: Show song options */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Playlist options menu
        if (uiState.showPlaylistOptionsMenu) {
            PlaylistOptionsMenu(
                playlist = uiState.currentPlaylist,
                canEdit = uiState.canEditPlaylist,
                canDelete = uiState.canDeletePlaylist,
                onEditPlaylist = {
                    viewModel.togglePlaylistOptionsMenu()
                    viewModel.toggleEditPlaylistDialog()
                },
                onDeletePlaylist = {
                    viewModel.togglePlaylistOptionsMenu()
                    viewModel.toggleDeletePlaylistDialog()
                },
                onSharePlaylist = {
                    // TODO: Implement share
                    viewModel.togglePlaylistOptionsMenu()
                },
                onExportPlaylist = {
                    // TODO: Implement export
                    viewModel.togglePlaylistOptionsMenu()
                },
                onDismiss = viewModel::togglePlaylistOptionsMenu,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        // Edit playlist dialog
        if (uiState.showEditPlaylistDialog) {
            EditPlaylistDialog(
                playlistName = uiState.editingPlaylistName,
                playlistDescription = uiState.editingPlaylistDescription,
                isUpdating = uiState.updatingPlaylist,
                onNameChanged = { /* TODO: viewModel::updateEditingName */ },
                onDescriptionChanged = { /* TODO: viewModel::updateEditingDescription */ },
                onConfirm = {
                    viewModel.updatePlaylistInfo(
                        uiState.editingPlaylistName,
                        uiState.editingPlaylistDescription
                    )
                },
                onDismiss = viewModel::toggleEditPlaylistDialog
            )
        }

        // Delete playlist dialog
        if (uiState.showDeletePlaylistDialog) {
            DeletePlaylistConfirmDialog(
                playlistName = uiState.currentPlaylist?.name ?: "",
                isDeleting = uiState.deletingPlaylist,
                onConfirm = viewModel::deleteCurrentPlaylist,
                onDismiss = viewModel::toggleDeletePlaylistDialog
            )
        }

        // Sort dialog
        if (uiState.showPlaylistSortDialog) {
            PlaylistSortDialog(
                currentSortOrder = uiState.playlistSongsSortOrder,
                currentAscending = uiState.playlistSongsSortAscending,
                canUseCustomOrder = uiState.canReorderSongs,
                onDismiss = viewModel::togglePlaylistSortDialog,
                onApply = { sortOrder, ascending ->
                    // TODO: viewModel.updateSortOrder(sortOrder, ascending)
                    viewModel.togglePlaylistSortDialog()
                }
            )
        }

        // Add songs bottom sheet
        if (uiState.showAddSongsBottomSheet) {
            AddSongsBottomSheet(
                availableSongs = uiState.filteredAvailableSongs,
                selectedSongs = uiState.selectedSongsToAdd,
                searchQuery = uiState.addSongsSearchQuery,
                isLoading = uiState.availableSongsLoading,
                isAdding = uiState.addingSongs,
                onSearchQueryChanged = { /* TODO: viewModel::searchAvailableSongs */ },
                onToggleSongSelection = { /* TODO: viewModel::toggleSongSelectionForAdding */ },
                onAddSelectedSongs = { /* TODO: viewModel::addSelectedSongsToPlaylist */ },
                onDismiss = viewModel::toggleAddSongsBottomSheet
            )
        }

        // Undo snackbar
        AnimatedVisibility(
            visible = uiState.showUndoSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            UndoSnackbar(
                message = uiState.undoMessage,
                onUndo = {
                    // TODO: Implement undo functionality
                    viewModel.dismissUndoSnackbar()
                },
                onDismiss = viewModel::dismissUndoSnackbar,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Error snackbar
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ErrorSnackbar(
                error = uiState.error ?: "",
                onDismiss = viewModel::clearError,
                onRetry = { viewModel.loadPlaylistDetail(playlistId) },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailTopBar(
    playlist: Playlist?,
    isSelectionMode: Boolean,
    selectedCount: Int,
    canEdit: Boolean,
    onNavigateBack: () -> Unit,
    onToggleOptions: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSelectionMode) {
                Text("$selectedCount selected")
            } else {
                Text(
                    text = playlist?.name ?: "Playlist",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Clear selection")
                }
            } else {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                }
                IconButton(onClick = { /* TODO: Remove selected */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove selected")
                }
            } else {
                if (canEdit) {
                    IconButton(onClick = onToggleSelectionMode) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Select mode")
                    }
                }
                IconButton(onClick = onToggleOptions) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
        }
    )
}

// Temporary placeholder components (will be replaced by actual components)
@Composable
private fun PlaylistSongsList(
    songs: List<Song>,
    isSelectionMode: Boolean,
    selectedSongs: Set<Long>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    favoriteSongs: Set<Long>,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    // This will be replaced by actual DraggableSongList or individual PlaylistSongItem components
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            count = songs.size,
            key = { index -> songs[index].id }
        ) { index ->
            val song = songs[index]
            PlaylistSongItem(
                song = song,
                index = index + 1,
                isSelected = selectedSongs.contains(song.id),
                isFavorite = favoriteSongs.contains(song.id),
                isCurrentlyPlaying = currentPlayingSong?.id == song.id,
                isPlaying = isPlaying,
                isSelectionMode = isSelectionMode,
                canReorder = false, // Will be determined by parent
                onClick = { onSongClick(song) },
                onLongClick = { onSongLongClick(song) },
                onToggleSelection = { onToggleSelection(song.id) },
                onToggleFavorite = { onToggleFavorite(song) },
                onMoreClick = { onMoreClick(song) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
private fun DeletePlaylistConfirmDialog(
    playlistName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Delete Playlist")
        },
        text = {
            Text("Are you sure you want to delete \"$playlistName\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PlaylistDetailLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading playlist...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaylistDetailErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Failed to load playlist",
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
}

@Composable
private fun UndoSnackbar(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.weight(1f)
            )

            Row {
                TextButton(onClick = onUndo) {
                    Text(
                        text = "UNDO",
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "DISMISS",
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
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

// BackHandler import
@Composable
private fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
