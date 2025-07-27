package com.tinhtx.localplayerapplication.presentation.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.settings.*

@Composable
fun PlaybackSettingsSection(
    audioQuality: AudioQuality,
    crossfadeDuration: Int,
    replayGainMode: ReplayGainMode,
    skipSilence: Boolean,
    resumeOnHeadphoneConnect: Boolean,
    pauseOnHeadphoneDisconnect: Boolean,
    ducking: Boolean,
    onAudioQualityChange: (AudioQuality) -> Unit,
    onCrossfadeDurationChange: (Int) -> Unit,
    onReplayGainModeChange: (ReplayGainMode) -> Unit,
    onToggleSkipSilence: () -> Unit,
    onToggleResumeOnHeadphoneConnect: () -> Unit,
    onTogglePauseOnHeadphoneDisconnect: () -> Unit,
    onToggleDucking: () -> Unit,
    onEqualizerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(
            title = "Playback",
            icon = Icons.Default.PlayArrow
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Audio quality
            SettingDropdownItem(
                title = "Audio Quality",
                subtitle = audioQuality.getAudioQualityDisplayName(),
                icon = Icons.Default.HighQuality,
                selectedOption = audioQuality.displayName,
                options = AudioQuality.values().map { it.displayName },
                onOptionSelected = { qualityName ->
                    val selectedQuality = AudioQuality.values().find { it.displayName == qualityName }
                    selectedQuality?.let { onAudioQualityChange(it) }
                }
            )

            // Crossfade duration
            SettingSliderItem(
                title = "Crossfade",
                subtitle = "Smooth transition between songs",
                icon = Icons.Default.BlendingMode,
                value = crossfadeDuration.toFloat(),
                valueRange = 0f..10f,
                steps = 9,
                onValueChange = { onCrossfadeDurationChange(it.toInt()) },
                valueFormatter = { "${it.toInt()}s" }
            )

            // Replay gain
            SettingDropdownItem(
                title = "Replay Gain",
                subtitle = "Normalize volume levels",
                icon = Icons.Default.VolumeUp,
                selectedOption = replayGainMode.displayName,
                options = ReplayGainMode.values().map { it.displayName },
                onOptionSelected = { modeName ->
                    val selectedMode = ReplayGainMode.values().find { it.displayName == modeName }
                    selectedMode?.let { onReplayGainModeChange(it) }
                }
            )

            // Equalizer
            SettingItem(
                title = "Equalizer",
                subtitle = "Adjust sound frequencies",
                icon = Icons.Default.Equalizer,
                onClick = onEqualizerClick
            )

            // Skip silence
            SettingSwitchItem(
                title = "Skip Silence",
                subtitle = "Automatically skip silent parts of songs",
                icon = Icons.Default.FastForward,
                checked = skipSilence,
                onCheckedChange = { onToggleSkipSilence() }
            )

            // Headphone controls
            SettingSwitchItem(
                title = "Resume on Headphone Connect",
                subtitle = "Auto-resume playback when headphones are connected",
                icon = Icons.Default.Headphones,
                checked = resumeOnHeadphoneConnect,
                onCheckedChange = { onToggleResumeOnHeadphoneConnect() }
            )

            SettingSwitchItem(
                title = "Pause on Headphone Disconnect",
                subtitle = "Auto-pause when headphones are disconnected",
                icon = Icons.Default.HeadsetOff,
                checked = pauseOnHeadphoneDisconnect,
                onCheckedChange = { onTogglePauseOnHeadphoneDisconnect() }
            )

            // Audio ducking
            SettingSwitchItem(
                title = "Audio Ducking",
                subtitle = "Lower volume during notifications",
                icon = Icons.Default.VolumeDown,
                checked = ducking,
                onCheckedChange = { onToggleDucking() }
            )
        }
    }
}

private fun AudioQuality.getAudioQualityDisplayName(): String = when (this) {
    AudioQuality.LOW -> "Low (128 kbps)"
    AudioQuality.MEDIUM -> "Medium (256 kbps)"
    AudioQuality.HIGH -> "High (320 kbps)"
    AudioQuality.LOSSLESS -> "Lossless"
}
