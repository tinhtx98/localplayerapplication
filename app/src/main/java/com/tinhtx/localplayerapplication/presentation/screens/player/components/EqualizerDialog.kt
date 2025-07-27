package com.tinhtx.localplayerapplication.presentation.screens.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun EqualizerDialog(
    enabled: Boolean,
    preset: String,
    bands: List<Float>,
    onDismiss: () -> Unit,
    onApply: (Boolean, String, List<Float>) -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var selectedPreset by remember { mutableStateOf(preset) }
    var bandValues by remember { mutableStateOf(bands) }
    
    val presets = listOf("Normal", "Classical", "Dance", "Bass", "Loud", "Treble", "Party", "Pop", "Rock", "Custom")
    val frequencies = listOf("60", "170", "310", "600", "1K", "3K", "6K", "12K", "14K", "16K")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enable switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Equalizer",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preset selection
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { presetName ->
                        FilterChip(
                            onClick = { selectedPreset = presetName },
                            label = { Text(presetName) },
                            selected = selectedPreset == presetName
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Equalizer bands
                Text(
                    text = "Frequency Bands",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    frequencies.forEachIndexed { index, freq ->
                        if (index < bandValues.size) {
                            EqualizerBand(
                                frequency = freq,
                                value = bandValues[index],
                                onValueChange = { newValue ->
                                    bandValues = bandValues.toMutableList().apply {
                                        set(index, newValue)
                                    }
                                },
                                enabled = isEnabled
                            )
                        }
                    }
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
                            onApply(isEnabled, selectedPreset, bandValues)
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
private fun EqualizerBand(
    frequency: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "+15",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -15f..15f,
            enabled = enabled,
            modifier = Modifier
                .height(120.dp)
                .width(40.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Text(
            text = "-15",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = frequency,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
