package com.tinhtx.localplayerapplication.core.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// String Extensions
fun String?.orEmpty(): String = this ?: ""

fun String.toUri(): Uri = Uri.parse(this)

fun String.capitalizeWords(): String = 
    split(" ").joinToString(" ") { word -> 
        word.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        } 
    }

// Long Extensions (Duration)
fun Long.formatDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

fun Long.formatDurationWithHours(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

fun Long.toDateString(): String {
    val date = Date(this * 1000) // Convert to milliseconds
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

// Int Extensions
fun Int.toDp(): Dp = this.dp

fun Int.px(): Dp {
    return (this / android.content.res.Resources.getSystem().displayMetrics.density).dp
}

// Color Extensions
fun Color.isDark(): Boolean {
    return ColorUtils.calculateLuminance(this.toArgb()) < 0.5
}

fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

fun Color.darken(factor: Float = 0.1f): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.lighten(factor: Float = 0.1f): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceIn(0f, 1f),
        green = (green + (1 - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Context Extensions
fun Context.isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.isTablet(): Boolean {
    return (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

// Flow Extensions
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.success(it) }
        .onStart { emit(Result.success(null)) }
        .catch { emit(Result.failure(it)) }
}

// Compose Extensions
@Composable
fun WindowInsets.Companion.statusBarHeight(): Dp {
    return WindowInsets.systemBars.getTop(LocalDensity.current).toDp()
}

@Composable
fun WindowInsets.Companion.navigationBarHeight(): Dp {
    return WindowInsets.systemBars.getBottom(LocalDensity.current).toDp()
}

@Composable
fun WindowInsets.Companion.keyboardHeight(): Dp {
    return WindowInsets.ime.getBottom(LocalDensity.current).toDp()
}

@Composable
fun <T> T.rememberUpdated(): State<T> = rememberUpdatedState(this)

// List Extensions
fun <T> List<T>.shuffle(isShuffled: Boolean): List<T> {
    return if (isShuffled) this.shuffled() else this
}

fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return this
    val mutableList = this.toMutableList()
    val item = mutableList.removeAt(fromIndex)
    mutableList.add(toIndex, item)
    return mutableList
}

// Bitmap Extensions
fun Bitmap.getDominantColor(): Int {
    val width = width
    val height = height
    val pixelCount = width * height
    val pixels = IntArray(pixelCount)
    
    getPixels(pixels, 0, width, 0, 0, width, height)
    
    val colorCounts = mutableMapOf<Int, Int>()
    
    for (pixel in pixels) {
        val color = pixel and 0xFFFFFF // Remove alpha
        colorCounts[color] = colorCounts.getOrDefault(color, 0) + 1
    }
    
    return colorCounts.maxByOrNull { it.value }?.key ?: android.graphics.Color.GRAY
}

// Drawable Extensions
fun Drawable.toBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

// Collection Extensions
fun <T> Collection<T>.isNotEmpty(): Boolean = !isEmpty()

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

// Safe Cast Extensions
inline fun <reified T> Any?.safeCast(): T? = this as? T

// Validation Extensions
fun String?.isValidEmail(): Boolean {
    return this?.let { 
        android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() 
    } ?: false
}

fun String?.isNotNullOrBlank(): Boolean = !isNullOrBlank()
