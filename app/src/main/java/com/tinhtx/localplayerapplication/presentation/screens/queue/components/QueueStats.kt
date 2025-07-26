package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun QueueStatsCard(
    totalSongs: Int,
    totalDuration: String,
    remainingSongs: Int,
    remainingDuration: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Queue Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QueueStatItem(
                    icon = Icons.Default.QueueMusic,
                    value = totalSongs.toString(),
                    label = "Total Songs",
                    color = MaterialTheme.colorScheme.primary
                )
                
                QueueStatItem(
                    icon = Icons.Default.Schedule,
                    value = totalDuration,
                    label = "Total Time",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QueueStatItem(
                    icon = Icons.Default.PlaylistPlay,
                    value = remainingSongs.toString(),
                    label = "Remaining",
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                QueueStatItem(
                    icon = Icons.Default.Timer,
                    value = remainingDuration,
                    label = "Time Left",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun QueueStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp),
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
