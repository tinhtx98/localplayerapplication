package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.*

@Composable
fun StorageSettingsSection(
    settings: StorageSettings,
    storageInfo: StorageInfo,
    onSettingChanged: (StorageSettingType, Any) -> Unit,
    onShowStorageDialog: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Storage Info
            SettingClickableItem(
                title = "Storage Usage",
                subtitle = "${formatFileSize(storageInfo.usedSpace)} used of ${formatFileSize(storageInfo.totalSpace)}",
                icon = Icons.Default.Storage,
                value = "View Details",
                onClick = onShowStorageDialog
            )
            
            HorizontalDivider()
            
            // Clear Cache
            SettingActionItem(
                title = "Clear Cache",
                subtitle = "Free up ${formatFileSize(storageInfo.cacheSize)} of storage",
                icon = Icons.Default.Delete,
                actionText = "Clear",
                onClick = onClearCache
            )
            
            HorizontalDivider()
            
            // Auto Clear Cache
            SettingSwitchItem(
                title = "Auto Clear Cache",
                subtitle = "Automatically clear old cache files",
                icon = Icons.Default.AutoDelete,
                checked = settings.autoClearCache,
                onCheckedChange = { 
                    onSettingChanged(StorageSettingType.AUTO_CLEAR_CACHE, it)
                }
            )
            
            HorizontalDivider()
            
            // Cache Artwork
            SettingSwitchItem(
                title = "Cache Artwork",
                subtitle = "Cache album artwork for faster loading",
                icon = Icons.Default.Image,
                checked = settings.cacheArtwork,
                onCheckedChange = { 
                    onSettingChanged(StorageSettingType.CACHE_ARTWORK, it)
                }
            )
            
            HorizontalDivider()
            
            // Cache Lyrics
            SettingSwitchItem(
                title = "Cache Lyrics",
                subtitle = "Cache song lyrics for offline access",
                icon = Icons.Default.Lyrics,
                checked = settings.cacheLyrics,
                onCheckedChange = { 
                    onSettingChanged(StorageSettingType.CACHE_LYRICS, it)
                }
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
