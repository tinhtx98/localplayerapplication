package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdvancedSettingsSection(
    enableAnalytics: Boolean,
    enableCrashReporting: Boolean,
    enableExperimentalFeatures: Boolean,
    debugMode: Boolean,
    showDeveloperOptions: Boolean,
    onToggleAnalytics: () -> Unit,
    onToggleCrashReporting: () -> Unit,
    onToggleExperimentalFeatures: () -> Unit,
    onToggleDebugMode: () -> Unit,
    onToggleDeveloperOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Advanced",
            icon = Icons.Default.Settings
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Analytics
            SettingSwitchItem(
                title = "Analytics",
                subtitle = "Help improve the app by sharing usage data",
                icon = Icons.Default.Analytics,
                checked = enableAnalytics,
                onCheckedChange = { onToggleAnalytics() }
            )

            // Crash reporting
            SettingSwitchItem(
                title = "Crash Reporting",
                subtitle = "Automatically report crashes to help fix bugs",
                icon = Icons.Default.BugReport,
                checked = enableCrashReporting,
                onCheckedChange = { onToggleCrashReporting() }
            )

            // Experimental features
            SettingSwitchItem(
                title = "Experimental Features",
                subtitle = "Enable new features that are still in development",
                icon = Icons.Default.Science,
                checked = enableExperimentalFeatures,
                onCheckedChange = { onToggleExperimentalFeatures() }
            )

            // Debug mode
            SettingSwitchItem(
                title = "Debug Mode",
                subtitle = "Show additional debugging information",
                icon = Icons.Default.Code,
                checked = debugMode,
                onCheckedChange = { onToggleDebugMode() }
            )

            // Developer options
            SettingItem(
                title = "Developer Options",
                subtitle = if (showDeveloperOptions) "Hide developer settings" else "Show developer settings",
                icon = Icons.Default.DeveloperMode,
                onClick = onToggleDeveloperOptions
            )

            // Developer options (shown when enabled)
            if (showDeveloperOptions) {
                DeveloperOptionsSection()
            }
        }
    }
}

@Composable
private fun DeveloperOptionsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Force crash (for testing)
        SettingItem(
            title = "Force Crash",
            subtitle = "Trigger a crash for testing crash reporting",
            icon = Icons.Default.Warning,
            onClick = {
                // TODO: Implement force crash for testing
                throw RuntimeException("Test crash triggered from settings")
            }
        )

        // Clear all data
        SettingItem(
            title = "Clear All Data",
            subtitle = "Remove all app data (for testing)",
            icon = Icons.Default.DeleteForever,
            onClick = {
                // TODO: Implement clear all data
            }
        )

        // Show logs
        SettingItem(
            title = "View Logs",
            subtitle = "Show application logs",
            icon = Icons.Default.Article,
            onClick = {
                // TODO: Navigate to logs screen
            }
        )

        // Database info
        SettingItem(
            title = "Database Info",
            subtitle = "Show database statistics",
            icon = Icons.Default.Storage,
            onClick = {
                // TODO: Show database info dialog
            }
        )
    }
}
