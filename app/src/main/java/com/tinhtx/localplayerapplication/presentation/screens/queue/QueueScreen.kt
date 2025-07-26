package com.tinhtx.localplayerapplication.presentation.screens.queue

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.ui.MusicTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.queue.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var showClearQueueDialog by remember { mutableStateOf(false) }
    var showSearchMode by remember { mutableStateOf(false) }

    // Auto scroll to current song when screen opens
    LaunchedEffect(uiState.currentSongIndex) {
        if (uiState.currentSongIndex >= 0 && uiState.queue.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentSongIndex)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadQueue()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopAppBar(
                title = if (showSearchMode) "Search Queue" else "Now Playing",
                subtitle = if (!showSearchMode && uiState.queue.isNotEmpty()) {
                    "${uiState.queue.size} songs • ${uiState.totalDuration}"
                } else null,
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    if (uiState.queue.isNotEmpty()) {
                        // Search toggle
                        IconButton(onClick = { 
                            showSearchMode = !showSearchMode
                            if (!showSearchMode) {
                                viewModel.clearSearch()
                            }
                        }) {
                            Icon(
                                imageVector = if (showSearchMode) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearchMode) "Close search" else "Search queue"
                            )
                        }

                        // More options
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options"
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Shuffle queue") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shuffleQueue()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shuffle, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Go to current") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.scrollToCurrentSong()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.MyLocation, contentDescription = null)
                                    },
                                    enabled = uiState.currentSongIndex >= 0
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Save as playlist") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.saveQueueAsPlaylist()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share queue") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shareQueue()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Clear queue", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showClearQueueDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenLoadingIndicator(
                        message = "Loading queue...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load queue",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadQueue() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.queue.isEmpty() -> {
                    EmptyQueueState(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    QueueContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        listState = listState,
                        showSearchMode = showSearchMode,
                        onSongClick = { song, index ->
                            viewModel.skipToSong(index)
                            onNavigateToPlayer()
                        },
                        onRemoveFromQueue = { index ->
                            viewModel.removeFromQueue(index)
                        },
                        onMoveItem = { fromIndex, toIndex ->
                            viewModel.moveQueueItem(fromIndex, toIndex)
                        },
                        onSearchQueryChange = { query ->
                            viewModel.updateSearchQuery(query)
                        },
                        onToggleFavorite = { song ->
                            viewModel.toggleFavorite(song)
                        }
                    )
                }
            }
        }
    }

    // Clear queue dialog
    if (showClearQueueDialog) {
        ConfirmationDialog(
            title = "Clear Queue",
            message = "Are you sure you want to clear the entire queue? This will stop playback and remove all ${uiState.queue.size} songs.",
            onConfirm = {
                viewModel.clearQueue()
                showClearQueueDialog = false
                onNavigateBack()
            },
            onDismiss = { showClearQueueDialog = false },
            confirmText = "Clear",
            dismissText = "Cancel",
            isDestructive = true
        )
    }
}

@Composable
private fun QueueContent(
    uiState: QueueUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    showSearchMode: Boolean,
    onSongClick: (Song, Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    val displayedQueue = if (uiState.searchQuery.isBlank()) {
        uiState.queue
    } else {
        uiState.filteredQueue
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        AnimatedVisibility(
            visible = showSearchMode,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            QueueSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
            )
        }

        // Queue header with stats
        if (displayedQueue.isNotEmpty()) {
            QueueHeaderCard(
                currentSong = uiState.currentSong,
                queueSize = displayedQueue.size,
                totalDuration = uiState.totalDuration,
                repeatMode = uiState.repeatMode,
                shuffleMode = uiState.shuffleMode,
                isSearching = showSearchMode,
                searchQuery = uiState.searchQuery,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Queue list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = displayedQueue,
                key = { "${it.id}_${displayedQueue.indexOf(it)}" }
            ) { song ->
                val originalIndex = uiState.queue.indexOf(song)
                val isCurrentSong = originalIndex == uiState.currentSongIndex
                val isPlaying = isCurrentSong && uiState.isPlaying

                QueueSongItem(
                    song = song,
                    position = displayedQueue.indexOf(song) + 1,
                    isCurrentSong = isCurrentSong,
                    isPlaying = isPlaying,
                    onClick = { onSongClick(song, originalIndex) },
                    onRemoveFromQueue = { onRemoveFromQueue(originalIndex) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }

            // Search results info
            if (showSearchMode && uiState.searchQuery.isNotBlank()) {
                item {
                    QueueSearchResults(
                        query = uiState.searchQuery,
                        resultCount = displayedQueue.size,
                        totalCount = uiState.queue.size,
                        modifier = Modifier.padding(horizontalPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQueueState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Queue is empty",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start playing songs to see them in your queue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { /* Navigate to library or home */ },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Music")
        }
    }
}
