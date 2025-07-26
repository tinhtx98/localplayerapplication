package com.tinhtx.localplayerapplication.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry

object NavigationTransitions {
    
    // Standard slide transitions
    fun slideInFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    fun slideInFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    // Vertical transitions for modals
    fun slideInFromBottom(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(400))
    }
    
    fun slideOutToBottom(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(400, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(400))
    }
    
    fun slideInFromTop(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToTop(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    // Scale transitions for player
    fun scaleInEnter(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(400))
    }
    
    fun scaleOutExit(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(400, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(400))
    }
    
    // Fade transitions for subtle changes
    fun fadeIn(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(300))
    }
    
    fun fadeOut(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(300))
    }
    
    // Shared element-like transitions
    fun sharedAxisXEnter(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(400, delayMillis = 100)
        )
    }
    
    fun sharedAxisXExit(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(400, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    }
    
    fun sharedAxisYEnter(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(400, delayMillis = 100)
        )
    }
    
    fun sharedAxisYExit(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { -it / 3 },
            animationSpec = tween(400, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    }
}

// Extension functions for easier usage
fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}
