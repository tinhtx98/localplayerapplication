package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry

object NavigationTransitions {
    
    // Default transition durations
    const val DEFAULT_ENTER_DURATION = 400
    const val DEFAULT_EXIT_DURATION = 300
    const val FAST_ENTER_DURATION = 250
    const val FAST_EXIT_DURATION = 200
    const val SLOW_ENTER_DURATION = 600
    const val SLOW_EXIT_DURATION = 500
    
    // Standard slide transitions
    fun slideInFromRight(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DEFAULT_ENTER_DURATION))
    }
    
    fun slideOutToLeft(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DEFAULT_EXIT_DURATION))
    }
    
    fun slideInFromLeft(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DEFAULT_ENTER_DURATION))
    }
    
    fun slideOutToRight(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DEFAULT_EXIT_DURATION))
    }
    
    // Vertical slide transitions
    fun slideInFromBottom(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DEFAULT_ENTER_DURATION))
    }
    
    fun slideOutToBottom(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DEFAULT_EXIT_DURATION))
    }
    
    fun slideInFromTop(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DEFAULT_ENTER_DURATION))
    }
    
    fun slideOutToTop(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DEFAULT_EXIT_DURATION))
    }
    
    // Scale transitions
    fun scaleIn(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DEFAULT_ENTER_DURATION))
    }
    
    fun scaleOut(): ExitTransition {
        return scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DEFAULT_EXIT_DURATION))
    }
    
    // Fade transitions
    fun fadeIn(): EnterTransition {
        return fadeIn(
            animationSpec = tween(DEFAULT_ENTER_DURATION, easing = LinearEasing)
        )
    }
    
    fun fadeOut(): ExitTransition {
        return fadeOut(
            animationSpec = tween(DEFAULT_EXIT_DURATION, easing = LinearEasing)
        )
    }
    
    // No transition
    fun noTransition(): Pair<EnterTransition, ExitTransition> {
        return EnterTransition.None to ExitTransition.None
    }
}

// Transition sets for different navigation scenarios
object TransitionSets {
    
    // Standard forward navigation
    val Forward = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.slideInFromRight() to NavigationTransitions.slideOutToLeft()
    }
    
    // Standard backward navigation
    val Backward = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.slideInFromLeft() to NavigationTransitions.slideOutToRight()
    }
    
    // Modal presentation (bottom sheet style)
    val Modal = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.slideInFromBottom() to NavigationTransitions.slideOutToBottom()
    }
    
    // Overlay presentation
    val Overlay = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.scaleIn() to NavigationTransitions.scaleOut()
    }
    
    // Fade transition for same-level navigation
    val Fade = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.fadeIn() to NavigationTransitions.fadeOut()
    }
    
    // Fast transitions for tab switching
    val FastTab = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        fadeIn(animationSpec = tween(NavigationTransitions.FAST_ENTER_DURATION)) to 
        fadeOut(animationSpec = tween(NavigationTransitions.FAST_EXIT_DURATION))
    }
    
    // No animation
    val None = AnimatedContentTransitionScope<NavBackStackEntry>.() -> Pair<EnterTransition, ExitTransition> {
        NavigationTransitions.noTransition()
    }
}

// Route-specific transition configuration
enum class TransitionType {
    FORWARD,
    BACKWARD,
    MODAL,
    OVERLAY,
    FADE,
    FAST_TAB,
    NONE
}

data class RouteTransitionConfig(
    val enterTransition: TransitionType = TransitionType.FORWARD,
    val exitTransition: TransitionType = TransitionType.FORWARD,
    val popEnterTransition: TransitionType = TransitionType.BACKWARD,
    val popExitTransition: TransitionType = TransitionType.BACKWARD
)

object RouteTransitions {
    
    private val configurations = mapOf(
        // Main destinations - fast tab switching
        NavDestinations.HOME to RouteTransitionConfig(
            enterTransition = TransitionType.FAST_TAB,
            exitTransition = TransitionType.FAST_TAB,
            popEnterTransition = TransitionType.FAST_TAB,
            popExitTransition = TransitionType.FAST_TAB
        ),
        
        NavDestinations.SEARCH to RouteTransitionConfig(
            enterTransition = TransitionType.FAST_TAB,
            exitTransition = TransitionType.FAST_TAB,
            popEnterTransition = TransitionType.FAST_TAB,
            popExitTransition = TransitionType.FAST_TAB
        ),
        
        NavDestinations.LIBRARY to RouteTransitionConfig(
            enterTransition = TransitionType.FAST_TAB,
            exitTransition = TransitionType.FAST_TAB,
            popEnterTransition = TransitionType.FAST_TAB,
            popExitTransition = TransitionType.FAST_TAB
        ),
        
        NavDestinations.QUEUE to RouteTransitionConfig(
            enterTransition = TransitionType.FAST_TAB,
            exitTransition = TransitionType.FAST_TAB,
            popEnterTransition = TransitionType.FAST_TAB,
            popExitTransition = TransitionType.FAST_TAB
        ),
        
        // Detail destinations - forward/backward
        NavDestinations.ALBUM_DETAIL to RouteTransitionConfig(
            enterTransition = TransitionType.FORWARD,
            exitTransition = TransitionType.FORWARD,
            popEnterTransition = TransitionType.BACKWARD,
            popExitTransition = TransitionType.BACKWARD
        ),
        
        NavDestinations.ARTIST_DETAIL to RouteTransitionConfig(
            enterTransition = TransitionType.FORWARD,
            exitTransition = TransitionType.FORWARD,
            popEnterTransition = TransitionType.BACKWARD,
            popExitTransition = TransitionType.BACKWARD
        ),
        
        NavDestinations.PLAYLIST_DETAIL to RouteTransitionConfig(
            enterTransition = TransitionType.FORWARD,
            exitTransition = TransitionType.FORWARD,
            popEnterTransition = TransitionType.BACKWARD,
            popExitTransition = TransitionType.BACKWARD
        ),
        
        // Player - modal presentation
        NavDestinations.PLAYER to RouteTransitionConfig(
            enterTransition = TransitionType.MODAL,
            exitTransition = TransitionType.MODAL,
            popEnterTransition = TransitionType.MODAL,
            popExitTransition = TransitionType.MODAL
        ),
        
        // Settings - forward/backward
        NavDestinations.SETTINGS to RouteTransitionConfig(
            enterTransition = TransitionType.FORWARD,
            exitTransition = TransitionType.FORWARD,
            popEnterTransition = TransitionType.BACKWARD,
            popExitTransition = TransitionType.BACKWARD
        ),
        
        // Equalizer - overlay
        NavDestinations.EQUALIZER to RouteTransitionConfig(
            enterTransition = TransitionType.OVERLAY,
            exitTransition = TransitionType.OVERLAY,
            popEnterTransition = TransitionType.OVERLAY,
            popExitTransition = TransitionType.OVERLAY
        )
    )
    
