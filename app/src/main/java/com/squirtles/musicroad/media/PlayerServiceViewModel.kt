package com.squirtles.musicroad.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.domain.usecase.MediaPlayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerServiceViewModel @Inject constructor(
    private val mediaPlayerUseCase: MediaPlayerUseCase,
) : ViewModel() {

    lateinit var _playerUiState: StateFlow<PlayerUiState>

    val audioSessionId get() = mediaPlayerUseCase.audioSessionId

    fun readyPlayer(url: String) {
        _playerUiState = mediaPlayerUseCase.playerUiStateFlow(url)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PlayerUiState()
            )
    }

    fun onPlay(url: String) {
        mediaPlayerUseCase.play(url)
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
