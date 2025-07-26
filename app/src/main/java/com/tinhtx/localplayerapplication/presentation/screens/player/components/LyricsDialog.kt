package com.tinhtx.localplayerapplication.presentation.screens.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tinhtx.localplayerapplication.domain.model.Song

@Composable
fun LyricsDialog(
    song: Song,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lyrics = remember { 
        // Mock lyrics - in real app this would come from lyrics service
        generateMockLyrics(song.title, song.displayArtist)
    }
    
    val listState = rememberLazyListState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = song.displayArtist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close lyrics",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Lyrics content
                if (lyrics.isEmpty()) {
                    NoLyricsAvailable(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(lyrics) { line ->
                            LyricsLine(
                                text = line.text,
                                isCurrent = line.isCurrent,
                                timestamp = line.timestamp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsLine(
    text: String,
    isCurrent: Boolean,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = if (isCurrent) {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        } else {
            MaterialTheme.typography.bodyLarge
        },
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        },
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun NoLyricsAvailable(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No lyrics available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Lyrics for this song are not available at the moment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

private data class LyricsLine(
    val text: String,
    val timestamp: Long,
    val isCurrent: Boolean = false
)

private fun generateMockLyrics(title: String, artist: String): List<LyricsLine> {
    return listOf(
        LyricsLine("♪ $title ♪", 0L),
        LyricsLine("by $artist", 2000L),
        LyricsLine("", 4000L),
        LyricsLine("[Verse 1]", 6000L),
        LyricsLine("In the silence of the night", 8000L),
        LyricsLine("When the stars are shining bright", 12000L),
        LyricsLine("I can hear the music play", 16000L),
        LyricsLine("Taking all my fears away", 20000L),
        LyricsLine("", 24000L),
        LyricsLine("[Chorus]", 26000L),
        LyricsLine("This is where the magic happens", 28000L),
        LyricsLine("When the melody unfolds", 32000L),
        LyricsLine("Every note a new emotion", 36000L),
        LyricsLine("Every beat a story told", 40000L),
        LyricsLine("", 44000L),
        LyricsLine("[Verse 2]", 46000L),
        LyricsLine("Through the rhythm and the rhyme", 48000L),
        LyricsLine("We can travel back in time", 52000L),
        LyricsLine("To a place where dreams come true", 56000L),
        LyricsLine("Where the music carries you", 60000L)
    )
}
