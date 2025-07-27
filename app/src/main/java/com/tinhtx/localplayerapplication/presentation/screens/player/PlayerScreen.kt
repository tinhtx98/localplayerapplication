package com.tinhtx.localplayerapplication.presentation.screens.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.image.CoilAsyncImage
import com.tinhtx.localplayerapplication.presentation.screens.player.components.*

/**
 * Player Screen - Complete music player interface
 * Maps 100% với PlayerViewModel và PlayerUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlaylist: (Playlist) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle error states với SnackBar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Show error message
        }
    }

    // Auto-clear error after 5 seconds
    LaunchedEffect(uiState.hasError) {
        if (uiState.hasError) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background với gradient
        PlayerBackground(
            currentSong = uiState.currentSong,
            isPlaying = uiState.isPlaying
        )

        // Main player content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            PlayerTopBar(
                onNavigateBack = onNavigateBack,
                currentSong = uiState.currentSong,
                onMoreClick = { /* TODO: Show more options */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Main content
            if (uiState.isLoading) {
                PlayerLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (uiState.hasError) {
                PlayerErrorState(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { /* viewModel.retryLastAction() */ },
                    onDismiss = viewModel::clearError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else if (uiState.isEmpty) {
                PlayerEmptyState(
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                PlayerMainContent(
                    uiState = uiState,
                    onPlayPauseClick = viewModel::togglePlayPause,
                    onNextClick = viewModel::skipToNext,
                    onPreviousClick = viewModel::skipToPrevious,
                    onSeekChange = viewModel::seekByPercentage,
                    onSeekStart = { /* Optional: Handle seek start */ },
                    onSeekEnd = { /* Optional: Handle seek end */ },
                    onRepeatClick = viewModel::toggleRepeatMode,
                    onShuffleClick = viewModel::toggleShuffleMode,
                    onFavoriteClick = viewModel::toggleFavorite,
                    onSpeedClick = { viewModel.showSpeedDialog(true) },
                    onEqualizerClick = { viewModel.showEqualizer(true) },
                    onSleepTimerClick = { viewModel.showSleepTimerDialog(true) },
                    onLyricsClick = { viewModel.showLyrics(true) },
                    onQueueClick = { viewModel.showQueue(true) },
                    onVisualizationClick = viewModel::toggleVisualization,
                    onArtistClick = onNavigateToArtist,
                    onAlbumClick = onNavigateToAlbum,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sleep timer indicator
            AnimatedVisibility(
                visible = uiState.sleepTimerEnabled,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                SleepTimerIndicator(
                    remainingTime = uiState.formattedSleepTimer,
                    onCancelClick = viewModel::cancelSleepTimer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Dialogs
        PlayerDialogs(
            uiState = uiState,
            viewModel = viewModel
        )

        // Error snackbar
        AnimatedVisibility(
            visible = uiState.hasError,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ErrorSnackbar(
                error = uiState.error ?: "",
                onDismiss = viewModel::clearError,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun PlayerBackground(
    currentSong: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.3f else 0.1f,
        animationSpec = tween(1000),
        label = "background_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Album art background (blurred)
        currentSong?.albumArtPath?.let { artPath ->
            CoilAsyncImage(
                imageUrl = artPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = animatedAlpha
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )
    }
}

@Composable
private fun PlayerTopBar(
    onNavigateBack: () -> Unit,
    currentSong: Song?,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Close player",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            currentSong?.let { song ->
                Text(
                    text = song.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlayerMainContent(
    uiState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onQueueClick: () -> Unit,
    onVisualizationClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Album art with rotation animation
        AlbumArtSection(
            currentSong = uiState.currentSong,
            isPlaying = uiState.isPlaying,
            showVisualization = uiState.showVisualization,
            onVisualizationClick = onVisualizationClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Song info
        SongInfoSection(
            currentSong = uiState.currentSong,
            isFavorite = uiState.isFavorite,
            playCount = uiState.playCount,
            onFavoriteClick = onFavoriteClick,
            onArtistClick = onArtistClick,
            onAlbumClick = onAlbumClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress section
        ProgressSection(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            bufferedPosition = uiState.bufferedPosition,
            isSeekingByUser = uiState.isSeekingByUser,
            onSeekChange = onSeekChange,
            onSeekStart = onSeekStart,
            onSeekEnd = onSeekEnd,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main controls
        MainControlsSection(
            isPlaying = uiState.isPlaying,
            isPaused = uiState.isPaused,
            isBuffering = uiState.isBuffering,
            canPlay = uiState.canPlay,
            canPause = uiState.canPause,
            hasNext = uiState.hasNext,
            hasPrevious = uiState.hasPrevious,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary controls
        SecondaryControlsSection(
            repeatMode = uiState.repeatMode,
            shuffleMode = uiState.shuffleMode,
            playbackSpeed = uiState.playbackSpeed,
            queueSize = uiState.queueSize,
            onRepeatClick = onRepeatClick,
            onShuffleClick = onShuffleClick,
            onSpeedClick = onSpeedClick,
            onQueueClick = onQueueClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Additional controls
        AdditionalControlsSection(
            onEqualizerClick = onEqualizerClick,
            onSleepTimerClick = onSleepTimerClick,
            onLyricsClick = onLyricsClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AlbumArtSection(
    currentSong: Song?,
    isPlaying: Boolean,
    showVisualization: Boolean,
    onVisualizationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "album_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Album art
        Card(
            modifier = Modifier
                .size(280.dp)
                .rotate(if (isPlaying) rotation else 0f),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (currentSong?.albumArtPath != null) {
                CoilAsyncImage(
                    imageUrl = currentSong.albumArtPath,
                    contentDescription = "Album art for ${currentSong.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                DefaultAlbumArtwork(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Visualization toggle
        if (isPlaying) {
            FloatingActionButton(
                onClick = onVisualizationClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (-16).dp)
                    .size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (showVisualization) Icons.Default.GraphicEq else Icons.Default.Equalizer,
                    contentDescription = "Toggle visualization",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Pulsing effect when playing
        if (isPlaying) {
            PulsingMusicIcon(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun SongInfoSection(
    currentSong: Song?,
    isFavorite: Boolean,
    playCount: Int,
    onFavoriteClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentSong?.let { song ->
            // Song title
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Artist and album
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onArtistClick(song.artist) }
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = song.album,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onAlbumClick(song.album) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Additional info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Favorite button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Song details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (song.year > 0) {
                        Text(
                            text = song.year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (playCount > 0) {
                        Text(
                            text = "$playCount plays",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Genre
                if (song.genre?.isNotBlank() == true) {
                    Chip(
                        onClick = { /* TODO: Navigate to genre */ },
                        label = {
                            Text(
                                text = song.genre,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    isSeekingByUser: Boolean,
    onSeekChange: (Float) -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress bar
        PlayerSeekBar(
            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            bufferedProgress = if (duration > 0) bufferedPosition.toFloat() / duration else 0f,
            onSeekChange = onSeekChange,
            onSeekStart = onSeekStart,
            onSeekEnd = onSeekEnd,
            enabled = duration > 0 && !isSeekingByUser,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MainControlsSection(
    isPlaying: Boolean,
    isPaused: Boolean,
    isBuffering: Boolean,
    canPlay: Boolean,
    canPause: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = onPreviousClick,
            enabled = hasPrevious,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(32.dp),
                tint = if (hasPrevious) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        // Play/Pause button
        FloatingActionButton(
            onClick = onPlayPauseClick,
            modifier = Modifier.size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Next button
        IconButton(
            onClick = onNextClick,
            enabled = hasNext,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(32.dp),
                tint = if (hasNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SecondaryControlsSection(
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    playbackSpeed: Float,
    queueSize: Int,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleMode == ShuffleMode.ON) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Repeat
        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.OFF -> Icons.Default.Repeat
                    RepeatMode.ALL -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                },
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Playback speed
        TextButton(
            onClick = onSpeedClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "${playbackSpeed}x",
                style = MaterialTheme.typography.labelLarge,
                color = if (playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Queue
        BadgedBox(
            badge = {
                if (queueSize > 1) {
                    Badge {
                        Text(
                            text = queueSize.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        ) {
            IconButton(onClick = onQueueClick) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Queue",
                    tint = if (queueSize > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdditionalControlsSection(
    onEqualizerClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEqualizerClick) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Equalizer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onSleepTimerClick) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Sleep Timer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onLyricsClick) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = "Lyrics",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerDialogs(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel
) {
    // Lyrics Dialog
    if (uiState.showLyrics) {
        LyricsDialog(
            song = uiState.currentSong,
            onDismiss = { viewModel.showLyrics(false) }
        )
    }

    // Equalizer Dialog
    if (uiState.showEqualizer) {
        EqualizerDialog(
            enabled = uiState.equalizerEnabled,
            preset = uiState.equalizerPreset,
            bands = uiState.equalizerBands,
            onDismiss = { viewModel.showEqualizer(false) },
            onApply = { enabled, preset, bands ->
                // TODO: Apply equalizer settings
                viewModel.showEqualizer(false)
            }
        )
    }

    // Playback Speed Dialog
    if (uiState.showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = uiState.playbackSpeed,
            onDismiss = { viewModel.showSpeedDialog(false) },
            onSpeedChange = { speed ->
                viewModel.updatePlaybackSpeed(speed)
                viewModel.showSpeedDialog(false)
            }
        )
    }

    // Sleep Timer Dialog
    if (uiState.showSleepTimer) {
        SleepTimerDialog(
            isEnabled = uiState.sleepTimerEnabled,
            currentDuration = uiState.sleepTimerRemaining,
            onDismiss = { viewModel.showSleepTimerDialog(false) },
            onSetTimer = { minutes ->
                viewModel.startSleepTimer(minutes)
                viewModel.showSleepTimerDialog(false)
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
                viewModel.showSleepTimerDialog(false)
            }
        )
    }

    // Queue Dialog
    if (uiState.showQueue) {
        QueueDialog(
            queue = uiState.currentQueue,
            currentIndex = uiState.currentIndex,
            onDismiss = { viewModel.showQueue(false) },
            onSongClick = { index ->
                viewModel.jumpToQueueIndex(index)
                viewModel.showQueue(false)
            },
            onRemoveFromQueue = viewModel::removeFromQueue
        )
    }
}

@Composable
private fun PlayerLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Playback Error",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
            
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PlayerEmptyState(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MusicOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Song Playing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select a song to start playback",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Music")
        }
    }
}

// Helper Components

@Composable
private fun PlayerSeekBar(
    progress: Float,
    bufferedProgress: Float,
    onSeekChange: (Float) -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Slider(
        value = progress.coerceIn(0f, 1f),
        onValueChange = onSeekChange,
        modifier = modifier,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun SleepTimerIndicator(
    remainingTime: String,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Sleep timer: $remainingTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            TextButton(onClick = onCancelClick) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ErrorSnackbar(
    error: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun QueueDialog(
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSongClick: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Play Queue (${queue.size} songs)")
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                itemsIndexed(queue) { index, song ->
                    QueueSongItem(
                        song = song,
                        isCurrentSong = index == currentIndex,
                        onClick = { onSongClick(index) },
                        onRemove = { onRemoveFromQueue(index) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun QueueSongItem(
    song: Song,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSong) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isCurrentSong) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Currently playing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove from queue",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(4.dp))
}

// Duration formatting helper
private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
