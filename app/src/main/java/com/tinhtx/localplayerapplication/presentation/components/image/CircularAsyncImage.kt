package com.tinhtx.localplayerapplication.presentation.components.image

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CircularProfileImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    placeholder: @Composable (BoxScope.() -> Unit)? = { CircularPlaceholder() },
    error: @Composable (BoxScope.() -> Unit)? = { CircularPlaceholder() }
) {
    Box(
        modifier = modifier
            .size(size)
            .let { mod ->
                if (borderWidth > 0.dp) {
                    mod.border(borderWidth, borderColor, CircleShape)
                } else {
                    mod
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = placeholder?.let { { it() } },
            error = error?.let { { it() } }
        )
    }
}

@Composable
fun CircularArtistImage(
    artistImageUrl: String?,
    artistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showInitials: Boolean = true
) {
    CircularProfileImage(
        imageUrl = artistImageUrl,
        contentDescription = "Artist photo for $artistName",
        modifier = modifier,
        size = size,
        error = {
            if (showInitials && artistName.isNotBlank()) {
                CircularInitialsPlaceholder(
                    text = getInitials(artistName),
                    backgroundColor = generateColorFromText(artistName)
                )
            } else {
                CircularPlaceholder()
            }
        }
    )
}

@Composable
fun BoxScope.CircularPlaceholder(
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
    }
}

@Composable
fun BoxScope.CircularInitialsPlaceholder(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

@Composable
fun AnimatedCircularImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isVisible: Boolean = true,
    animationDuration: Int = 300
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = tween(animationDuration)
        ) + fadeIn(
            animationSpec = tween(animationDuration)
        ),
        exit = scaleOut(
            animationSpec = tween(animationDuration)
        ) + fadeOut(
            animationSpec = tween(animationDuration)
        )
    ) {
        CircularProfileImage(
            imageUrl = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            size = size
        )
    }
}

private fun getInitials(name: String): String {
    return name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { name.take(2).uppercase() }
}

private fun generateColorFromText(text: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFF7B1FA2), // Purple
        Color(0xFF00796B), // Teal
        Color(0xFF5D4037), // Brown
        Color(0xFF455A64), // Blue Grey
        Color(0xFF00695C), // Teal
        Color(0xFF6A1B9A), // Purple
        Color(0xFF2E7D32), // Green
        Color(0xFF1565C0)  // Blue
    )
    
    val hash = text.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}
