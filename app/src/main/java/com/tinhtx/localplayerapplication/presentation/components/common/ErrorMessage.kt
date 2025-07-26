package com.tinhtx.localplayerapplication.presentation.components.common

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Thử lại",
    icon: ImageVector = Icons.Default.Error,
    backgroundColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = MaterialTheme.colorScheme.onErrorContainer
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = contentColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = contentColor
            )
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = retryText)
                }
            }
        }
    }
}

@Composable
fun MusicErrorMessage(
    title: String = "Có lỗi xảy ra",
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    errorType: MusicErrorType = MusicErrorType.GENERAL
) {
    val (icon, backgroundColor, contentColor) = when (errorType) {
        MusicErrorType.NO_PERMISSION -> Triple(
            Icons.Default.Block,
            MaterialTheme.colorScheme.warningContainer,
            MaterialTheme.colorScheme.onWarningContainer
        )
        MusicErrorType.NO_MUSIC_FOUND -> Triple(
            Icons.Default.MusicOff,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        MusicErrorType.PLAYBACK_ERROR -> Triple(
            Icons.Default.PlayArrow,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        MusicErrorType.NETWORK_ERROR -> Triple(
            Icons.Default.WifiOff,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        MusicErrorType.GENERAL -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = contentColor.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = contentColor,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        contentColor.copy(alpha = 0.5f)
                    ).brush
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

enum class MusicErrorType {
    NO_PERMISSION,
    NO_MUSIC_FOUND,
    PLAYBACK_ERROR,
    NETWORK_ERROR,
    GENERAL
}

@Composable
fun InlineErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Color extensions for warning
val ColorScheme.warningContainer: Color
    @Composable get() = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color(0xFF4A4300)
    } else {
        Color(0xFFFFE08C)
    }

val ColorScheme.onWarningContainer: Color
    @Composable get() = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color(0xFFFFE08C)
    } else {
        Color(0xFF4A4300)
    }
