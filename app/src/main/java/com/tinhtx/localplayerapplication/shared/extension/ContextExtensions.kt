package com.tinhtx.localplayerapplication.presentation.shared.extension

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/**
 * Context extension functions for common operations
 */

// Resource access extensions
@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int): Int = 
    ContextCompat.getColor(this, colorRes)

fun Context.getDrawableCompat(drawableRes: Int) = 
    ContextCompat.getDrawable(this, drawableRes)

@ColorInt
fun Context.getThemeColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

fun Context.getDimensionPixelSize(dimenRes: Int): Int = 
    resources.getDimensionPixelSize(dimenRes)

fun Context.getDimension(dimenRes: Int): Float = 
    resources.getDimension(dimenRes)

// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

fun Context.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

fun Context.showLongToast(@StringRes messageRes: Int) {
    showToast(messageRes, Toast.LENGTH_LONG)
}

// Clipboard extensions
fun Context.copyToClipboard(text: String, label: String = "Copied Text") {
    val clipboard = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
    showToast("Copied to clipboard")
}

fun Context.getClipboardText(): String? {
    val clipboard = getSystemService<ClipboardManager>()
    return clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
}

// Network extensions
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } else {
        @Suppress("DEPRECATION")
        connectivityManager?.activeNetworkInfo?.isConnected == true
    }
}

fun Context.isWifiConnected(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager?.activeNetworkInfo
        networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
    }
}

fun Context.isMobileDataConnected(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager?.activeNetworkInfo
        networkInfo?.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
    }
}

// Permission extensions
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.hasPermissions(vararg permissions: String): Boolean =
    permissions.all { hasPermission(it) }

// Vibration extensions
fun Context.vibrate(duration: Long = 100L) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService<VibratorManager>()
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService<Vibrator>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
}

fun Context.vibratePattern(pattern: LongArray, repeat: Int = -1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService<VibratorManager>()
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService<Vibrator>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, repeat)
        }
    }
}

// Intent extensions
fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        showToast("Unable to open URL")
    }
}

fun Context.openEmail(email: String, subject: String = "", body: String = "") {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (e: Exception) {
        showToast("No email app found")
    }
}

fun Context.shareText(text: String, subject: String = "") {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (subject.isNotEmpty()) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
        }
        startActivity(Intent.createChooser(intent, "Share"))
    } catch (e: Exception) {
        showToast("Unable to share")
    }
}

fun Context.openAppSettings() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    } catch (e: Exception) {
        showToast("Unable to open settings")
    }
}

// File and storage extensions
fun Context.getInternalStorageSize(): Long {
    val dataDir = filesDir
    return dataDir.totalSpace
}

fun Context.getAvailableInternalStorage(): Long {
    val dataDir = filesDir
    return dataDir.freeSpace
}

fun Context.getUsedInternalStorage(): Long = 
    getInternalStorageSize() - getAvailableInternalStorage()

// Display extensions
fun Context.dpToPx(dp: Float): Float = 
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun Context.dpToPx(dp: Int): Int = dpToPx(dp.toFloat()).toInt()

fun Context.spToPx(sp: Float): Float = 
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

fun Context.spToPx(sp: Int): Int = spToPx(sp.toFloat()).toInt()

fun Context.pxToDp(px: Float): Float = px / resources.displayMetrics.density

fun Context.pxToDp(px: Int): Int = pxToDp(px.toFloat()).toInt()

// App info extensions
fun Context.getVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun Context.getVersionCode(): Long {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (e: Exception) {
        0L
    }
}

// Activity extensions
fun Context.finishActivity() {
    (this as? Activity)?.finish()
}

fun Context.finishAffinity() {
    (this as? Activity)?.finishAffinity()
}

// System service extensions
inline fun <reified T> Context.getSystemService(): T? = 
    ContextCompat.getSystemService(this, T::class.java)

// Cache extensions
fun Context.clearAppCache() {
    try {
        cacheDir.deleteRecursively()
    } catch (e: Exception) {
        android.util.Log.e("ContextExtensions", "Failed to clear cache", e)
    }
}

fun Context.getCacheSize(): Long {
    return try {
        cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    } catch (e: Exception) {
        0L
    }
}

// Language and locale extensions
fun Context.getCurrentLocale(): java.util.Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
}

fun Context.isRtl(): Boolean = 
    resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL

// Theme extensions
fun Context.isDarkTheme(): Boolean {
    val nightModeFlags = resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
}

fun Context.isLandscape(): Boolean = 
    resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

fun Context.isPortrait(): Boolean = 
    resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
