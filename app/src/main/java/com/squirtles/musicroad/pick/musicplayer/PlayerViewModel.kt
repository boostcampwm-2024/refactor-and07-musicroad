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
)

private val INITIAL_PLAYER_STATE = PlayerState(isReady = false)

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private var player: ExoPlayer? = null

    private val _playerState = MutableStateFlow(INITIAL_PLAYER_STATE)
    val playerState: StateFlow<PlayerState> = _playerState

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage: StateFlow<Int> = _bufferPercentage

    private val _duration = MutableStateFlow(30_000L)
    val duration: StateFlow<Long> = _duration

    fun initializePlayer(context: Context) {
        releasePlayer()

        val exoPlayer = ExoPlayer.Builder(context).build().also {
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    handleError(error)
                }
            })
        }

        this.player = exoPlayer
    }

    fun readyPlayer(sourceUrl: String) {
        player?.let {
            val mediaItem = MediaItem.fromUri(sourceUrl)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = false
            it.seekTo(_playerState.value.currentPosition)
            it.volume = 0.5f

            _playerState.value = PlayerState(isReady = true)
        }
    }

    fun readyPlayerSetList(sourceUrls: List<String>) {
        player?.let {
            sourceUrls.forEach { url ->
                it.addMediaItem(MediaItem.fromUri(url))
            }
            it.prepare()
            it.playWhenReady = false
            it.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun shuffleNextItem() {
        viewModelScope.launch {
            player?.let {
                if (it.isPlaying) {
                    _playerState.value = PlayerState(isPlaying = false)
                    it.pause()
                } else {
                    _playerState.value = PlayerState(isPlaying = true)
                    it.seekToNextMediaItem()
                    it.play()
                }
            }
        }
    }

    fun updatePlayerStatePeriodically(exoPlayer: ExoPlayer? = this.player) {
        exoPlayer?.let {
            viewModelScope.launch {
                while (_playerState.value.isReady) {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = exoPlayer.isPlaying,
                        currentPosition = exoPlayer.currentPosition,
                    )
                    _duration.value =
                        if (exoPlayer.duration == TIME_UNSET) 30_000L else exoPlayer.duration
                    _bufferPercentage.value = exoPlayer.bufferedPercentage
                    delay(1000)
                }
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
                _playerState.value =
                    _playerState.value.copy(isPlaying = !(_playerState.value.isPlaying))
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
            )
        }
    }

    fun releasePlayer() {
        player?.release()
        _playerState.value = INITIAL_PLAYER_STATE
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
