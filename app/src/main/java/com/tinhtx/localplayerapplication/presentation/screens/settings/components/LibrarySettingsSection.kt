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
fun LibrarySettingsSection(
    settings: LibrarySettings,
    onSettingChanged: (LibrarySettingType, Any) -> Unit,
    onScanLibrary: () -> Unit,
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
            // Scan Library Button
            SettingActionItem(
                title = "Scan Library",
                subtitle = "Refresh your music library",
                icon = Icons.Default.Refresh,
                actionText = "Scan Now",
                onClick = onScanLibrary
            )
            
            HorizontalDivider()
            
            // Auto Scan
            SettingSwitchItem(
                title = "Auto Scan",
                subtitle = "Automatically scan for new music",
                icon = Icons.Default.AutoMode,
                checked = settings.autoScan,
                onCheckedChange = { 
                    onSettingChanged(LibrarySettingType.AUTO_SCAN, it)
                }
            )
            
            HorizontalDivider()
            
            // Ignore Short Tracks
            SettingSwitchItem(
                title = "Ignore Short Tracks",
                subtitle = "Skip tracks shorter than minimum duration",
                icon = Icons.Default.SkipNext,
                checked = settings.ignoreShortTracks,
                onCheckedChange = { 
                    onSettingChanged(LibrarySettingType.IGNORE_SHORT_TRACKS, it)
                }
            )
            
            // Min Track Duration (only show if ignore short tracks is enabled)
            if (settings.ignoreShortTracks) {
                SettingSliderItem(
                    title = "Minimum Track Duration",
                    subtitle = "${settings.minTrackDuration} seconds",
                    icon = Icons.Default.Timer,
                    value = settings.minTrackDuration.toFloat(),
                    valueRange = 5f..60f,
                    steps = 10,
                    onValueChange = { 
                        onSettingChanged(LibrarySettingType.MIN_TRACK_DURATION, it.toInt())
                    }
                )
            }
            
            HorizontalDivider()
            
            // Download Artwork
            SettingSwitchItem(
                title = "Download Artwork",
                subtitle = "Download missing album artwork",
                icon = Icons.Default.Download,
                checked = settings.downloadArtwork,
                onCheckedChange = { 
                    onSettingChanged(LibrarySettingType.DOWNLOAD_ARTWORK, it)
                }
            )
            
            HorizontalDivider()
            
            // Download Lyrics
            SettingSwitchItem(
                title = "Download Lyrics",
                subtitle = "Download lyrics from online sources",
                icon = Icons.Default.Lyrics,
                checked = settings.downloadLyrics,
                onCheckedChange = { 
                    onSettingChanged(LibrarySettingType.DOWNLOAD_LYRICS, it)
                }
            )
        }
    }
}
