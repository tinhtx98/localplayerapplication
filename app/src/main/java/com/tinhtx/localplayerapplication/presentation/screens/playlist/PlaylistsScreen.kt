package com.tinhtx.localplayerapplication.presentation.screens.playlist

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
import com.tinhtx.localplayerapplication.presentation.components.music.*
import com.tinhtx.localplayerapplication.presentation.components.ui.MusicTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.playlist.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddSongsBottomSheet by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopAppBar(
                title = uiState.playlist?.name ?: "Playlist",
                subtitle = uiState.playlist?.let { "${it.songCount} songs • ${it.formattedDuration}" },
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    if (uiState.playlist != null) {
                        // Search in playlist
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                imageVector = if (uiState.isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearching) "Close search" else "Search in playlist"
                            )
                        }

                        // Sort songs
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort songs"
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
                                    text = { Text("Play all") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.playAllSongs()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Shuffle play") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shufflePlaylist()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shuffle, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add songs") },
                                    onClick = {
                                        showMenu = false
                                        showAddSongsBottomSheet = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Edit playlist") },
                                    onClick = {
                                        showMenu = false
                                        showEditDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share playlist") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.sharePlaylist()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.exportPlaylist()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Delete playlist", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
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
        },
        floatingActionButton = {
            if (uiState.playlist != null && uiState.songs.isNotEmpty() && !uiState.isSearching) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.playAllSongs()
                        onNavigateToPlayer()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = { Text("Play All") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
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
                        message = "Loading playlist...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load playlist",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadPlaylist() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.playlist == null -> {
                    PlaylistNotFoundState(
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PlaylistContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        listState = listState,
                        onSongClick = { song ->
                            viewModel.playSong(song)
                            onNavigateToPlayer()
                        },
                        onFavoriteClick = { song ->
                            viewModel.toggleFavorite(song)
                        },
                        onRemoveFromPlaylistClick = { song ->
                            viewModel.removeSongFromPlaylist(song)
                        },
                        onSearchQueryChange = { query ->
                            viewModel.updateSearchQuery(query)
                        },
                        onAddSongsClick = {
                            showAddSongsBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    // Dialogs and Bottom Sheets
    if (showEditDialog && uiState.playlist != null) {
        EditPlaylistDialog(
            playlist = uiState.playlist,
            onSave = { name, description, coverUri ->
                viewModel.updatePlaylist(name, description, coverUri)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Playlist",
            message = "Are you sure you want to delete \"${uiState.playlist?.name}\"? This action cannot be undone.",
            onConfirm = {
                viewModel.deletePlaylist()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false },
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true
        )
    }

    if (showAddSongsBottomSheet) {
        AddSongsBottomSheet(
            currentPlaylistSongs = uiState.songs,
            onSongsSelected = { songs ->
                viewModel.addSongsToPlaylist(songs)
                showAddSongsBottomSheet = false
            },
            onDismiss = { showAddSongsBottomSheet = false }
        )
    }

    if (showSortDialog) {
        PlaylistSortDialog(
            currentSortOrder = uiState.sortOrder,
            onSortOrderChange = { sortOrder ->
                viewModel.updateSortOrder(sortOrder)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
private fun PlaylistContent(
    uiState: PlaylistUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onRemoveFromPlaylistClick: (Song) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddSongsClick: () -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    val playlist = uiState.playlist!!
    val displayedSongs = if (uiState.searchQuery.isBlank()) {
        uiState.songs
    } else {
        uiState.filteredSongs
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Playlist header with cover art and info
        item {
            PlaylistHeaderCard(
                playlist = playlist,
                onPlayClick = {
                    if (uiState.songs.isNotEmpty()) {
                        onSongClick(uiState.songs.first())
                    }
                },
                onShuffleClick = {
                    if (uiState.songs.isNotEmpty()) {
                        onSongClick(uiState.songs.random())
                    }
                },
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Search bar (when active)
        if (uiState.isSearching) {
            item {
                PlaylistSearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
                )
            }
        }

        // Songs section header
        if (displayedSongs.isNotEmpty()) {
            item {
                PlaylistSongsHeader(
                    songCount = displayedSongs.size,
                    totalDuration = uiState.totalDuration,
                    isSearching = uiState.isSearching,
                    searchQuery = uiState.searchQuery,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            // Songs list
            items(
                items = displayedSongs,
                key = { it.id }
            ) { song ->
                PlaylistSongItem(
                    song = song,
                    position = displayedSongs.indexOf(song) + 1,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onRemoveFromPlaylistClick = { onRemoveFromPlaylistClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        } else {
            // Empty state
            item {
                if (uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                    PlaylistSearchNoResults(
                        query = uiState.searchQuery,
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    EmptyPlaylistState(
                        onAddSongsClick = onAddSongsClick,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistNotFoundState(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlaylistRemove,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Playlist not found",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The playlist you're looking for doesn't exist or has been deleted",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Go Back")
        }
    }
}
