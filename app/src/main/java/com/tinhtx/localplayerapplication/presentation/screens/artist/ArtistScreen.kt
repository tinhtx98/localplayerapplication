package com.tinhtx.localplayerapplication.presentation.screens.artist

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tinhtx.localplayerapplication.presentation.screens.artist.components.*

/**
 * Artist Screen - Complete artist detail interface
 * Maps 100% với ArtistViewModel và ArtistUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: Long? = null,
    artistName: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onNavigateToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Dialog states
    var showSortDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showMoreOptionsDialog by remember { mutableStateOf(false) }

    // Load artist data on first composition
    LaunchedEffect(artistId, artistName) {
        when {
            artistId != null -> viewModel.loadArtist(artistId)
            artistName != null -> viewModel.loadArtist(artistName)
        }
    }

    // Handle navigation side effects
    LaunchedEffect(uiState.selectedSongForPlayback) {
        uiState.selectedSongForPlayback?.let { song ->
            onNavigateToPlayer(song)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.selectedAlbumForNavigation) {
        uiState.selectedAlbumForNavigation?.let { album ->
            onNavigateToAlbum(album)
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

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshArtist,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    ArtistLoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasError -> {
                    ArtistErrorState(
                        error = uiState.currentError ?: "Unknown error",
                        onRetry = viewModel::refreshArtist,
                        onDismiss = viewModel::clearError,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.isEmpty -> {
                    EmptyArtistState(
                        artistName = uiState.artist?.name ?: "Unknown Artist",
                        onRefresh = viewModel::refreshArtist,
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
                        // Artist header card
                        item {
                            ArtistHeaderCard(
                                artist = uiState.artist!!,
                                artistStats = uiState.artistStats,
                                isArtistFavorite = uiState.isArtistFavorite,
                                isFollowing = uiState.isFollowing,
                                isPlaying = uiState.isPlaying,
                                currentPlayingSong = uiState.currentPlayingSong,
                                onNavigateBack = onNavigateBack,
                                onToggleArtistFavorite = viewModel::toggleArtistFavorite,
                                onToggleFollow = viewModel::toggleFollowArtist,
                                onMoreClick = viewModel::toggleMoreOptions,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Artist stats card
                        item {
                            ArtistStatsCard(
                                artistStats = uiState.artistStats,
                                onPlayAllSongs = viewModel::playAllSongs,
                                onShufflePlay = viewModel::shufflePlayAllSongs,
                                onPlayPopular = viewModel::playPopularSongs,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Tab navigation
                        item {
                            ArtistTabsHeader(
                                selectedTab = uiState.currentTab,
                                onTabSelected = viewModel::selectTab,
                                songsCount = uiState.songs.size,
                                albumsCount = uiState.albums.size,
                                popularCount = uiState.popularSongs.size,
                                isSelectionMode = uiState.isSelectionMode,
                                onToggleSelectionMode = viewModel::toggleSelectionMode,
                                onSortClick = { showSortDialog = true },
                                onSearchClick = { /* TODO: Implement search */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Selection mode toolbar
                        if (uiState.isSelectionMode) {
                            item {
                                SelectionModeToolbar(
                                    selectedCount = uiState.totalSelectedCount,
                                    canSelectAll = uiState.canSelectAll,
                                    allSelected = uiState.allSongsSelected,
                                    onSelectAll = viewModel::selectAllItems,
                                    onClearSelection = viewModel::clearSelection,
                                    onAddToFavorites = viewModel::addSelectedToFavorites,
                                    onAddToPlaylist = { showAddToPlaylistDialog = true },
                                    onPlaySelected = viewModel::playSelectedSongs,
                                    currentTab = uiState.currentTab,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Tab content
                        item {
                            when (uiState.currentTab) {
                                ArtistTab.SONGS -> {
                                    ArtistSongsSection(
                                        songs = uiState.filteredSongs,
                                        isSelectionMode = uiState.isSelectionMode,
                                        selectedSongs = uiState.selectedSongs,
                                        favoriteSongs = uiState.favoriteSongs,
                                        currentPlayingSong = uiState.currentPlayingSong,
                                        isPlaying = uiState.isPlaying,
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
                                        onToggleSelection = viewModel::toggleSongSelection,
                                        onToggleFavorite = viewModel::toggleSongFavorite,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                ArtistTab.ALBUMS -> {
                                    ArtistAlbumsSection(
                                        albums = uiState.filteredAlbums,
                                        isSelectionMode = uiState.isSelectionMode,
                                        selectedAlbums = uiState.selectedAlbums,
                                        onAlbumClick = { album ->
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleAlbumSelection(album.id)
                                            } else {
                                                viewModel.navigateToAlbum(album)
                                            }
                                        },
                                        onAlbumLongClick = { album ->
                                            if (!uiState.isSelectionMode) {
                                                viewModel.toggleSelectionMode()
                                            }
                                            viewModel.toggleAlbumSelection(album.id)
                                        },
                                        onToggleSelection = viewModel::toggleAlbumSelection,
                                        onPlayAlbum = viewModel::playAlbum,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                ArtistTab.POPULAR -> {
                                    ArtistSongsSection(
                                        songs = uiState.popularSongs,
                                        isSelectionMode = uiState.isSelectionMode,
                                        selectedSongs = uiState.selectedSongs,
                                        favoriteSongs = uiState.favoriteSongs,
                                        currentPlayingSong = uiState.currentPlayingSong,
                                        isPlaying = uiState.isPlaying,
                                        showTrackNumbers = false, // Popular songs don't need track numbers
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
                                        onToggleSelection = viewModel::toggleSongSelection,
                                        onToggleFavorite = viewModel::toggleSongFavorite,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        ArtistDialogs(
            showSortDialog = showSortDialog,
            onDismissSortDialog = { showSortDialog = false },
            showAddToPlaylistDialog = showAddToPlaylistDialog,
            onDismissAddToPlaylistDialog = { showAddToPlaylistDialog = false },
            showMoreOptionsDialog = showMoreOptionsDialog,
            onDismissMoreOptionsDialog = { showMoreOptionsDialog = false },
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
                onRetry = viewModel::refreshArtist,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ArtistTabsHeader(
    selectedTab: ArtistTab,
    onTabSelected: (ArtistTab) -> Unit,
    songsCount: Int,
    albumsCount: Int,
    popularCount: Int,
    isSelectionMode: Boolean,
    onToggleSelectionMode: () -> Unit,
    onSortClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Actions row
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = onSortClick) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = onToggleSelectionMode) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Select",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = selectedTab == ArtistTab.SONGS,
                    onClick = { onTabSelected(ArtistTab.SONGS) },
                    text = {
                        Text(
                            text = "Songs ($songsCount)",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
                
                Tab(
                    selected = selectedTab == ArtistTab.ALBUMS,
                    onClick = { onTabSelected(ArtistTab.ALBUMS) },
                    text = {
                        Text(
                            text = "Albums ($albumsCount)",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
                
                Tab(
                    selected = selectedTab == ArtistTab.POPULAR,
                    onClick = { onTabSelected(ArtistTab.POPULAR) },
                    text = {
                        Text(
                            text = "Popular ($popularCount)",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
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
    onAddToPlaylist: () -> Unit,
    onPlaySelected: () -> Unit,
    currentTab: ArtistTab,
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

            // Action buttons (only for songs)
            if (currentTab == ArtistTab.SONGS || currentTab == ArtistTab.POPULAR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
}

@Composable
private fun ArtistLoadingState(
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
            text = "Loading artist...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArtistErrorState(
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
            text = "Failed to load artist",
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
private fun ArtistDialogs(
    showSortDialog: Boolean,
    onDismissSortDialog: () -> Unit,
    showAddToPlaylistDialog: Boolean,
    onDismissAddToPlaylistDialog: () -> Unit,
    showMoreOptionsDialog: Boolean,
    onDismissMoreOptionsDialog: () -> Unit,
    uiState: ArtistUiState,
    viewModel: ArtistViewModel,
    onNavigateToPlaylist: () -> Unit
) {
    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = onDismissSortDialog,
            title = { Text("Sort by") },
            text = {
                Column {
                    Text("Sort options for ${uiState.currentTab.displayName}")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissSortDialog) {
                    Text("Close")
                }
            }
        )
    }

    // Add to playlist dialog
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = onDismissAddToPlaylistDialog,
            title = { Text("Add to Playlist") },
            text = {
                Column {
                    Text("Add ${uiState.totalSelectedCount} items to playlist")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToPlaylist) {
                        Text("Create New Playlist")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissAddToPlaylistDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // More options dialog
    if (showMoreOptionsDialog) {
        AlertDialog(
            onDismissRequest = onDismissMoreOptionsDialog,
            title = { Text("Artist Options") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            // TODO: Share artist
                            onDismissMoreOptionsDialog()
                        }
                    ) {
                        Text("Share Artist")
                    }
                    TextButton(
                        onClick = {
                            // TODO: View artist info
                            onDismissMoreOptionsDialog()
                        }
                    ) {
                        Text("View Info")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissMoreOptionsDialog) {
                    Text("Close")
                }
            }
        )
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
