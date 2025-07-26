package com.tinhtx.localplayerapplication.presentation.shared.extension

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Compose extension functions and utilities
 */

// Modifier extensions for common patterns
fun Modifier.clickableWithRipple(
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    onClick: () -> Unit
) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = bounded, radius = radius, color = color),
        onClick = onClick
    )
}

fun Modifier.clickableWithoutRipple(onClick: () -> Unit) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

fun Modifier.clickableWithRole(
    role: Role,
    onClick: () -> Unit
) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(),
        role = role,
        onClick = onClick
    )
}

// Conditional modifiers
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) then(modifier()) else this

fun Modifier.conditionalThen(
    condition: Boolean,
    modifier: Modifier
): Modifier = if (condition) then(modifier) else this

// Layout extensions
fun Modifier.fillMaxWidthConditional(condition: Boolean) = 
    if (condition) fillMaxWidth() else this

fun Modifier.fillMaxHeightConditional(condition: Boolean) = 
    if (condition) fillMaxHeight() else this

fun Modifier.aspectRatioConditional(ratio: Float, condition: Boolean) = 
    if (condition) aspectRatio(ratio) else this

// Padding extensions
fun Modifier.paddingHorizontal(horizontal: Dp) = 
    padding(horizontal = horizontal)

fun Modifier.paddingVertical(vertical: Dp) = 
    padding(vertical = vertical)

fun Modifier.paddingTop(top: Dp) = 
    padding(top = top)

fun Modifier.paddingBottom(bottom: Dp) = 
    padding(bottom = bottom)

fun Modifier.paddingStart(start: Dp) = 
    padding(start = start)

fun Modifier.paddingEnd(end: Dp) = 
    padding(end = end)

// Animation extensions
fun Modifier.shimmer(
    durationMillis: Int = 1000,
    colors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )
) = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = colors,
            start = androidx.compose.ui.geometry.Offset(translateAnimation - 1000f, 0f),
            end = androidx.compose.ui.geometry.Offset(translateAnimation, 0f)
        )
    )
}

fun Modifier.bounce(
    scale: Float = 0.9f,
    durationMillis: Int = 100
) = composed {
    var pressed by remember { mutableStateOf(false) }
    val scaleAnimation by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = tween(durationMillis),
        label = "bounce_scale"
    )
    
    graphicsLayer(scaleX = scaleAnimation, scaleY = scaleAnimation)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            pressed = true
        }
}

fun Modifier.pulse(
    scale: Float = 1.1f,
    durationMillis: Int = 1000
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scaleAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = scale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    graphicsLayer(scaleX = scaleAnimation, scaleY = scaleAnimation)
}

// Shape extensions
fun Modifier.roundedCorners(radius: Dp) = 
    clip(androidx.compose.foundation.shape.RoundedCornerShape(radius))

fun Modifier.roundedCornersTop(radius: Dp) = 
    clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = radius, topEnd = radius))

fun Modifier.roundedCornersBottom(radius: Dp) = 
    clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = radius, bottomEnd = radius))

fun Modifier.circleClip() = 
    clip(androidx.compose.foundation.shape.CircleShape)

// Border extensions
fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape
) = composed {
    drawBehind {
        drawRoundRect(
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = width.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f), 0f
                )
            )
        )
    }.clip(shape)
}

// Scrollbar extensions
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        val isScrollingUp = if (previousIndex != firstVisibleItemIndex) {
            previousIndex > firstVisibleItemIndex
        } else {
            previousScrollOffset >= firstVisibleItemScrollOffset
        }
        previousIndex = firstVisibleItemIndex
        previousScrollOffset = firstVisibleItemScrollOffset
        isScrollingUp
    }
}

@Composable
fun LazyListState.isScrolled(): Boolean = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

@Composable
fun LazyGridState.isScrolled(): Boolean = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

// State extensions
@Composable
fun <T> rememberMutableStateOf(value: T) = remember { mutableStateOf(value) }

@Composable
fun <T> rememberMutableStateListOf(vararg elements: T) = remember { mutableStateListOf(*elements) }

@Composable
fun <K, V> rememberMutableStateMapOf(vararg pairs: Pair<K, V>) = remember { mutableStateMapOf(*pairs) }

// Effect extensions
@Composable
fun LaunchedEffectOnce(
    key: Any = Unit,
    block: suspend () -> Unit
) {
    LaunchedEffect(key) {
        block()
    }
}

@Composable
fun LaunchedEffectWithDelay(
    key: Any?,
    delayMillis: Long,
    block: suspend () -> Unit
) {
    LaunchedEffect(key) {
        delay(delayMillis)
        block()
    }
}

// Window insets extensions
@Composable
fun Modifier.systemBarsPadding() = composed {
    WindowInsets.systemBars.asPaddingValues().let { padding ->
        this.padding(padding)
    }
}

@Composable
fun Modifier.navigationBarsPadding() = composed {
    WindowInsets.navigationBars.asPaddingValues().let { padding ->
        this.padding(bottom = padding.calculateBottomPadding())
    }
}

@Composable
fun Modifier.statusBarsPadding() = composed {
    WindowInsets.statusBars.asPaddingValues().let { padding ->
        this.padding(top = padding.calculateTopPadding())
    }
}

// Layout direction extensions
@Composable
fun Modifier.mirrorInRtl() = composed {
    val layoutDirection = LocalLayoutDirection.current
    graphicsLayer {
        scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
    }
}

@Composable
fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl

// Density extensions
@Composable
fun Int.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

@Composable
fun Float.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun Dp.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

// Animation utilities
@Composable
fun slideInFromTop(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): EnterTransition = slideInVertically(
    initialOffsetY = { -it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeIn(animationSpec = tween(durationMillis, delayMillis))

@Composable
fun slideInFromBottom(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): EnterTransition = slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeIn(animationSpec = tween(durationMillis, delayMillis))

@Composable
fun slideInFromLeft(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): EnterTransition = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeIn(animationSpec = tween(durationMillis, delayMillis))

@Composable
fun slideInFromRight(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): EnterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeIn(animationSpec = tween(durationMillis, delayMillis))

@Composable
fun slideOutToTop(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): ExitTransition = slideOutVertically(
    targetOffsetY = { -it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeOut(animationSpec = tween(durationMillis, delayMillis))

@Composable
fun slideOutToBottom(
    durationMillis: Int = 300,
    delayMillis: Int = 0
): ExitTransition = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(durationMillis, delayMillis)
) + fadeOut(animationSpec = tween(durationMillis, delayMillis))

// Color extensions
fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

fun Color.darken(factor: Float = 0.2f): Color {
    return Color(
        red = (red * (1 - factor)).coerceAtLeast(0f),
        green = (green * (1 - factor)).coerceAtLeast(0f),
        blue = (blue * (1 - factor)).coerceAtLeast(0f),
        alpha = alpha
    )
}

fun Color.lighten(factor: Float = 0.2f): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceAtMost(1f),
        green = (green + (1 - green) * factor).coerceAtMost(1f),
        blue = (blue + (1 - blue) * factor).coerceAtMost(1f),
        alpha = alpha
    )
}

// Preview utilities
@Composable
fun PreviewContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
