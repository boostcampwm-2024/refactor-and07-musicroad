package com.squirtles.musicroad.videoplayer

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.musicroad.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlayerState {
    Playing, Pause, Replay
}

@HiltViewModel
class VideoPlayerViewModel @Inject constructor() : ViewModel() {

    private var _player: ExoPlayer? = null
    val player get() = _player

    private var _swipePlayState = MutableStateFlow(false) // swipe 상태에 따른 play 여부
    val swipePlayState: StateFlow<Boolean> = _swipePlayState

    private var _playerState = MutableStateFlow(PlayerState.Playing) // 현재 플레이어의 상태
    val playerState: StateFlow<PlayerState> = _playerState

    private val _swipeState = MutableStateFlow(0f) // 현재 offset 저장
    val swipeState: StateFlow<Float> = _swipeState

    private var _showMusicVideo = false
    val showMusicVideo get() = _showMusicVideo

    private var lastPosition = 0L

    fun setShowMusicVideo(isShow: Boolean) {
        _showMusicVideo = isShow
    }

    fun updateSwipeState(newOffset: Float) {
        _swipeState.value = newOffset
    }

    fun initializePlayer(context: Context, videoUri: String) {
        if (player != null) return
        val exoPlayer = ExoPlayer.Builder(context).build().run {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
            seekTo(lastPosition)
            this
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    viewModelScope.launch {
                        Toast.makeText(context, getString(context, R.string.video_player_ended_message), Toast.LENGTH_SHORT).show()
                        _playerState.emit(PlayerState.Replay)
                        exoPlayer.seekTo(0)
                    }
                }
            }
        })

        _player = exoPlayer
        setPlayer()
    }

    fun releasePlayer() {
        _player = null
    }

    fun setPlayState(playerState: PlayerState) {
        viewModelScope.launch {
            _playerState.emit(playerState)
        }
    }

    fun setSwipePlayState(isPlaying: Boolean) {
        viewModelScope.launch {
            _swipePlayState.emit(isPlaying)
        }
    }

    fun setLastPosition(time: Long) {
        lastPosition = time
    }

    private fun setPlayer() {
        viewModelScope.launch {
            combine(swipePlayState, playerState) { swipeState, playState ->
                swipeState && (playState == PlayerState.Playing)
            }.collect { shouldPlay ->
                if (shouldPlay) {
                    player?.play()
                } else {
                    player?.pause()
                }
            }
        }
    }
}
