package com.tinhtx.localplayerapplication.presentation.screens.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tinhtx.localplayerapplication.domain.model.EqualizerPreset

@Composable
fun EqualizerDialog(
    currentPreset: EqualizerPreset,
    onPresetChange: (EqualizerPreset) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember {
        listOf(
            EqualizerPreset.NORMAL,
            EqualizerPreset.ROCK,
            EqualizerPreset.POP,
            EqualizerPreset.JAZZ,
            EqualizerPreset.CLASSICAL,
            EqualizerPreset.BASS_BOOST,
            EqualizerPreset.VOCAL_BOOST
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close equalizer"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Equalizer visualization
                EqualizerVisualization(
                    preset = currentPreset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Preset buttons
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { preset ->
                        FilterChip(
                            selected = preset == currentPreset,
                            onClick = { onPresetChange(preset) },
                            label = {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Apply button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun EqualizerVisualization(
    preset: EqualizerPreset,
    modifier: Modifier = Modifier
) {
    val bands = remember(preset) { getEqualizerBands(preset) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bands.forEachIndexed { index, level ->
            EqualizerBand(
                frequency = getFrequencyLabel(index),
                level = level,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EqualizerBand(
    frequency: String,
    level: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Level indicator
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(80.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
            )
            
            // Level bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(level.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = frequency,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun getEqualizerBands(preset: EqualizerPreset): List<Float> {
    return when (preset) {
        EqualizerPreset.NORMAL -> listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
        EqualizerPreset.ROCK -> listOf(0.7f, 0.6f, 0.4f, 0.3f, 0.4f, 0.6f, 0.7f, 0.8f)
        EqualizerPreset.POP -> listOf(0.4f, 0.6f, 0.7f, 0.6f, 0.5f, 0.4f, 0.5f, 0.6f)
        EqualizerPreset.JAZZ -> listOf(0.6f, 0.5f, 0.4f, 0.6f, 0.7f, 0.6f, 0.5f, 0.4f)
        EqualizerPreset.CLASSICAL -> listOf(0.7f, 0.6f, 0.5f, 0.6f, 0.7f, 0.8f, 0.7f, 0.6f)
        EqualizerPreset.BASS_BOOST -> listOf(0.9f, 0.8f, 0.6f, 0.4f, 0.3f, 0.4f, 0.5f, 0.6f)
        EqualizerPreset.VOCAL_BOOST -> listOf(0.3f, 0.4f, 0.6f, 0.8f, 0.7f, 0.6f, 0.4f, 0.3f)
    }
}

private fun getFrequencyLabel(index: Int): String {
    val frequencies = listOf("60", "170", "310", "600", "1K", "3K", "6K", "12K")
    return frequencies.getOrElse(index) { "${index * 1000}" }
}
