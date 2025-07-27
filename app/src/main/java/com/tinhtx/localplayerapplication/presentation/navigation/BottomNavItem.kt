package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null
)

object BottomNavItems {
    
    val Home = BottomNavItem(
        route = NavDestinations.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    val Library = BottomNavItem(
        route = NavDestinations.LIBRARY,
        label = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    )
    
    val Search = BottomNavItem(
        route = NavDestinations.SEARCH,
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    val Queue = BottomNavItem(
        route = NavDestinations.QUEUE,
        label = "Queue",
        selectedIcon = Icons.Filled.QueueMusic,
        unselectedIcon = Icons.Outlined.QueueMusic
    )
    
    val Profile = BottomNavItem(
        route = NavDestinations.PROFILE,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

// Predefined navigation sets for different app configurations
object NavigationSets {
    
    val Default = listOf(
        BottomNavItems.Home,
        BottomNavItems.Search,
        BottomNavItems.Library,
        BottomNavItems.Queue
    )
    
    val WithProfile = listOf(
        BottomNavItems.Home,
        BottomNavItems.Search,
        BottomNavItems.Library,
        BottomNavItems.Queue,
        BottomNavItems.Profile
    )
    
    val Compact = listOf(
        BottomNavItems.Home,
        BottomNavItems.Search,
        BottomNavItems.Library
    )
    
    val Extended = listOf(
        BottomNavItems.Home,
        BottomNavItem(
            route = NavDestinations.DISCOVER,
            label = "Discover",
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore
        ),
        BottomNavItems.Search,
        BottomNavItems.Library,
        BottomNavItems.Queue,
        BottomNavItem(
            route = NavDestinations.SETTINGS,
            label = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )
}

// Extension functions for BottomNavItem
fun BottomNavItem.withBadge(count: Int?): BottomNavItem {
    return this.copy(badgeCount = count)
}

fun BottomNavItem.withNews(hasNews: Boolean): BottomNavItem {
    return this.copy(hasNews = hasNews)
}

fun List<BottomNavItem>.updateBadges(badgeMap: Map<String, Int>): List<BottomNavItem> {
    return this.map { item ->
        val badgeCount = badgeMap[item.route]
        if (badgeCount != null && badgeCount > 0) {
            item.withBadge(badgeCount)
        } else {
            item.copy(badgeCount = null)
        }
    }
}

fun List<BottomNavItem>.updateNews(newsMap: Map<String, Boolean>): List<BottomNavItem> {
    return this.map { item ->
        val hasNews = newsMap[item.route] ?: false
        item.withNews(hasNews)
    }
}

// Navigation item categories
enum class NavItemCategory {
    PRIMARY,    // Main features
    SECONDARY,  // Supporting features
    USER,       // User-related
    SETTINGS    // Configuration
}

data class CategorizedNavItem(
    val item: BottomNavItem,
    val category: NavItemCategory,
    val priority: Int = 0
)

object CategorizedNavItems {
    
    val All = listOf(
        CategorizedNavItem(BottomNavItems.Home, NavItemCategory.PRIMARY, 1),
        CategorizedNavItem(BottomNavItems.Search, NavItemCategory.PRIMARY, 2),
        CategorizedNavItem(BottomNavItems.Library, NavItemCategory.PRIMARY, 3),
        CategorizedNavItem(BottomNavItems.Queue, NavItemCategory.SECONDARY, 1),
        CategorizedNavItem(BottomNavItems.Profile, NavItemCategory.USER, 1),
        CategorizedNavItem(
            BottomNavItem(
                route = NavDestinations.FAVORITES,
                label = "Favorites",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder
            ),
            NavItemCategory.SECONDARY, 2
        ),
        CategorizedNavItem(
            BottomNavItem(
                route = NavDestinations.RECENTLY_PLAYED,
                label = "Recent",
                selectedIcon = Icons.Filled.History,
                unselectedIcon = Icons.Outlined.History
            ),
            NavItemCategory.SECONDARY, 3
        ),
        CategorizedNavItem(
            BottomNavItem(
                route = NavDestinations.SETTINGS,
                label = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            ),
            NavItemCategory.SETTINGS, 1
        )
    )
    
    fun getByCategory(category: NavItemCategory): List<BottomNavItem> {
        return All.filter { it.category == category }
            .sortedBy { it.priority }
            .map { it.item }
    }
    
    fun getPrimaryItems(maxCount: Int = 4): List<BottomNavItem> {
        return getByCategory(NavItemCategory.PRIMARY).take(maxCount)
    }
    
    fun getSecondaryItems(maxCount: Int = 2): List<BottomNavItem> {
        return getByCategory(NavItemCategory.SECONDARY).take(maxCount)
    }
}

// Adaptive navigation configuration
data class AdaptiveNavConfig(
    val compactItems: List<BottomNavItem>,
    val mediumItems: List<BottomNavItem>,
    val expandedItems: List<BottomNavItem>
)

object AdaptiveNavConfigs {
    
    val Default = AdaptiveNavConfig(
        compactItems = NavigationSets.Compact,
        mediumItems = NavigationSets.Default,
        expandedItems = NavigationSets.WithProfile
    )
    
    val Extended = AdaptiveNavConfig(
        compactItems = NavigationSets.Default,
        mediumItems = NavigationSets.WithProfile,
        expandedItems = NavigationSets.Extended
    )
    
    val Minimal = AdaptiveNavConfig(
        compactItems = listOf(BottomNavItems.Home, BottomNavItems.Search),
        mediumItems = NavigationSets.Compact,
        expandedItems = NavigationSets.Default
    )
}

// Navigation item validation
object NavItemValidator {
    
    fun isValidRoute(route: String): Boolean {
        return route.isNotBlank() && 
               route.startsWith("/") && 
               !route.contains("//")
    }
    
    fun validateNavItems(items: List<BottomNavItem>): List<String> {
        val errors = mutableListOf<String>()
        
        // Check for duplicate routes
        val routes = items.map { it.route }
        val duplicates = routes.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            errors.add("Duplicate routes found: ${duplicates.keys}")
        }
        
        // Check for invalid routes
        items.forEach { item ->
            if (!isValidRoute(item.route)) {
                errors.add("Invalid route: ${item.route}")
            }
        }
        
        // Check for empty labels
        items.forEach { item ->
            if (item.label.isBlank()) {
                errors.add("Empty label for route: ${item.route}")
            }
        }
        
        return errors
    }
}
