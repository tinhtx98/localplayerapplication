package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.settings.*

@Composable
fun AppearanceSettingsSection(
    theme: AppTheme,
    isDynamicColor: Boolean,
    accentColor: AccentColor,
    fontSize: FontSize,
    gridSize: GridSize,
    onThemeClick: () -> Unit,
    onToggleDynamicColor: () -> Unit,
    onAccentColorChange: (AccentColor) -> Unit,
    onFontSizeChange: (FontSize) -> Unit,
    onGridSizeChange: (GridSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Appearance",
            icon = Icons.Default.Palette
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme selection
            SettingItem(
                title = "Theme",
                subtitle = theme.getThemeDisplayName(),
                icon = Icons.Default.DarkMode,
                onClick = onThemeClick
            )

            // Dynamic color (Android 12+)
            SettingSwitchItem(
                title = "Dynamic Color",
                subtitle = "Use colors from your wallpaper",
                icon = Icons.Default.ColorLens,
                checked = isDynamicColor,
                onCheckedChange = { onToggleDynamicColor() }
            )

            // Accent color selection
            if (!isDynamicColor) {
                SettingDropdownItem(
                    title = "Accent Color",
                    subtitle = "Choose your preferred accent color",
                    icon = Icons.Default.Brush,
                    selectedOption = accentColor.displayName,
                    options = AccentColor.values().map { it.displayName },
                    onOptionSelected = { colorName ->
                        val selectedColor = AccentColor.values().find { it.displayName == colorName }
                        selectedColor?.let { onAccentColorChange(it) }
                    }
                )
            }

            // Font size
            SettingDropdownItem(
                title = "Font Size",
                subtitle = "Adjust text size throughout the app",
                icon = Icons.Default.FormatSize,
                selectedOption = fontSize.displayName,
                options = FontSize.values().map { it.displayName },
                onOptionSelected = { sizeName ->
                    val selectedSize = FontSize.values().find { it.displayName == sizeName }
                    selectedSize?.let { onFontSizeChange(it) }
                }
            )

            // Grid size
            SettingDropdownItem(
                title = "Grid Size",
                subtitle = "Change the size of album and playlist grids",
                icon = Icons.Default.GridView,
                selectedOption = gridSize.displayName,
                options = GridSize.values().map { it.displayName },
                onOptionSelected = { sizeName ->
                    val selectedSize = GridSize.values().find { it.displayName == sizeName }
                    selectedSize?.let { onGridSizeChange(it) }
                }
            )
        }
    }
}

private fun AppTheme.getThemeDisplayName(): String = when (this) {
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"  
    AppTheme.SYSTEM -> "Follow System"
}
