package com.tinhtx.localplayerapplication.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.Color as ComposeColor

object ColorUtils {
    
    /**
     * Extract dominant color from bitmap using Palette API
     */
    suspend fun extractDominantColor(bitmap: Bitmap?): ComposeColor = withContext(Dispatchers.Default) {
        if (bitmap == null) return@withContext ComposeColor.Gray
        
        try {
            val palette = Palette.from(bitmap).generate()
            val dominantColor = palette.getDominantColor(Color.GRAY)
            ComposeColor(dominantColor)
        } catch (e: Exception) {
            ComposeColor.Gray
        }
    }
    
    /**
     * Extract vibrant color from bitmap
     */
    suspend fun extractVibrantColor(bitmap: Bitmap?): ComposeColor = withContext(Dispatchers.Default) {
        if (bitmap == null) return@withContext ComposeColor.Gray
        
        try {
            val palette = Palette.from(bitmap).generate()
            val vibrantColor = palette.getVibrantColor(Color.GRAY)
            ComposeColor(vibrantColor)
        } catch (e: Exception) {
            ComposeColor.Gray
        }
    }
    
    /**
     * Extract muted color from bitmap
     */
    suspend fun extractMutedColor(bitmap: Bitmap?): ComposeColor = withContext(Dispatchers.Default) {
        if (bitmap == null) return@withContext ComposeColor.Gray
        
        try {
            val palette = Palette.from(bitmap).generate()
            val mutedColor = palette.getMutedColor(Color.GRAY)
            ComposeColor(mutedColor)
        } catch (e: Exception) {
            ComposeColor.Gray
        }
    }
    
    /**
     * Create dynamic color scheme from extracted color
     */
    suspend fun createDynamicColorScheme(
        bitmap: Bitmap?,
        isDark: Boolean = false
    ): ColorScheme = withContext(Dispatchers.Default) {
        if (bitmap == null) {
            return@withContext if (isDark) darkColorScheme() else lightColorScheme()
        }
        
        try {
            val palette = Palette.from(bitmap).generate()
            
            val primary = ComposeColor(palette.getVibrantColor(Color.BLUE))
            val primaryVariant = ComposeColor(palette.getDarkVibrantColor(Color.BLUE))
            val secondary = ComposeColor(palette.getMutedColor(Color.GREEN))
            val background = if (isDark) ComposeColor.Black else ComposeColor.White
            val surface = if (isDark) ComposeColor(0xFF121212) else ComposeColor.White
            val onPrimary = if (primary.isDark()) ComposeColor.White else ComposeColor.Black
            val onSecondary = if (secondary.isDark()) ComposeColor.White else ComposeColor.Black
            val onBackground = if (isDark) ComposeColor.White else ComposeColor.Black
            val onSurface = if (isDark) ComposeColor.White else ComposeColor.Black
            
            if (isDark) {
                darkColorScheme(
                    primary = primary,
                    secondary = secondary,
                    background = background,
                    surface = surface,
                    onPrimary = onPrimary,
                    onSecondary = onSecondary,
                    onBackground = onBackground,
                    onSurface = onSurface
                )
            } else {
                lightColorScheme(
                    primary = primary,
                    secondary = secondary,
                    background = background,
                    surface = surface,
                    onPrimary = onPrimary,
                    onSecondary = onSecondary,
                    onBackground = onBackground,
                    onSurface = onSurface
                )
            }
        } catch (e: Exception) {
            if (isDark) darkColorScheme() else lightColorScheme()
        }
    }
    
    /**
     * Check if color is dark
     */
    fun ComposeColor.isDark(): Boolean {
        return ColorUtils.calculateLuminance(this.toArgb()) < 0.5
    }
    
    /**
     * Get contrasting color (black or white)
     */
    fun ComposeColor.getContrastingColor(): ComposeColor {
        return if (isDark()) ComposeColor.White else ComposeColor.Black
    }
    
    /**
     * Darken color by specified factor
     */
    fun ComposeColor.darken(factor: Float = 0.2f): ComposeColor {
        return ComposeColor(
            red = max(0f, red - factor),
            green = max(0f, green - factor),
            blue = max(0f, blue - factor),
            alpha = alpha
        )
    }
    
    /**
     * Lighten color by specified factor
     */
    fun ComposeColor.lighten(factor: Float = 0.2f): ComposeColor {
        return ComposeColor(
            red = min(1f, red + factor),
            green = min(1f, green + factor),
            blue = min(1f, blue + factor),
            alpha = alpha
        )
    }
    
    /**
     * Blend two colors
     */
    fun blendColors(color1: ComposeColor, color2: ComposeColor, ratio: Float): ComposeColor {
        val normalizedRatio = ratio.coerceIn(0f, 1f)
        val inverseRatio = 1f - normalizedRatio
        
        return ComposeColor(
            red = color1.red * inverseRatio + color2.red * normalizedRatio,
            green = color1.green * inverseRatio + color2.green * normalizedRatio,
            blue = color1.blue * inverseRatio + color2.blue * normalizedRatio,
            alpha = color1.alpha * inverseRatio + color2.alpha * normalizedRatio
        )
    }
    
    /**
     * Generate gradient colors from base color
     */
    fun generateGradientColors(baseColor: ComposeColor): List<ComposeColor> {
        return listOf(
            baseColor.lighten(0.3f),
            baseColor,
            baseColor.darken(0.3f)
        )
    }
    
    /**
     * Get material design color variations
     */
    fun getMaterialColorVariations(baseColor: ComposeColor): MaterialColorPalette {
        val base = baseColor.toArgb()
        
        return MaterialColorPalette(
            color50 = ComposeColor(ColorUtils.blendARGB(base, Color.WHITE, 0.95f)),
            color100 = ComposeColor(ColorUtils.blendARGB(base, Color.WHITE, 0.9f)),
            color200 = ComposeColor(ColorUtils.blendARGB(base, Color.WHITE, 0.8f)),
            color300 = ComposeColor(ColorUtils.blendARGB(base, Color.WHITE, 0.6f)),
            color400 = ComposeColor(ColorUtils.blendARGB(base, Color.WHITE, 0.4f)),
            color500 = baseColor,
            color600 = ComposeColor(ColorUtils.blendARGB(base, Color.BLACK, 0.15f)),
            color700 = ComposeColor(ColorUtils.blendARGB(base, Color.BLACK, 0.3f)),
            color800 = ComposeColor(ColorUtils.blendARGB(base, Color.BLACK, 0.45f)),
            color900 = ComposeColor(ColorUtils.blendARGB(base, Color.BLACK, 0.6f))
        )
    }
    
    /**
     * Calculate color distance
     */
    fun calculateColorDistance(color1: ComposeColor, color2: ComposeColor): Float {
        val r1 = color1.red * 255
        val g1 = color1.green * 255
        val b1 = color1.blue * 255
        
        val r2 = color2.red * 255
        val g2 = color2.green * 255
        val b2 = color2.blue * 255
        
        val deltaR = r1 - r2
        val deltaG = g1 - g2
        val deltaB = b1 - b2
        
        return kotlin.math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB)
    }
    
    data class MaterialColorPalette(
        val color50: ComposeColor,
        val color100: ComposeColor,
        val color200: ComposeColor,
        val color300: ComposeColor,
        val color400: ComposeColor,
        val color500: ComposeColor,
        val color600: ComposeColor,
        val color700: ComposeColor,
        val color800: ComposeColor,
        val color900: ComposeColor
    )
}
