package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibrarySettingsSection(
    libraryStats: String,
    lastScanTime: String,
    isScanning: Boolean,
    scanProgress: Float,
    scanOnStartup: Boolean,
    includeSubfolders: Boolean,
    onScanLibrary: () -> Unit,
    onCancelScan: () -> Unit,
    onToggleScanOnStartup: () -> Unit,
    onToggleIncludeSubfolders: () -> Unit,
    onManageFolders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Library",
            icon = Icons.Default.LibraryMusic
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Library stats
            SettingItem(
                title = "Music Library",
                subtitle = libraryStats,
                icon = Icons.Default.MusicNote,
                enabled = false
            )

            // Last scan time
            SettingItem(
                title = "Last Scan",
                subtitle = lastScanTime,
                icon = Icons.Default.Update,
                enabled = false
            )

            // Scan library
            if (isScanning) {
                SettingProgressItem(
                    title = "Scanning Library",
                    subtitle = "Finding music files...",
                    icon = Icons.Default.Search,
                    progress = scanProgress,
                    progressText = "${(scanProgress * 100).toInt()}%",
                    onClick = onCancelScan
                )
            } else {
                SettingItem(
                    title = "Scan Library",
                    subtitle = "Search for new music files",
                    icon = Icons.Default.Search,
                    onClick = onScanLibrary
                )
            }

            // Scan on startup
            SettingSwitchItem(
                title = "Scan on Startup",
                subtitle = "Automatically scan for new music when app starts",
                icon = Icons.Default.PowerSettingsNew,
                checked = scanOnStartup,
                onCheckedChange = { onToggleScanOnStartup() }
            )

            // Include subfolders
            SettingSwitchItem(
                title = "Include Subfolders",
                subtitle = "Scan all subfolders recursively",
                icon = Icons.Default.Folder,
                checked = includeSubfolders,
                onCheckedChange = { onToggleIncludeSubfolders() }
            )

            // Manage folders
            SettingItem(
                title = "Manage Folders",
                subtitle = "Choose which folders to scan",
                icon = Icons.Default.FolderOpen,
                onClick = onManageFolders
            )
        }
    }
}
