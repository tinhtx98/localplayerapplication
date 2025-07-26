package com.tinhtx.localplayerapplication.presentation.screens.search.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@Composable
fun SearchInitialState(
    recentSearches: List<String>,
    popularSearches: List<String>,
    searchSuggestions: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onPopularSearchClick: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Search welcome header
        item {
            SearchWelcomeHeader(
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Recent searches
        if (recentSearches.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Recent searches",
                    count = recentSearches.size,
                    onSeeAllClick = null,
                    actionText = "Clear",
                    onActionClick = onClearRecentSearches,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = recentSearches.take(10),
                        key = { it }
                    ) { search ->
                        RecentSearchChip(
                            text = search,
                            onClick = { onRecentSearchClick(search) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }

        // Popular searches
        if (popularSearches.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Popular searches",
                    count = popularSearches.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            item {
                PopularSearchesGrid(
                    searches = popularSearches,
                    onSearchClick = onPopularSearchClick,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }

        // Search suggestions
        if (searchSuggestions.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    title = "Suggested searches",
                    count = searchSuggestions.size,
                    onSeeAllClick = null,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            items(
                items = searchSuggestions.take(5),
                key = { it }
            ) { suggestion ->
                SearchSuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .animateItemPlacement()
                )
            }
        }

        // Search tips
        item {
            SearchTipsCard(
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }
    }
}

@Composable
private fun SearchWelcomeHeader(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Search your music",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Find songs, artists, albums, and playlists",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecentSearchChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun PopularSearchesGrid(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        searches.chunked(2).forEach { rowSearches ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSearches.forEach { search ->
                    PopularSearchCard(
                        text = search,
                        onClick = { onSearchClick(search) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if odd number
                if (rowSearches.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PopularSearchCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchSuggestionItem(
    suggestion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.NorthWest,
                contentDescription = "Search",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SearchTipsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Search tips",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val tips = listOf(
                "Try different keywords or spellings",
                "Search by artist, album, or song title",
                "Use quotes for exact phrases"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
