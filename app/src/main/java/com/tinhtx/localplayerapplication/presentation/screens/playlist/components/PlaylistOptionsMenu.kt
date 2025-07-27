package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.tinhtx.localplayerapplication.domain.model.Playlist

@Composable
fun PlaylistOptionsMenu(
    playlist: Playlist?,
    canEdit: Boolean,
    canDelete: Boolean,
    onEditPlaylist: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onSharePlaylist: () -> Unit,
    onExportPlaylist: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier,
        properties = PopupProperties(focusable = true)
    ) {
        // Edit playlist
        if (canEdit) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Edit playlist")
                    }
                },
                onClick = onEditPlaylist
            )
        }

        // Share playlist
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Share playlist")
                }
            },
            onClick = onSharePlaylist
        )

        // Export playlist
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Export playlist")
                }
            },
            onClick = onExportPlaylist
        )

        // Add to favorites (if not system playlist)
        if (playlist?.isSystem != true) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Add to favorites")
                    }
                },
                onClick = {
                    // TODO: Add to favorites functionality
                    onDismiss()
                }
            )
        }

        // Duplicate playlist
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Duplicate playlist")
                }
            },
            onClick = {
                // TODO: Duplicate playlist functionality
                onDismiss()
            }
        )

        // View playlist info
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Playlist info")
                }
            },
            onClick = {
                // TODO: Show playlist info dialog
                onDismiss()
            }
        )

        // Divider before destructive actions
        if (canDelete) {
            Divider()

            // Delete playlist
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Delete playlist",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                onClick = onDeletePlaylist
            )
        }
    }
}
