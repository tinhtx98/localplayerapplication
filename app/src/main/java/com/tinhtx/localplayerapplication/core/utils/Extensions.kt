package com.tinhtx.localplayerapplication.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Resource wrapper for handling loading, success, and error states
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val  T) : Resource<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : Resource<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = if (this is Success) data else null
    fun exceptionOrNull(): Throwable? = if (this is Error) exception else null
}

/**
 * Flow Extensions
 */

/**
 * Convert Flow to Resource with loading state
 */
fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return this
        .map<T, Resource<T>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading) }
        .catch { emit(Resource.Error(it)) }
}

/**
 * Convert Flow to Result without initial state
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
}

/**
 * Collect flow with lifecycle awareness
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> = collectAsState(
    initial = initialValue,
    context = lifecycle.flowWithLifecycle(lifecycle, minActiveState).let { 
        kotlinx.coroutines.Dispatchers.Main.immediate 
    }
)

/**
 * Debounce with custom duration
 */
fun <T> Flow<T>.debounceSearch(timeoutMillis: Long = 300L): Flow<T> {
    return this.debounce(timeoutMillis)
}

/**
 * Context Extensions
 */

/**
 * Get drawable as bitmap
 */
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    }
    
    val bitmap = Bitmap.createBitmap(
        intrinsicWidth.takeIf { it > 0 } ?: 1,
        intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

/**
 * Get album art from MediaStore
 */
fun Context.getAlbumArt(albumId: Long): Bitmap? {
    return try {
        val uri = Uri.parse("content://media/external/audio/albumart/$albumId")
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    } catch (exception: Exception) {
        null
    }
}

/**
 * Time and Duration Extensions
 */

/**
 * Format milliseconds to MM:SS or HH:MM:SS
 */
fun Long.formatDuration(): String {
    val duration = this.milliseconds
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)
    val seconds = (duration.inWholeSeconds % 60)
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Format date
 */
fun Long.formatDate(pattern: String = "MMM dd, yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * String Extensions
 */

/**
 * Capitalize first letter of each word
 */
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
}

/**
 * Safe substring
 */
fun String.safeSubstring(startIndex: Int, endIndex: Int = length): String {
    val safeStart = startIndex.coerceAtLeast(0)
    val safeEnd = endIndex.coerceAtMost(length)
    return if (safeStart < safeEnd) substring(safeStart, safeEnd) else ""
}

/**
 * Check if string contains query ignoring case
 */
fun String.containsIgnoreCase(query: String): Boolean {
    return this.lowercase().contains(query.lowercase())
}

/**
 * Collection Extensions
 */

/**
 * Safe get item from list
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in indices) get(index) else null
}

/**
 * Toggle item in list
 */
fun <T> List<T>.toggle(item: T): List<T> {
    return if (contains(item)) {
        this - item
    } else {
        this + item
    }
}

/**
 * Move item in list
 */
fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex in indices && toIndex in indices) {
        val item = removeAt(fromIndex)
        add(toIndex, item)
    }
}

/**
 * Nullable Extensions
 */

/**
 * Execute block if not null
 */
inline fun <T> T?.ifNotNull(block: (T) -> Unit) {
    if (this != null) block(this)
}

/**
 * Return default value if null
 */
fun <T> T?.orDefault(default: T): T = this ?: default

/**
 * Boolean Extensions
 */

/**
 * Convert boolean to int (0 or 1)
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * Convert int to boolean
 */
fun Int.toBoolean(): Boolean = this != 0

/**
 * Number Extensions
 */

/**
 * Clamp number between min and max
 */
fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)
fun Float.clamp(min: Float, max: Float): Float = coerceIn(min, max)
fun Double.clamp(min: Double, max: Double): Double = coerceIn(min, max)

/**
 * Convert bytes to human readable format
 */
fun Long.formatBytes(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var bytes = this.toDouble()
    var unitIndex = 0
    
    while (bytes >= 1024 && unitIndex < units.size - 1) {
        bytes /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", bytes, units[unitIndex])
}

/**
 * Uri Extensions
 */

/**
 * Get file name from URI
 */
fun Uri.getFileName(context: Context): String? {
    return try {
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                if (nameIndex != -1) cursor.getString(nameIndex) else null
            } else null
        }
    } catch (exception: Exception) {
        null
    }
}
