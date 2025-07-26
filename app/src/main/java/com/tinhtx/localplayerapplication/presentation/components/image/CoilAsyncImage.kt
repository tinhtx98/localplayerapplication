package com.tinhtx.localplayerapplication.presentation.components.image

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.core.utils.MediaUtils

@Composable
fun AlbumArtImage(
    albumId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable (BoxScope.() -> Unit)? = null,
    error: @Composable (BoxScope.() -> Unit)? = { DefaultAlbumArtPlaceholder() },
    onLoading: @Composable (BoxScope.() -> Unit)? = null,
    crossfadeEnabled: Boolean = true
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(MediaUtils.getAlbumArtUri(albumId))
            .crossfade(crossfadeEnabled)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        placeholder = placeholder?.let { { it() } },
        error = error?.let { { it() } },
        onLoading = onLoading?.let { { it() } }
    )
}

@Composable
fun ArtistImage(
    artistImageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable (BoxScope.() -> Unit)? = null,
    error: @Composable (BoxScope.() -> Unit)? = { DefaultArtistPlaceholder() }
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artistImageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = contentScale,
        placeholder = placeholder?.let { { it() } },
        error = error?.let { { it() } }
    )
}

@Composable
fun PlaylistCoverImage(
    playlistCoverUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable (BoxScope.() -> Unit)? = null,
    error: @Composable (BoxScope.() -> Unit)? = { DefaultPlaylistPlaceholder() }
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(playlistCoverUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        placeholder = placeholder?.let { { it() } },
        error = error?.let { { it() } }
    )
}

@Composable
fun LoadingImagePlaceholder(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )
    }
}

@Composable
fun BoxScope.DefaultAlbumArtPlaceholder(
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    icon: ImageVector = Icons.Default.Album,
    showGradient: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (showGradient) {
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.8f)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(backgroundColor, backgroundColor))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = iconColor
        )
    }
}

@Composable
fun BoxScope.DefaultArtistPlaceholder(
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = iconColor
        )
    }
}

@Composable
fun BoxScope.DefaultPlaylistPlaceholder(
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = iconColor
        )
    }
}
