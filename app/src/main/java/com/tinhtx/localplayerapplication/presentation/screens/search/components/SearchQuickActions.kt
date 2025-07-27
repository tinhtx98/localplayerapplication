package com.tinhtx.localplayerapplication.presentation.screens.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.search.QuickAction
import com.tinhtx.localplayerapplication.presentation.screens.search.QuickActionType

@Composable
fun SearchQuickActions(
    quickActions: List<QuickAction>,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(quickActions) { action ->
            QuickActionItem(
                action = action,
                onClick = { onQuickAction(action) }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    action: QuickAction,
    onClick: () -> Unit
) {
    val backgroundColor = when (action.action) {
        QuickActionType.SHUFFLE_ALL -> MaterialTheme.colorScheme.primaryContainer
        QuickActionType.FAVORITES -> MaterialTheme.colorScheme.secondaryContainer
        QuickActionType.VOICE_SEARCH -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (action.action) {
        QuickActionType.SHUFFLE_ALL -> MaterialTheme.colorScheme.onPrimaryContainer
        QuickActionType.FAVORITES -> MaterialTheme.colorScheme.onSecondaryContainer
        QuickActionType.VOICE_SEARCH -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 100.dp, height = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getActionIcon(action.action),
                contentDescription = action.title,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    quickActions: List<QuickAction>,
    onQuickAction: (QuickAction) -> Unit,
    columns: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val chunkedActions = quickActions.chunked(columns)
        
        chunkedActions.forEach { rowActions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionGridItem(
                        action = action,
                        onClick = { onQuickAction(action) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill empty spaces
                repeat(columns - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionGridItem(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                imageVector = getActionIcon(action.action),
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun QuickActionButtons(
    quickActions: List<QuickAction>,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickActions.forEach { action ->
            OutlinedButton(
                onClick = { onQuickAction(action) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = getActionIcon(action.action),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(action.title)
            }
        }
    }
}

@Composable
fun FeaturedQuickAction(
    action: QuickAction,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onQuickAction(action) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getActionIcon(action.action),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = getActionDescription(action.action),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun getActionIcon(actionType: QuickActionType): ImageVector {
    return when (actionType) {
        QuickActionType.SHUFFLE_ALL -> Icons.Default.Shuffle
        QuickActionType.RECENTLY_ADDED -> Icons.Default.NewReleases
        QuickActionType.MOST_PLAYED -> Icons.Default.TrendingUp
        QuickActionType.FAVORITES -> Icons.Default.Favorite
        QuickActionType.VOICE_SEARCH -> Icons.Default.Mic
        QuickActionType.SCAN_LIBRARY -> Icons.Default.Search
        QuickActionType.CREATE_PLAYLIST -> Icons.Default.PlaylistAdd
        QuickActionType.IMPORT_MUSIC -> Icons.Default.FileDownload
    }
}

private fun getActionDescription(actionType: QuickActionType): String {
    return when (actionType) {
        QuickActionType.SHUFFLE_ALL -> "Play all songs randomly"
        QuickActionType.RECENTLY_ADDED -> "Recently added music"
        QuickActionType.MOST_PLAYED -> "Your most played songs"
        QuickActionType.FAVORITES -> "Your favorite music"
        QuickActionType.VOICE_SEARCH -> "Search with your voice"
        QuickActionType.SCAN_LIBRARY -> "Scan for new music"
        QuickActionType.CREATE_PLAYLIST -> "Create a new playlist"
        QuickActionType.IMPORT_MUSIC -> "Import music files"
    }
}

@Composable
fun QuickActionChips(
    quickActions: List<QuickAction>,
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(quickActions) { action ->
            FilterChip(
                selected = false,
                onClick = { onQuickAction(action) },
                label = { Text(action.title) },
                leadingIcon = {
                    Icon(
                        imageVector = getActionIcon(action.action),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
