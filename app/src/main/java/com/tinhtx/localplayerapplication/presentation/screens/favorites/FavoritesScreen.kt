package com.tinhtx.localplayerapplication.presentation.screens.favorites

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.music.*
import com.tinhtx.localplayerapplication.presentation.components.ui.MusicTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.favorites.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showRemoveAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopAppBar(
                title = "Favorites",
                subtitle = if (uiState.favorites.isNotEmpty()) {
                    "${uiState.favorites.size} songs • ${uiState.totalDuration}"
                } else null,
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    if (uiState.favorites.isNotEmpty()) {
                        // Search in favorites
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                imageVector = if (uiState.isSearching) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearching) "Close search" else "Search favorites"
                            )
                        }

                        // Sort options
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort favorites"
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
                                        viewModel.playAllFavorites()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Shuffle") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.shuffleFavorites()
                                        onNavigateToPlayer()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Shuffle, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Export playlist") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.exportFavorites()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear all", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showRemoveAllDialog = true
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
        },
        floatingActionButton = {
            if (uiState.favorites.isNotEmpty() && !uiState.isSearching) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.playAllFavorites()
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
                        message = "Loading your favorite songs...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load favorites",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadFavorites() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.favorites.isEmpty() -> {
                    NoFavoritesState(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    FavoritesContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        listState = listState,
                        onSongClick = { song ->
                            viewModel.playSong(song)
                            onNavigateToPlayer()
                        },
                        onFavoriteClick = { song ->
                            viewModel.removeFromFavorites(song)
                        },
                        onAddToPlaylistClick = { song ->
                            viewModel.showAddToPlaylistDialog(song)
                        },
                        onSearchQueryChange = { query ->
                            viewModel.updateSearchQuery(query)
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showSortDialog) {
        SortOptionsDialog(
            currentSortOrder = uiState.sortOrder,
            onSortOrderChange = { sortOrder ->
                viewModel.updateSortOrder(sortOrder)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showRemoveAllDialog) {
        ConfirmationDialog(
            title = "Clear All Favorites",
            message = "Are you sure you want to remove all ${uiState.favorites.size} songs from your favorites? This action cannot be undone.",
            onConfirm = {
                viewModel.removeAllFavorites()
                showRemoveAllDialog = false
            },
            onDismiss = { showRemoveAllDialog = false },
            confirmText = "Clear All",
            dismissText = "Cancel",
            isDestructive = true
        )
    }
}

@Composable
private fun FavoritesContent(
    uiState: FavoritesUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    val displayedSongs = if (uiState.searchQuery.isBlank()) {
        uiState.favorites
    } else {
        uiState.filteredFavorites
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        AnimatedVisibility(
            visible = uiState.isSearching,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            FavoritesSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
            )
        }

        // Stats header
        if (displayedSongs.isNotEmpty()) {
            FavoritesStatsHeader(
                songCount = displayedSongs.size,
                totalDuration = uiState.totalDuration,
                averageRating = uiState.averageRating,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp)
            )
        }

        // Favorites list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = displayedSongs,
                key = { it.id }
            ) { song ->
                FavoriteSongItem(
                    song = song,
                    position = displayedSongs.indexOf(song) + 1,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onAddToPlaylistClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }

            // Show search results info
            if (uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                item {
                    FavoritesSearchResults(
                        query = uiState.searchQuery,
                        resultCount = displayedSongs.size,
                        totalCount = uiState.favorites.size,
                        modifier = Modifier.padding(horizontalPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOptionsDialog(
    currentSortOrder: AppConstants.SortOrder,
    onSortOrderChange: (AppConstants.SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort favorites",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                val sortOptions = listOf(
                    AppConstants.SortOrder.TITLE_ASC to "Song title (A-Z)",
                    AppConstants.SortOrder.TITLE_DESC to "Song title (Z-A)",
                    AppConstants.SortOrder.ARTIST_ASC to "Artist (A-Z)",
                    AppConstants.SortOrder.ARTIST_DESC to "Artist (Z-A)",
                    AppConstants.SortOrder.ALBUM_ASC to "Album (A-Z)",
                    AppConstants.SortOrder.ALBUM_DESC to "Album (Z-A)",
                    AppConstants.SortOrder.DATE_ADDED_DESC to "Recently added",
                    AppConstants.SortOrder.DATE_ADDED_ASC to "Oldest first"
                )

                sortOptions.forEach { (sortOrder, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortOrderChange(sortOrder) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOrder == sortOrder,
                            onClick = { onSortOrderChange(sortOrder) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
