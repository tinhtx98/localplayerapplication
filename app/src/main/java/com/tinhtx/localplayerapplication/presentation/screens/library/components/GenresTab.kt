package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenresTab(
    genres: List<String>,
    onGenreClick: (String) -> Unit,
    onPlayGenre: (String) -> Unit,
    getSongCountForGenre: (String) -> Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = genres,
            key = { genre -> genre }
        ) { genre ->
            GenreListItem(
                genre = genre,
                songCount = getSongCountForGenre(genre),
                onClick = { onGenreClick(genre) },
                onPlayClick = { onPlayGenre(genre) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
private fun GenreListItem(
    genre: String,
    songCount: Int,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Genre icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = getGenreColors(genre)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getGenreIcon(genre),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Genre info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$songCount songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Genre description
                Text(
                    text = getGenreDescription(genre),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Song count badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = songCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Play button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play genre",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getGenreIcon(genre: String): ImageVector {
    return when (genre.lowercase()) {
        "rock", "hard rock", "metal", "heavy metal" -> Icons.Default.MusicNote
        "pop", "dance", "electronic", "edm" -> Icons.Default.Equalizer
        "jazz", "blues", "soul" -> Icons.Default.Piano
        "classical", "orchestral", "symphony" -> Icons.Default.MusicNote
        "country", "folk", "acoustic" -> Icons.Default.MusicNote
        "rap", "hip hop", "hip-hop" -> Icons.Default.GraphicEq
        "reggae", "ska" -> Icons.Default.MusicNote
        "latin", "salsa", "bachata" -> Icons.Default.MusicNote
        "r&b", "rnb", "funk" -> Icons.Default.MusicNote
        "alternative", "indie", "grunge" -> Icons.Default.MusicNote
        "world", "ethnic", "traditional" -> Icons.Default.Public
        "soundtrack", "score", "ost" -> Icons.Default.Movie
        "ambient", "new age", "meditation" -> Icons.Default.Spa
        "punk", "hardcore" -> Icons.Default.MusicNote
        "house", "techno", "trance" -> Icons.Default.Equalizer
        else -> Icons.Default.Category
    }
}

private fun getGenreColors(genre: String): List<Color> {
    return when (genre.lowercase()) {
        "rock", "hard rock", "metal", "heavy metal" -> listOf(Color(0xFF8B0000), Color(0xFF FF6347))
        "pop", "dance", "electronic", "edm" -> listOf(Color(0xFF FF69B4), Color(0xFF FF1493))
        "jazz", "blues", "soul" -> listOf(Color(0xFF 4B0082), Color(0xFF 9370DB))
        "classical", "orchestral", "symphony" -> listOf(Color(0xFF 2F4F4F), Color(0xFF 708090))
        "country", "folk", "acoustic" -> listOf(Color(0xFF DEB887), Color(0xFF D2691E))
        "rap", "hip hop", "hip-hop" -> listOf(Color(0xFF 2F2F2F), Color(0xFF 696969))
        "reggae", "ska" -> listOf(Color(0xFF 228B22), Color(0xFF 32CD32))
        "latin", "salsa", "bachata" -> listOf(Color(0xFF FF4500), Color(0xFF FF6347))
        "r&b", "rnb", "funk" -> listOf(Color(0xFF 8B008B), Color(0xFF DA70D6))
        "alternative", "indie", "grunge" -> listOf(Color(0xFF 556B2F), Color(0xFF 9ACD32))
        "world", "ethnic", "traditional" -> listOf(Color(0xFF CD853F), Color(0xFF F4A460))
        "soundtrack", "score", "ost" -> listOf(Color(0xFF 4682B4), Color(0xFF 87CEEB))
        "ambient", "new age", "meditation" -> listOf(Color(0xFF 00CED1), Color(0xFF 40E0D0))
        "punk", "hardcore" -> listOf(Color(0xFF DC143C), Color(0xFF FF6347))
        "house", "techno", "trance" -> listOf(Color(0xFF 00BFFF), Color(0xFF 1E90FF))
        else -> listOf(Color(0xFF 6A5ACD), Color(0xFF 9370DB))
    }
}

private fun getGenreDescription(genre: String): String {
    return when (genre.lowercase()) {
        "rock" -> "Energetic guitar-driven music"
        "pop" -> "Popular mainstream music"
        "jazz" -> "Improvised musical expression"
        "classical" -> "Orchestral and chamber music"
        "hip hop", "hip-hop", "rap" -> "Rhythmic spoken lyrics"
        "electronic", "edm" -> "Electronic dance music"
        "country" -> "American folk and western"
        "r&b", "rnb" -> "Rhythm and blues"
        "reggae" -> "Jamaican musical style"
        "folk" -> "Traditional acoustic music"
        "blues" -> "Soulful expression music"
        "metal" -> "Heavy guitar and drums"
        "punk" -> "Fast and raw rock music"
        "alternative" -> "Non-mainstream rock"
        "indie" -> "Independent music"
        "world" -> "International music"
        "ambient" -> "Atmospheric soundscapes"
        "soundtrack" -> "Film and game music"
        else -> "Musical genre"
    }
}
