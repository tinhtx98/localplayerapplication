package com.tinhtx.localplayerapplication.presentation.screens.album.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tinhtx.localplayerapplication.domain.model.SortOrder

@Composable
fun AlbumSortDialog(
    currentSortOrder: SortOrder,
    currentAscending: Boolean,
    onDismiss: () -> Unit,
    onApply: (SortOrder, Boolean) -> Unit
) {
    var selectedSortOrder by remember { mutableStateOf(currentSortOrder) }
    var selectedAscending by remember { mutableStateOf(currentAscending) }

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
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Sort Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sort options
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(getSortOptions()) { sortOption ->
                        SortOptionItem(
                            sortOption = sortOption,
                            isSelected = selectedSortOrder == sortOption.sortOrder,
                            onClick = { selectedSortOrder = sortOption.sortOrder }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort direction
                Text(
                    text = "Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ascending option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAscending,
                            onClick = { selectedAscending = true }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAscending,
                        onClick = { selectedAscending = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ascending (A-Z, 1-9)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Descending option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = !selectedAscending,
                            onClick = { selectedAscending = false }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !selectedAscending,
                        onClick = { selectedAscending = false }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descending (Z-A, 9-1)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onApply(selectedSortOrder, selectedAscending)
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SortOptionItem(
    sortOption: SortOptionData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = sortOption.icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = sortOption.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (sortOption.description.isNotEmpty()) {
                Text(
                    text = sortOption.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class SortOptionData(
    val sortOrder: SortOrder,
    val title: String,
    val description: String,
    val icon: ImageVector
)

private fun getSortOptions(): List<SortOptionData> {
    return listOf(
        SortOptionData(
            sortOrder = SortOrder.TRACK_NUMBER,
            title = "Track Number",
            description = "Original album order",
            icon = Icons.Default.Numbers
        ),
        SortOptionData(
            sortOrder = SortOrder.TITLE,
            title = "Title",
            description = "Song title alphabetically",
            icon = Icons.Default.SortByAlpha
        ),
        SortOptionData(
            sortOrder = SortOrder.DURATION,
            title = "Duration",
            description = "Song length",
            icon = Icons.Default.Schedule
        ),
        SortOptionData(
            sortOrder = SortOrder.PLAY_COUNT,
            title = "Play Count",
            description = "Most played first",
            icon = Icons.Default.PlayArrow
        ),
        SortOptionData(
            sortOrder = SortOrder.DATE_ADDED,
            title = "Date Added",
            description = "When added to library",
            icon = Icons.Default.CalendarToday
        ),
        SortOptionData(
            sortOrder = SortOrder.LAST_PLAYED,
            title = "Last Played",
            description = "Recently played first",
            icon = Icons.Default.History
        )
    )
}
