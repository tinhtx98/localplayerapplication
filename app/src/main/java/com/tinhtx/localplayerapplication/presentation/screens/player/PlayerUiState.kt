package com.tinhtx.localplayerapplication.presentation.screens.player

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentTimeString: String = "0:00",
    val totalTimeString: String = "0:00",
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val equalizerPreset: EqualizerPreset = EqualizerPreset.NORMAL,
    val sleepTimer: Int = 0, // minutes
    val audioData: FloatArray = floatArrayOf(),
    val showVisualizer: Boolean = true,
    val error: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerUiState

        if (currentSong != other.currentSong) return false
        if (isPlaying != other.isPlaying) return false
        if (progress != other.progress) return false
        if (currentTimeString != other.currentTimeString) return false
        if (totalTimeString != other.totalTimeString) return false
        if (shuffleMode != other.shuffleMode) return false
        if (repeatMode != other.repeatMode) return false
        if (volume != other.volume) return false
        if (isMuted != other.isMuted) return false
        if (hasNext != other.hasNext) return false
        if (hasPrevious != other.hasPrevious) return false
        if (playbackSpeed != other.playbackSpeed) return false
        if (equalizerPreset != other.equalizerPreset) return false
        if (sleepTimer != other.sleepTimer) return false
        if (!audioData.contentEquals(other.audioData)) return false
        if (showVisualizer != other.showVisualizer) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentSong?.hashCode() ?: 0
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + progress.hashCode()
        result = 31 * result + currentTimeString.hashCode()
        result = 31 * result + totalTimeString.hashCode()
        result = 31 * result + shuffleMode.hashCode()
        result = 31 * result + repeatMode.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + isMuted.hashCode()
        result = 31 * result + hasNext.hashCode()
        result = 31 * result + hasPrevious.hashCode()
        result = 31 * result + playbackSpeed.hashCode()
        result = 31 * result + equalizerPreset.hashCode()
        result = 31 * result + sleepTimer
        result = 31 * result + audioData.contentHashCode()
        result = 31 * result + showVisualizer.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}