package com.tinhtx.localplayerapplication.presentation.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.ui.MusicTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.library.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { LibraryTab.entries.size }
    )

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showViewOptionsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedTabIndex) {
        if (pagerState.currentPage != uiState.selectedTabIndex) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.selectedTabIndex) {
            viewModel.selectTab(pagerState.currentPage)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLibraryData()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LibraryTopAppBar(
                uiState = uiState,
                scrollBehavior = scrollBehavior,
                onSearchClick = { viewModel.toggleSearchMode() },
                onSortClick = { showSortDialog = true },
                onViewToggleClick = { viewModel.toggleViewMode() },
                onFilterClick = { showFilterDialog = true },
                onSearchQueryChange = { query -> 
                    viewModel.updateSearchQuery(query)
                },
                onNavigateToSearch = onNavigateToSearch
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
                        message = "Loading your music library...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load library",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadLibrary() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.isEmpty -> {
                    NoMusicFoundState(
                        onScanClick = { viewModel.startMediaScan() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Library stats overview
                        LibraryStatsOverview(
                            stats = uiState.libraryStats,
                            onStatsClick = { stat ->
                                when (stat) {
                                    LibraryStatType.FAVORITES -> onNavigateToFavorites()
                                    else -> {
                                        val tabIndex = when (stat) {
                                            LibraryStatType.SONGS -> 0
                                            LibraryStatType.ALBUMS -> 1
                                            LibraryStatType.ARTISTS -> 2
                                            LibraryStatType.PLAYLISTS -> 3
                                            else -> 0
                                        }
                                        scope.launch {
                                            viewModel.selectTab(tabIndex)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = windowSizeClass.getHorizontalPadding())
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tab navigation
                        LibraryTabsHeader(
                            selectedTabIndex = uiState.selectedTabIndex,
                            onTabClick = { tabIndex ->
                                scope.launch {
                                    viewModel.selectTab(tabIndex)
                                }
                            },
                            tabCounts = uiState.tabCounts,
                            modifier = Modifier.padding(horizontal = windowSizeClass.getHorizontalPadding())
                        )

                        // Search bar (when active)
                        AnimatedVisibility(
                            visible = uiState.isSearching,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            LibrarySearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                currentTab = LibraryTab.entries[uiState.selectedTabIndex],
                                modifier = Modifier.padding(
                                    horizontal = windowSizeClass.getHorizontalPadding(),
                                    vertical = 8.dp
                                )
                            )
                        }

                        // Filter chips
                        if (uiState.activeFilters.isNotEmpty()) {
                            LibraryFilterChips(
                                filters = uiState.activeFilters,
                                onRemoveFilter = { filter ->
                                    viewModel.removeFilter(filter)
                                },
                                onClearAllFilters = {
                                    viewModel.clearAllFilters()
                                },
                                modifier = Modifier.padding(horizontal = windowSizeClass.getHorizontalPadding())
                            )
                        }

                        // Tab content
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            LibraryTabContent(
                                tab = LibraryTab.entries[page],
                                uiState = uiState,
                                windowSizeClass = windowSizeClass,
                                onSongClick = { song ->
                                    viewModel.playSong(song)
                                    onNavigateToPlayer()
                                },
                                onAlbumClick = { album ->
                                    viewModel.playAlbum(album)
                                    onNavigateToPlayer()
                                },
                                onArtistClick = { artist ->
                                    viewModel.playArtist(artist)
                                    onNavigateToPlayer()
                                },
                                onPlaylistClick = onNavigateToPlaylist,
                                onFavoriteClick = { song ->
                                    viewModel.toggleFavorite(song)
                                },
                                onAddToPlaylistClick = { song ->
                                    viewModel.showAddToPlaylistDialog(song)
                                },
                                onCreatePlaylistClick = {
                                    viewModel.showCreatePlaylistDialog()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSortDialog) {
        LibrarySortDialog(
            currentTab = LibraryTab.entries[uiState.selectedTabIndex],
            currentSortOrder = uiState.sortOrder,
            onSortOrderChange = { sortOrder ->
                viewModel.updateSortOrder(sortOrder)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showFilterDialog) {
        LibraryFilterDialog(
            currentTab = LibraryTab.entries[uiState.selectedTabIndex],
            availableFilters = uiState.availableFilters,
            activeFilters = uiState.activeFilters,
            onFiltersChange = { filters ->
                viewModel.updateFilters(filters)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showViewOptionsDialog) {
        LibraryViewOptionsDialog(
            currentViewMode = uiState.viewMode,
            currentGridSize = uiState.gridSize,
            onViewModeChange = { viewMode ->
                viewModel.updateViewMode(viewMode)
            },
            onGridSizeChange = { gridSize ->
                viewModel.updateGridSize(gridSize)
            },
            onDismiss = { showViewOptionsDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopAppBar(
    uiState: LibraryUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onViewToggleClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    if (uiState.isSearching) {
        // Search mode top bar
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text("Search in ${LibraryTab.entries[uiState.selectedTabIndex].displayName.lowercase()}...")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Close search"
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    } else {
        // Normal mode top bar
        MusicTopAppBar(
            title = "Music Library",
            subtitle = "${uiState.libraryStats.totalSongs} songs • ${uiState.libraryStats.totalDuration}",
            scrollBehavior = scrollBehavior,
            actions = {
                // Search
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search library"
                    )
                }

                // Sort
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort options"
                    )
                }

                // View mode toggle
                IconButton(onClick = onViewToggleClick) {
                    Icon(
                        imageVector = if (uiState.viewMode == AppConstants.ViewMode.LIST) {
                            Icons.Default.GridView
                        } else {
                            Icons.Default.ViewList
                        },
                        contentDescription = "Toggle view mode"
                    )
                }

                // Filter
                Box {
                    IconButton(onClick = onFilterClick) {
                        Badge(
                            modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                            containerColor = if (uiState.activeFilters.isNotEmpty()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            }
                        ) {
                            if (uiState.activeFilters.isNotEmpty()) {
                                Text(
                                    text = uiState.activeFilters.size.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter options"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun LibraryTabContent(
    tab: LibraryTab,
    uiState: LibraryUiState,
    windowSizeClass: WindowSizeClass,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    when (tab) {
        LibraryTab.SONGS -> {
            SongsTab(
                songs = if (uiState.searchQuery.isBlank()) uiState.songs else uiState.filteredSongs,
                viewMode = uiState.viewMode,
                onSongClick = onSongClick,
                onFavoriteClick = onFavoriteClick,
                onAddToPlaylistClick = onAddToPlaylistClick,
                windowSizeClass = windowSizeClass
            )
        }
        LibraryTab.ALBUMS -> {
            AlbumsTab(
                albums = if (uiState.searchQuery.isBlank()) uiState.albums else uiState.filteredAlbums,
                viewMode = uiState.viewMode,
                gridSize = uiState.gridSize,
                onAlbumClick = onAlbumClick,
                windowSizeClass = windowSizeClass
            )
        }
        LibraryTab.ARTISTS -> {
            ArtistsTab(
                artists = if (uiState.searchQuery.isBlank()) uiState.artists else uiState.filteredArtists,
                viewMode = uiState.viewMode,
                gridSize = uiState.gridSize,
                onArtistClick = onArtistClick,
                windowSizeClass = windowSizeClass
            )
        }
        LibraryTab.PLAYLISTS -> {
            PlaylistsTab(
                playlists = if (uiState.searchQuery.isBlank()) uiState.playlists else uiState.filteredPlaylists,
                viewMode = uiState.viewMode,
                gridSize = uiState.gridSize,
                onPlaylistClick = onPlaylistClick,
                onCreatePlaylistClick = onCreatePlaylistClick,
                windowSizeClass = windowSizeClass
            )
        }
    }
}

enum class LibraryTab(
    val displayName: String,
    val icon: ImageVector
) {
    SONGS("Songs", Icons.Default.MusicNote),
    ALBUMS("Albums", Icons.Default.Album),
    ARTISTS("Artists", Icons.Default.Person),
    PLAYLISTS("Playlists", Icons.Default.QueueMusic)
}

enum class LibraryStatType {
    SONGS, ALBUMS, ARTISTS, PLAYLISTS, FAVORITES, DURATION
}
