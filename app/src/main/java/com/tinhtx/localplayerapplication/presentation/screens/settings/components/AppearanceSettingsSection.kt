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
fun AppearanceSettingsSection(
    settings: AppearanceSettings,
    onSettingChanged: (AppearanceSettingType, Any) -> Unit,
    onShowThemeDialog: () -> Unit,
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
            // Theme Selection
            SettingClickableItem(
                title = "App Theme",
                subtitle = "Choose app appearance",
                icon = Icons.Default.Palette,
                value = settings.theme.displayName,
                onClick = onShowThemeDialog
            )
            
            HorizontalDivider()
            
            // Dynamic Colors (Android 12+)
            SettingSwitchItem(
                title = "Dynamic Colors",
                subtitle = "Use system colors (Android 12+)",
                icon = Icons.Default.ColorLens,
                checked = settings.dynamicColors,
                onCheckedChange = { 
                    onSettingChanged(AppearanceSettingType.DYNAMIC_COLORS, it)
                }
            )
            
            HorizontalDivider()
            
            // Grid Size
            SettingDropdownItem(
                title = "Grid Size",
                subtitle = "Size of grid items",
                icon = Icons.Default.GridView,
                value = settings.gridSize.displayName,
                options = GridSize.values().map { it.displayName },
                onValueChange = { displayName ->
                    val gridSize = GridSize.values().find { it.displayName == displayName }
                    gridSize?.let { onSettingChanged(AppearanceSettingType.GRID_SIZE, it) }
                }
            )
            
            HorizontalDivider()
            
            // Show Album Art
            SettingSwitchItem(
                title = "Show Album Art",
                subtitle = "Display album artwork in lists",
                icon = Icons.Default.Image,
                checked = settings.showAlbumArt,
                onCheckedChange = { 
                    onSettingChanged(AppearanceSettingType.SHOW_ALBUM_ART, it)
                }
            )
            
            HorizontalDivider()
            
            // Blur Background
            SettingSwitchItem(
                title = "Blur Background",
                subtitle = "Blur background behind player",
                icon = Icons.Default.Blur,
                checked = settings.blurBackground,
                onCheckedChange = { 
                    onSettingChanged(AppearanceSettingType.BLUR_BACKGROUND, it)
                }
            )
            
            HorizontalDivider()
            
            // Animated Artwork
            SettingSwitchItem(
                title = "Animated Artwork",
                subtitle = "Animate album art transitions",
                icon = Icons.Default.Animation,
                checked = settings.animatedArtwork,
                onCheckedChange = { 
                    onSettingChanged(AppearanceSettingType.ANIMATED_ARTWORK, it)
                }
            )
        }
    }
}
