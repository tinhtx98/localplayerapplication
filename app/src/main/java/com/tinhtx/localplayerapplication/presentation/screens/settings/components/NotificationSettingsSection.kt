package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.settings.NotificationAction

@Composable
fun NotificationSettingsSection(
    showNotifications: Boolean,
    showLockScreenControls: Boolean,
    showAlbumArt: Boolean,
    compactNotification: Boolean,
    notificationActions: List<NotificationAction>,
    onToggleNotifications: () -> Unit,
    onToggleLockScreenControls: () -> Unit,
    onToggleAlbumArt: () -> Unit,
    onToggleCompactNotification: () -> Unit,
    onCustomizeActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Notifications",
            icon = Icons.Default.Notifications
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show notifications
            SettingSwitchItem(
                title = "Show Notifications",
                subtitle = "Display playback controls in notification panel",
                icon = Icons.Default.Notifications,
                checked = showNotifications,
                onCheckedChange = { onToggleNotifications() }
            )

            // Lock screen controls
            SettingSwitchItem(
                title = "Lock Screen Controls",
                subtitle = "Show playback controls on lock screen",
                icon = Icons.Default.Lock,
                checked = showLockScreenControls,
                enabled = showNotifications,
                onCheckedChange = { onToggleLockScreenControls() }
            )

            // Album art in notification
            SettingSwitchItem(
                title = "Show Album Art",
                subtitle = "Display album artwork in notifications",
                icon = Icons.Default.Image,
                checked = showAlbumArt,
                enabled = showNotifications,
                onCheckedChange = { onToggleAlbumArt() }
            )

            // Compact notification
            SettingSwitchItem(
                title = "Compact Notification",
                subtitle = "Use smaller notification layout",
                icon = Icons.Default.Compress,
                checked = compactNotification,
                enabled = showNotifications,
                onCheckedChange = { onToggleCompactNotification() }
            )

            // Customize actions
            SettingItem(
                title = "Notification Actions",
                subtitle = "${notificationActions.size} actions configured",
                icon = Icons.Default.TouchApp,
                enabled = showNotifications,
                onClick = onCustomizeActions
            )
        }
    }
}
