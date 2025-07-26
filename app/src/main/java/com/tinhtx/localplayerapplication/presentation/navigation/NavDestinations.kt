package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.tinhtx.localplayerapplication.R

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Player : Screen("player")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object Queue : Screen("queue")
    object Favorites : Screen("favorites")
}

enum class TopLevelDestination(
    val route: String,
    @StringRes val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Screen.Home.route,
        titleResId = R.string.home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    LIBRARY(
        route = Screen.Library.route,
        titleResId = R.string.library,
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    ),
    SEARCH(
        route = Screen.Search.route,
        titleResId = R.string.search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    SETTINGS(
        route = Screen.Settings.route,
        titleResId = R.string.settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}
