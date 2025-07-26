package com.tinhtx.localplayerapplication.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = VinylBlack,
    surface = PlayerSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = PlayerOnSurface,
    onSurface = PlayerOnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF313033),
    inverseSurface = Color(0xFFE6E1E5),
    inversePrimary = Purple40,
    surfaceTint = Purple80,
    outlineVariant = Color(0xFF49454F),
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = SurfaceLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFF4EFF4),
    inverseSurface = Color(0xFF313033),
    inversePrimary = Purple80,
    surfaceTint = Purple40,
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color.Black
)

@Composable
fun LocalPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Theme Extensions
@Composable
fun MaterialTheme.musicColors(): MusicColors {
    return if (isSystemInDarkTheme()) {
        MusicColors.dark()
    } else {
        MusicColors.light()
    }
}

data class MusicColors(
    val playerBackground: Color,
    val playerSurface: Color,
    val playerOnSurface: Color,
    val accent: Color,
    val vinyl: Color,
    val waveform: Color,
    val progress: Color,
    val progressBackground: Color
) {
    companion object {
        fun light() = MusicColors(
            playerBackground = Color.White,
            playerSurface = Grey100,
            playerOnSurface = Grey900,
            accent = DeepPurple,
            vinyl = VinylBlack,
            waveform = DeepPurple.copy(alpha = 0.7f),
            progress = DeepPurple,
            progressBackground = Grey300
        )
        
        fun dark() = MusicColors(
            playerBackground = PlayerBackground,
            playerSurface = PlayerSurface,
            playerOnSurface = PlayerOnSurface,
            accent = PlayerAccent,
            vinyl = Grey800,
            waveform = PlayerAccent.copy(alpha = 0.8f),
            progress = PlayerAccent,
            progressBackground = Grey700
        )
    }
}

val LocalMusicColors = staticCompositionLocalOf { MusicColors.light() }

@Composable
fun ProvideMusicColors(
    colors: MusicColors = MaterialTheme.musicColors(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalMusicColors provides colors,
        content = content
    )
}
