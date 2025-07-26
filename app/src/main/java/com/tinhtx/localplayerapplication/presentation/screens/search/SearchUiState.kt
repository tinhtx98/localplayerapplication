package com.tinhtx.localplayerapplication.presentation.screens.search

data class SearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: SearchResults = SearchResults(),
    val totalResults: Int = 0,
    val searchTime: Long = 0L,
    val topResult: Any? = null,
    val quickActions: List<SearchQuickAction> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val popularSearches: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val error: String? = null
)