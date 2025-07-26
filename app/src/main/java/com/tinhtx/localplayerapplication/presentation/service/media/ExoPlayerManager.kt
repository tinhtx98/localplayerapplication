package com.tinhtx.localplayerapplication.presentation.service.media

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.presentation.service.PlayerState
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerManager @Inject constructor(
    private val context: Context
) {
    
    private var exoPlayer: ExoPlayer? = null
    private val dataSourceFactory = DefaultDataSource.Factory(context)
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState {
                copy(
                    isPlaying = exoPlayer?.isPlaying == true,
                    bufferingPercentage = when (playbackState) {
                        Player.STATE_BUFFERING -> 0
                        Player.STATE_READY -> 100
                        else -> bufferingPercentage
                    }
                )
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState { copy(isPlaying = isPlaying) }
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlayerState {
                copy(currentPosition = newPosition.positionMs)
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            val playerError = PlayerError(
                type = mapExoPlayerError(error),
                message = error.message ?: "Playback error occurred",
                cause = error.cause
            )
            updatePlayerState { copy(error = playerError) }
        }
        
        override fun onVolumeChanged(volume: Float) {
            updatePlayerState { copy(volume = volume) }
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            val mode = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF
                Player.REPEAT_MODE_ONE -> com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL
                else -> com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF
            }
            updatePlayerState { copy(repeatMode = mode) }
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val mode = if (shuffleModeEnabled) {
                com.tinhtx.localplayerapplication.domain.model.ShuffleMode.ON
            } else {
                com.tinhtx.localplayerapplication.domain.model.ShuffleMode.OFF
            }
            updatePlayerState { copy(shuffleMode = mode) }
        }
    }
    
    fun initialize() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    addListener(playerListener)
                }
        }
    }
    
    fun prepareAndPlay(song: Song) {
        val mediaSource = createMediaSource(song)
        exoPlayer?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
        
        updatePlayerState {
            copy(
                currentSong = song,
                duration = song.duration,
                currentPosition = 0L
            )
        }
    }
    
    fun play() {
        exoPlayer?.playWhenReady = true
    }
    
    fun pause() {
        exoPlayer?.playWhenReady = false
    }
    
    fun stop() {
        exoPlayer?.stop()
        updatePlayerState { PlayerState() }
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        updatePlayerState { copy(currentPosition = position) }
    }
    
    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        updatePlayerState { copy(playbackSpeed = speed) }
    }
    
    fun setRepeatMode(repeatMode: com.tinhtx.localplayerapplication.domain.model.RepeatMode) {
        val exoRepeatMode = when (repeatMode) {
            com.tinhtx.localplayerapplication.domain.model.RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            com.tinhtx.localplayerapplication.domain.model.RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            com.tinhtx.localplayerapplication.domain.model.RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        exoPlayer?.repeatMode = exoRepeatMode
    }
    
    fun setShuffleMode(shuffleMode: com.tinhtx.localplayerapplication.domain.model.ShuffleMode) {
        exoPlayer?.shuffleModeEnabled = shuffleMode == com.tinhtx.localplayerapplication.domain.model.ShuffleMode.ON
    }
    
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    
    fun getDuration(): Long = exoPlayer?.duration ?: 0L
    
    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true
    
    fun getAudioSessionId(): Int = exoPlayer?.audioSessionId ?: 0
    
    private fun createMediaSource(song: Song): MediaSource {
        val uri = Uri.parse(song.path)
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }
    
    private fun updatePlayerState(update: PlayerState.() -> PlayerState) {
        _playerState.value = _playerState.value.update()
    }
    
    private fun mapExoPlayerError(error: PlaybackException): PlayerErrorType {
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> PlayerErrorType.NETWORK_ERROR
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> PlayerErrorType.FILE_NOT_FOUND
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> PlayerErrorType.UNSUPPORTED_FORMAT
            else -> PlayerErrorType.PLAYBACK_ERROR
        }
    }
    
    fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }
}
