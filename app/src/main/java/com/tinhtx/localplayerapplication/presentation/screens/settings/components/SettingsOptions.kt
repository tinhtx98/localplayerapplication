package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Pre-defined settings options and configurations
 */

data class SettingsOption(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val action: SettingsAction = SettingsAction.Navigation
)

enum class SettingsAction {
    Navigation,
    Toggle,
    Selection,
    Custom
}

object SettingsOptions {
    
    // Appearance options
    val appearanceOptions = listOf(
        SettingsOption(
            title = "Theme",
            subtitle = "Choose your preferred theme",
            icon = Icons.Default.DarkMode,
            action = SettingsAction.Selection
        ),
        SettingsOption(
            title = "Dynamic Color",
            subtitle = "Use colors from your wallpaper",
            icon = Icons.Default.ColorLens,
            action = SettingsAction.Toggle
        ),
        SettingsOption(
            title = "Font Size",
            subtitle = "Adjust text size",
            icon = Icons.Default.FormatSize,
            action = SettingsAction.Selection
        ),
        SettingsOption(
            title = "Grid Size",
            subtitle = "Change grid layout size",
            icon = Icons.Default.GridView,
            action = SettingsAction.Selection
        )
    )
    
    // Playback options
    val playbackOptions = listOf(
        SettingsOption(
            title = "Audio Quality",
            subtitle = "Set playback quality",
            icon = Icons.Default.HighQuality,
            action = SettingsAction.Selection
        ),
        SettingsOption(
            title = "Crossfade",
            subtitle = "Smooth transitions between songs",
            icon = Icons.Default.BlendingMode,
            action = SettingsAction.Custom
        ),
        SettingsOption(
            title = "Equalizer",
            subtitle = "Adjust sound frequencies",
            icon = Icons.Default.Equalizer,
            action = SettingsAction.Navigation
        ),
        SettingsOption(
            title = "Skip Silence",
            subtitle = "Auto-skip silent parts",
            icon = Icons.Default.FastForward,
            action = SettingsAction.Toggle
        )
    )
    
    // Library options
    val libraryOptions = listOf(
        SettingsOption(
            title = "Scan Library",
            subtitle = "Search for new music files",
            icon = Icons.Default.Search,
            action = SettingsAction.Custom
        ),
        SettingsOption(
            title = "Scan on Startup",
            subtitle = "Auto-scan when app starts",
            icon = Icons.Default.PowerSettingsNew,
            action = SettingsAction.Toggle
        ),
        SettingsOption(
            title = "Include Subfolders",
            subtitle = "Scan all subfolders",
            icon = Icons.Default.Folder,
            action = SettingsAction.Toggle
        ),
        SettingsOption(
            title = "Manage Folders",
            subtitle = "Choose folders to scan",
            icon = Icons.Default.FolderOpen,
            action = SettingsAction.Navigation
        )
    )
    
    // Storage options
    val storageOptions = listOf(
        SettingsOption(
            title = "Clear Cache",
            subtitle = "Free up storage space",
            icon = Icons.Default.CleaningServices,
            action = SettingsAction.Custom
        ),
        SettingsOption(
            title = "Auto Clear Cache",
            subtitle = "Auto-clear when full",
            icon = Icons.Default.AutoDelete,
            action = SettingsAction.Toggle
        ),
        SettingsOption(
            title = "Manage Storage",
            subtitle = "Advanced storage settings",
            icon = Icons.Default.Storage,
            action = SettingsAction.Navigation
        )
    )
    
    // About options
    val aboutOptions = listOf(
        SettingsOption(
            title = "What's New",
            subtitle = "View recent updates",
            icon = Icons.Default.NewReleases,
            action = SettingsAction.Navigation
        ),
        SettingsOption(
            title = "Open Source Licenses",
            subtitle = "Third-party libraries",
            icon = Icons.Default.Description,
            action = SettingsAction.Navigation
        ),
        SettingsOption(
            title = "Privacy Policy",
            subtitle = "How we handle your data",
            icon = Icons.Default.Privacy,
            action = SettingsAction.Navigation
        ),
        SettingsOption(
            title = "Contact Support",
            subtitle = "Get help or report issues",
            icon = Icons.Default.Support,
            action = SettingsAction.Navigation
        ),
        SettingsOption(
            title = "Rate This App",
            subtitle = "Leave a review",
            icon = Icons.Default.Star,
            action = SettingsAction.Navigation
        )
    )
}

@Composable
fun SettingsOptionsList(
    options: List<SettingsOption>,
    onOptionClick: (SettingsOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            SettingItem(
                title = option.title,
                subtitle = option.subtitle,
                icon = option.icon,
                enabled = option.enabled,
                onClick = if (option.enabled) {
                    { onOptionClick(option) }
                } else null
            )
        }
    }
}

@Composable
fun QuickSettingsRow(
    options: List<SettingsOption>,
    onOptionClick: (SettingsOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.take(4).forEach { option ->
            QuickSettingButton(
                option = option,
                onClick = { onOptionClick(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickSettingButton(
    option: SettingsOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = if (option.enabled) onClick else { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            if (option.icon != null) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (option.enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = option.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (option.enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
