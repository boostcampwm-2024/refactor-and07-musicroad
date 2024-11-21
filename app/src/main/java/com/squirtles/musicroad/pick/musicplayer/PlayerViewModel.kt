package com.squirtles.musicroad.pick.musicplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val isReady: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 30_000L,
)

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private var player: ExoPlayer? = null

    private val _playerState = MutableStateFlow(PlayerState(isReady = false))
    val playerState: StateFlow<PlayerState> = _playerState

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage: StateFlow<Int> = _bufferPercentage

    fun initializePlayer(context: Context, sourceUrl: String) {
        if (player != null) {
            return
        }

        val exoPlayer = ExoPlayer.Builder(context).build().also {
            val mediaItem = MediaItem.fromUri(sourceUrl)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = false
            it.seekTo(_playerState.value.currentPosition)
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    handleError(error)
                }
            })
        }

        this.player = exoPlayer

        _playerState.value = _playerState.value.copy(isReady = true)

        updatePlayerStatePeriodically(exoPlayer)
    }

    private fun updatePlayerStatePeriodically(exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            while (_playerState.value.isReady) {
                _playerState.value = PlayerState(
                    isPlaying = exoPlayer.isPlaying,
                    currentPosition = exoPlayer.currentPosition,
                    duration = if (exoPlayer.duration == TIME_UNSET) 30_000L else exoPlayer.duration
                )
                _bufferPercentage.value = exoPlayer.bufferedPercentage
                delay(1000)
            }
        }
    }

    fun replayForward(sec: Long) {
        player?.let {
            it.seekTo(it.currentPosition + sec)
            viewModelScope.launch {
                _playerState.value = _playerState.value.copy(currentPosition = it.currentPosition)
            }
        }
    }

    fun togglePlayPause() {
        player?.let {
            viewModelScope.launch {
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
            if (it.isPlaying) it.pause()
            else it.play()
        }
    }

    fun pause() {
        player?.let {
            viewModelScope.launch {
                _playerState.value = _playerState.value.copy(isPlaying = false)
            }
            it.pause()
        }
    }

    fun playerSeekTo(sec: Long) {
        viewModelScope.launch {
            player?.let {
                _playerState.value = _playerState.value.copy(currentPosition = sec)
                it.seekTo(sec)
            }
        }
    }

    fun savePlayerState() {
        player?.let {
            _playerState.value = _playerState.value.copy(
                isPlaying = it.isPlaying,
                currentPosition = it.currentPosition,
                duration = it.duration
            )
        }
    }

    fun releasePlayer() {
        player?.release()
        _playerState.value = PlayerState(isReady = false)
    }

    private fun handleError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                // TODO: Handle network connection error
                Log.d("PlayerViewModel", "Network connection error")
            }

            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                // TODO: Handle file not found error
                Log.d("PlayerViewModel", "File not found")
            }

            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                // TODO: Handle decoder initialization error
                Log.d("PlayerViewModel", "Decoder initialization error")
            }

            else -> {
                // TODO: Handle other types of errors
                Log.d("PlayerViewModel", "${error.message}")
            }
        }
    }
}
