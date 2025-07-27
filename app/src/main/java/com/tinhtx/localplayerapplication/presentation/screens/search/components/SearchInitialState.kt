package com.tinhtx.localplayerapplication.presentation.screens.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.search.*

@Composable
fun SearchInitialState(
    searchHistory: List<SearchHistoryItem>,
    recentSearches: List<String>,
    trendingSearches: List<String>,
    quickActions: List<QuickAction>,
    onSearchFromHistory: (SearchHistoryItem) -> Unit,
    onSearchFromRecent: (String) -> Unit,
    onSearchFromTrending: (String) -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome message
        item {
            WelcomeSection()
        }

        // Quick actions
        if (quickActions.isNotEmpty()) {
            item {
                QuickActionsSection(
                    quickActions = quickActions,
                    onQuickAction = onQuickAction
                )
            }
        }

        // Recent searches
        if (recentSearches.isNotEmpty()) {
            item {
                RecentSearchesSection(
                    recentSearches = recentSearches,
                    onSearchFromRecent = onSearchFromRecent
                )
            }
        }

        // Trending searches
        if (trendingSearches.isNotEmpty()) {
            item {
                TrendingSearchesSection(
                    trendingSearches = trendingSearches,
                    onSearchFromTrending = onSearchFromTrending
                )
            }
        }

        // Search history
        if (searchHistory.isNotEmpty()) {
            item {
                SearchHistorySection(
                    searchHistory = searchHistory,
                    onSearchFromHistory = onSearchFromHistory,
                    onClearHistory = onClearHistory
                )
            }
        }

        // Search tips
        item {
            SearchTipsSection()
        }

        // Add bottom padding
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun WelcomeSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Discover Your Music",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Search for songs, artists, albums, and playlists",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickActionsSection(
    quickActions: List<QuickAction>,
    onQuickAction: (QuickAction) -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickActions) { action ->
                QuickActionCard(
                    action = action,
                    onClick = { onQuickAction(action) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 120.dp, height = 80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getIconForAction(action.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<String>,
    onSearchFromRecent: (String) -> Unit
) {
    Column {
        Text(
            text = "Recent Searches",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentSearches) { search ->
                SuggestionChip(
                    onClick = { onSearchFromRecent(search) },
                    label = { Text(search) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TrendingSearchesSection(
    trendingSearches: List<String>,
    onSearchFromTrending: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Trending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingSearches) { search ->
                SuggestionChip(
                    onClick = { onSearchFromTrending(search) },
                    label = { Text(search) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    searchHistory: List<SearchHistoryItem>,
    onSearchFromHistory: (SearchHistoryItem) -> Unit,
    onClearHistory: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(onClick = onClearHistory) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        searchHistory.take(5).forEach { historyItem ->
            SearchHistoryItem(
                historyItem = historyItem,
                onClick = { onSearchFromHistory(historyItem) }
            )
        }
    }
}

@Composable
private fun SearchHistoryItem(
    historyItem: SearchHistoryItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(historyItem.query) },
        supportingContent = {
            Row {
                Text("${historyItem.resultCount} results")
                Text(" • ")
                Text(formatHistoryTimestamp(historyItem.timestamp))
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.NorthWest,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun SearchTipsSection() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Search Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val tips = listOf(
                "Use quotes for exact matches: \"song title\"",
                "Search by artist: artist:\"Beatles\"",
                "Filter by year: year:1970-1980",
                "Find by genre: genre:rock",
                "Use voice search for hands-free searching"
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getIconForAction(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "shuffle" -> Icons.Default.Shuffle
        "new_releases" -> Icons.Default.NewReleases
        "trending_up" -> Icons.Default.TrendingUp
        "favorite" -> Icons.Default.Favorite
        "mic" -> Icons.Default.Mic
        "search" -> Icons.Default.Search
        "playlist_add" -> Icons.Default.PlaylistAdd
        "file_download" -> Icons.Default.FileDownload
        else -> Icons.Default.MusicNote
    }
}

private fun formatHistoryTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "Long ago"
    }
}
