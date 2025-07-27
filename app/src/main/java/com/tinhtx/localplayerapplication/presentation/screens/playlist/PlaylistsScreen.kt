package com.tinhtx.localplayerapplication.presentation.screens.playlist

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.tinhtx.localplayerapplication.presentation.screens.playlist.components.*

/**
 * Playlists Screen - Danh sách tất cả playlists
 * Maps với PlaylistViewModel và PlaylistUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlaylistDetail: (Playlist) -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Handle navigation side effects
    LaunchedEffect(uiState.selectedPlaylistForNavigation) {
        uiState.selectedPlaylistForNavigation?.let { playlist ->
            onNavigateToPlaylistDetail(playlist)
            viewModel.clearNavigationStates()
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
    BackHandler(enabled = uiState.playlistsSelectionMode) {
        viewModel.clearPlaylistsSelection()
    }

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshAll,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top app bar
                PlaylistsTopBar(
                    isSelectionMode = uiState.playlistsSelectionMode,
                    selectedCount = uiState.selectedPlaylistsCount,
                    onNavigateBack = onNavigateBack,
                    onCreatePlaylist = viewModel::toggleCreatePlaylistDialog,
                    onToggleSelectionMode = viewModel::togglePlaylistsSelectionMode,
                    onSelectAll = viewModel::selectAllPlaylists,
                    onClearSelection = viewModel::clearPlaylistsSelection,
                    onDeleteSelected = viewModel::deleteSelectedPlaylists
                )

                when {
                    uiState.playlistsLoading -> {
                        PlaylistsLoadingState(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.playlistsError != null -> {
                        PlaylistsErrorState(
                            error = uiState.playlistsError,
                            onRetry = viewModel::loadAllPlaylists,
                            onDismiss = viewModel::clearError,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.playlistsEmpty -> {
                        EmptyPlaylistState(
                            onCreatePlaylist = viewModel::toggleCreatePlaylistDialog,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.hasPlaylists -> {
                        // Search bar (only show if not in selection mode)
                        if (!uiState.playlistsSelectionMode) {
                            PlaylistSearchBar(
                                searchQuery = uiState.playlistsSearchQuery,
                                onSearchQueryChanged = viewModel::searchPlaylists,
                                isSearchActive = uiState.playlistsSearchActive,
                                onActiveChanged = { isActive ->
                                    if (!isActive) viewModel.clearPlaylistsSearch()
                                },
                                totalCount = uiState.playlists.size,
                                filteredCount = uiState.playlistsSearchResultsCount,
                                onSortClick = { /* TODO: Implement sort dialog */ },
                                onViewModeClick = { /* TODO: Implement view mode dialog */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Playlists content
                        when (uiState.playlistsViewMode) {
                            PlaylistsViewMode.LIST -> {
                                PlaylistsList(
                                    playlists = uiState.filteredPlaylists,
                                    isSelectionMode = uiState.playlistsSelectionMode,
                                    selectedPlaylists = uiState.selectedPlaylists,
                                    onPlaylistClick = { playlist ->
                                        if (uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistSelection(playlist.id)
                                        } else {
                                            viewModel.navigateToPlaylistDetail(playlist)
                                        }
                                    },
                                    onPlaylistLongClick = { playlist ->
                                        if (!uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistsSelectionMode()
                                        }
                                        viewModel.togglePlaylistSelection(playlist.id)
                                    },
                                    onToggleSelection = viewModel::togglePlaylistSelection,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                )
                            }

                            PlaylistsViewMode.GRID -> {
                                PlaylistsGrid(
                                    playlists = uiState.filteredPlaylists,
                                    isSelectionMode = uiState.playlistsSelectionMode,
                                    selectedPlaylists = uiState.selectedPlaylists,
                                    onPlaylistClick = { playlist ->
                                        if (uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistSelection(playlist.id)
                                        } else {
                                            viewModel.navigateToPlaylistDetail(playlist)
                                        }
                                    },
                                    onPlaylistLongClick = { playlist ->
                                        if (!uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistsSelectionMode()
                                        }
                                        viewModel.togglePlaylistSelection(playlist.id)
                                    },
                                    onToggleSelection = viewModel::togglePlaylistSelection,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                )
                            }

                            PlaylistsViewMode.COMPACT -> {
                                // TODO: Implement compact view
                                PlaylistsList(
                                    playlists = uiState.filteredPlaylists,
                                    isSelectionMode = uiState.playlistsSelectionMode,
                                    selectedPlaylists = uiState.selectedPlaylists,
                                    onPlaylistClick = { playlist ->
                                        if (uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistSelection(playlist.id)
                                        } else {
                                            viewModel.navigateToPlaylistDetail(playlist)
                                        }
                                    },
                                    onPlaylistLongClick = { playlist ->
                                        if (!uiState.playlistsSelectionMode) {
                                            viewModel.togglePlaylistsSelectionMode()
                                        }
                                        viewModel.togglePlaylistSelection(playlist.id)
                                    },
                                    onToggleSelection = viewModel::togglePlaylistSelection,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Create playlist dialog
        if (uiState.showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                playlistName = uiState.newPlaylistName,
                playlistDescription = uiState.newPlaylistDescription,
                isCreating = uiState.creatingPlaylist,
                onNameChanged = viewModel::updateNewPlaylistName,
                onDescriptionChanged = viewModel::updateNewPlaylistDescription,
                onConfirm = {
                    viewModel.createPlaylist(
                        uiState.newPlaylistName,
                        uiState.newPlaylistDescription
                    )
                },
                onDismiss = viewModel::toggleCreatePlaylistDialog
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
                onRetry = viewModel::refreshAll,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistsTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onNavigateBack: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onToggleSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSelectionMode) {
                Text("$selectedCount selected")
            } else {
                Text(
                    text = "Playlists",
                    fontWeight = FontWeight.Bold
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
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                }
            } else {
                IconButton(onClick = onToggleSelectionMode) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Select mode")
                }
                IconButton(onClick = onCreatePlaylist) {
                    Icon(Icons.Default.Add, contentDescription = "Create playlist")
                }
            }
        }
    )
}

@Composable
private fun PlaylistsList(
    playlists: List<Playlist>,
    isSelectionMode: Boolean,
    selectedPlaylists: Set<Long>,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistLongClick: (Playlist) -> Unit,
    onToggleSelection: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = playlists,
            key = { playlist -> playlist.id }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                isSelected = selectedPlaylists.contains(playlist.id),
                isSelectionMode = isSelectionMode,
                viewMode = PlaylistCardViewMode.LIST,
                onClick = { onPlaylistClick(playlist) },
                onLongClick = { onPlaylistLongClick(playlist) },
                onToggleSelection = { onToggleSelection(playlist.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
private fun PlaylistsGrid(
    playlists: List<Playlist>,
    isSelectionMode: Boolean,
    selectedPlaylists: Set<Long>,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistLongClick: (Playlist) -> Unit,
    onToggleSelection: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = playlists,
            key = { playlist -> playlist.id }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                isSelected = selectedPlaylists.contains(playlist.id),
                isSelectionMode = isSelectionMode,
                viewMode = PlaylistCardViewMode.GRID,
                onClick = { onPlaylistClick(playlist) },
                onLongClick = { onPlaylistLongClick(playlist) },
                onToggleSelection = { onToggleSelection(playlist.id) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
private fun PlaylistsLoadingState(
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
                text = "Loading playlists...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaylistsErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
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
                text = "Failed to load playlists",
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
