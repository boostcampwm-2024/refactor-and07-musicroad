package com.squirtles.musicroad.musicplayer

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.musicroad.musicplayer.PlayerState.Companion.PLAYER_STATE_INITIAL
import com.squirtles.musicroad.musicplayer.PlayerState.Companion.PLAYER_STATE_STOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private var player: ExoPlayer? = null

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId

    private val _playerState = MutableStateFlow(PLAYER_STATE_INITIAL)
    val playerState: StateFlow<PlayerState> = _playerState

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage: StateFlow<Int> = _bufferPercentage

    private val _duration = MutableStateFlow(30_000L)
    val duration: StateFlow<Long> = _duration

    private var playList: List<String> = emptyList()

    private fun initializePlayer(context: Context) {
        val exoPlayer = ExoPlayer.Builder(context).build().also {
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    handleError(error)
                }
            })
            it.volume = 0.5f
        }
        this.player = exoPlayer
    }

    @OptIn(UnstableApi::class)
    fun readyPlayer(context: Context, sourceUrl: String) {
        if (player != null) return

        initializePlayer(context)

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

            _audioSessionId.value = it.audioSessionId

            updatePlayerStatePeriodically(it)
        }
    }

    fun readyPlayerSetList(context: Context, sourceUrls: List<String>) {
        if (player != null) return

        initializePlayer(context)

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

    fun play() {
        player?.let {
            play(it)
        }
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
