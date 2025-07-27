package com.tinhtx.localplayerapplication.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlin.math.*

/**
 * Utility functions for color manipulation and extraction
 */
object ColorUtils {

    /**
     * Extract dominant color from bitmap using Palette API
     */
    fun extractDominantColor(bitmap: Bitmap?, @ColorInt fallbackColor: Int = Color.GRAY): Int {
        return try {
            bitmap?.let {
                val palette = Palette.from(it).generate()
                palette.getDominantColor(fallbackColor)
            } ?: fallbackColor
        } catch (e: Exception) {
            fallbackColor
        }
    }

    /**
     * Extract vibrant color from bitmap
     */
    fun extractVibrantColor(bitmap: Bitmap?, @ColorInt fallbackColor: Int = Color.BLUE): Int {
        return try {
            bitmap?.let {
                val palette = Palette.from(it).generate()
                palette.getVibrantColor(fallbackColor)
            } ?: fallbackColor
        } catch (e: Exception) {
            fallbackColor
        }
    }

    /**
     * Extract muted color from bitmap
     */
    fun extractMutedColor(bitmap: Bitmap?, @ColorInt fallbackColor: Int = Color.GRAY): Int {
        return try {
            bitmap?.let {
                val palette = Palette.from(it).generate()
                palette.getMutedColor(fallbackColor)
            } ?: fallbackColor
        } catch (e: Exception) {
            fallbackColor
        }
    }

    /**
     * Get appropriate text color (black or white) based on background color
     */
    @ColorInt
    fun getContrastColor(@ColorInt backgroundColor: Int): Int {
        val luminance = calculateLuminance(backgroundColor)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    /**
     * Calculate luminance of a color
     */
    private fun calculateLuminance(@ColorInt color: Int): Double {
        val red = Color.red(color) / 255.0
        val green = Color.green(color) / 255.0
        val blue = Color.blue(color) / 255.0

        val r = if (red <= 0.03928) red / 12.92 else ((red + 0.055) / 1.055).pow(2.4)
        val g = if (green <= 0.03928) green / 12.92 else ((green + 0.055) / 1.055).pow(2.4)
        val b = if (blue <= 0.03928) blue / 12.92 else ((blue + 0.055) / 1.055).pow(2.4)

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Darken a color by a percentage
     */
    @ColorInt
    fun darkenColor(@ColorInt color: Int, factor: Float): Int {
        val clampedFactor = factor.coerceIn(0f, 1f)
        val a = Color.alpha(color)
        val r = (Color.red(color) * (1 - clampedFactor)).roundToInt()
        val g = (Color.green(color) * (1 - clampedFactor)).roundToInt()
        val b = (Color.blue(color) * (1 - clampedFactor)).roundToInt()
        return Color.argb(a, r, g, b)
    }

    /**
     * Lighten a color by a percentage
     */
    @ColorInt
    fun lightenColor(@ColorInt color: Int, factor: Float): Int {
        val clampedFactor = factor.coerceIn(0f, 1f)
        val a = Color.alpha(color)
        val r = (Color.red(color) + (255 - Color.red(color)) * clampedFactor).roundToInt()
        val g = (Color.green(color) + (255 - Color.green(color)) * clampedFactor).roundToInt()
        val b = (Color.blue(color) + (255 - Color.blue(color)) * clampedFactor).roundToInt()
        return Color.argb(a, r, g, b)
    }

    /**
     * Adjust color alpha
     */
    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, alpha: Float): Int {
        val clampedAlpha = (alpha * 255).roundToInt().coerceIn(0, 255)
        return Color.argb(clampedAlpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * Check if color is light
     */
    fun isColorLight(@ColorInt color: Int): Boolean {
        val luminance = calculateLuminance(color)
        return luminance > 0.5
    }

    /**
     * Check if color is dark
     */
    fun isColorDark(@ColorInt color: Int): Boolean {
        return !isColorLight(color)
    }

    /**
     * Convert hex string to color int
     */
    @ColorInt
    fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: IllegalArgumentException) {
            Color.GRAY
        }
    }

    /**
     * Convert color int to hex string
     */
    fun colorToHex(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    /**
     * Blend two colors
     */
    @ColorInt
    fun blendColors(@ColorInt color1: Int, @ColorInt color2: Int, ratio: Float): Int {
        val clampedRatio = ratio.coerceIn(0f, 1f)
        val inverseRatio = 1f - clampedRatio

        val a = (Color.alpha(color1) * inverseRatio + Color.alpha(color2) * clampedRatio).roundToInt()
        val r = (Color.red(color1) * inverseRatio + Color.red(color2) * clampedRatio).roundToInt()
        val g = (Color.green(color1) * inverseRatio + Color.green(color2) * clampedRatio).roundToInt()
        val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * clampedRatio).roundToInt()

        return Color.argb(a, r, g, b)
    }

    /**
     * Generate gradient colors for visualizer
     */
    fun generateGradientColors(@ColorInt baseColor: Int, steps: Int): List<Int> {
        val colors = mutableListOf<Int>()
        for (i in 0 until steps) {
            val factor = i.toFloat() / (steps - 1)
            val blendedColor = blendColors(baseColor, Color.WHITE, factor * 0.3f)
            colors.add(blendedColor)
        }
        return colors
    }

    /**
     * Create material design color palette from primary color
     */
    data class MaterialColorPalette(
        @ColorInt val primary: Int,
        @ColorInt val primaryVariant: Int,
        @ColorInt val primaryContainer: Int,
        @ColorInt val onPrimary: Int,
        @ColorInt val onPrimaryContainer: Int
    )

    fun createMaterialPalette(@ColorInt primaryColor: Int): MaterialColorPalette {
        return MaterialColorPalette(
            primary = primaryColor,
            primaryVariant = darkenColor(primaryColor, 0.2f),
            primaryContainer = lightenColor(primaryColor, 0.8f),
            onPrimary = getContrastColor(primaryColor),
            onPrimaryContainer = getContrastColor(lightenColor(primaryColor, 0.8f))
        )
    }
}
