package com.tinhtx.localplayerapplication.presentation.screens.library

data class LibraryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val sortOrder: AppConstants.SortOrder = AppConstants.SortOrder.TITLE_ASC,
    val viewMode: AppConstants.ViewMode = AppConstants.ViewMode.LIST,
    val gridSize: AppConstants.GridSize = AppConstants.GridSize.MEDIUM,
    val activeFilters: List<LibraryFilter> = emptyList(),
    val availableFilters: List<LibraryFilter> = emptyList(),
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val filteredAlbums: List<Album> = emptyList(),
    val filteredArtists: List<Artist> = emptyList(),
    val filteredPlaylists: List<Playlist> = emptyList(),
    val libraryStats: LibraryStats = LibraryStats(),
    val tabCounts: Map<LibraryTab, Int> = emptyMap(),
    val isEmpty: Boolean = false
)