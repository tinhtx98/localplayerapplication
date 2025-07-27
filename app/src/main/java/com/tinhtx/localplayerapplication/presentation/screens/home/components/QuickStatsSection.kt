package com.tinhtx.localplayerapplication.presentation.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.home.HomeLibraryStats

@Composable
fun QuickStatsSection(
    stats: HomeLibraryStats,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statItems = listOf(
        StatDisplayItem("Songs", stats.totalSongs.toString(), Icons.Default.MusicNote),
        StatDisplayItem("Artists", stats.totalArtists.toString(), Icons.Default.Person),
        StatDisplayItem("Albums", stats.totalAlbums.toString(), Icons.Default.Album),
        StatDisplayItem("Favorites", stats.totalFavorites.toString(), Icons.Default.Favorite)
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(statItems) { item ->
            QuickStatCard(
                title = item.label,
                value = item.value,
                icon = item.icon,
                onClick = onNavigateToLibrary,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private data class StatDisplayItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)
