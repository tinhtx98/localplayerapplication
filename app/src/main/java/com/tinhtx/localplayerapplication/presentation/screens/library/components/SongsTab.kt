package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.components.music.SongItem
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@Composable
fun SongsTab(
    songs: List<Song>,
    viewMode: AppConstants.ViewMode,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    val listState = rememberLazyListState()
    
    if (songs.isEmpty()) {
        EmptyLibraryTabState(
            message = "No songs found",
            subtitle = "Your music library is empty",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    when (viewMode) {
        AppConstants.ViewMode.LIST -> {
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = songs,
                    key = { it.id }
                ) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        onMoreClick = { onAddToPlaylistClick(song) },
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .animateItemPlacement()
                    )
                }
            }
        }
        
        AppConstants.ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 8.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = songs,
                    key = { it.id }
                ) { song ->
                    SongGridItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        onMoreClick = { onAddToPlaylistClick(song) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@Composable
private fun SongGridItem(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SongItem(
        song = song,
        onClick = onClick,
        onFavoriteClick = onFavoriteClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
        showAlbumArt = true,
        showDuration = false
    )
}
