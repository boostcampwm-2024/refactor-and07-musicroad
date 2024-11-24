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
import com.squirtles.musicroad.pick.musicplayer.PlayerState.Companion.PLAYER_STATE_INITIAL
import com.squirtles.musicroad.pick.musicplayer.PlayerState.Companion.PLAYER_STATE_STOP
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
) {
    companion object {
        val PLAYER_STATE_INITIAL = PlayerState(isReady = false)
        val PLAYER_STATE_STOP = PlayerState(isReady = true, isPlaying = false)
    }
}

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private var player: ExoPlayer? = null

    private val _playerState = MutableStateFlow(PLAYER_STATE_INITIAL)
    val playerState: StateFlow<PlayerState> = _playerState

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage: StateFlow<Int> = _bufferPercentage

    private val _duration = MutableStateFlow(30_000L)
    val duration: StateFlow<Long> = _duration

    private var playList: List<String> = emptyList()

    fun initializePlayer(context: Context) {
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

            _playerState.value =
                PlayerState(isReady = true, currentPosition = _playerState.value.currentPosition)

            _duration.value =
                if (it.duration == TIME_UNSET) 30_000L else it.duration

            updatePlayerStatePeriodically(it)
        }
    }

    fun readyPlayerSetList(sourceUrls: List<String>) {
        player?.let {
            it.setMediaItems(sourceUrls.map { url ->
                MediaItem.fromUri(url)
            })
            it.prepare()
            it.playWhenReady = false
            it.repeatMode = Player.REPEAT_MODE_ALL
            playList = sourceUrls
        }
    }

    private fun updatePlayerStatePeriodically(exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            while (_playerState.value.isReady) {
                _playerState.value = _playerState.value.copy(
                    isPlaying = exoPlayer.isPlaying,
                    currentPosition = exoPlayer.currentPosition,
                )
                _bufferPercentage.value = exoPlayer.bufferedPercentage
                delay(1000)
            }
        }
    }

    fun shuffleNextItem() {
        viewModelScope.launch {
            player?.let {
                if (!it.isPlaying) it.seekToNextMediaItem()
                togglePlayPause(it)
            }
        }
    }

    fun replayForward(sec: Long) {
        player?.let {
            it.seekTo(it.currentPosition + sec)
            viewModelScope.launch {
                _playerState.value =
                    _playerState.value.copy(currentPosition = it.currentPosition)
            }
        }
    }

    fun togglePlayPause() {
        player?.let {
            togglePlayPause(it)
        }
    }

    private fun togglePlayPause(exoPlayer: ExoPlayer) {
        if (exoPlayer.isPlaying) pause(exoPlayer)
        else play(exoPlayer)
    }

    private fun play(exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            _playerState.value = _playerState.value.copy(isPlaying = true)
        }
        exoPlayer.play()
    }

    fun pause() {
        player?.let {
            pause(it)
        }
    }

    private fun pause(exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            _playerState.value = _playerState.value.copy(isPlaying = false)
        }
        exoPlayer.pause()
    }

    fun stop() {
        player?.let {
            viewModelScope.launch {
                _playerState.value = PLAYER_STATE_STOP
            }
            it.stop()
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
                isReady = false,
                isPlaying = false,
                currentPosition = it.currentPosition,
            )
        }
    }

    private fun releasePlayer() {
        player?.release()
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

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
