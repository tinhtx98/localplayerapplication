package com.tinhtx.localplayerapplication.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.screens.home.components.*

/**
 * Home Screen - Main dashboard with music overview
 * Maps với HomeViewModel, HomeUiState và tất cả components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToPlayer: (Song) -> Unit,
    onNavigateToPlaylist: (Playlist) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle error states
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Show snackbar or toast for error
            // For now, error is shown in UI
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = viewModel::refresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top App Bar Section
            HomeTopBar(
                onSearchClick = onNavigateToSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Welcome Section
            WelcomeSection(
                libraryStats = uiState.libraryStats,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Quick Stats Section - MAPPED COMPONENT
            QuickStatsSection(
                stats = uiState.libraryStats,
                onNavigateToLibrary = onNavigateToLibrary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else if (uiState.hasError) {
                ErrorSection(
                    error = uiState.error ?: "Unknown error",
                    onRetry = viewModel::loadHomeData,
                    onClearError = viewModel::clearError,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                HomeContentSections(
                    uiState = uiState,
                    onSongClick = { song ->
                        viewModel.playSong(song)
                        onNavigateToPlayer(song)
                    },
                    onPlaylistClick = { playlist ->
                        viewModel.playPlaylist(playlist)
                        onNavigateToPlaylist(playlist)
                    },
                    onFavoriteClick = viewModel::toggleFavorite,
                    onSectionHeaderClick = { section ->
                        when (section) {
                            HomeSection.RECENT_SONGS -> onNavigateToLibrary()
                            HomeSection.FAVORITES -> onNavigateToFavorites()
                            HomeSection.PLAYLISTS -> onNavigateToLibrary()
                            HomeSection.RECENTLY_PLAYED -> onNavigateToLibrary()
                        }
                        viewModel.navigateToSection(section)
                    }
                )
            }

            // Bottom spacing for mini player
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good ${getGreeting()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ready to discover music?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WelcomeSection(
    libraryStats: HomeLibraryStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Music Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WelcomeStatItem(
                    label = "Songs",
                    value = libraryStats.totalSongs.toString(),
                    icon = Icons.Default.MusicNote
                )
                WelcomeStatItem(
                    label = "Duration",
                    value = libraryStats.formattedDuration,
                    icon = Icons.Default.Schedule
                )
                WelcomeStatItem(
                    label = "Artists",
                    value = libraryStats.totalArtists.toString(),
                    icon = Icons.Default.Person
                )
            }
        }
    }
}

@Composable
private fun WelcomeStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun HomeContentSections(
    uiState: HomeUiState,
    onSongClick: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onSectionHeaderClick: (HomeSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        
        // Recently Added Songs Section
        if (uiState.recentSongs.isNotEmpty()) {
            HomeSectionHeader(
                title = HomeSection.RECENT_SONGS.displayName,
                subtitle = "${uiState.recentSongs.size} songs",
                onViewAllClick = { onSectionHeaderClick(HomeSection.RECENT_SONGS) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(
                    items = uiState.recentSongs,
                    key = { song -> song.id }
                ) { song ->
                    RecommendedSongCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        isFavorite = uiState.favoriteSongs.any { it.id == song.id },
                        showRecommendationReason = false,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Favorite Songs Section
        if (uiState.favoriteSongs.isNotEmpty()) {
            HomeSectionHeader(
                title = HomeSection.FAVORITES.displayName,
                subtitle = "${uiState.favoriteSongs.size} songs",
                onViewAllClick = { onSectionHeaderClick(HomeSection.FAVORITES) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(
                    items = uiState.favoriteSongs,
                    key = { song -> song.id }
                ) { song ->
                    RecommendedSongCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        isFavorite = true,
                        showRecommendationReason = true,
                        recommendationReason = "❤️ Your favorite",
                        modifier = Modifier.width(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Quick Access Playlists Section
        if (uiState.playlists.isNotEmpty()) {
            HomeSectionHeader(
                title = HomeSection.PLAYLISTS.displayName,
                subtitle = "${uiState.playlists.size} playlists",
                onViewAllClick = { onSectionHeaderClick(HomeSection.PLAYLISTS) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(
                    items = uiState.playlists,
                    key = { playlist -> playlist.id }
                ) { playlist ->
                    PlaylistQuickAccessCard(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        modifier = Modifier.width(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Played Section
        if (uiState.recentlyPlayedSongs.isNotEmpty()) {
            HomeSectionHeader(
                title = HomeSection.RECENTLY_PLAYED.displayName,
                subtitle = "Last ${uiState.recentlyPlayedSongs.size} played",
                onViewAllClick = { onSectionHeaderClick(HomeSection.RECENTLY_PLAYED) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recentlyPlayedSongs.take(5).forEach { song ->
                    RecentlyPlayedItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        isFavorite = uiState.favoriteSongs.any { it.id == song.id },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Search Results Section (if searching)
        if (uiState.showSearchResults) {
            HomeSectionHeader(
                title = "Search Results",
                subtitle = "${uiState.searchResults.size} songs found",
                onViewAllClick = null,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(
                    items = uiState.searchResults,
                    key = { song -> "search_${song.id}" }
                ) { song ->
                    RecommendedSongCardCompact(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        isFavorite = uiState.favoriteSongs.any { it.id == song.id },
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Empty State
        if (uiState.isEmpty) {
            EmptyHomeState(
                onNavigateToLibrary = onNavigateToLibrary,
                onRefresh = { onSectionHeaderClick(HomeSection.RECENT_SONGS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }
    }
}

@Composable
private fun ErrorSection(
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearError,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Dismiss")
                }
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeState(
    onNavigateToLibrary: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your music library is empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Add some music to get started and enjoy your favorite tunes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
            
            Button(
                onClick = onNavigateToLibrary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Library")
            }
        }
    }
}

// Helper function for greeting
private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..20 -> "evening"
        else -> "night"
    }
}
