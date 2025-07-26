package com.tinhtx.localplayerapplication.presentation.screens.album.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.Album

@Composable
fun AlbumInfoCard(
    album: Album,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Album Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Album details
            AlbumInfoRow(
                icon = Icons.Default.Album,
                label = "Album",
                value = album.displayName
            )
            
            AlbumInfoRow(
                icon = Icons.Default.Person,
                label = "Artist",
                value = album.displayArtist
            )
            
            if (album.year > 0) {
                AlbumInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Year",
                    value = album.year.toString()
                )
            }
            
            AlbumInfoRow(
                icon = Icons.Default.MusicNote,
                label = "Tracks",
                value = "${album.songCount} ${if (album.songCount == 1) "song" else "songs"}"
            )
            
            // Additional metadata if available
            if (album.genre?.isNotBlank() == true) {
                AlbumInfoRow(
                    icon = Icons.Default.Category,
                    label = "Genre",
                    value = album.genre!!
                )
            }
        }
    }
}

@Composable
private fun AlbumInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
