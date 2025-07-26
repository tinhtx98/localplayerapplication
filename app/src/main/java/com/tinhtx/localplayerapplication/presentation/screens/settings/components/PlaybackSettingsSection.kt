package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.domain.model.*

@Composable
fun PlaybackSettingsSection(
    settings: PlaybackSettings,
    onSettingChanged: (PlaybackSettingType, Any) -> Unit,
    onShowEqualizerDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gapless Playback
            SettingSwitchItem(
                title = "Gapless Playback",
                subtitle = "Seamless playback between tracks",
                icon = Icons.Default.SkipNext,
                checked = settings.gaplessPlayback,
                onCheckedChange = { 
                    onSettingChanged(PlaybackSettingType.GAPLESS_PLAYBACK, it)
                }
            )
            
            HorizontalDivider()
            
            // Crossfade
            SettingSwitchItem(
                title = "Crossfade",
                subtitle = "Smooth transition between songs",
                icon = Icons.Default.BlendingMode,
                checked = settings.crossfadeEnabled,
                onCheckedChange = { 
                    onSettingChanged(PlaybackSettingType.CROSSFADE, it)
                }
            )
            
            // Crossfade Duration (only show if crossfade is enabled)
            if (settings.crossfadeEnabled) {
                SettingSliderItem(
                    title = "Crossfade Duration",
                    subtitle = "${settings.crossfadeDuration} seconds",
                    icon = Icons.Default.Timer,
                    value = settings.crossfadeDuration.toFloat(),
                    valueRange = 1f..15f,
                    steps = 13,
                    onValueChange = { 
                        onSettingChanged(PlaybackSettingType.CROSSFADE_DURATION, it.toInt())
                    }
                )
            }
            
            HorizontalDivider()
            
            // Audio Focus
            SettingSwitchItem(
                title = "Audio Focus",
                subtitle = "Pause when other apps play audio",
                icon = Icons.Default.VolumeUp,
                checked = settings.audioFocus,
                onCheckedChange = { 
                    onSettingChanged(PlaybackSettingType.AUDIO_FOCUS, it)
                }
            )
            
            HorizontalDivider()
            
            // Equalizer
            SettingClickableItem(
                title = "Equalizer",
                subtitle = "Adjust audio frequencies",
                icon = Icons.Default.Equalizer,
                value = settings.equalizerSettings.preset.displayName,
                onClick = onShowEqualizerDialog
            )
            
            HorizontalDivider()
            
            // Replay Gain
            SettingSwitchItem(
                title = "Replay Gain",
                subtitle = "Normalize volume across tracks",
                icon = Icons.Default.VolumeDown,
                checked = settings.replayGain,
                onCheckedChange = { 
                    onSettingChanged(PlaybackSettingType.REPLAY_GAIN, it)
                }
            )
            
            HorizontalDivider()
            
            // Audio Effects
            SettingSwitchItem(
                title = "Audio Effects",
                subtitle = "Enable system audio effects",
                icon = Icons.Default.GraphicEq,
                checked = settings.audioEffects,
                onCheckedChange = { 
                    onSettingChanged(PlaybackSettingType.AUDIO_EFFECTS, it)
                }
            )
        }
    }
}
