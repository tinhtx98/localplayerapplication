package com.tinhtx.localplayerapplication.presentation.shared.state

/**
 * Base UI state interface for all screens
 */
interface UiState {
    val isLoading: Boolean
    val error: String?
}

/**
 * Generic UI state with data
 */
data class DataUiState<T>(
    val  T? = null,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isEmpty: Boolean = false,
    val isRefreshing: Boolean = false
) : UiState {
    
    val hasData: Boolean get() = data != null
    val isIdle: Boolean get() = !isLoading && error == null
    val isSuccess: Boolean get() = hasData && !isLoading && error == null
    val isError: Boolean get() = error != null
    
    companion object {
        fun <T> loading( T? = null): DataUiState<T> = DataUiState(data = data, isLoading = true)
        fun <T> success( T): DataUiState<T> = DataUiState(data = data)
        fun <T> error(message: String,  T? = null): DataUiState<T> = DataUiState(data = data, error = message)
        fun <T> empty(): DataUiState<T> = DataUiState(isEmpty = true)
        fun <T> refreshing( T): DataUiState<T> = DataUiState(data = data, isRefreshing = true)
    }
}

/**
 * List UI state for collections
 */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val selectedItems: Set<T> = emptySet()
) : UiState {
    
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
    val hasItems: Boolean get() = items.isNotEmpty()
    val isSuccess: Boolean get() = hasItems && !isLoading && error == null
    val isError: Boolean get() = error != null
    val hasSelection: Boolean get() = selectedItems.isNotEmpty()
    val isSearching: Boolean get() = searchQuery.isNotBlank()
    val totalItems: Int get() = items.size
    val selectedCount: Int get() = selectedItems.size
    
    companion object {
        fun <T> loading(): ListUiState<T> = ListUiState(isLoading = true)
        fun <T> success(items: List<T>): ListUiState<T> = ListUiState(items = items)
        fun <T> error(message: String): ListUiState<T> = ListUiState(error = message)
        fun <T> empty(): ListUiState<T> = ListUiState()
        fun <T> refreshing(items: List<T>): ListUiState<T> = ListUiState(items = items, isRefreshing = true)
        fun <T> loadingMore(items: List<T>): ListUiState<T> = ListUiState(items = items, isLoadingMore = true)
    }
}

/**
 * Paged UI state for paginated data
 */
data class PagedUiState<T>(
    val items: List<T> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalItems: Int = 0,
    val pageSize: Int = 20
) : UiState {
    
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading
    val hasItems: Boolean get() = items.isNotEmpty()
    val isSuccess: Boolean get() = hasItems && !isLoading && error == null
    val isError: Boolean get() = error != null
    val isFirstPage: Boolean get() = currentPage <= 1
    val isLastPage: Boolean get() = currentPage >= totalPages || !hasMore
    val canLoadMore: Boolean get() = hasMore && !isLoadingMore && !isLoading
    
    companion object {
        fun <T> loading(): PagedUiState<T> = PagedUiState(isLoading = true)
        fun <T> success(
            items: List<T>,
            currentPage: Int = 1,
            totalPages: Int = 1,
            totalItems: Int = items.size,
            hasMore: Boolean = false
        ): PagedUiState<T> = PagedUiState(
            items = items,
            currentPage = currentPage,
            totalPages = totalPages,
            totalItems = totalItems,
            hasMore = hasMore
        )
        fun <T> error(message: String): PagedUiState<T> = PagedUiState(error = message)
        fun <T> empty(): PagedUiState<T> = PagedUiState()
    }
}

/**
 * Form UI state for input forms
 */
