package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryTab

@Composable
fun LibraryTabsHeader(
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit,
    tabCounts: Map<LibraryTab, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            },
            divider = {}
        ) {
            LibraryTab.entries.forEachIndexed { index, tab ->
                val isSelected = index == selectedTabIndex
                val count = tabCounts[tab] ?: 0
                
                Tab(
                    selected = isSelected,
                    onClick = { onTabClick(index) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        AnimatedContent(
                            targetState = count,
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() with
                                slideOutVertically { -it } + fadeOut()
                            },
                            label = "count_animation"
                        ) { animatedCount ->
                            Text(
                                text = animatedCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactLibraryTabsHeader(
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LibraryTab.entries.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            
            FilterChip(
                selected = isSelected,
                onClick = { onTabClick(index) },
                label = {
                    Text(
                        text = tab.displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
