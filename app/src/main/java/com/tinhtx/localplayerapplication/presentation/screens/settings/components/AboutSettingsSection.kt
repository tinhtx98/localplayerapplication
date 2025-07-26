package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.AppInfo

@Composable
fun AboutSettingsSection(
    appInfo: AppInfo,
    onShowAboutDialog: () -> Unit,
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
            // App Version
            SettingClickableItem(
                title = "App Version",
                subtitle = "Version ${appInfo.versionName} (${appInfo.versionCode})",
                icon = Icons.Default.Info,
                value = "View Details",
                onClick = onShowAboutDialog
            )
            
            HorizontalDivider()
            
            // Privacy Policy
            SettingActionItem(
                title = "Privacy Policy",
                subtitle = "Read our privacy policy",
                icon = Icons.Default.PrivacyTip,
                actionText = "View",
                onClick = { /* Open privacy policy */ }
            )
            
            HorizontalDivider()
            
            // Terms of Service
            SettingActionItem(
                title = "Terms of Service",
                subtitle = "Read our terms of service",
                icon = Icons.Default.Description,
                actionText = "View",
                onClick = { /* Open terms of service */ }
            )
            
            HorizontalDivider()
            
            // Open Source Licenses
            SettingActionItem(
                title = "Open Source Licenses",
                subtitle = "Third-party library licenses",
                icon = Icons.Default.Code,
                actionText = "View",
                onClick = { /* Open licenses */ }
            )
        }
    }
}