data class FormUiState(
    val fields: Map<String, FormFieldState> = emptyMap(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isSubmitting: Boolean = false,
    val isValid: Boolean = true,
    val hasChanges: Boolean = false
) : UiState {
    
    val canSubmit: Boolean get() = isValid && hasChanges && !isSubmitting && !isLoading
    val hasErrors: Boolean get() = fields.values.any { it.hasError } || error != null
    
    fun getField(key: String): FormFieldState = fields[key] ?: FormFieldState()
    
    fun updateField(key: String, value: String, error: String? = null): FormUiState {
        val updatedFields = fields.toMutableMap()
        updatedFields[key] = FormFieldState(value = value, error = error)
        
        return copy(
            fields = updatedFields,
            hasChanges = true,
            isValid = updatedFields.values.none { it.hasError }
        )
    }
    
    companion object {
        fun loading(): FormUiState = FormUiState(isLoading = true)
        fun submitting(fields: Map<String, FormFieldState>): FormUiState = 
            FormUiState(fields = fields, isSubmitting = true)
        fun error(message: String, fields: Map<String, FormFieldState>): FormUiState = 
            FormUiState(fields = fields, error = message)
    }
}

/**
 * Form field state
 */
data class FormFieldState(
    val value: String = "",
    val error: String? = null,
    val isRequired: Boolean = false,
    val isValid: Boolean = true
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = value.isBlank()
    val isValidRequired: Boolean get() = !isRequired || value.isNotBlank()
}

/**
 * Search UI state
 */
data class SearchUiState<T>(
    val query: String = "",
    val results: List<T> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val totalResults: Int = 0
) : UiState {
    
    val hasQuery: Boolean get() = query.isNotBlank()
    val hasResults: Boolean get() = results.isNotEmpty()
    val isEmpty: Boolean get() = hasSearched && results.isEmpty() && !isLoading
    val hasSuggestions: Boolean get() = suggestions.isNotEmpty()
    val hasRecentSearches: Boolean get() = recentSearches.isNotEmpty()
    val showSuggestions: Boolean get() = hasQuery && !hasSearched && !isLoading
    val showRecentSearches: Boolean get() = !hasQuery && !hasSearched && hasRecentSearches
    
    companion object {
        fun <T> idle(): SearchUiState<T> = SearchUiState()
        fun <T> searching(query: String): SearchUiState<T> = SearchUiState(
            query = query,
            isLoading = true,
            isSearching = true
        )
        fun <T> results(
            query: String,
            results: List<T>,
            totalResults: Int = results.size
        ): SearchUiState<T> = SearchUiState(
            query = query,
            results = results,
            hasSearched = true,
            totalResults = totalResults
        )
        fun <T> error(query: String, message: String): SearchUiState<T> = SearchUiState(
            query = query,
            error = message,
            hasSearched = true
        )
    }
}

/**
 * Player UI state
 */
data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val isBuffering: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val volume: Float = 1.0f,
    val playbackSpeed: Float = 1.0f,
    val canPlay: Boolean = false
) : UiState {
    
    val progress: Float get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
    val bufferedProgress: Float get() = if (duration > 0) bufferedPosition.toFloat() / duration else 0f
    val remainingTime: Long get() = duration - currentPosition
    val canSeek: Boolean get() = duration > 0 && !isLoading
    val isIdle: Boolean get() = !isPlaying && !isLoading && !isBuffering
    
    companion object {
        fun idle(): PlayerUiState = PlayerUiState()
        fun loading(): PlayerUiState = PlayerUiState(isLoading = true)
        fun buffering(): PlayerUiState = PlayerUiState(isBuffering = true)
        fun playing(
            currentPosition: Long,
            duration: Long,
            bufferedPosition: Long = currentPosition
        ): PlayerUiState = PlayerUiState(
            isPlaying = true,
            currentPosition = currentPosition,
            duration = duration,
            bufferedPosition = bufferedPosition,
            canPlay = true
        )
        fun paused(
            currentPosition: Long,
            duration: Long,
            bufferedPosition: Long = currentPosition
        ): PlayerUiState = PlayerUiState(
            isPlaying = false,
            currentPosition = currentPosition,
            duration = duration,
            bufferedPosition = bufferedPosition,
            canPlay = true
        )
        fun error(message: String): PlayerUiState = PlayerUiState(error = message)
    }
}
