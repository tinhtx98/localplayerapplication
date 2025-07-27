package com.tinhtx.localplayerapplication.presentation.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun CircularAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    placeholder: @Composable () -> Unit = { CircularImagePlaceholder(size) },
    error: @Composable () -> Unit = { CircularImageError(size) },
    loading: @Composable () -> Unit = { CircularImageLoading(size) },
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                loading()
            }
            is AsyncImagePainter.State.Success -> {
                androidx.compose.foundation.Image(
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
fun CircularArtistImage(
    imageUrl: String?,
    artistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showBorder: Boolean = false,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        showBorder -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }
    
    val borderWidth = when {
        isSelected -> 3.dp
        showBorder -> 1.dp
        else -> 0.dp
    }
    
    Box(modifier = modifier) {
        CircularAsyncImage(
            imageUrl = imageUrl,
            contentDescription = "Artist image for $artistName",
            size = size,
            borderWidth = borderWidth,
            borderColor = borderColor,
            placeholder = {
                ArtistImagePlaceholder(
                    artistName = artistName,
                    size = size
                )
            },
            error = {
                ArtistImageError(
                    artistName = artistName,
                    size = size
                )
            },
            loading = {
                ArtistImageLoading(size = size)
            },
            modifier = if (onClick != null) {
                Modifier.clickable { onClick() }
            } else Modifier
        )
        
        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun CircularUserAvatar(
    imageUrl: String?,
    userName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showOnlineIndicator: Boolean = false,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        CircularAsyncImage(
            imageUrl = imageUrl,
            contentDescription = "Avatar for $userName",
            size = size,
            placeholder = {
                UserAvatarPlaceholder(
                    userName = userName,
                    size = size
                )
            },
            error = {
                UserAvatarError(
                    userName = userName,
                    size = size
                )
            },
            loading = {
                UserAvatarLoading(size = size)
            },
            modifier = if (onClick != null) {
                Modifier.clickable { onClick() }
            } else Modifier
        )
        
        // Online indicator
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .background(
                        if (isOnline) Color.Green else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun CircularAlbumArt(
    artworkUrl: String?,
    albumName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showPlayButton: Boolean = false,
    onPlayClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        CircularAsyncImage(
            imageUrl = artworkUrl,
            contentDescription = "Album artwork for $albumName",
            size = size,
            placeholder = {
                CircularAlbumPlaceholder(
                    albumName = albumName,
                    size = size
                )
            },
            error = {
                CircularAlbumError(
                    albumName = albumName,
                    size = size
                )
            },
            loading = {
                CircularAlbumLoading(size = size)
            }
        )
        
        // Play button overlay
        if (showPlayButton && onPlayClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play album",
                    modifier = Modifier.size(size * 0.4f),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CircularPlaylistCover(
    coverUrl: String?,
    playlistName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isCreatedByUser: Boolean = false
) {
    Box(modifier = modifier) {
        CircularAsyncImage(
            imageUrl = coverUrl,
            contentDescription = "Playlist cover for $playlistName",
            size = size,
            placeholder = {
                CircularPlaylistPlaceholder(
                    playlistName = playlistName,
                    size = size,
                    isCreatedByUser = isCreatedByUser
                )
            },
            error = {
                CircularPlaylistError(
                    playlistName = playlistName,
                    size = size,
                    isCreatedByUser = isCreatedByUser
                )
            },
            loading = {
                CircularPlaylistLoading(size = size)
            }
        )
        
        // User created indicator
        if (isCreatedByUser) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Created by you",
                    modifier = Modifier.size(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Placeholder components
@Composable
private fun CircularImagePlaceholder(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularImageError(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularImageLoading(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun ArtistImagePlaceholder(artistName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ArtistImageError(artistName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ArtistImageLoading(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun UserAvatarPlaceholder(userName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = userName.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun UserAvatarError(userName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(size * 0.7f),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun UserAvatarLoading(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun CircularAlbumPlaceholder(albumName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Album,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularAlbumError(albumName: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularAlbumLoading(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}

@Composable
private fun CircularPlaylistPlaceholder(
    playlistName: String,
    size: Dp,
    isCreatedByUser: Boolean
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlaylistPlay,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularPlaylistError(
    playlistName: String,
    size: Dp,
    isCreatedByUser: Boolean
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CircularPlaylistLoading(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator(
            size = com.tinhtx.localplayerapplication.presentation.components.common.LoadingSize.Small
        )
    }
}
