package com.tinhtx.localplayerapplication.shared.state

/**
 * A generic wrapper class that represents a resource with loading, success, and error states
 */
sealed class Resource<out T> {
    
    /**
     * Loading state
     */
    data class Loading<out T>(val data: T? = null) : Resource<T>()

    /**
     * Success state with data
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * Error state with error message and optional data
     */
    data class Error<out T>(
        val message: String,
        val throwable: Throwable? = null,
        val data: T? = null
    ) : Resource<T>()
    
    /**
     * Empty state (no data available)
     */
    class Empty<out T> : Resource<T>()
    
    // Convenience properties
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isEmpty: Boolean get() = this is Empty
    
    /**
     * Get data regardless of state
     */
    fun data(): T? = when (this) {
        is Success -> data
        is Error -> data
        is Loading -> data
        is Empty -> null
    }
    
    /**
     * Get data or throw exception if error
     */
    fun dataOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw throwable ?: Exception(message)
        is Loading -> throw IllegalStateException("Data is still loading")
        is Empty -> throw IllegalStateException("No data available")
    }
    
    /**
     * Get data or return null
     */
    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Get error message or null
     */
    fun errorOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
    
    /**
     * Transform the data if success
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, throwable, data?.let(transform))
        is Loading -> Loading(data?.let(transform))
        is Empty -> Empty()
    }
    
    /**
     * Execute action if success
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action if error
     */
    inline fun onError(action: (String, Throwable?) -> Unit): Resource<T> {
        if (this is Error) action(message, throwable)
        return this
    }
    
    /**
     * Execute action if loading
     */
    inline fun onLoading(action: (T?) -> Unit): Resource<T> {
        if (this is Loading) action(data)
        return this
    }
    
    /**
     * Execute action if empty
     */
    inline fun onEmpty(action: () -> Unit): Resource<T> {
        if (this is Empty) action()
        return this
    }
    
    companion object {
        /**
         * Create loading resource
         */
        fun <T> loading(data: T? = null): Resource<T> = Loading(data)

        /**
         * Create success resource
         */
        fun <T> success(data: T): Resource<T> = Success(data)

        /**
         * Create error resource
         */
        fun <T> error(
            message: String,
            throwable: Throwable? = null,
            data: T? = null
        ): Resource<T> = Error(message, throwable, data)
        
        /**
         * Create empty resource
         */
        fun <T> empty(): Resource<T> = Empty()
        
        /**
         * Create resource from nullable data
         */
        fun <T> fromNullable(data: T?, errorMessage: String = "Data is null"): Resource<T> =
            data?.let { success(it) } ?: error(errorMessage)
        
        /**
         * Create resource from result
         */
        fun <T> fromResult(result: Result<T>): Resource<T> = result.fold(
            onSuccess = { success(it) },
            onFailure = { error(it.message ?: "Unknown error", it) }
        )
        
        /**
         * Combine multiple resources
         */
        fun <T1, T2, R> combine(
            resource1: Resource<T1>,
            resource2: Resource<T2>,
            transform: (T1, T2) -> R
        ): Resource<R> = when {
            resource1 is Error -> Error(resource1.message, resource1.throwable)
            resource2 is Error -> Error(resource2.message, resource2.throwable)
            resource1 is Loading || resource2 is Loading -> Loading()
            resource1 is Empty || resource2 is Empty -> Empty()
            resource1 is Success && resource2 is Success -> Success(transform(resource1.data, resource2.data))
            else -> Empty()
        }
        
        /**
         * Combine three resources
         */
        fun <T1, T2, T3, R> combine(
            resource1: Resource<T1>,
            resource2: Resource<T2>,
            resource3: Resource<T3>,
            transform: (T1, T2, T3) -> R
        ): Resource<R> = when {
            resource1 is Error -> Error(resource1.message, resource1.throwable)
            resource2 is Error -> Error(resource2.message, resource2.throwable)
            resource3 is Error -> Error(resource3.message, resource3.throwable)
            resource1 is Loading || resource2 is Loading || resource3 is Loading -> Loading()
            resource1 is Empty || resource2 is Empty || resource3 is Empty -> Empty()
            resource1 is Success && resource2 is Success && resource3 is Success -> 
                Success(transform(resource1.data, resource2.data, resource3.data))
            else -> Empty()
        }
    }
}

/**
 * Resource result for operations that don't return data
 */
sealed class ResourceResult {
    object Success : ResourceResult()
    object Loading : ResourceResult()
    data class Error(val message: String, val throwable: Throwable? = null) : ResourceResult()
    
    val isSuccess: Boolean get() = this is Success
    val isLoading: Boolean get() = this is Loading
    val isError: Boolean get() = this is Error
    
    fun errorOrNull(): String? = (this as? Error)?.message
    
    companion object {
        fun loading(): ResourceResult = Loading
        fun success(): ResourceResult = Success
        fun error(message: String, throwable: Throwable? = null): ResourceResult = Error(message, throwable)
        
        fun fromResult(result: Result<*>): ResourceResult = result.fold(
            onSuccess = { success() },
            onFailure = { error(it.message ?: "Unknown error", it) }
        )
    }
}

/**
 * Extension functions for Resource
 */

/**
 * Convert Resource to UiState
 */
fun <T> Resource<T>.toUiState(): DataUiState<T> = when (this) {
    is Resource.Loading -> DataUiState.loading(data)
    is Resource.Success -> DataUiState.success(data)
    is Resource.Error -> DataUiState.error(message, data)
    is Resource.Empty -> DataUiState.empty()
}

/**
 * Flatten nested resources
 */
fun <T> Resource<Resource<T>>.flatten(): Resource<T> = when (this) {
    is Resource.Success -> data
    is Resource.Error -> Resource.error(message, throwable)
    is Resource.Loading -> Resource.loading()
    is Resource.Empty -> Resource.empty()
}

/**
 * Safe cast resource data
 */
inline fun <reified R> Resource<*>.safeCast(): Resource<R> = when (this) {
    is Resource.Success -> if (data is R) Resource.success(data) else Resource.error("Invalid data type")
    is Resource.Error -> Resource.error(message, throwable)
    is Resource.Loading -> Resource.loading()
    is Resource.Empty -> Resource.empty()
}
