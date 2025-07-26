package com.tinhtx.localplayerapplication.presentation.screens.playlist

data class PlaylistUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: AppConstants.SortOrder = AppConstants.SortOrder.CUSTOM,
    val isSearching: Boolean = false,
    val totalDuration: String = ""
)