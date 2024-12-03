package com.squirtles.musicroad.media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.PlayerState
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.MediaPlayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerServiceViewModel @Inject constructor(
    private val mediaPlayerUseCase: MediaPlayerUseCase,
) : ViewModel() {

    private var _playerState: StateFlow<PlayerState> = mediaPlayerUseCase.playerUiStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState()
        )

    val playerState get() = _playerState
    val audioSessionId get() = mediaPlayerUseCase.audioSessionId

    init {
        viewModelScope.launch {
            playerState.collect { state ->
                Log.d("PlayerServiceViewModel", "$state")
            }
        }
    }

    suspend fun readyPlayer() {
        val job = viewModelScope.async {
            mediaPlayerUseCase.readyPlayer()
        }
        job.await()
    }

    fun setMediaItem(pick: Pick) {
        viewModelScope.launch {
            if (_playerState.value.id == pick.id) {
                mediaPlayerUseCase.changeRepeatMode(false)
            } else {
                mediaPlayerUseCase.setMediaItem(pick)
            }
        }
    }

    fun setMediaItems(picks: List<Pick>) {
        viewModelScope.launch {
            val find = picks.find { it.id == _playerState.value.id }
            if (find != null) {
//                mediaPlayerUseCase.setMediaItem(find)
                mediaPlayerUseCase.addMediaItems(picks.minus(find))
                onPlay()
            } else {
                mediaPlayerUseCase.setMediaItems(picks)
            }
        }
    }

    private fun onPlay() {
        mediaPlayerUseCase.play()
    }

    fun onPause() {
        mediaPlayerUseCase.pause()
    }

    fun onStop() {
        mediaPlayerUseCase.stop()
    }

    fun onRelease() {
        mediaPlayerUseCase.release()
    }

    fun onPrevious() {
        mediaPlayerUseCase.previous()
    }

    fun onNext() {
        mediaPlayerUseCase.next()
    }

    fun onAdvanceBy() {
        mediaPlayerUseCase.advanceBy()
    }

    fun shuffleNext() {
        if (_playerState.value.isPlaying) {
            onPause()
        } else {
            onNext()
            onPlay()
        }
    }

    fun togglePlayPause(song: Song) {
        if (_playerState.value.isPlaying) {
            onPause()
        } else {
            onPlay()
        }
    }

    fun onRewindBy() {
        mediaPlayerUseCase.rewindBy()
    }

    fun onSeekingStarted() {
        mediaPlayerUseCase.onSeekingStarted()
    }

    fun onSeekingFinished(time: Long) {
        mediaPlayerUseCase.onSeekingFinished(time)
    }

    fun onAddToQueue(pick: Pick) {
        mediaPlayerUseCase.addMediaItem(pick)
    }

//    fun onAddToQueue(urls: List<String>) {
//        mediaPlayerUseCase.setMediaItems(urls)
//    }
}