    fun getConfig(route: String): RouteTransitionConfig {
        return configurations[route] ?: RouteTransitionConfig()
    }
    
    fun getEnterTransition(route: String, isPop: Boolean = false): EnterTransition {
        val config = getConfig(route)
        val transitionType = if (isPop) config.popEnterTransition else config.enterTransition
        return getTransitionForType(transitionType).first
    }
    
    fun getExitTransition(route: String, isPop: Boolean = false): ExitTransition {
        val config = getConfig(route)
        val transitionType = if (isPop) config.popExitTransition else config.exitTransition
        return getTransitionForType(transitionType).second
    }
    
    private fun getTransitionForType(type: TransitionType): Pair<EnterTransition, ExitTransition> {
        return when (type) {
            TransitionType.FORWARD -> NavigationTransitions.slideInFromRight() to NavigationTransitions.slideOutToLeft()
            TransitionType.BACKWARD -> NavigationTransitions.slideInFromLeft() to NavigationTransitions.slideOutToRight()
            TransitionType.MODAL -> NavigationTransitions.slideInFromBottom() to NavigationTransitions.slideOutToBottom()
            TransitionType.OVERLAY -> NavigationTransitions.scaleIn() to NavigationTransitions.scaleOut()
            TransitionType.FADE -> NavigationTransitions.fadeIn() to NavigationTransitions.fadeOut()
            TransitionType.FAST_TAB -> fadeIn(animationSpec = tween(NavigationTransitions.FAST_ENTER_DURATION)) to 
                                      fadeOut(animationSpec = tween(NavigationTransitions.FAST_EXIT_DURATION))
            TransitionType.NONE -> NavigationTransitions.noTransition()
        }
    }
}

// Shared element transitions (for future implementation)
object SharedElementTransitions {
    
    fun albumArtSharedElement(): SharedTransitionScope.() -> EnterTransition {
        return {
            // TODO: Implement shared element transition for album art
            NavigationTransitions.scaleIn()
        }
    }
    
    fun songItemSharedElement(): SharedTransitionScope.() -> EnterTransition {
        return {
            // TODO: Implement shared element transition for song items
            NavigationTransitions.fadeIn()
        }
    }
}

// Transition utilities
object TransitionUtils {
    
    fun createCustomSlide(
        direction: SlideDirection,
        duration: Int = NavigationTransitions.DEFAULT_ENTER_DURATION,
        easing: Easing = FastOutSlowInEasing
    ): Pair<EnterTransition, ExitTransition> {
        return when (direction) {
            SlideDirection.LEFT_TO_RIGHT -> {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(duration, easing = easing)
                ) to slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(duration, easing = easing)
                )
            }
            SlideDirection.RIGHT_TO_LEFT -> {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(duration, easing = easing)
                ) to slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(duration, easing = easing)
                )
            }
            SlideDirection.TOP_TO_BOTTOM -> {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(duration, easing = easing)
                ) to slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(duration, easing = easing)
                )
            }
            SlideDirection.BOTTOM_TO_TOP -> {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(duration, easing = easing)
                ) to slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(duration, easing = easing)
                )
            }
        }
    }
    
    fun createCustomFade(
        duration: Int = NavigationTransitions.DEFAULT_ENTER_DURATION,
        easing: Easing = LinearEasing
    ): Pair<EnterTransition, ExitTransition> {
        return fadeIn(
            animationSpec = tween(duration, easing = easing)
        ) to fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun createCustomScale(
        initialScale: Float = 0.8f,
        targetScale: Float = 1.2f,
        duration: Int = NavigationTransitions.DEFAULT_ENTER_DURATION,
        easing: Easing = FastOutSlowInEasing
    ): Pair<EnterTransition, ExitTransition> {
        return scaleIn(
            initialScale = initialScale,
            animationSpec = tween(duration, easing = easing)
        ) to scaleOut(
            targetScale = targetScale,
            animationSpec = tween(duration, easing = easing)
        )
    }
}

enum class SlideDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP
}
