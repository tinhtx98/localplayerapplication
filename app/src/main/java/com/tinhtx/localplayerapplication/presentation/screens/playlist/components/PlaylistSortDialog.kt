package com.tinhtx.localplayerapplication.presentation.screens.playlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.SortOrder

@Composable
fun PlaylistSortDialog(
    currentSortOrder: SortOrder,
    currentAscending: Boolean,
    canUseCustomOrder: Boolean,
    onDismiss: () -> Unit,
    onApply: (SortOrder, Boolean) -> Unit
) {
    var selectedSortOrder by remember { mutableStateOf(currentSortOrder) }
    var selectedAscending by remember { mutableStateOf(currentAscending) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "Sort songs",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sort options
                val sortOptions = buildList {
                    if (canUseCustomOrder) {
                        add(SortOrder.CUSTOM to "Custom order")
                    }
                    add(SortOrder.TITLE to "Title")
                    add(SortOrder.ARTIST to "Artist")
                    add(SortOrder.ALBUM to "Album")
                    add(SortOrder.DURATION to "Duration")
                    add(SortOrder.DATE_ADDED to "Date added")
                    add(SortOrder.PLAY_COUNT to "Play count")
                }

                sortOptions.forEach { (sortOrder, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSortOrder == sortOrder,
                                onClick = { selectedSortOrder = sortOrder },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortOrder == sortOrder,
                            onClick = null // onClick is handled by parent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Sort direction (only for non-custom orders)
                if (selectedSortOrder != SortOrder.CUSTOM) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sort direction",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedAscending,
                                onClick = { selectedAscending = true },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAscending,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ascending (A to Z)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = !selectedAscending,
                                onClick = { selectedAscending = false },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !selectedAscending,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Descending (Z to A)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(selectedSortOrder, selectedAscending)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
