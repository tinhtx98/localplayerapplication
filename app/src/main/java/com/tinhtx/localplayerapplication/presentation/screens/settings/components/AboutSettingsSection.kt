package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutSettingsSection(
    appVersion: String,
    buildNumber: String,
    appSize: String,
    onViewChangelog: () -> Unit,
    onViewLicenses: () -> Unit,
    onContactSupport: () -> Unit,
    onVisitGitHub: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "About",
            icon = Icons.Default.Info
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App info
            SettingItem(
                title = "App Information",
                subtitle = "Version $appVersion • $appSize",
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout
            )

            // What's new
            SettingItem(
                title = "What's New",
                subtitle = "View changelog and recent updates",
                icon = Icons.Default.NewReleases,
                onClick = onViewChangelog
            )

            // Open source licenses
            SettingItem(
                title = "Open Source Licenses",
                subtitle = "View licenses for third-party libraries",
                icon = Icons.Default.Description,
                onClick = onViewLicenses
            )

            // Privacy policy
            SettingItem(
                title = "Privacy Policy",
                subtitle = "How we handle your data",
                icon = Icons.Default.Privacy,
                onClick = {
                    // TODO: Open privacy policy
                }
            )

            // Terms of service
            SettingItem(
                title = "Terms of Service",
                subtitle = "Terms and conditions of use",
                icon = Icons.Default.Gavel,
                onClick = {
                    // TODO: Open terms of service
                }
            )

            // Contact support
            SettingItem(
                title = "Contact Support",
                subtitle = "Get help or report issues",
                icon = Icons.Default.Support,
                onClick = onContactSupport
            )

            // Rate app
            SettingItem(
                title = "Rate This App",
                subtitle = "Leave a review on Google Play",
                icon = Icons.Default.Star,
                onClick = {
                    // TODO: Open app store rating
                }
            )

            // GitHub
            SettingItem(
                title = "Source Code",
                subtitle = "View project on GitHub",
                icon = Icons.Default.Code,
                onClick = onVisitGitHub
            )
        }
    }
}
