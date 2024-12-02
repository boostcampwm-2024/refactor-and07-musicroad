package com.squirtles.musicroad.media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.MediaPlayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerServiceViewModel @Inject constructor(
    private val mediaPlayerUseCase: MediaPlayerUseCase,
) : ViewModel() {

    private var _playerUiState: StateFlow<PlayerUiState> = mediaPlayerUseCase.playerUiStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerUiState()
        )
    val playerState get() = _playerUiState

    val audioSessionId get() = mediaPlayerUseCase.audioSessionId

    init {
        viewModelScope.launch {
            playerState.collect { state ->
                Log.d("PlayerServiceViewModel", "$state")
            }
        }
    }

    fun setMediaItem(song: Song) {
        viewModelScope.launch {
            mediaPlayerUseCase.setMediaItem(song)
        }
    }

    private fun onPlay(song: Song) {
        mediaPlayerUseCase.play(song)
    }

    fun onPause() {
        mediaPlayerUseCase.pause()
    }

    fun onStop() {
        mediaPlayerUseCase.stop()
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

    fun togglePlayPause(song: Song) {
        if (_playerUiState.value.isPlaying) {
            onPause()
        } else {
            onPlay(song)
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

    fun onAddToQueue(url: String) {
        mediaPlayerUseCase.addMediaItem(url)
    }

    fun onAddToQueue(urls: List<String>) {
        mediaPlayerUseCase.addMediaItems(urls)
    }
}
