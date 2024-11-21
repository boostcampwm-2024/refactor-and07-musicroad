package com.squirtles.musicroad.pick.musicplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private val _playerState = MutableStateFlow<ExoPlayer?>(null)
    val playerState: StateFlow<ExoPlayer?> = _playerState

    private val _isPlaying = MutableSharedFlow<Boolean>()
    val isPlaying: SharedFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _bufferPercentage = MutableStateFlow(0)
    val bufferPercentage: StateFlow<Int> = _bufferPercentage

    fun initializePlayer(context: Context, sourceUrl: String) {
        if (_playerState.value != null) {
            releasePlayer()
        }

        val exoPlayer = ExoPlayer.Builder(context).build().also {
            val mediaItem = MediaItem.fromUri(sourceUrl)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = false
            it.seekTo(_currentPosition.value)
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    handleError(error)
                }
            })
        }
        _playerState.value = exoPlayer

        viewModelScope.launch {
            while (_playerState.value != null) {
                _currentPosition.value = exoPlayer.currentPosition
                _bufferPercentage.value = exoPlayer.bufferedPercentage
                delay(1000)
            }
        }
    }

    fun replay(sec: Long) {
        _playerState.value?.let {
            it.seekTo(it.currentPosition - sec)
        }
    }

    fun forward(sec: Long) {
        _playerState.value?.let {
            it.seekTo(it.currentPosition + sec)
        }
    }

    fun togglePlayPause() {
        _playerState.value?.let {
            if (it.isPlaying) it.pause()
            else it.play()

            viewModelScope.launch {
                _isPlaying.emit(it.isPlaying)
            }
        }
    }

    fun playerSeekTo(sec: Long) {
        viewModelScope.launch {
            _playerState.value?.let {
                _currentPosition.value = sec
                it.seekTo(sec)
            }
        }
    }

    fun savePlayerState() {
        _playerState.value?.let {
            _currentPosition.value = it.currentPosition
        }
    }

    fun releasePlayer() {
        _playerState.value?.release()
        _playerState.value = null
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
