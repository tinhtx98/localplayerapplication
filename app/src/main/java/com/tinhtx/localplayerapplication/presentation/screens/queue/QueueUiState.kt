package com.tinhtx.localplayerapplication.presentation.screens.queue

data class QueueUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val queue: List<Song> = emptyList(),
    val filteredQueue: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val currentSongIndex: Int = -1,
    val isPlaying: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val searchQuery: String = "",
    val totalDuration: String = ""
)