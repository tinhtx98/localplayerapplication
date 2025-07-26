package com.tinhtx.localplayerapplication.presentation.screens.search

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.screens.search.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query -> 
                    viewModel.updateSearchQuery(query)
                },
                onNavigateBack = onNavigateBack,
                onClearSearch = {
                    viewModel.clearSearch()
                },
                focusRequester = focusRequester,
                isSearching = uiState.isSearching
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.searchQuery.isBlank() -> {
                    SearchInitialState(
                        recentSearches = uiState.recentSearches,
                        popularSearches = uiState.popularSearches,
                        searchSuggestions = uiState.searchSuggestions,
                        onRecentSearchClick = { query ->
                            viewModel.updateSearchQuery(query)
                        },
                        onPopularSearchClick = { query ->
                            viewModel.updateSearchQuery(query)
                        },
                        onSuggestionClick = { suggestion ->
                            viewModel.updateSearchQuery(suggestion)
                        },
                        onClearRecentSearches = {
                            viewModel.clearRecentSearches()
                        },
                        windowSizeClass = windowSizeClass
                    )
                }
                uiState.isSearching -> {
                    SearchLoadingState(
                        query = uiState.searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Search Error",
                        message = uiState.error,
                        onRetry = { 
                            viewModel.retrySearch()
                        },
                        errorType = MusicErrorType.NETWORK,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                uiState.searchResults.isEmpty() -> {
                    SearchNoResultsState(
                        query = uiState.searchQuery,
                        onTryAgain = {
                            viewModel.retrySearch()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    SearchResultsContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        listState = listState,
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
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearSearch: () -> Unit,
    focusRequester: FocusRequester,
    isSearching: Boolean
) {
    TopAppBar(
        title = {
            SearchTextField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearClick = onClearSearch,
                focusRequester = focusRequester,
                isSearching = isSearching,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                AnimatedContent(
                    targetState = searchQuery.isNotEmpty(),
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    },
                    label = "back_icon"
                ) { hasQuery ->
                    Icon(
                        imageVector = if (hasQuery) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = if (hasQuery) "Clear search" else "Back"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    focusRequester: FocusRequester,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        placeholder = {
            Text(
                text = "Search songs, artists, albums...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            AnimatedContent(
                targetState = isSearching,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "search_icon"
            ) { searching ->
                if (searching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    )
}

@Composable
private fun SearchResultsContent(
    uiState: SearchUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search results header
        item {
            SearchResultsHeader(
                query = uiState.searchQuery,
                totalResults = uiState.totalResults,
                searchTime = uiState.searchTime,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Quick actions (if available)
        if (uiState.quickActions.isNotEmpty()) {
            item {
                SearchQuickActions(
                    actions = uiState.quickActions,
                    onActionClick = { action ->
                        when (action.type) {
                            SearchActionType.PLAY_ALL -> {
                                if (uiState.searchResults.songs.isNotEmpty()) {
                                    onSongClick(uiState.searchResults.songs.first())
                                }
                            }
                            SearchActionType.SHUFFLE_ALL -> {
                                if (uiState.searchResults.songs.isNotEmpty()) {
                                    onSongClick(uiState.searchResults.songs.random())
                                }
                            }
                            else -> {}
                        }
                    },
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }

        // Top result (if available)
        uiState.topResult?.let { topResult ->
            item {
                SearchTopResult(
                    result = topResult,
                    onClick = {
                        when (topResult) {
                            is Song -> onSongClick(topResult)
                            is Album -> onAlbumClick(topResult)
                            is Artist -> onArtistClick(topResult)
                            is Playlist -> onPlaylistClick(topResult.id)
                        }
                    },
                    onFavoriteClick = if (topResult is Song) {
                        { onFavoriteClick(topResult) }
                    } else null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }

        // Songs section
        if (uiState.searchResults.songs.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Songs",
                    count = uiState.searchResults.songs.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.searchResults.songs.take(5),
                key = { it.id }
            ) { song ->
                SearchSongItem(
                    song = song,
                    query = uiState.searchQuery,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onAddToPlaylistClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }

            if (uiState.searchResults.songs.size > 5) {
                item {
                    ShowMoreButton(
                        text = "Show ${uiState.searchResults.songs.size - 5} more songs",
                        onClick = {
                            // Expand songs list or navigate to full songs results
                        },
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )
                }
            }
        }

        // Artists section
        if (uiState.searchResults.artists.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Artists",
                    count = uiState.searchResults.artists.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.searchResults.artists.take(3),
                key = { it.id }
            ) { artist ->
                SearchArtistItem(
                    artist = artist,
                    query = uiState.searchQuery,
                    onClick = { onArtistClick(artist) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }

        // Albums section
        if (uiState.searchResults.albums.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Albums",
                    count = uiState.searchResults.albums.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.searchResults.albums.take(3),
                key = { it.id }
            ) { album ->
                SearchAlbumItem(
                    album = album,
                    query = uiState.searchQuery,
                    onClick = { onAlbumClick(album) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }

        // Playlists section
        if (uiState.searchResults.playlists.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Playlists",
                    count = uiState.searchResults.playlists.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.searchResults.playlists.take(3),
                key = { it.id }
            ) { playlist ->
                SearchPlaylistItem(
                    playlist = playlist,
                    query = uiState.searchQuery,
                    onClick = { onPlaylistClick(playlist.id) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun SearchLoadingState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Searching for \"$query\"...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Please wait while we find your music",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchNoResultsState(
    query: String,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No results found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "We couldn't find anything for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try again")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Try different keywords or check your spelling",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShowMoreButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
