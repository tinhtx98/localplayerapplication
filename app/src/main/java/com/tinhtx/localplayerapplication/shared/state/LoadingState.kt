package com.tinhtx.localplayerapplication.presentation.shared.state

/**
 * Represents different loading states in the application
 */
sealed class LoadingState {
    
    /**
     * Not loading
     */
    object Idle : LoadingState()
    
    /**
     * Initial loading (first time)
     */
    object Loading : LoadingState()
    
    /**
     * Refreshing existing data
     */
    object Refreshing : LoadingState()
    
    /**
     * Loading more data (pagination)
     */
    object LoadingMore : LoadingState()
    
    /**
     * Submitting/Saving data
     */
    object Submitting : LoadingState()
    
    /**
     * Custom loading state with message
     */
    data class Custom(val message: String) : LoadingState()
    
    // Convenience properties
    val isLoading: Boolean get() = this !is Idle
    val isIdle: Boolean get() = this is Idle
    val isInitialLoading: Boolean get() = this is Loading
    val isRefreshing: Boolean get() = this is Refreshing
    val isLoadingMore: Boolean get() = this is LoadingMore
    val isSubmitting: Boolean get() = this is Submitting
    val isCustom: Boolean get() = this is Custom
    
    /**
     * Get loading message
     */
    fun getMessage(): String = when (this) {
        is Idle -> ""
        is Loading -> "Loading..."
        is Refreshing -> "Refreshing..."
        is LoadingMore -> "Loading more..."
        is Submitting -> "Submitting..."
        is Custom -> message
    }
    
    /**
     * Get loading progress (indeterminate by default)
     */
    fun getProgress(): Float? = null
    
    companion object {
        fun idle(): LoadingState = Idle
        fun loading(): LoadingState = Loading
        fun refreshing(): LoadingState = Refreshing
        fun loadingMore(): LoadingState = LoadingMore
        fun submitting(): LoadingState = Submitting
        fun custom(message: String): LoadingState = Custom(message)
    }
}

/**
 * Progress loading state with determinate progress
 */
data class ProgressLoadingState(
    val progress: Float,
    val message: String = "",
    val total: Long = 100L,
    val current: Long = (progress * total).toLong()
) : LoadingState() {
    
    val percentage: Int get() = (progress * 100).toInt()
    val isComplete: Boolean get() = progress >= 1.0f
    val progressText: String get() = "$percentage%"
    val detailedProgressText: String get() = "$current / $total"
    
    override fun getMessage(): String = if (message.isNotBlank()) message else "Loading... $progressText"
    override fun getProgress(): Float = progress
    
    companion object {
        fun create(current: Long, total: Long, message: String = ""): ProgressLoadingState {
            val progress = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            return ProgressLoadingState(progress, message, total, current)
        }
        
        fun fromPercentage(percentage: Int, message: String = ""): ProgressLoadingState {
            val progress = (percentage.toFloat() / 100f).coerceIn(0f, 1f)
            return ProgressLoadingState(progress, message)
        }
    }
}

/**
 * Batch loading state for multiple operations
 */
data class BatchLoadingState(
    val operations: Map<String, LoadingState> = emptyMap(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val failedCount: Int = 0
) : LoadingState() {
    
    val isAllComplete: Boolean get() = completedCount == totalCount && totalCount > 0
    val isAnyLoading: Boolean get() = operations.values.any { it.isLoading }
    val isAnyFailed: Boolean get() = failedCount > 0
    val successCount: Int get() = completedCount - failedCount
    val progress: Float get() = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val remainingCount: Int get() = totalCount - completedCount
    
    override fun getMessage(): String = when {
        isAllComplete && failedCount == 0 -> "All operations completed successfully"
        isAllComplete && failedCount > 0 -> "Completed with $failedCount failures"
        isAnyLoading -> "Processing operations... ($completedCount/$totalCount)"
        else -> "Preparing operations..."
    }
    
    override fun getProgress(): Float = progress
    
    fun addOperation(key: String, state: LoadingState): BatchLoadingState {
        val newOperations = operations.toMutableMap()
        newOperations[key] = state
        
        return copy(
            operations = newOperations,
            totalCount = newOperations.size,
            completedCount = newOperations.values.count { it.isIdle },
            failedCount = 0 // Reset failed count when adding new operations
        )
    }
    
    fun updateOperation(key: String, state: LoadingState, failed: Boolean = false): BatchLoadingState {
        val newOperations = operations.toMutableMap()
        newOperations[key] = state
        
        val newCompletedCount = newOperations.values.count { it.isIdle }
        val newFailedCount = if (failed) failedCount + 1 else failedCount
        
        return copy(
            operations = newOperations,
            completedCount = newCompletedCount,
            failedCount = newFailedCount
        )
    }
    
    companion object {
        fun create(operationKeys: List<String>): BatchLoadingState {
            val operations = operationKeys.associateWith { LoadingState.idle() }
            return BatchLoadingState(
                operations = operations,
                totalCount = operations.size
            )
        }
    }
}

/**
 * Network loading state with connection info
 */
data class NetworkLoadingState(
    val isConnected: Boolean = true,
    val connectionType: ConnectionType = ConnectionType.UNKNOWN,
    val downloadSpeed: Long = 0L, // bytes per second
    val uploadSpeed: Long = 0L, // bytes per second
    val latency: Long = 0L, // milliseconds
    val baseState: LoadingState = LoadingState.idle()
) : LoadingState() {
    
    val hasConnection: Boolean get() = isConnected
    val isSlowConnection: Boolean get() = downloadSpeed < 1024 * 1024 // < 1 MB/s
    val isFastConnection: Boolean get() = downloadSpeed > 10 * 1024 * 1024 // > 10 MB/s
    val isMetered: Boolean get() = connectionType == ConnectionType.MOBILE
    
    override fun getMessage(): String = when {
        !isConnected -> "No internet connection"
        isSlowConnection && baseState.isLoading -> "Loading... (slow connection)"
        else -> baseState.getMessage()
    }
    
    override fun getProgress(): Float? = baseState.getProgress()
    
    enum class ConnectionType {
        WIFI,
        MOBILE,
        ETHERNET,
        UNKNOWN
    }
    
    companion object {
        fun connected(
            connectionType: ConnectionType = ConnectionType.WIFI,
            baseState: LoadingState = LoadingState.idle()
        ): NetworkLoadingState = NetworkLoadingState(
            isConnected = true,
            connectionType = connectionType,
            baseState = baseState
        )
        
        fun disconnected(): NetworkLoadingState = NetworkLoadingState(
            isConnected = false,
            baseState = LoadingState.idle()
        )
    }
}

/**
 * Extension functions for LoadingState
 */

/**
 * Combine multiple loading states
 */
fun List<LoadingState>.combine(): LoadingState = when {
    any { it.isSubmitting } -> LoadingState.submitting()
    any { it.isInitialLoading } -> LoadingState.loading()
    any { it.isRefreshing } -> LoadingState.refreshing()
    any { it.isLoadingMore } -> LoadingState.loadingMore()
    any { it.isLoading } -> LoadingState.loading()
    else -> LoadingState.idle()
}

/**
 * Convert to boolean
 */
fun LoadingState.toBoolean(): Boolean = isLoading

/**
 * Convert to progress loading state
 */
fun LoadingState.withProgress(progress: Float, message: String = ""): ProgressLoadingState =
    ProgressLoadingState(progress, message.ifBlank { getMessage() })

/**
 * Create batch loading state from list
 */
fun List<String>.toBatchLoadingState(): BatchLoadingState = BatchLoadingState.create(this)
