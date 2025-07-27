package com.tinhtx.localplayerapplication.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tinhtx.localplayerapplication.presentation.screens.library.LibraryTab

@Composable
fun EmptyLibraryTabState(
    selectedTab: LibraryTab,
    onScanLibrary: () -> Unit,
    onCreatePlaylist: (() -> Unit)? = null,
    onImportMusic: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val emptyStateInfo = getEmptyStateInfo(selectedTab)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state icon
        Icon(
            imageVector = emptyStateInfo.icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = emptyStateInfo.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Description
        Text(
            text = emptyStateInfo.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        when (selectedTab) {
            LibraryTab.SONGS, LibraryTab.ALBUMS, LibraryTab.ARTISTS, LibraryTab.GENRES -> {
                // Music-related tabs
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScanLibrary,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan for Music")
                    }
                    
                    if (onImportMusic != null) {
                        OutlinedButton(
                            onClick = onImportMusic,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Music")
                        }
                    }
                }
            }
            
            LibraryTab.PLAYLISTS -> {
                // Playlists tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onCreatePlaylist != null) {
                        Button(
                            onClick = onCreatePlaylist,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Your First Playlist")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onScanLibrary,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan for Music First")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Additional tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                emptyStateInfo.tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class EmptyStateInfo(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tips: List<String>
)

private fun getEmptyStateInfo(tab: LibraryTab): EmptyStateInfo {
    return when (tab) {
        LibraryTab.SONGS -> EmptyStateInfo(
            icon = Icons.Default.MusicNote,
            title = "No Songs Found",
            description = "Your music library is empty. Start by scanning your device for music files or importing your favorite tracks.",
            tips = listOf(
                "Make sure music files are stored in common folders like Music, Downloads",
                "Supported formats: MP3, AAC, FLAC, WAV, OGG",
                "Check that the app has storage permissions"
            )
        )
        
        LibraryTab.ALBUMS -> EmptyStateInfo(
            icon = Icons.Default.Album,
            title = "No Albums Found",
            description = "No albums are available in your library. Albums are automatically organized from your music collection.",
            tips = listOf(
                "Add music files with proper album metadata",
                "Scan your device to find existing music",
                "Albums are grouped by album name and artist"
            )
        )
        
        LibraryTab.ARTISTS -> EmptyStateInfo(
            icon = Icons.Default.Person,
            title = "No Artists Found",
            description = "No artists are available in your library. Artists are automatically organized from your music collection.",
            tips = listOf(
                "Add music files with proper artist metadata",
                "Scan your device to find existing music",
                "Artists are grouped by performer information"
            )
        )
        
        LibraryTab.PLAYLISTS -> EmptyStateInfo(
            icon = Icons.Default.PlaylistPlay,
            title = "No Playlists Yet",
            description = "You haven't created any playlists yet. Create custom playlists to organize your favorite songs.",
            tips = listOf(
                "Create playlists for different moods or occasions",
                "Add songs from your library to playlists",
                "Playlists can be public or private"
            )
        )
        
        LibraryTab.GENRES -> EmptyStateInfo(
            icon = Icons.Default.Category,
            title = "No Genres Found",
            description = "No music genres are available. Genres are automatically detected from your music metadata.",
            tips = listOf(
                "Add music files with genre information",
                "Genres help organize music by style",
                "Popular genres include Rock, Pop, Jazz, Classical"
            )
        )
    }
}
