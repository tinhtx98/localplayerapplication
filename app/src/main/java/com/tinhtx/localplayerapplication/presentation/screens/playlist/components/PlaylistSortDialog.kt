package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tinhtx.localplayerapplication.core.constants.AppConstants

@Composable
fun PlaylistSortDialog(
    currentSortOrder: AppConstants.SortOrder,
    onSortOrderChange: (AppConstants.SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Sort playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val sortOptions = listOf(
                    AppConstants.SortOrder.CUSTOM to "Custom order",
                    AppConstants.SortOrder.TITLE_ASC to "Song title (A-Z)",
                    AppConstants.SortOrder.TITLE_DESC to "Song title (Z-A)",
                    AppConstants.SortOrder.ARTIST_ASC to "Artist (A-Z)",
                    AppConstants.SortOrder.ARTIST_DESC to "Artist (Z-A)",
                    AppConstants.SortOrder.ALBUM_ASC to "Album (A-Z)",
                    AppConstants.SortOrder.ALBUM_DESC to "Album (Z-A)",
                    AppConstants.SortOrder.DATE_ADDED_DESC to "Recently added",
                    AppConstants.SortOrder.DATE_ADDED_ASC to "Oldest first",
                    AppConstants.SortOrder.DURATION_ASC to "Duration (shortest)",
                    AppConstants.SortOrder.DURATION_DESC to "Duration (longest)"
                )

                sortOptions.forEach { (sortOrder, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortOrderChange(sortOrder) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOrder == sortOrder,
                            onClick = { onSortOrderChange(sortOrder) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done")
                }
            }
        }
    }
}
