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
fun NotificationSettingsSection(
    settings: NotificationSettings,
    onSettingChanged: (NotificationSettingType, Any) -> Unit,
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
            // Show Controls
            SettingSwitchItem(
                title = "Show Controls",
                subtitle = "Display playback controls in notification",
                icon = Icons.Default.ControlCamera,
                checked = settings.showControls,
                onCheckedChange = { 
                    onSettingChanged(NotificationSettingType.SHOW_CONTROLS, it)
                }
            )
            
            HorizontalDivider()
            
            // Show Album Art
            SettingSwitchItem(
                title = "Show Album Art",
                subtitle = "Display album artwork in notification",
                icon = Icons.Default.Image,
                checked = settings.showAlbumArt,
                onCheckedChange = { 
                    onSettingChanged(NotificationSettingType.SHOW_ALBUM_ART, it)
                }
            )
            
            HorizontalDivider()
            
            // Colored Notification
            SettingSwitchItem(
                title = "Colored Notification",
                subtitle = "Use album colors for notification",
                icon = Icons.Default.Palette,
                checked = settings.coloredNotification,
                onCheckedChange = { 
                    onSettingChanged(NotificationSettingType.COLORED_NOTIFICATION, it)
                }
            )
            
            HorizontalDivider()
            
            // Notification Priority
            SettingDropdownItem(
                title = "Priority",
                subtitle = "Notification importance level",
                icon = Icons.Default.PriorityHigh,
                value = settings.priority.displayName,
                options = NotificationPriority.values().map { it.displayName },
                onValueChange = { displayName ->
                    val priority = NotificationPriority.values().find { it.displayName == displayName }
                    priority?.let { onSettingChanged(NotificationSettingType.PRIORITY, it) }
                }
            )
            
            HorizontalDivider()
            
            // Show on Lock Screen
            SettingSwitchItem(
                title = "Show on Lock Screen",
                subtitle = "Display controls on lock screen",
                icon = Icons.Default.Lock,
                checked = settings.showOnLockScreen,
                onCheckedChange = { 
                    onSettingChanged(NotificationSettingType.SHOW_ON_LOCK_SCREEN, it)
                }
            )
        }
    }
}
