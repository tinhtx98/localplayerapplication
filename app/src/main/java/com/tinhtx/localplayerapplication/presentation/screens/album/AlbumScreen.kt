package com.tinhtx.localplayerapplication.presentation.screens.album

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
import com.tinhtx.localplayerapplication.presentation.screens.album.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var showSortDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopAppBar(
                title = uiState.album?.displayName ?: "Album",
                subtitle = uiState.album?.let { "${it.displayArtist} • ${it.year}" },
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    if (uiState.album != null) {
                        // Search in album
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                imageVector = if (uiState.isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearching) "Close search" else "Search in album"
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
                                    text = { Text("Play album") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.playAlbum()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Shuffle album") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shuffleAlbum()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shuffle, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add to queue") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.addAlbumToQueue()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.QueueMusic, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Go to artist") },
                                    onClick = {
                                        showMenu = false
                                        uiState.album?.let { album ->
                                            onNavigateToArtist(album.artistId)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add all to favorites") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.addAllToFavorites()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.FavoriteOutline, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share album") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shareAlbum()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.album != null && uiState.songs.isNotEmpty() && !uiState.isSearching) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.playAlbum()
                        onNavigateToPlayer()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = { Text("Play Album") },
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
                        message = "Loading album...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load album",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadAlbum() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.album == null -> {
                    AlbumNotFoundState(
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    AlbumContent(
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
                        onAddToPlaylistClick = { song ->
                            showAddToPlaylistDialog = song
                        },
                        onSearchQueryChange = { query ->
                            viewModel.updateSearchQuery(query)
                        },
                        onArtistClick = {
                            uiState.album?.let { album ->
                                onNavigateToArtist(album.artistId)
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showSortDialog) {
        AlbumSortDialog(
            currentSortOrder = uiState.sortOrder,
            onSortOrderChange = { sortOrder ->
                viewModel.updateSortOrder(sortOrder)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    showAddToPlaylistDialog?.let { song ->
        AddToPlaylistDialog(
            song = song,
            onPlaylistSelected = { playlistId ->
                viewModel.addSongToPlaylist(song, playlistId)
                showAddToPlaylistDialog = null
            },
            onCreateNewPlaylist = { playlistName ->
                viewModel.createPlaylistWithSong(song, playlistName)
                showAddToPlaylistDialog = null
            },
            onDismiss = { showAddToPlaylistDialog = null }
        )
    }
}

@Composable
private fun AlbumContent(
    uiState: AlbumUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onArtistClick: () -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    val album = uiState.album!!
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
        // Album header with cover art and info
        item {
            AlbumHeaderCard(
                album = album,
                totalDuration = uiState.totalDuration,
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
                onArtistClick = onArtistClick,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Search bar (when active)
        if (uiState.isSearching) {
            item {
                AlbumSearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
                )
            }
        }

        // Album tracks header
        if (displayedSongs.isNotEmpty()) {
            item {
                AlbumTracksHeader(
                    trackCount = displayedSongs.size,
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
                AlbumSongItem(
                    song = song,
                    trackNumber = song.trackNumber ?: (displayedSongs.indexOf(song) + 1),
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        } else {
            // Empty state
            item {
                if (uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                    AlbumSearchNoResults(
                        query = uiState.searchQuery,
                        albumName = album.displayName,
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    EmptyAlbumState(
                        albumName = album.displayName,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }

        // Album info section
        if (!uiState.isSearching) {
            item {
                AlbumInfoCard(
                    album = album,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }
    }
}

@Composable
private fun AlbumNotFoundState(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Album,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Album not found",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The album you're looking for doesn't exist or has been removed",
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
