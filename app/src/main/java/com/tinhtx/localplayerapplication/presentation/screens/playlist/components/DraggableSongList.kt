package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Song

@Composable
fun DraggableSongList(
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    favoriteSongs: Set<Long>,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    
    // State for drag and drop
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = songs,
            key = { _, song -> song.id }
        ) { index, song ->
            
            val isDragged = draggedIndex == index
            val isTarget = targetIndex == index
            
            PlaylistSongItem(
                song = song,
                index = index + 1,
                isSelected = false, // Not used in draggable list
                isFavorite = favoriteSongs.contains(song.id),
                isCurrentlyPlaying = currentPlayingSong?.id == song.id,
                isPlaying = isPlaying,
                isSelectionMode = false, // Not used in draggable list
                canReorder = true, // Always true for draggable list
                onClick = { onSongClick(song) },
                onLongClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSongLongClick(song)
                },
                onToggleSelection = { /* Not used in draggable list */ },
                onToggleFavorite = { onToggleFavorite(song) },
                onMoreClick = { onMoreClick(song) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .then(
                        if (isDragged) {
                            Modifier.alpha(0.5f)
                        } else if (isTarget) {
                            Modifier.alpha(0.8f)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

// Extension function for alpha modifier
private fun Modifier.alpha(alpha: Float): Modifier = this.then(
    androidx.compose.ui.Modifier.alpha(alpha)
)
