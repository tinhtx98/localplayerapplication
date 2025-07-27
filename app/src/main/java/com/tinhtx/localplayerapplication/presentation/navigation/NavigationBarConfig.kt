package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.runtime.*
import com.tinhtx.localplayerapplication.presentation.components.common.ScreenSize

data class NavigationBarConfig(
    val showLabels: Boolean = true,
    val showBadges: Boolean = true,
    val animationDuration: Int = 300,
    val style: NavigationBarStyle = NavigationBarStyle.Auto,
    val items: List<BottomNavItem> = NavigationSets.Default,
    val maxItems: Int = 5,
    val enableHapticFeedback: Boolean = true,
    val compactThreshold: ScreenSize = ScreenSize.Compact
)

enum class NavigationBarStyle {
    Auto,       // Automatically choose based on screen size
    Bottom,     // Always use bottom navigation
    Rail,       // Always use navigation rail
    Drawer,     // Always use navigation drawer
    Hidden      // Hide navigation
}

class NavigationBarConfigManager {
    
    private var _config by mutableStateOf(NavigationBarConfig())
    val config: NavigationBarConfig get() = _config
    
    fun updateConfig(newConfig: NavigationBarConfig) {
        _config = newConfig
    }
    
    fun updateStyle(style: NavigationBarStyle) {
        _config = _config.copy(style = style)
    }
    
    fun updateItems(items: List<BottomNavItem>) {
        _config = _config.copy(items = items.take(_config.maxItems))
    }
    
    fun toggleLabels() {
        _config = _config.copy(showLabels = !_config.showLabels)
    }
    
    fun toggleBadges() {
        _config = _config.copy(showBadges = !_config.showBadges)
    }
    
    fun toggleHapticFeedback() {
        _config = _config.copy(enableHapticFeedback = !_config.enableHapticFeedback)
    }
    
    // Adaptive configuration based on screen size
    fun getAdaptiveConfig(screenSize: ScreenSize): NavigationBarConfig {
        return when (screenSize) {
            ScreenSize.Compact -> _config.copy(
                items = AdaptiveNavConfigs.Default.compactItems,
                showLabels = true,
                style = if (_config.style == NavigationBarStyle.Auto) NavigationBarStyle.Bottom else _config.style
            )
            ScreenSize.Medium -> _config.copy(
                items = AdaptiveNavConfigs.Default.mediumItems,
                showLabels = true,
                style = if (_config.style == NavigationBarStyle.Auto) NavigationBarStyle.Rail else _config.style
            )
            else -> _config.copy(
                items = AdaptiveNavConfigs.Default.expandedItems,
                showLabels = true,
                style = if (_config.style == NavigationBarStyle.Auto) NavigationBarStyle.Drawer else _config.style
            )
        }
    }
}

// Predefined configurations
object NavigationBarConfigs {
    
    val Minimal = NavigationBarConfig(
        showLabels = false,
        items = AdaptiveNavConfigs.Minimal.compactItems,
        maxItems = 3
    )
    
    val Standard = NavigationBarConfig(
        showLabels = true,
        items = NavigationSets.Default,
        maxItems = 4
    )
    
    val Extended = NavigationBarConfig(
        showLabels = true,
        items = NavigationSets.Extended,
        maxItems = 6
    )
    
    val AccessibilityOptimized = NavigationBarConfig(
        showLabels = true,
        showBadges = true,
        animationDuration = 500, // Slower animations
        enableHapticFeedback = true,
        items = NavigationSets.Default
    )
    
    val PerformanceOptimized = NavigationBarConfig(
        showLabels = true,
        showBadges = false,
        animationDuration = 150, // Faster animations
        enableHapticFeedback = false,
        items = NavigationSets.Compact
    )
}

// Configuration validation
object NavigationBarConfigValidator {
    
    fun validate(config: NavigationBarConfig): List<String> {
        val errors = mutableListOf<String>()
        
        // Validate animation duration
        if (config.animationDuration < 0) {
            errors.add("Animation duration cannot be negative")
        }
        if (config.animationDuration > 2000) {
            errors.add("Animation duration too long (max 2000ms)")
        }
        
        // Validate max items
        if (config.maxItems < 1) {
            errors.add("Max items must be at least 1")
        }
        if (config.maxItems > 10) {
            errors.add("Max items should not exceed 10")
        }
        
        // Validate items count
        if (config.items.size > config.maxItems) {
            errors.add("Items count (${config.items.size}) exceeds maxItems (${config.maxItems})")
        }
        
        // Validate items
        val itemErrors = NavItemValidator.validateNavItems(config.items)
        errors.addAll(itemErrors)
        
        return errors
    }
    
    fun isValid(config: NavigationBarConfig): Boolean {
        return validate(config).isEmpty()
    }
}

// Configuration persistence
interface NavigationBarConfigRepository {
    suspend fun saveConfig(config: NavigationBarConfig)
    suspend fun loadConfig(): NavigationBarConfig?
    suspend fun clearConfig()
}

// Dynamic configuration updates
@Composable
fun rememberNavigationBarConfig(
    initialConfig: NavigationBarConfig = NavigationBarConfigs.Standard
): NavigationBarConfigManager {
    val manager = remember { NavigationBarConfigManager() }
    
    LaunchedEffect(initialConfig) {
        manager.updateConfig(initialConfig)
    }
    
    return manager
}

// Configuration helpers
fun NavigationBarConfig.withScreenSizeAdaptation(screenSize: ScreenSize): NavigationBarConfig {
    return when (screenSize) {
        ScreenSize.Compact -> copy(
            items = items.take(3),
            showLabels = true
        )
        ScreenSize.Medium -> copy(
            items = items.take(4),
            showLabels = true
        )
        else -> copy(
            showLabels = true
        )
    }
}

fun NavigationBarConfig.withPerformanceOptimization(): NavigationBarConfig {
    return copy(
        animationDuration = 150,
        enableHapticFeedback = false,
        showBadges = false
    )
}

fun NavigationBarConfig.withAccessibilityOptimization(): NavigationBarConfig {
    return copy(
        showLabels = true,
        showBadges = true,
        animationDuration = 500,
        enableHapticFeedback = true
    )
}

// Configuration presets for different use cases
enum class NavigationBarPreset {
    DEFAULT,
    MINIMAL,
    EXTENDED,
    ACCESSIBILITY,
    PERFORMANCE
}

fun getPresetConfig(preset: NavigationBarPreset): NavigationBarConfig {
    return when (preset) {
        NavigationBarPreset.DEFAULT -> NavigationBarConfigs.Standard
        NavigationBarPreset.MINIMAL -> NavigationBarConfigs.Minimal
        NavigationBarPreset.EXTENDED -> NavigationBarConfigs.Extended
        NavigationBarPreset.ACCESSIBILITY -> NavigationBarConfigs.AccessibilityOptimized
        NavigationBarPreset.PERFORMANCE -> NavigationBarConfigs.PerformanceOptimized
    }
}
