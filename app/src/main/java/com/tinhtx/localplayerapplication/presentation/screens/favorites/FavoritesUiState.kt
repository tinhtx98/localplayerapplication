package com.tinhtx.localplayerapplication.presentation.screens.favorites

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val favorites: List<Song> = emptyList(),
    val filteredFavorites: List<Song> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: AppConstants.SortOrder = AppConstants.SortOrder.DATE_ADDED_DESC,
    val isSearching: Boolean = false,
    val totalDuration: String = "",
    val averageRating: Float = 0f
)