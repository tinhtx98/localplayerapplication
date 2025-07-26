package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdvancedSettingsSection(
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
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
            // Export Settings
            SettingActionItem(
                title = "Export Settings",
                subtitle = "Backup your settings to file",
                icon = Icons.Default.Upload,
                actionText = "Export",
                onClick = onExportSettings
            )
            
            HorizontalDivider()
            
            // Import Settings
            SettingActionItem(
                title = "Import Settings",
                subtitle = "Restore settings from backup file",
                icon = Icons.Default.Download,
                actionText = "Import",
                onClick = onImportSettings
            )
        }
    }
}
