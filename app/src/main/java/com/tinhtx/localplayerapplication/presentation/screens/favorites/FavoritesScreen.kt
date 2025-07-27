package com.tinhtx.localplayerapplication.presentation.screens.favorites

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.screens.favorites.components.*

/**
 * Favorites Screen - Complete favorites interface
 * Maps 100% với FavoritesViewModel và FavoritesUiState
 * Uses correct components: FavoriteSongItem, FavoritesSearchBar, FavoritesStatsHeader
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    onNavigateToPlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Dialog states
    var showSortDialog by remember { mutableStateOf(false) }
    var showViewModeDialog by remember { mutableStateOf(false) }
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }

    // Load favorites on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAllFavorites()
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

    LaunchedEffect(uiState.selectedArtistForNavigation) {
        uiState.selectedArtistForNavigation?.let { artist ->
            onNavigateToArtist(artist)
            viewModel.clearNavigationStates()
        }
    }

    LaunchedEffect(uiState.selectedPlaylistForNavigation) {
        uiState.selectedPlaylistForNavigation?.let { playlist ->
            onNavigateToPlaylist(playlist)
            viewModel.clearNavigationStates()
        }
    }

    // Handle dialog states from ViewModel
    LaunchedEffect(uiState.showSortDialog) {
        showSortDialog = uiState.showSortDialog
    }

    LaunchedEffect(uiState.showRemoveConfirmDialog) {
        showRemoveConfirmDialog = uiState.showRemoveConfirmDialog
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
            onRefresh = viewModel::refreshFavorites,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    FavoritesLoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasError -> {
                    FavoritesErrorState(
                        error = uiState.currentError ?: "Unknown error",
                        onRetry = viewModel::refreshFavorites,
                        onDismiss = viewModel::clearError,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.isEmpty -> {
                    EmptyFavoritesState(
                        onNavigateBack = onNavigateBack,
                        onExploreMusic = { /* TODO: Navigate to library */ },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasData -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // Space for mini player
                    ) {
                        // Top bar with back button
                        item {
                            TopAppBar(
                                title = { 
                                    Text(
                                        text = "Favorites",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                navigationIcon = {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Favorites stats header
                        item {
                            FavoritesStatsHeader(
                                favoritesStats = uiState.favoritesStats,
                                isLoading = uiState.isCurrentTabLoading,
                                onPlayAllFavorites = viewModel::playAllFavorites,
                                onShufflePlayFavorites = viewModel::shufflePlayFavorites,
                                onToggleSelectionMode = viewModel::toggleSelectionMode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Search bar (only show if not in selection mode)
                        if (!uiState.isSelectionMode) {
                            item {
                                FavoritesSearchBar(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChanged = viewModel::searchFavorites,
                                    isSearchActive = uiState.isSearchActive,
                                    onActiveChanged = { isActive ->
                                        if (!isActive) viewModel.clearSearch()
                                    },
                                    totalCount = uiState.favoriteSongs.size,
                                    filteredCount = uiState.searchResultsCount,
                                    onSortClick = { showSortDialog = true },
                                    onViewModeClick = { showViewModeDialog = true },
                                    placeholder = "Search your favorites...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }

                        // Tabs (if needed for multiple favorite types)
                        if (!uiState.isSelectionMode && uiState.hasData) {
                            item {
                                FavoritesTabsRow(
                                    selectedTab = uiState.selectedTab,
                                    onTabSelected = viewModel::selectTab,
                                    songsCount = uiState.favoriteSongs.size,
                                    albumsCount = uiState.favoriteAlbums.size,
                                    artistsCount = uiState.favoriteArtists.size,
                                    playlistsCount = uiState.favoritePlaylists.size,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Selection mode toolbar
                        if (uiState.isSelectionMode) {
                            item {
                                SelectionModeToolbar(
                                    selectedCount = uiState.selectedItemsCount,
                                    canSelectAll = uiState.canSelectAll,
                                    allSelected = uiState.allItemsSelected,
                                    onSelectAll = viewModel::selectAllItems,
                                    onClearSelection = viewModel::clearSelection,
                                    onRemoveFromFavorites = { showRemoveConfirmDialog = true },
                                    onPlaySelected = viewModel::playSelectedSongs,
                                    currentTab = uiState.selectedTab,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Favorites content based on selected tab
                        when (uiState.selectedTab) {
                            FavoritesTab.SONGS -> {
                                // Favorite songs list using FavoriteSongItem
                                items(
                                    items = uiState.filteredSongs,
                                    key = { song -> song.id }
                                ) { song ->
                                    FavoriteSongItem(
                                        song = song,
                                        isSelected = uiState.isSongSelected(song.id),
                                        isCurrentlyPlaying = uiState.isCurrentlyPlaying(song),
                                        isPlaying = uiState.isPlaying,
                                        isSelectionMode = uiState.isSelectionMode,
                                        showAlbumArt = true,
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
                                            viewModel.removeSongFromFavorites(song)
                                        },
                                        onMoreClick = {
                                            selectedSongForOptions = song
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .animateItemPlacement()
                                    )
                                }

                                // Empty songs state
                                if (uiState.filteredSongs.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                                    item {
                                        EmptySearchResultsState(
                                            searchQuery = uiState.searchQuery,
                                            onClearSearch = { viewModel.clearSearch() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp)
                                        )
                                    }
                                }
                            }

                            FavoritesTab.ALBUMS -> {
                                // Favorite albums (simplified for now)
                                items(
                                    items = uiState.filteredAlbums,
                                    key = { album -> album.id }
                                ) { album ->
                                    FavoriteAlbumItem(
                                        album = album,
                                        isSelected = uiState.isAlbumSelected(album.id),
                                        isSelectionMode = uiState.isSelectionMode,
                                        onClick = {
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleAlbumSelection(album.id)
                                            } else {
                                                viewModel.navigateToAlbum(album)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isSelectionMode) {
                                                viewModel.toggleSelectionMode()
                                            }
                                            viewModel.toggleAlbumSelection(album.id)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .animateItemPlacement()
                                    )
                                }
                            }

                            FavoritesTab.ARTISTS -> {
                                // Favorite artists (simplified for now)
                                items(
                                    items = uiState.filteredArtists,
                                    key = { artist -> artist.id }
                                ) { artist ->
                                    FavoriteArtistItem(
                                        artist = artist,
                                        isSelected = uiState.isArtistSelected(artist.id),
                                        isSelectionMode = uiState.isSelectionMode,
                                        onClick = {
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleArtistSelection(artist.id)
                                            } else {
                                                viewModel.navigateToArtist(artist)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isSelectionMode) {
                                                viewModel.toggleSelectionMode()
                                            }
                                            viewModel.toggleArtistSelection(artist.id)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .animateItemPlacement()
                                    )
                                }
                            }

                            FavoritesTab.PLAYLISTS -> {
                                // Favorite playlists (simplified for now)
                                items(
                                    items = uiState.filteredPlaylists,
                                    key = { playlist -> playlist.id }
                                ) { playlist ->
                                    FavoritePlaylistItem(
                                        playlist = playlist,
                                        isSelected = uiState.isPlaylistSelected(playlist.id),
                                        isSelectionMode = uiState.isSelectionMode,
                                        onClick = {
                                            if (uiState.isSelectionMode) {
                                                viewModel.togglePlaylistSelection(playlist.id)
                                            } else {
                                                viewModel.navigateToPlaylist(playlist)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isSelectionMode) {
                                                viewModel.toggleSelectionMode()
                                            }
                                            viewModel.togglePlaylistSelection(playlist.id)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        FavoritesDialogs(
            showSortDialog = showSortDialog,
            onDismissSortDialog = { showSortDialog = false },
            showViewModeDialog = showViewModeDialog,
            onDismissViewModeDialog = { showViewModeDialog = false },
            showRemoveConfirmDialog = showRemoveConfirmDialog,
            onDismissRemoveConfirmDialog = { showRemoveConfirmDialog = false },
            selectedSongForOptions = selectedSongForOptions,
            onDismissSongOptions = { selectedSongForOptions = null },
            uiState = uiState,
            viewModel = viewModel
        )

        // Undo snackbar
        AnimatedVisibility(
            visible = uiState.showUndoSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            UndoSnackbar(
                message = "Removed from favorites",
                onUndo = viewModel::undoRemoveFromFavorites,
                onDismiss = viewModel::dismissUndoSnackbar,
                modifier = Modifier.padding(16.dp)
            )
        }

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
                onRetry = viewModel::refreshFavorites,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun FavoritesTabsRow(
    selectedTab: FavoritesTab,
    onTabSelected: (FavoritesTab) -> Unit,
    songsCount: Int,
    albumsCount: Int,
    artistsCount: Int,
    playlistsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == FavoritesTab.SONGS,
                onClick = { onTabSelected(FavoritesTab.SONGS) },
                text = {
                    Text(
                        text = "Songs ($songsCount)",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
            
            Tab(
                selected = selectedTab == FavoritesTab.ALBUMS,
                onClick = { onTabSelected(FavoritesTab.ALBUMS) },
                text = {
                    Text(
                        text = "Albums ($albumsCount)",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
            
            Tab(
                selected = selectedTab == FavoritesTab.ARTISTS,
                onClick = { onTabSelected(FavoritesTab.ARTISTS) },
                text = {
                    Text(
                        text = "Artists ($artistsCount)",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
            
            Tab(
                selected = selectedTab == FavoritesTab.PLAYLISTS,
                onClick = { onTabSelected(FavoritesTab.PLAYLISTS) },
                text = {
                    Text(
                        text = "Playlists ($playlistsCount)",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
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
    onRemoveFromFavorites: () -> Unit,
    onPlaySelected: () -> Unit,
    currentTab: FavoritesTab,
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
                // Play selected (only for songs)
                if (currentTab == FavoritesTab.SONGS) {
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
                }

                // Remove from favorites
                OutlinedButton(
                    onClick = onRemoveFromFavorites,
                    modifier = Modifier.weight(1f),
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove")
                }
            }
        }
    }
}

// Simplified favorite item components for other tabs
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FavoriteAlbumItem(
    album: Album,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${album.artist} • ${album.songCount} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FavoriteArtistItem(
    artist: Artist,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${artist.songCount} songs • ${artist.albumCount} albums",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FavoritePlaylistItem(
    playlist: Playlist,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Icon(
                imageVector = Icons.Default.PlaylistPlay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${playlist.songCount} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FavoritesLoadingState(
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
            text = "Loading favorites...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FavoritesErrorState(
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
            text = "Failed to load favorites",
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
private fun EmptyFavoritesState(
    onNavigateBack: () -> Unit,
    onExploreMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Favorites Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start adding songs, albums, artists, and playlists to your favorites to see them here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onExploreMusic,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explore Music")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Go Back")
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
            text = "No favorites found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "No favorites match \"$searchQuery\"",
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

@Composable
private fun FavoritesDialogs(
    showSortDialog: Boolean,
    onDismissSortDialog: () -> Unit,
    showViewModeDialog: Boolean,
    onDismissViewModeDialog: () -> Unit,
    showRemoveConfirmDialog: Boolean,
    onDismissRemoveConfirmDialog: () -> Unit,
    selectedSongForOptions: Song?,
    onDismissSongOptions: () -> Unit,
    uiState: FavoritesUiState,
    viewModel: FavoritesViewModel
) {
    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = onDismissSortDialog,
            title = { Text("Sort favorites") },
            text = {
                Column {
                    Text("Sort your favorites by different criteria")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissSortDialog) {
                    Text("Close")
                }
            }
        )
    }

    // View mode dialog
    if (showViewModeDialog) {
        AlertDialog(
            onDismissRequest = onDismissViewModeDialog,
            title = { Text("View mode") },
            text = {
                Column {
                    Text("Choose how to display your favorites")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissViewModeDialog) {
                    Text("Close")
                }
            }
        )
    }

    // Remove confirmation dialog
    if (showRemoveConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissRemoveConfirmDialog,
            title = { Text("Remove from favorites") },
            text = {
                Text("Remove ${uiState.selectedItemsCount} items from favorites?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeSelectedFromFavorites()
                        onDismissRemoveConfirmDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRemoveConfirmDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // Song options dialog
    if (selectedSongForOptions != null) {
        AlertDialog(
            onDismissRequest = onDismissSongOptions,
            title = { Text(selectedSongForOptions.title) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.removeSongFromFavorites(selectedSongForOptions)
                            onDismissSongOptions()
                        }
                    ) {
                        Text("Remove from favorites")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissSongOptions) {
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
