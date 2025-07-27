package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.settings.SleepTimerAction

@Composable
fun SleepTimerSettings(
    isEnabled: Boolean,
    remainingTime: String,
    duration: Int,
    action: SleepTimerAction,
    fadeOutDuration: Int,
    onToggleTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onActionChange: (SleepTimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Sleep Timer",
            icon = Icons.Default.Timer
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isEnabled) {
            // Active sleep timer card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sleep Timer Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = "Time remaining: $remainingTime",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            
                            Text(
                                text = "Action: ${action.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        OutlinedButton(
                            onClick = onStopTimer,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop")
                        }
                    }
                }
            }
        } else {
            // Sleep timer controls
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Set sleep timer
                SettingItem(
                    title = "Set Sleep Timer",
                    subtitle = "Automatically pause playback after a set time",
                    icon = Icons.Default.Timer,
                    onClick = onToggleTimer
                )

                // Timer action
                SettingDropdownItem(
                    title = "Timer Action",
                    subtitle = "What to do when timer ends",
                    icon = Icons.Default.PlaylistPlay,
                    selectedOption = action.displayName,
                    options = SleepTimerAction.values().map { it.displayName },
                    onOptionSelected = { actionName ->
                        val selectedAction = SleepTimerAction.values().find { it.displayName == actionName }
                        selectedAction?.let { onActionChange(it) }
                    }
                )

                // Fade out duration
                SettingItem(
                    title = "Fade Out Duration",
                    subtitle = "${fadeOutDuration}s fade out before timer action",
                    icon = Icons.Default.VolumeDown,
                    enabled = false
                )
            }
        }
    }
}
