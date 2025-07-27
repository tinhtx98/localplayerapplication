package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
fun DraggableQueueList(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteSongs: Set<Long>,
    selectedSongs: Set<Long>,
    isSelectionMode: Boolean,
    compactView: Boolean,
    showSongNumbers: Boolean,
    canReorder: Boolean,
    isDragging: Boolean,
    draggedIndex: Int?,
    dropTargetIndex: Int?,
    searchQuery: String,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onStartDragging: (Int) -> Unit,
    onUpdateDropTarget: (Int?) -> Unit,
    onEndDragging: () -> Unit,
    onReorderSong: (Int, Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(if (compactView) 2.dp else 4.dp)
    ) {
        itemsIndexed(
            items = songs,
            key = { _, song -> song.id }
        ) { index, song ->
            val isSelected = selectedSongs.contains(song.id)
            val isFavorite = favoriteSongs.contains(song.id)
            val isCurrentlyPlaying = currentSong?.id == song.id
            val isDragged = draggedIndex == index
            val isDropTarget = dropTargetIndex == index

            if (compactView) {
                QueueSongItemCompact(
                    song = song,
                    index = index + 1,
                    isSelected = isSelected,
                    isFavorite = isFavorite,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    isPlaying = isPlaying,
                    onClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onRemove = { onRemoveSong(song) },
                    searchQuery = searchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                )
            } else {
                QueueSongItem(
                    song = song,
                    index = index + 1,
                    isSelected = isSelected,
                    isFavorite = isFavorite,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    isPlaying = isPlaying,
                    isSelectionMode = isSelectionMode,
                    compactView = compactView,
                    showSongNumber = showSongNumbers,
                    canReorder = canReorder,
                    isDragging = isDragged,
                    isDropTarget = isDropTarget,
                    searchQuery = searchQuery,
                    onClick = { onSongClick(song) },
                    onLongClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSongLongClick(song)
                    },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onRemove = { onRemoveSong(song) },
                    onMoreClick = { /* TODO: Show song options */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .let { baseModifier ->
                            if (canReorder && !isSelectionMode) {
                                baseModifier.draggableItem(
                                    index = index,
                                    onStartDragging = onStartDragging,
                                    onUpdateDropTarget = onUpdateDropTarget,
                                    onEndDragging = onEndDragging,
                                    onReorder = onReorderSong
                                )
                            } else {
                                baseModifier
                            }
                        }
                )
            }
        }

        // Add bottom padding for mini player
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DraggableQueueGrid(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    favoriteSongs: Set<Long>,
    selectedSongs: Set<Long>,
    isSelectionMode: Boolean,
    columns: Int = 2,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chunkedSongs = songs.chunked(columns)
        
        items(chunkedSongs.size) { rowIndex ->
            val songsInRow = chunkedSongs[rowIndex]
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                songsInRow.forEach { song ->
                    val isSelected = selectedSongs.contains(song.id)
                    val isFavorite = favoriteSongs.contains(song.id)
                    val isCurrentlyPlaying = currentSong?.id == song.id
                    
                    QueueSongGridItem(
                        song = song,
                        isSelected = isSelected,
                        isFavorite = isFavorite,
                        isCurrentlyPlaying = isCurrentlyPlaying,
                        isPlaying = isPlaying,
                        onClick = { onSongClick(song) },
                        onLongClick = { onSongLongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onRemove = { onRemoveSong(song) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill empty spaces
                repeat(columns - songsInRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QueueSongGridItem(
    song: Song,
    isSelected: Boolean,
    isFavorite: Boolean,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCurrentlyPlaying -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Song info
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Extension function for draggable behavior
private fun Modifier.draggableItem(
    index: Int,
    onStartDragging: (Int) -> Unit,
    onUpdateDropTarget: (Int?) -> Unit,
    onEndDragging: () -> Unit,
    onReorder: (Int, Int) -> Unit
): Modifier {
    // TODO: Implement actual drag and drop behavior using Modifier.dragAndDropSource/Target
    // For now, return the modifier as-is
    return this
}
