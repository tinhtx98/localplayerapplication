package com.tinhtx.localplayerapplication.presentation.components.image

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.tinhtx.localplayerapplication.presentation.components.common.LoadingIndicator

@Composable
fun SmartAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader? = null,
    shape: Shape? = null,
    contentScale: ContentScale = ContentScale.Crop,
    showLoading: Boolean = true,
    showError: Boolean = true,
    placeholder: @Composable (BoxScope.() -> Unit)? = null,
    error: @Composable (BoxScope.() -> Unit)? = null,
    onLoading: @Composable (BoxScope.() -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null
) {
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            imageLoader = imageLoader,
            modifier = if (shape != null) {
                Modifier
                    .fillMaxSize()
                    .clip(shape)
            } else {
                Modifier.fillMaxSize()
            },
            contentScale = contentScale,
            onState = { state ->
                imageState = state
                when (state) {
                    is AsyncImagePainter.State.Success -> onSuccess?.invoke(state)
                    is AsyncImagePainter.State.Error -> onError?.invoke(state)
                    else -> {}
                }
            }
        )
        
        // Loading state
        if (showLoading && imageState is AsyncImagePainter.State.Loading) {
            if (onLoading != null) {
                onLoading()
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(size = 32.dp)
                }
            }
        }
        
        // Placeholder state
        if (imageState is AsyncImagePainter.State.Empty && placeholder != null) {
            placeholder()
        }
        
        // Error state
        if (showError && imageState is AsyncImagePainter.State.Error) {
            if (error != null) {
                error()
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PreloadedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    preloadSize: androidx.compose.ui.geometry.Size = androidx.compose.ui.geometry.Size.UNSPECIFIED,
    contentScale: ContentScale = ContentScale.Crop,
    onImageReady: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isPreloaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(model) {
        if (model != null) {
            val request = ImageRequest.Builder(context)
                .data(model)
                .size(preloadSize)
                .listener(
                    onSuccess = { _, _ -> 
                        isPreloaded = true
                        onImageReady?.invoke()
                    }
                )
                .build()
            
            context.imageLoader.execute(request)
        }
    }
    
    if (isPreloaded || model == null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(size = 32.dp)
        }
    }
}
