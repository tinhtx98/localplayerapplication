package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.model.Album
import com.tinhtx.localplayerapplication.presentation.components.music.AlbumCard
import com.tinhtx.localplayerapplication.presentation.components.music.HorizontalAlbumCard
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@Composable
fun AlbumsTab(
    albums: List<Album>,
    viewMode: AppConstants.ViewMode,
    gridSize: AppConstants.GridSize,
    onAlbumClick: (Album) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()
    
    if (albums.isEmpty()) {
        EmptyLibraryTabState(
            message = "No albums found",
            subtitle = "Your album collection is empty",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    when (viewMode) {
        AppConstants.ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 8.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = albums,
                    key = { it.id }
                ) { album ->
                    HorizontalAlbumCard(
                        album = album,
                        onClick = { onAlbumClick(album) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
        
        AppConstants.ViewMode.GRID -> {
            val columns = when (gridSize) {
                AppConstants.GridSize.SMALL -> GridCells.Adaptive(120.dp)
                AppConstants.GridSize.MEDIUM -> GridCells.Adaptive(160.dp)
                AppConstants.GridSize.LARGE -> GridCells.Adaptive(200.dp)
            }
            
            LazyVerticalGrid(
                columns = columns,
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
                    items = albums,
                    key = { it.id }
                ) { album ->
                    AlbumCard(
                        album = album,
                        onClick = { onAlbumClick(album) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}
