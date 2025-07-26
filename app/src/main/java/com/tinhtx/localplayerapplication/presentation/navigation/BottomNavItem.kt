package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun RowScope.LocalPlayerNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    destination: TopLevelDestination,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = stringResource(destination.titleResId),
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
