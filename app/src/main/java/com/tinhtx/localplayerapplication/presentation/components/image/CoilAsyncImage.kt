package com.tinhtx.localplayerapplication.presentation.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator

@Composable
fun CoilAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(0.dp),
    placeholder: ImageVector = Icons.Default.MusicNote,
    error: ImageVector = Icons.Default.BrokenImage,
    showLoadingIndicator: Boolean = true,
    colorFilter: ColorFilter? = null,
    alpha: Float = 1f,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        colorFilter = colorFilter,
        alpha = alpha,
        placeholder = if (showLoadingIndicator) {
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data("")
                    .build()
            )
        } else null,
        error = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data("")
                .build()
        ),
        onLoading = onLoading,
        onSuccess = onSuccess,
        onError = onError,
        fallback = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data("")
                .build()
        )
    )
}

@Composable
fun CoilAsyncImageWithStates(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(8.dp),
    placeholder: @Composable () -> Unit = { DefaultImagePlaceholder() },
    error: @Composable () -> Unit = { DefaultImageError() },
    loading: @Composable () -> Unit = { DefaultImageLoading() }
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )
    
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                loading()
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            is AsyncImagePainter.State.Error -> {
                error()
            }
            else -> {
                placeholder()
            }
        }
    }
}

@Composable
fun AlbumArtwork(
    artworkUrl: String?,
    albumName: String,
    artistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 8.dp,
    showGradientOverlay: Boolean = false
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CoilAsyncImageWithStates(
            imageUrl = artworkUrl,
            contentDescription = "Album artwork for $albumName by $artistName",
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(cornerRadius),
            placeholder = {
                AlbumPlaceholder(
                    albumName = albumName,
                    artistName = artistName
                )
            },
            error = {
                AlbumPlaceholder(
                    albumName = albumName,
                    artistName = artistName,
                    isError = true
                )
            },
            loading = {
                AlbumLoadingState()
            }
        )
        
        if (showGradientOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun ArtistImage(
    imageUrl: String?,
    artistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isCircular: Boolean = true
) {
    val shape = if (isCircular) {
        androidx.compose.foundation.shape.CircleShape
    } else {
        RoundedCornerShape(8.dp)
    }
    
    CoilAsyncImageWithStates(
        imageUrl = imageUrl,
        contentDescription = "Artist image for $artistName",
        modifier = modifier.size(size),
        shape = shape,
        placeholder = {
            ArtistPlaceholder(
                artistName = artistName,
                isCircular = isCircular
            )
        },
        error = {
            ArtistPlaceholder(
                artistName = artistName,
                isCircular = isCircular,
                isError = true
            )
        },
        loading = {
            ArtistLoadingState(isCircular = isCircular)
        }
    )
}

@Composable
fun PlaylistCover(
    coverUrl: String?,
    playlistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 8.dp
) {
    CoilAsyncImageWithStates(
        imageUrl = coverUrl,
        contentDescription = "Playlist cover for $playlistName",
        modifier = modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        placeholder = {
            PlaylistPlaceholder(playlistName = playlistName)
        },
        error = {
            PlaylistPlaceholder(
                playlistName = playlistName,
                isError = true
            )
        },
        loading = {
            PlaylistLoadingState()
        }
    )
}

@Composable
private fun DefaultImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DefaultImageError() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DefaultImageLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun AlbumPlaceholder(
    albumName: String,
    artistName: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isError) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.BrokenImage else Icons.Default.Album,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
private fun ArtistPlaceholder(
    artistName: String,
    isCircular: Boolean = true,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isError) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.BrokenImage else Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
private fun PlaylistPlaceholder(
    playlistName: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isError) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.BrokenImage else Icons.Default.PlaylistPlay,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

@Composable
private fun AlbumLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun ArtistLoadingState(isCircular: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun PlaylistLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

// Image variants for different use cases
@Composable
fun LargeAlbumArtwork(
    artworkUrl: String?,
    albumName: String,
    artistName: String,
    modifier: Modifier = Modifier
) {
    AlbumArtwork(
        artworkUrl = artworkUrl,
        albumName = albumName,
        artistName = artistName,
        modifier = modifier,
        size = 200.dp,
        cornerRadius = 16.dp,
        showGradientOverlay = true
    )
}

@Composable
fun SmallAlbumArtwork(
    artworkUrl: String?,
    albumName: String,
    artistName: String,
    modifier: Modifier = Modifier
) {
    AlbumArtwork(
        artworkUrl = artworkUrl,
        albumName = albumName,
        artistName = artistName,
        modifier = modifier,
        size = 32.dp,
        cornerRadius = 4.dp
    )
}

@Composable
fun MediumArtistImage(
    imageUrl: String?,
    artistName: String,
    modifier: Modifier = Modifier
) {
    ArtistImage(
        imageUrl = imageUrl,
        artistName = artistName,
        modifier = modifier,
        size = 64.dp,
        isCircular = true
    )
}

@Composable
fun LargeArtistImage(
    imageUrl: String?,
    artistName: String,
    modifier: Modifier = Modifier
) {
    ArtistImage(
        imageUrl = imageUrl,
        artistName = artistName,
        modifier = modifier,
        size = 120.dp,
        isCircular = true
    )
}
