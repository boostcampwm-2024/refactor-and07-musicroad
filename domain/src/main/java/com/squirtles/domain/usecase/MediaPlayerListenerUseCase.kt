package com.squirtles.domain.usecase

import androidx.media3.common.Player
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.mediaservice.MediaControllerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/* 현재 플레이어에 이벤트 리스너 등록 -> 로딩,재생,seekbar 탐색, 재생시간 등 데이터를 UI데이터로 변환  */
class MediaPlayerListenerUseCase @Inject constructor(
    private val mediaControllerManager: MediaControllerManager
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    fun playerUiStateFlow(uri: String) = callbackFlow {
        val mediaController = mediaControllerManager.mediaControllerFlow.first()
        val currentMediaItem = mediaController.currentMediaItem

        val currentPlayerUiState = MutableStateFlow(
            if (mediaController.currentMediaItem?.mediaId == uri) {
                PlayerUiState(
                    isLoading = mediaController.isLoading,
                    isPlaying = mediaController.isPlaying,
                    hasNext = mediaController.hasNextMediaItem(),
                    currentPosition = mediaController.currentPosition,
                    duration = mediaController.duration,
                    bufferPercentage = mediaController.bufferedPercentage
                )
            } else {
                PlayerUiState()
            }
        )

        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                currentPlayerUiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                currentPlayerUiState.update { it.copy(isLoading = isLoading) }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    currentPlayerUiState.update {
                        it.copy(currentPosition = mediaController.currentPosition)
                    }
                }
            }
        }

        // Start timer when player is playing
        coroutineScope.launch {
            currentPlayerUiState
                .map { it.isLoading.not() && it.isPlaying }
                .distinctUntilChanged()
                .collect { isPlaying ->
                    if (isPlaying) {
                        timerJob = coroutineScope.launch {
                            val startDuration = mediaController.currentPosition
                            val maxDuration = mediaController.contentDuration

                            while (isActive && startDuration <= maxDuration) {
                                // Update time
                                currentPlayerUiState.update {
                                    it.copy(currentPosition = mediaController.currentPosition)
                                }
                            }
                        }
                    } else {
                        timerJob?.cancel()
                        timerJob = null
                    }
                }
        }

        // Update when player state changes
        coroutineScope.launch {
            currentPlayerUiState
                .onEach { send(it) }
                .collect {

                }
        }

        mediaController.addListener(playerListener)

        awaitClose {
            mediaController.removeListener(playerListener)
            coroutineScope.cancel()
        }
    }
}
