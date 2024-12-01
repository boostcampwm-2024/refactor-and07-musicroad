package com.squirtles.musicroad.videoplayer

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor() : ViewModel() {

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player = _player.asStateFlow()

    private var _swipePlayState = MutableStateFlow(false) // swipe 상태에 따른 play 여부
    private val swipePlayState = _swipePlayState.asStateFlow()

    private var _playerState = MutableStateFlow(VideoPlayerState.Playing) // 현재 플레이어의 상태
    val playerState = _playerState.asStateFlow()

    private val _videoSize = MutableStateFlow<VideoSize?>(null)
    val videoSize = _videoSize.asStateFlow()

    private var lastPosition = 0L

    fun initializePlayer(context: Context, pick: Pick) {
        if (player.value != null) return

        val exoPlayer = ExoPlayer.Builder(context).build().run {
            val mediaItem = MediaItem.fromUri(pick.musicVideoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
            seekTo(lastPosition)
            this
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                _videoSize.value = videoSize
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    viewModelScope.launch {
                        Toast.makeText(context, getString(context, R.string.video_player_ended_message), Toast.LENGTH_SHORT).show()
                        _playerState.emit(VideoPlayerState.Replay)
                        exoPlayer.seekTo(0)
                    }
                }
            }
        })

        _player.value = exoPlayer
        setPlayer()
    }

    fun releasePlayer() {
        _player.value = null
    }

    fun setPlayState(playerState: VideoPlayerState) {
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
                swipeState && (playState == VideoPlayerState.Playing)
            }.collect { shouldPlay ->
                if (shouldPlay) {
                    player.value?.play()
                } else {
                    player.value?.pause()
                }
            }
        }
    }
}
