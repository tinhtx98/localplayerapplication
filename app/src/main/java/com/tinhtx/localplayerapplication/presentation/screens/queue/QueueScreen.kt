package com.tinhtx.localplayerapplication.presentation.screens.queue

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
import com.tinhtx.localplayerapplication.presentation.screens.queue.components.*

/**
 * Queue Screen - Main queue interface
 * Maps với QueueViewModel và QueueUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToSong: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Handle navigation side effects
    LaunchedEffect(uiState.navigateToNowPlaying) {
        if (uiState.navigateToNowPlaying) {
            onNavigateToNowPlaying()
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.selectedSongForPlayback) {
        uiState.selectedSongForPlayback?.let { song ->
            onNavigateToSong(song)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.selectedAlbumForNavigation) {
        uiState.selectedAlbumForNavigation?.let { album ->
            onNavigateToAlbum(album)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.selectedArtistForNavigation) {
        uiState.selectedArtistForNavigation?.let { artist ->
            onNavigateToArtist(artist)
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
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.clearSelection()
    }

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshQueue,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top app bar
                QueueTopBar(
                    title = uiState.queueTitle,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedCount = uiState.selectedSongsCount,
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onNavigateBack = onNavigateBack,
                    onToggleSelection = viewModel::toggleSelectionMode,
                    onSelectAll = viewModel::selectAllSongs,
                    onClearSelection = viewModel::clearSelection,
                    onUndo = viewModel::undoLastOperation,
                    onRedo = viewModel::redoLastOperation,
                    onShowOptions = viewModel::toggleQueueOptionsMenu
                )

                // Search bar (when queue has songs)
                if (uiState.hasQueue) {
                    QueueSearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = viewModel::searchInQueue,
                        onClearSearch = viewModel::clearSearch,
                        isSearchActive = uiState.isSearchActive,
                        resultCount = uiState.searchResultsCount,
                        totalCount = uiState.queueSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Main content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        // Loading state
                        uiState.isLoading -> {
                            QueueLoadingState(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Error state
                        uiState.error != null -> {
                            QueueErrorState(
                                error = uiState.error,
                                onRetry = viewModel::refreshQueue,
                                onDismiss = viewModel::clearError,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Empty queue state
                        uiState.shouldShowEmptyState -> {
                            QueueEmptyStates(
                                onAddSongs = viewModel::toggleAddSongsBottomSheet,
                                onShuffleAll = { /* TODO: Shuffle all songs */ },
                                onBrowseMusic = { /* TODO: Navigate to browse */ },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Search empty state
                        uiState.shouldShowSearchEmptyState -> {
                            QueueSearchEmptyState(
                                searchQuery = uiState.searchQuery,
                                onClearSearch = viewModel::clearSearch,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Queue content
                        else -> {
                            QueueContent(
                                uiState = uiState,
                                onSongClick = { song ->
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSongSelection(song.id)
                                    } else {
                                        viewModel.playSong(song)
                                    }
                                },
                                onSongLongClick = { song ->
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleSongSelection(song.id)
                                },
                                onRemoveSong = viewModel::removeSongFromQueue,
                                onReorderSong = viewModel::reorderSong,
                                onToggleFavorite = viewModel::toggleSongFavorite,
                                onStartDragging = viewModel::startDragging,
                                onUpdateDropTarget = viewModel::updateDropTarget,
                                onEndDragging = viewModel::endDragging,
                                listState = listState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Mini player (when there's a current song)
                if (uiState.hasCurrentSong && uiState.showMiniPlayer) {
                    QueueMiniPlayer(
                        currentSong = uiState.currentSong!!,
                        isPlaying = uiState.isPlaying,
                        progress = uiState.progress,
                        onPlayPause = viewModel::togglePlayPause,
                        onSkipNext = viewModel::skipToNext,
                        onSkipPrevious = viewModel::skipToPrevious,
                        onClick = viewModel::navigateToNowPlaying,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Floating Action Buttons
        QueueFloatingActions(
            isVisible = uiState.hasQueue && !uiState.isSelectionMode,
            onAddSongs = viewModel::toggleAddSongsBottomSheet,
            onShowStats = viewModel::toggleQueueStatsDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Queue Actions (for selection mode)
        AnimatedVisibility(
            visible = uiState.isSelectionMode && uiState.hasSelectedSongs,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            QueueActions(
                selectedCount = uiState.selectedSongsCount,
                onPlaySelected = { /* TODO: Play selected songs */ },
                onRemoveSelected = viewModel::removeSelectedSongs,
                onAddToPlaylist = { /* TODO: Add to playlist */ },
                onClearSelection = viewModel::clearSelection,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Options menu
        if (uiState.showQueueOptionsMenu) {
            QueueOptionsMenu(
                canShuffle = uiState.hasQueue,
                canClear = uiState.hasQueue,
                canSave = uiState.hasQueue && uiState.canSaveQueue,
                isShuffleEnabled = uiState.isShuffleEnabled,
                repeatMode = uiState.repeatMode,
                onShuffle = viewModel::toggleShuffleMode,
                onRepeat = viewModel::toggleRepeatMode,
                onClear = viewModel::toggleClearQueueDialog,
                onSave = viewModel::toggleSaveQueueDialog,
                onShowStats = viewModel::toggleQueueStatsDialog,
                onToggleCompactView = viewModel::toggleCompactView,
                onDismiss = viewModel::toggleQueueOptionsMenu
            )
        }

        // Undo/Redo Snackbar
        AnimatedVisibility(
            visible = uiState.showUndoSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            UndoRedoSnackbar(
                message = uiState.undoMessage,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = viewModel::undoLastOperation,
                onRedo = viewModel::redoLastOperation,
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
                onRetry = viewModel::refreshQueue,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Dialogs and Bottom Sheets
    if (uiState.showClearQueueDialog) {
        ClearQueueDialog(
            queueSize = uiState.queueSize,
            onConfirm = {
                viewModel.clearQueue()
            },
            onDismiss = viewModel::toggleClearQueueDialog
        )
    }

    if (uiState.showSaveQueueDialog) {
        SaveQueueDialog(
            playlistName = uiState.newPlaylistName,
            queueSize = uiState.queueSize,
            isSaving = uiState.isSavingQueue,
            onNameChanged = viewModel::updateNewPlaylistName,
            onSave = { name ->
                viewModel.saveQueueAsPlaylist(name)
            },
            onDismiss = viewModel::toggleSaveQueueDialog
        )
    }

    if (uiState.showQueueStatsDialog) {
        QueueStatsDialog(
            stats = uiState.queueStats,
            onDismiss = viewModel::toggleQueueStatsDialog
        )
    }

    if (uiState.showAddSongsBottomSheet) {
        AddSongsBottomSheet(
            availableSongs = uiState.availableSongs,
            selectedSongs = uiState.selectedSongsToAdd,
            searchQuery = uiState.addSongsSearchQuery,
            isLoading = uiState.availableSongsLoading,
            isAdding = uiState.isAddingSongs,
            onSearchQueryChanged = viewModel::searchAvailableSongs,
            onToggleSongSelection = viewModel::toggleSongSelectionForAdding,
            onAddSelectedSongs = viewModel::addSelectedSongsToQueue,
            onDismiss = viewModel::toggleAddSongsBottomSheet
        )
    }
}

@Composable
private fun QueueContent(
    uiState: QueueUiState,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onReorderSong: (Int, Int) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onStartDragging: (Int) -> Unit,
    onUpdateDropTarget: (Int?) -> Unit,
    onEndDragging: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Queue header with stats
        if (uiState.hasQueue) {
            QueueHeaderCard(
                queueTitle = uiState.queueTitle,
                queueSubtitle = uiState.queueSubtitle,
                queueSource = uiState.queueSource,
                totalDuration = uiState.formattedTotalDuration,
                remainingDuration = uiState.formattedRemainingDuration,
                progressPercentage = uiState.progressPercentage,
                showStats = uiState.showQueueStats,
                onToggleStats = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Draggable queue list
        DraggableQueueList(
            songs = uiState.displayedSongs,
            currentSong = uiState.currentSong,
            isPlaying = uiState.isPlaying,
            favoriteSongs = uiState.favoriteSongs,
            selectedSongs = uiState.selectedSongs,
            isSelectionMode = uiState.isSelectionMode,
            compactView = uiState.compactView,
            showSongNumbers = uiState.showSongNumbers,
            canReorder = uiState.canReorderQueue && !uiState.isSearchActive,
            isDragging = uiState.isDragging,
            draggedIndex = uiState.draggedSongIndex,
            dropTargetIndex = uiState.dropTargetIndex,
            searchQuery = uiState.searchQuery,
            onSongClick = onSongClick,
            onSongLongClick = onSongLongClick,
            onToggleFavorite = onToggleFavorite,
            onRemoveSong = onRemoveSong,
            onStartDragging = onStartDragging,
            onUpdateDropTarget = onUpdateDropTarget,
            onEndDragging = onEndDragging,
            onReorderSong = onReorderSong,
            listState = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueTopBar(
    title: String,
    isSelectionMode: Boolean,
    selectedCount: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    onNavigateBack: () -> Unit,
    onToggleSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onShowOptions: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSelectionMode) {
                Text(
                    text = "$selectedCount selected",
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = if (isSelectionMode) onClearSelection else onNavigateBack) {
                Icon(
                    imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                    contentDescription = if (isSelectionMode) "Clear selection" else "Back"
                )
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Select all"
                    )
                }
            } else {
                // Undo/Redo buttons
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }

                IconButton(onClick = onShowOptions) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }
        }
    )
}

@Composable
private fun QueueLoadingState(
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
                text = "Loading queue...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QueueErrorState(
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
                text = "Queue Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
private fun QueueSearchEmptyState(
    searchQuery: String,
    onClearSearch: () -> Unit,
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
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No songs in queue match \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onClearSearch) {
                Text("Clear Search")
            }
        }
    }
}

@Composable
private fun QueueFloatingActions(
    isVisible: Boolean,
    onAddSongs: () -> Unit,
    onShowStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = onShowStats,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Queue stats"
                )
            }

            FloatingActionButton(
                onClick = onAddSongs
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add songs"
                )
            }
        }
    }
}

// Placeholder dialog components
@Composable
private fun ClearQueueDialog(
    queueSize: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ClearAll,
                contentDescription = null
            )
        },
        title = { Text("Clear Queue") },
        text = { Text("Remove all $queueSize songs from the queue?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SaveQueueDialog(
    playlistName: String,
    queueSize: Int,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = null
            )
        },
        title = { Text("Save Queue as Playlist") },
        text = {
            Column {
                Text("Save all $queueSize songs as a new playlist")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = onNameChanged,
                    label = { Text("Playlist name") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(playlistName) },
                enabled = !isSaving && playlistName.trim().isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun QueueStatsDialog(
    stats: QueueStats,
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
        title = { Text("Queue Statistics") },
        text = {
            QueueStats(
                stats = stats,
                showExtendedStats = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun QueueOptionsMenu(
    canShuffle: Boolean,
    canClear: Boolean,
    canSave: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onShowStats: () -> Unit,
    onToggleCompactView: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isShuffleEnabled) "Turn off shuffle" else "Shuffle queue")
                }
            },
            onClick = {
                onShuffle()
                onDismiss()
            },
            enabled = canShuffle
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Repeat: ${repeatMode.displayName}")
                }
            },
            onClick = {
                onRepeat()
                onDismiss()
            }
        )

        Divider()

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Queue statistics")
                }
            },
            onClick = {
                onShowStats()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewCompact,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Compact view")
                }
            },
            onClick = {
                onToggleCompactView()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Save as playlist")
                }
            },
            onClick = {
                onSave()
                onDismiss()
            },
            enabled = canSave
        )

        Divider()

        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Clear queue",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            onClick = {
                onClear()
                onDismiss()
            },
            enabled = canClear
        )
    }
}

@Composable
private fun UndoRedoSnackbar(
    message: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.weight(1f)
            )

            if (canUndo) {
                TextButton(onClick = onUndo) {
                    Text(
                        text = "Undo",
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    color = MaterialTheme.colorScheme.inversePrimary
                )
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

// Placeholder bottom sheet
@Composable
private fun AddSongsBottomSheet(
    availableSongs: List<Song>,
    selectedSongs: Set<Long>,
    searchQuery: String,
    isLoading: Boolean,
    isAdding: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onToggleSongSelection: (Long) -> Unit,
    onAddSelectedSongs: () -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Implement bottom sheet
    // This will be implemented in AddSongsViewModel.kt component
}
