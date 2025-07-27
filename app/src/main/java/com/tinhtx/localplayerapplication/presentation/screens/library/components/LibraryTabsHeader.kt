package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryTab
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryViewMode

@Composable
fun LibraryTabsHeader(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChanged: (LibraryViewMode) -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onScanClick: () -> Unit,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Top row với actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Music Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View mode toggle
                IconButton(
                    onClick = {
                        val nextMode = when (viewMode) {
                            LibraryViewMode.LIST -> LibraryViewMode.GRID
                            LibraryViewMode.GRID -> LibraryViewMode.DETAILED_LIST
                            LibraryViewMode.DETAILED_LIST -> LibraryViewMode.LIST
                        }
                        onViewModeChanged(nextMode)
                    }
                ) {
                    Icon(
                        imageVector = when (viewMode) {
                            LibraryViewMode.LIST -> Icons.Default.List
                            LibraryViewMode.GRID -> Icons.Default.GridView
                            LibraryViewMode.DETAILED_LIST -> Icons.Default.ViewList
                        },
                        contentDescription = "Change view mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Sort button
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Filter button
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Scan button
                IconButton(
                    onClick = onScanClick,
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan library",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Tabs row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            edgePadding = 16.dp
        ) {
            LibraryTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                LibraryTab.SONGS -> Icons.Default.MusicNote
                                LibraryTab.ALBUMS -> Icons.Default.Album
                                LibraryTab.ARTISTS -> Icons.Default.Person
                                LibraryTab.PLAYLISTS -> Icons.Default.PlaylistPlay
                                LibraryTab.GENRES -> Icons.Default.Category
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}
