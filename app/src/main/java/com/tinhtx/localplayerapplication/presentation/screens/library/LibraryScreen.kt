package com.tinhtx.localplayerapplication.presentation.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tinhtx.localplayerapplication.presentation.screens.library.components.*

/**
 * Library Screen - Complete music library interface
 * Maps 100% với LibraryViewModel và LibraryUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    onNavigateToPlaylist: (Playlist) -> Unit,
    onNavigateToPlaylistDetail: (Playlist) -> Unit,
    onNavigateToGenre: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle navigation side effects
    LaunchedEffect(uiState.selectedSongForPlayback) {
        uiState.selectedSongForPlayback?.let { song ->
            onNavigateToPlayer(song)
            viewModel.clearNavigationSelections()
        }
    }

    LaunchedEffect(uiState.selectedAlbumForNavigation) {
        uiState.selectedAlbumForNavigation?.let { album ->
            onNavigateToAlbum(album)
            viewModel.clearNavigationSelections()
        }
    }

    LaunchedEffect(uiState.selectedArtistForNavigation) {
        uiState.selectedArtistForNavigation?.let { artist ->
            onNavigateToArtist(artist)
            viewModel.clearNavigationSelections()
        }
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
            onRefresh = viewModel::refreshCurrentTab,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Library tabs header với actions
                LibraryTabsHeader(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::selectTab,
                    viewMode = uiState.viewMode,
                    onViewModeChanged = viewModel::updateViewMode,
                    onSortClick = { showSortDialog = true },
                    onFilterClick = { showFilterDialog = true },
                    onScanClick = viewModel::scanLibrary,
                    isScanning = uiState.isScanningLibrary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Search bar
                AnimatedVisibility(
                    visible = !uiState.isSelectionMode,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    LibrarySearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = viewModel::searchLibrary,
                        isSearchActive = uiState.isSearchActive,
                        onActiveChanged = { isActive ->
                            if (!isActive) viewModel.clearSearch()
                        },
                        searchResultsCount = uiState.searchResultsCount,
                        isSearching = uiState.searchLoading,
                        placeholder = getSearchPlaceholder(uiState.selectedTab),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                // Library stats overview
                AnimatedVisibility(
                    visible = !uiState.isSearchActive && !uiState.isSelectionMode && uiState.selectedTab == LibraryTab.SONGS,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    LibraryStatsOverview(
                        stats = uiState.libraryStats,
                        isLoading = uiState.statsLoading,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Selection mode toolbar
                AnimatedVisibility(
                    visible = uiState.isSelectionMode,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    SelectionModeToolbar(
                        selectedCount = uiState.selectedItemsCount,
                        canSelectAll = uiState.canSelectAll,
                        allSelected = uiState.allItemsSelected,
                        onSelectAll = viewModel::selectAllItems,
                        onClearSelection = viewModel::clearSelection,
                        onAddToFavorites = viewModel::addSelectedToFavorites,
                        onRemoveFromFavorites = viewModel::removeSelectedFromFavorites,
                        onAddToPlaylist = { showAddToPlaylistDialog = true },
                        onDelete = { showDeleteConfirmDialog = true },
                        canPerformBatchOperations = viewModel.canPerformBatchOperations(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Main content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        uiState.isLoading -> {
                            LibraryLoadingState(
                                selectedTab = uiState.selectedTab,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.hasError -> {
                            LibraryErrorState(
                                error = uiState.currentError ?: "Unknown error",
                                onRetry = { viewModel.refreshCurrentTab() },
                                onDismiss = viewModel::clearError,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.isEmpty -> {
                            EmptyLibraryTabState(
                                selectedTab = uiState.selectedTab,
                                onScanLibrary = viewModel::scanLibrary,
                                onCreatePlaylist = if (uiState.selectedTab == LibraryTab.PLAYLISTS) {
                                    { showCreatePlaylistDialog = true }
                                } else null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            LibraryTabContent(
                                uiState = uiState,
                                onSongClick = viewModel::playSong,
                                onSongLongClick = { song ->
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleItemSelection(song.id)
                                },
                                onAlbumClick = viewModel::navigateToAlbum,
                                onAlbumLongClick = { album ->
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleItemSelection(album.id)
                                },
                                onArtistClick = viewModel::navigateToArtist,
                                onArtistLongClick = { artist ->
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleItemSelection(artist.id)
                                },
                                onPlaylistClick = { playlist ->
                                    onNavigateToPlaylistDetail(playlist)
                                },
                                onPlaylistLongClick = { playlist ->
                                    if (!uiState.isSelectionMode) {
                                        viewModel.toggleSelectionMode()
                                    }
                                    viewModel.toggleItemSelection(playlist.id)
                                },
                                onGenreClick = { genre ->
                                    onNavigateToGenre(genre)
                                },
                                onToggleSelection = viewModel::toggleItemSelection,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onPlaySong = { song ->
                                    val songs = when (uiState.selectedTab) {
                                        LibraryTab.SONGS -> uiState.filteredSongs
                                        else -> listOf(song)
                                    }
                                    val index = songs.indexOf(song)
                                    // TODO: Create queue and play
                                    viewModel.playSong(song)
                                },
                                onPlayAlbum = { album ->
                                    val songs = viewModel.getSongsByAlbum(album.name)
                                    if (songs.isNotEmpty()) {
                                        // TODO: Create queue and play first song
                                        viewModel.playSong(songs.first())
                                    }
                                },
                                onPlayArtist = { artist ->
                                    val songs = viewModel.getSongsByArtist(artist.name)
                                    if (songs.isNotEmpty()) {
                                        // TODO: Create queue and play first song
                                        viewModel.playSong(songs.first())
                                    }
                                },
                                onPlayPlaylist = { playlist ->
                                    onNavigateToPlaylistDetail(playlist)
                                },
                                onPlayGenre = { genre ->
                                    val songs = viewModel.getSongsByGenre(genre)
                                    if (songs.isNotEmpty()) {
                                        // TODO: Create queue and play first song
                                        viewModel.playSong(songs.first())
                                    }
                                },
                                onCreatePlaylist = { showCreatePlaylistDialog = true },
                                getSongCountForGenre = viewModel::getSongsByGenre,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Scanning progress indicator
                    AnimatedVisibility(
                        visible = uiState.isScanningLibrary,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        ScanningProgressIndicator(
                            progress = uiState.scanProgress,
                            statusMessage = uiState.scanStatusMessage,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Dialogs
        LibraryDialogs(
            uiState = uiState,
            viewModel = viewModel,
            showSortDialog = showSortDialog,
            onDismissSortDialog = { showSortDialog = false },
            showFilterDialog = showFilterDialog,
            onDismissFilterDialog = { showFilterDialog = false },
            showCreatePlaylistDialog = showCreatePlaylistDialog,
            onDismissCreatePlaylistDialog = { showCreatePlaylistDialog = false },
            showAddToPlaylistDialog = showAddToPlaylistDialog,
            onDismissAddToPlaylistDialog = { showAddToPlaylistDialog = false },
            showDeleteConfirmDialog = showDeleteConfirmDialog,
            onDismissDeleteConfirmDialog = { showDeleteConfirmDialog = false }
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
                onRetry = { viewModel.refreshCurrentTab() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // State variables for dialogs
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
}

@Composable
private fun LibraryTabContent(
    uiState: LibraryUiState,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onArtistLongClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistLongClick: (Playlist) -> Unit,
    onGenreClick: (String) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onPlaySong: (Song) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayArtist: (Artist) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onPlayGenre: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    getSongCountForGenre: (String) -> List<Song>,
    modifier: Modifier = Modifier
) {
    when (uiState.selectedTab) {
        LibraryTab.SONGS -> {
            SongsTab(
                songs = uiState.filteredSongs,
                viewMode = uiState.viewMode,
                showAlbumArt = uiState.showAlbumArt,
                isSelectionMode = uiState.isSelectionMode,
                selectedItems = uiState.selectedItems,
                favoriteSongs = uiState.favoriteSongs,
                onSongClick = onSongClick,
                onSongLongClick = onSongLongClick,
                onToggleSelection = onToggleSelection,
                onToggleFavorite = onToggleFavorite,
                modifier = modifier
            )
        }

        LibraryTab.ALBUMS -> {
            AlbumsTab(
                albums = uiState.filteredAlbums,
                gridSize = uiState.gridSize,
                isSelectionMode = uiState.isSelectionMode,
                selectedItems = uiState.selectedItems,
                onAlbumClick = onAlbumClick,
                onAlbumLongClick = onAlbumLongClick,
                onToggleSelection = onToggleSelection,
                onPlayAlbum = onPlayAlbum,
                modifier = modifier
            )
        }

        LibraryTab.ARTISTS -> {
            ArtistsTab(
                artists = uiState.filteredArtists,
                isSelectionMode = uiState.isSelectionMode,
                selectedItems = uiState.selectedItems,
                onArtistClick = onArtistClick,
                onArtistLongClick = onArtistLongClick,
                onToggleSelection = onToggleSelection,
                onPlayArtist = onPlayArtist,
                modifier = modifier
            )
        }

        LibraryTab.PLAYLISTS -> {
            PlaylistsTab(
                playlists = uiState.filteredPlaylists,
                gridSize = uiState.gridSize,
                isSelectionMode = uiState.isSelectionMode,
                selectedItems = uiState.selectedItems,
                onPlaylistClick = onPlaylistClick,
                onPlaylistLongClick = onPlaylistLongClick,
                onToggleSelection = onToggleSelection,
                onPlayPlaylist = onPlayPlaylist,
                onCreatePlaylist = onCreatePlaylist,
                modifier = modifier
            )
        }

        LibraryTab.GENRES -> {
            GenresTab(
                genres = uiState.genres.filter { genre ->
                    if (uiState.searchQuery.isBlank()) true
                    else genre.contains(uiState.searchQuery, ignoreCase = true)
                },
                onGenreClick = onGenreClick,
                onPlayGenre = onPlayGenre,
                getSongCountForGenre = { genre -> getSongCountForGenre(genre).size },
                modifier = modifier
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
    onDelete: () -> Unit,
    canPerformBatchOperations: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection info
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

            // Actions
            Row {
                // Select all
                if (canSelectAll && !allSelected) {
                    IconButton(onClick = onSelectAll) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Select all",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (canPerformBatchOperations) {
                    // Add to favorites
                    IconButton(onClick = onAddToFavorites) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Add to favorites",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Add to playlist
                    IconButton(onClick = onAddToPlaylist) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to playlist",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Delete
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryLoadingState(
    selectedTab: LibraryTab,
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
            text = "Loading ${selectedTab.displayName.lowercase()}...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LibraryErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
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
            text = "Failed to load library",
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

@Composable
private fun ScanningProgressIndicator(
    progress: Float,
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Scanning library...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (progress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
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

@Composable
private fun LibraryDialogs(
    uiState: LibraryUiState,
    viewModel: LibraryViewModel,
    showSortDialog: Boolean,
    onDismissSortDialog: () -> Unit,
    showFilterDialog: Boolean,
    onDismissFilterDialog: () -> Unit,
    showCreatePlaylistDialog: Boolean,
    onDismissCreatePlaylistDialog: () -> Unit,
    showAddToPlaylistDialog: Boolean,
    onDismissAddToPlaylistDialog: () -> Unit,
    showDeleteConfirmDialog: Boolean,
    onDismissDeleteConfirmDialog: () -> Unit
) {
    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSortOrder = uiState.sortOrder,
            currentAscending = uiState.sortAscending,
            onDismiss = onDismissSortDialog,
            onApply = { sortOrder, ascending ->
                viewModel.updateSortOrder(sortOrder, ascending)
                onDismissSortDialog()
            }
        )
    }

    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            filterByFavorites = uiState.filterByFavorites,
            selectedGenre = uiState.selectedGenre,
            selectedArtist = uiState.selectedArtist,
            selectedAlbum = uiState.selectedAlbum,
            availableGenres = uiState.genres,
            availableArtists = uiState.artists.map { it.name },
            availableAlbums = uiState.albums.map { it.name },
            onDismiss = onDismissFilterDialog,
            onApply = { favorites, genre, artist, album ->
                viewModel.filterByFavorites(favorites)
                viewModel.filterByGenre(genre)
                viewModel.filterByArtist(artist)
                viewModel.filterByAlbum(album)
                onDismissFilterDialog()
            },
            onClearAll = {
                viewModel.clearAllFilters()
                onDismissFilterDialog()
            }
        )
    }

    // Create playlist dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = onDismissCreatePlaylistDialog,
            onCreate = { name, description, isPublic ->
                // TODO: Implement playlist creation
                onDismissCreatePlaylistDialog()
            }
        )
    }

    // Add to playlist dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onDismiss = onDismissAddToPlaylistDialog,
            onAddToPlaylist = { playlistId ->
                viewModel.addSelectedToPlaylist(playlistId)
                onDismissAddToPlaylistDialog()
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            itemCount = uiState.selectedItemsCount,
            itemType = uiState.selectedTab.displayName.lowercase(),
            onDismiss = onDismissDeleteConfirmDialog,
            onConfirm = {
                viewModel.deleteSelectedSongs()
                onDismissDeleteConfirmDialog()
            }
        )
    }
}

// Helper Components for Dialogs
@Composable
private fun SortDialog(
    currentSortOrder: SortOrder,
    currentAscending: Boolean,
    onDismiss: () -> Unit,
    onApply: (SortOrder, Boolean) -> Unit
) {
    // Implementation for sort dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by") },
        text = {
            // Sort options implementation
            LazyColumn {
                items(SortOrder.values()) { sortOrder ->
                    // Sort option items
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(currentSortOrder, currentAscending) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Additional dialog implementations would go here...

// Helper functions
private fun getSearchPlaceholder(tab: LibraryTab): String {
    return when (tab) {
        LibraryTab.SONGS -> "Search songs..."
        LibraryTab.ALBUMS -> "Search albums..."
        LibraryTab.ARTISTS -> "Search artists..."
        LibraryTab.PLAYLISTS -> "Search playlists..."
        LibraryTab.GENRES -> "Search genres..."
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
