package com.tinhtx.localplayerapplication.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.domain.model.Artist
import com.tinhtx.localplayerapplication.domain.model.Playlist
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.music.*
import com.tinhtx.localplayerapplication.presentation.components.ui.HomeTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.home.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopAppBar(
                userName = uiState.userProfile.displayName,
                greeting = uiState.userProfile.greeting,
                profileImageUrl = uiState.userProfile.profileImageUri,
                onProfileClick = { viewModel.onProfileClick() },
                onSearchClick = onNavigateToSearch,
                onNotificationClick = { viewModel.onNotificationClick() },
                hasNotifications = uiState.hasNotifications,
                scrollBehavior = scrollBehavior
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
                        message = "Loading your music collection...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load music",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadData() },
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
                    HomeContent(
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
                        onSeeAllRecentClick = { onNavigateToLibrary() },
                        onSeeAllAlbumsClick = { onNavigateToLibrary() },
                        onSeeAllArtistsClick = { onNavigateToLibrary() },
                        onSeeAllPlaylistsClick = { onNavigateToLibrary() },
                        onFavoriteClick = { song ->
                            viewModel.toggleFavorite(song)
                        },
                        onAddToPlaylistClick = { song ->
                            viewModel.showAddToPlaylistDialog(song)
                        },
                        onRefresh = { viewModel.refreshData() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onSeeAllRecentClick: () -> Unit,
    onSeeAllAlbumsClick: () -> Unit,
    onSeeAllArtistsClick: () -> Unit,
    onSeeAllPlaylistsClick: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onRefresh: () -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Quick Stats Section
        item {
            QuickStatsSection(
                songCount = uiState.totalSongs,
                albumCount = uiState.totalAlbums,
                artistCount = uiState.totalArtists,
                totalDuration = uiState.totalDuration,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Recently Played Section
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Recently Played",
                    subtitle = "Continue where you left off",
                    onSeeAllClick = onSeeAllRecentClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.recentlyPlayed.take(10),
                        key = { it.id }
                    ) { song ->
                        RecentlyPlayedItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onFavoriteClick = { onFavoriteClick(song) },
                            modifier = Modifier
                                .width(280.dp)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        // Quick Access Playlists
        if (uiState.quickAccessPlaylists.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Your Playlists",
                    subtitle = "Quick access to your music",
                    onSeeAllClick = onSeeAllPlaylistsClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.quickAccessPlaylists.take(6),
                        key = { it.id }
                    ) { playlist ->
                        PlaylistQuickAccessCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                            modifier = Modifier
                                .width(160.dp)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        // New Albums Section
        if (uiState.recentAlbums.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Recent Albums",
                    subtitle = "Latest additions to your library",
                    onSeeAllClick = onSeeAllAlbumsClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.recentAlbums.take(8),
                        key = { it.id }
                    ) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onAlbumClick(album) },
                            modifier = Modifier
                                .width(160.dp)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        // Featured Artists Section
        if (uiState.featuredArtists.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Your Top Artists",
                    subtitle = "Artists you listen to most",
                    onSeeAllClick = onSeeAllArtistsClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.featuredArtists.take(6),
                        key = { it.id }
                    ) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { onArtistClick(artist) },
                            modifier = Modifier
                                .width(140.dp)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        // Most Played Songs Section
        if (uiState.mostPlayed.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Most Played",
                    subtitle = "Your favorite tracks",
                    onSeeAllClick = onSeeAllRecentClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.mostPlayed.take(5),
                key = { it.id }
            ) { song ->
                SongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onAddToPlaylistClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }

        // Made For You Section (Recommendations)
        if (uiState.recommendedSongs.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Made For You",
                    subtitle = "Personalized recommendations",
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.recommendedSongs.take(8),
                        key = { it.id }
                    ) { song ->
                        RecommendedSongCard(
                            song = song,
                            onClick = { onSongClick(song) },
                            onFavoriteClick = { onFavoriteClick(song) },
                            reason = generateRecommendationReason(song, uiState),
                            modifier = Modifier
                                .width(200.dp)
                                .animateItemPlacement()
                        )
                    }
                }
            }
        }

        // Smart Recommendations Section
        if (uiState.recommendedSongs.isNotEmpty()) {
            item {
                SmartRecommendationCard(
                    songs = uiState.recommendedSongs,
                    title = "Discovery Weekly",
                    subtitle = "New music picked just for you",
                    onViewAllClick = { onNavigateToLibrary() },
                    onSongClick = onSongClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }

        // Recently Added Section (if different from recent albums)
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Jump Back In",
                    subtitle = "Pick up where you left off",
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                RecentlyPlayedGrid(
                    songs = uiState.recentlyPlayed.take(4),
                    onSongClick = onSongClick,
                    onFavoriteClick = onFavoriteClick,
                    columns = 2,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }

        // Trending Section (if we have play count data)
        if (uiState.mostPlayed.isNotEmpty()) {
            item {
                HomeSectionHeader(
                    title = "Trending in Your Library",
                    subtitle = "Your most played this week",
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = uiState.mostPlayed.take(3),
                key = { it.id }
            ) { song ->
                TrendingSongItem(
                    song = song,
                    rank = uiState.mostPlayed.indexOf(song) + 1,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun TrendingSongItem(
    song: Song,
    rank: Int,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Album art
            AlbumArtImage(
                albumId = song.albumId,
                contentDescription = "Album art",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Song info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.displayArtist} • ${song.playCount} plays",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Trending indicator
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Trending",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    modifier = Modifier.size(18.dp),
                    tint = if (song.isFavorite) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun generateRecommendationReason(song: Song, uiState: HomeUiState): String {
    return when {
        uiState.featuredArtists.any { it.mediaStoreId == song.artistId } -> "From ${song.displayArtist}"
        song.playCount > 0 -> "Popular in your library"
        uiState.recentAlbums.any { it.mediaStoreId == song.albumId } -> "From ${song.displayAlbum}"
        else -> "Discover new music"
    }
}
