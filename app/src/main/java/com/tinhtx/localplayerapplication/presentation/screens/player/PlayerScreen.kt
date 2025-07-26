package com.tinhtx.localplayerapplication.presentation.screens.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.audio.AudioVisualizer
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.music.*
import com.tinhtx.localplayerapplication.presentation.components.ui.PlayerTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.player.components.*
import com.tinhtx.localplayerapplication.presentation.theme.MusicShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showLyricsDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPlayerState()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PlayerTopAppBar(
                songTitle = uiState.currentSong?.title ?: "No song playing",
                onBackClick = onNavigateBack,
                onQueueClick = onNavigateToQueue,
                onMoreClick = { /* Show more options */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.currentSong == null -> {
                    NoSongPlayingState(
                        onBrowseMusic = onNavigateToLibrary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PlayerContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onPrevious = { viewModel.skipToPrevious() },
                        onNext = { viewModel.skipToNext() },
                        onSeek = { position -> viewModel.seekTo(position) },
                        onShuffleClick = { viewModel.toggleShuffle() },
                        onRepeatClick = { viewModel.toggleRepeat() },
                        onFavoriteClick = { viewModel.toggleFavorite() },
                        onVolumeChange = { volume -> viewModel.setVolume(volume) },
                        onShowLyrics = { showLyricsDialog = true },
                        onShowEqualizer = { showEqualizerDialog = true },
                        onShowSleepTimer = { showSleepTimerDialog = true },
                        onShowSpeedControl = { showSpeedDialog = true },
                        onAddToPlaylist = { viewModel.showAddToPlaylistDialog() },
                        onShare = { viewModel.shareSong() }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showLyricsDialog) {
        LyricsDialog(
            song = uiState.currentSong!!,
            onDismiss = { showLyricsDialog = false }
        )
    }

    if (showEqualizerDialog) {
        EqualizerDialog(
            currentPreset = uiState.equalizerPreset,
            onPresetChange = { preset -> viewModel.setEqualizerPreset(preset) },
            onDismiss = { showEqualizerDialog = false }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentTimer = uiState.sleepTimer,
            onTimerSet = { minutes -> viewModel.setSleepTimer(minutes) },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = uiState.playbackSpeed,
            onSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) },
            onDismiss = { showSpeedDialog = false }
        )
    }
}

@Composable
private fun PlayerContent(
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onShowLyrics: () -> Unit,
    onShowEqualizer: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onShowSpeedControl: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit
) {
    val song = uiState.currentSong!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Album artwork with animation
        AlbumArtworkSection(
            song = song,
            isPlaying = uiState.isPlaying,
            modifier = Modifier.weight(0.4f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Song information
        SongInfoSection(
            song = song,
            onFavoriteClick = onFavoriteClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Audio visualizer (optional)
        if (uiState.isPlaying && uiState.showVisualizer) {
            AudioVisualizer(
                audioData = uiState.audioData,
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress section
        PlayerProgressSection(
            progress = uiState.progress,
            currentTime = uiState.currentTimeString,
            totalTime = uiState.totalTimeString,
            onSeek = onSeek,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main player controls
        PlayerControlsSection(
            isPlaying = uiState.isPlaying,
            shuffleMode = uiState.shuffleMode,
            repeatMode = uiState.repeatMode,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext,
            onShuffleClick = onShuffleClick,
            onRepeatClick = onRepeatClick,
            hasNext = uiState.hasNext,
            hasPrevious = uiState.hasPrevious,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Volume control
        VolumeControlSection(
            volume = uiState.volume,
            onVolumeChange = onVolumeChange,
            isMuted = uiState.isMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Additional controls
        PlayerAdditionalControls(
            onShowLyrics = onShowLyrics,
            onShowEqualizer = onShowEqualizer,
            onShowSleepTimer = onShowSleepTimer,
            onShowSpeedControl = onShowSpeedControl,
            onAddToPlaylist = onAddToPlaylist,
            onShare = onShare,
            sleepTimerActive = uiState.sleepTimer > 0,
            playbackSpeed = uiState.playbackSpeed,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AlbumArtworkSection(
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background glow effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 400f
                    ),
                    shape = CircleShape
                )
        )

        // Main album artwork
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(MediaUtils.getAlbumArtUri(song.albumId))
                .crossfade(true)
                .build(),
            contentDescription = "Album artwork for ${song.title}",
            modifier = Modifier
                .size(280.dp)
                .clip(MusicShapes.albumCover)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = {
                DefaultAlbumArtwork(
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isPlaying
                )
            }
        )

        // Playing indicator
        AnimatedVisibility(
            visible = isPlaying,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(MusicShapes.albumCover)
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                PulsingMusicIcon(
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
private fun SongInfoSection(
    song: Song,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.displayArtist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                AnimatedContent(
                    targetState = song.isFavorite,
                    transitionSpec = {
                        scaleIn() + fadeIn() with scaleOut() + fadeOut()
                    },
                    label = "favorite_animation"
                ) { isFavorite ->
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (song.displayAlbum.isNotBlank() && song.displayAlbum != "Unknown Album") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.displayAlbum,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayerProgressSection(
    progress: Float,
    currentTime: String,
    totalTime: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        MusicSeekBar(
            progress = progress,
            onProgressChanged = onSeek,
            currentTime = currentTime,
            totalTime = totalTime,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlayerControlsSection(
    isPlaying: Boolean,
    shuffleMode: ShuffleMode,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    hasNext: Boolean,
    hasPrevious: Boolean,
    modifier: Modifier = Modifier
) {
    PlayerControls(
        isPlaying = isPlaying,
        onPlayPause = onPlayPause,
        onPrevious = onPrevious,
        onNext = onNext,
        shuffleMode = shuffleMode,
        repeatMode = repeatMode,
        onShuffleClick = onShuffleClick,
        onRepeatClick = onRepeatClick,
        controlsSize = PlayerControlsSize.LARGE,
        hasNext = hasNext,
        hasPrevious = hasPrevious,
        modifier = modifier
    )
}

@Composable
private fun VolumeControlSection(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isMuted || volume <= 0f) {
                Icons.Default.VolumeOff
            } else if (volume < 0.5f) {
                Icons.Default.VolumeDown
            } else {
                Icons.Default.VolumeUp
            },
            contentDescription = "Volume",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )

        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun PlayerAdditionalControls(
    onShowLyrics: () -> Unit,
    onShowEqualizer: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onShowSpeedControl: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit,
    sleepTimerActive: Boolean,
    playbackSpeed: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Lyrics
        IconButton(onClick = onShowLyrics) {
            Icon(
                imageVector = Icons.Default.Subtitles,
                contentDescription = "Lyrics",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Equalizer
        IconButton(onClick = onShowEqualizer) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Equalizer",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Sleep timer
        IconButton(onClick = onShowSleepTimer) {
            Badge(
                modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                containerColor = if (sleepTimerActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
            ) {
                if (sleepTimerActive) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(6.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Sleep timer",
                tint = if (sleepTimerActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                }
            )
        }

        // Playback speed
        IconButton(onClick = onShowSpeedControl) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Playback speed",
                    tint = if (playbackSpeed != 1.0f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    }
                )
                if (playbackSpeed != 1.0f) {
                    Text(
                        text = "${playbackSpeed}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.offset(y = 12.dp)
                    )
                }
            }
        }

        // Add to playlist
        IconButton(onClick = onAddToPlaylist) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = "Add to playlist",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Share
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NoSongPlayingState(
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No song playing",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose a song from your library to start playing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBrowseMusic,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Music")
        }
    }
}
