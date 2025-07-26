package com.tinhtx.localplayerapplication.presentation.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.core.utils.MediaUtils
import com.tinhtx.localplayerapplication.domain.model.RepeatMode
import com.tinhtx.localplayerapplication.domain.model.ShuffleMode
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.music.*
import com.tinhtx.localplayerapplication.presentation.theme.MusicShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    queue: List<Song>,
    shuffleMode: ShuffleMode,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQueueItemClick: (Song) -> Unit,
    onQueueItemRemove: (Song) -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false
) {
    if (currentSong == null) return
    
    var selectedTab by remember { mutableStateOf(PlayerBottomSheetTab.PLAYER) }
    
    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetContent = {
            PlayerBottomSheetContent(
                currentSong = currentSong,
                isPlaying = isPlaying,
                progress = progress,
                currentTime = currentTime,
                totalTime = totalTime,
                queue = queue,
                shuffleMode = shuffleMode,
                repeatMode = repeatMode,
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onSeek = onSeek,
                onShuffleClick = onShuffleClick,
                onRepeatClick = onRepeatClick,
                onQueueItemClick = onQueueItemClick,
                onQueueItemRemove = onQueueItemRemove,
                onFavoriteClick = onFavoriteClick,
                onShareClick = onShareClick,
                onAddToPlaylistClick = onAddToPlaylistClick,
                isFavorite = isFavorite
            )
        },
        sheetPeekHeight = 0.dp,
        modifier = modifier
    ) {
        // Content when bottom sheet is collapsed
    }
}

@Composable
private fun PlayerBottomSheetContent(
    currentSong: Song,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    queue: List<Song>,
    shuffleMode: ShuffleMode,
    repeatMode: RepeatMode,
    selectedTab: PlayerBottomSheetTab,
    onTabChange: (PlayerBottomSheetTab) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQueueItemClick: (Song) -> Unit,
    onQueueItemRemove: (Song) -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    isFavorite: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab row
        PlayerBottomSheetTabs(
            selectedTab = selectedTab,
            onTabChange = onTabChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (selectedTab) {
            PlayerBottomSheetTab.PLAYER -> {
                FullPlayerContent(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    currentTime = currentTime,
                    totalTime = totalTime,
                    shuffleMode = shuffleMode,
                    repeatMode = repeatMode,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onSeek = onSeek,
                    onShuffleClick = onShuffleClick,
                    onRepeatClick = onRepeatClick,
                    onFavoriteClick = onFavoriteClick,
                    onShareClick = onShareClick,
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    isFavorite = isFavorite
                )
            }
            PlayerBottomSheetTab.QUEUE -> {
                QueueContent(
                    queue = queue,
                    currentSong = currentSong,
                    onQueueItemClick = onQueueItemClick,
                    onQueueItemRemove = onQueueItemRemove
                )
            }
            PlayerBottomSheetTab.LYRICS -> {
                LyricsContent(
                    currentSong = currentSong
                )
            }
        }
    }
}

@Composable
private fun PlayerBottomSheetTabs(
    selectedTab: PlayerBottomSheetTab,
    onTabChange: (PlayerBottomSheetTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PlayerBottomSheetTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            
            TextButton(
                onClick = { onTabChange(tab) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun FullPlayerContent(
    currentSong: Song,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    shuffleMode: ShuffleMode,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    isFavorite: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album artwork
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(MediaUtils.getAlbumArtUri(currentSong.albumId))
                .crossfade(true)
                .build(),
            contentDescription = "Album artwork",
            modifier = Modifier
                .size(280.dp)
                .clip(MusicShapes.albumCover)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Song info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentSong.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = currentSong.displayArtist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Seek bar
        MusicSeekBar(
            progress = progress,
            onProgressChanged = onSeek,
            currentTime = currentTime,
            totalTime = totalTime,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Player controls
        PlayerControls(
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            onShuffleClick = onShuffleClick,
            onRepeatClick = onRepeatClick,
            controlsSize = PlayerControlsSize.LARGE
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(onClick = onAddToPlaylistClick) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Add to playlist"
                )
            }
            
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
        }
    }
}

@Composable
private fun QueueContent(
    queue: List<Song>,
    currentSong: Song,
    onQueueItemClick: (Song) -> Unit,
    onQueueItemRemove: (Song) -> Unit
) {
    Column {
        Text(
            text = "Playing Queue (${queue.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn {
            items(queue, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentSong.id,
                    onClick = { onQueueItemClick(song) },
                    onMoreClick = { onQueueItemRemove(song) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun LyricsContent(
    currentSong: Song
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lyrics",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Lyrics not available for \"${currentSong.title}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

enum class PlayerBottomSheetTab(val title: String) {
    PLAYER("Player"),
    QUEUE("Queue"),
    LYRICS("Lyrics")
}
