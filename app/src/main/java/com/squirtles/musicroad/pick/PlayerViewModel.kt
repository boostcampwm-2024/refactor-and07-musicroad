package com.squirtles.musicroad.pick

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerViewModel @Inject constructor() : ViewModel() {

    private val _playerState = MutableStateFlow<ExoPlayer?>(null)
    val playerState: StateFlow<ExoPlayer?> = _playerState

    private var currentPosition: Long = 0L

    fun initializePlayer(context: Context, sourceUrl: String) {
        viewModelScope.launch {
            val exoPlayer = ExoPlayer.Builder(context).build().also {
                val mediaItem = MediaItem.fromUri(sourceUrl)
                it.setMediaItem(mediaItem)
                it.prepare()
                it.playWhenReady = false
                it.seekTo(currentPosition)
                it.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        handleError(error)
                    }
                })
            }
            _playerState.value = exoPlayer
        }
    }

    fun savePlayerState() {
        _playerState.value?.let {
            currentPosition = it.currentPosition
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
