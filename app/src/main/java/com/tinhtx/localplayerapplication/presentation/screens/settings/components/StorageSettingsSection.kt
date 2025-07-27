package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StorageSettingsSection(
    cacheSize: String,
    thumbnailCacheSize: String,
    cacheUsagePercentage: Float,
    isCacheNearLimit: Boolean,
    maxCacheSize: String,
    autoClearCache: Boolean,
    isClearingCache: Boolean,
    onClearCache: () -> Unit,
    onManageCache: () -> Unit,
    onToggleAutoClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Storage",
            icon = Icons.Default.Storage
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cache usage
            SettingProgressItem(
                title = "Cache Usage",
                subtitle = "$cacheSize of $maxCacheSize used",
                icon = if (isCacheNearLimit) Icons.Default.Warning else Icons.Default.Folder,
                progress = cacheUsagePercentage / 100f,
                progressText = "${cacheUsagePercentage.toInt()}%",
                onClick = onManageCache
            )

            // Thumbnail cache
            SettingItem(
                title = "Thumbnail Cache",
                subtitle = thumbnailCacheSize,
                icon = Icons.Default.Image,
                enabled = false
            )

            // Clear cache
            SettingItem(
                title = if (isClearingCache) "Clearing Cache..." else "Clear Cache",
                subtitle = "Free up storage space",
                icon = Icons.Default.CleaningServices,
                enabled = !isClearingCache,
                onClick = onClearCache,
                trailingContent = {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            // Auto clear cache
            SettingSwitchItem(
                title = "Auto Clear Cache",
                subtitle = "Automatically clear cache when it gets full",
                icon = Icons.Default.AutoDelete,
                checked = autoClearCache,
                onCheckedChange = { onToggleAutoClearCache() }
            )

            // Manage storage
            SettingItem(
                title = "Manage Storage",
                subtitle = "Advanced storage settings",
                icon = Icons.Default.Settings,
                onClick = onManageCache
            )
        }
    }
}
