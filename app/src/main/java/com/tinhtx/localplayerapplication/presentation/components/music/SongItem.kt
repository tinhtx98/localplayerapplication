package com.tinhtx.localplayerapplication.presentation.components.music

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.theme.songTitle
import com.tinhtx.localplayerapplication.presentation.theme.artistName
import com.tinhtx.localplayerapplication.presentation.theme.timestamp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAlbumArt: Boolean = true,
    showDuration: Boolean = true,
    showTrackNumber: Boolean = false,
    highlightWhenPlaying: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isPlaying && highlightWhenPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val contentColor = when {
        isPlaying && highlightWhenPlaying -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading content (track number, drag handle, etc.)
            leadingContent?.let { content ->
                content()
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Track number (if enabled)
            if (showTrackNumber && song.track > 0) {
                Text(
                    text = song.track.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .width(24.dp)
                        .wrapContentHeight()
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Album art or playing indicator
            if (showAlbumArt) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(MediaUtils.getAlbumArtUri(song.albumId))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album art",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                    )

                    // Playing indicator overlay
                    AnimatedVisibility(
                        visible = isPlaying,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveformIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            // Song info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.songTitle,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.displayArtist,
                        style = MaterialTheme.typography.artistName,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (song.displayAlbum.isNotBlank() && song.displayAlbum != "Unknown Album") {
                        Text(
                            text = " • ${song.displayAlbum}",
                            style = MaterialTheme.typography.artistName,
                            color = contentColor.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            // Duration
            if (showDuration) {
                Text(
                    text = song.formattedDuration,
                    style = MaterialTheme.typography.timestamp,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Trailing content or default actions
            if (trailingContent != null) {
                trailingContent()
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite button
                    if (onFavoriteClick != null) {
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                                modifier = Modifier.size(20.dp),
                                tint = if (song.isFavorite) Color.Red else contentColor.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // More options button
                    if (onMoreClick != null) {
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                modifier = Modifier.size(20.dp),
                                tint = contentColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactSongItem(
    song: Song,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    SongItem(
        song = song,
        isPlaying = isPlaying,
        onClick = onClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
        showAlbumArt = false,
        showDuration = false,
        showTrackNumber = false
    ) {
        // Leading content: Play indicator or index
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                WaveformIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun WaveformIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 3
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "waveform")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 100, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(12.dp * scale)
                    .background(color, RoundedCornerShape(1.dp))
            )
        }
    }
}
