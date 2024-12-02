package com.squirtles.domain.usecase

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.mediaservice.MediaControllerManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/*  */
class MediaPlayerUseCase @Inject constructor(
    private val mediaPlayerListenerUseCase: MediaPlayerListenerUseCase,
    private val mediaControllerManager: MediaControllerManager,
) {
    private var mediaController: MediaController? = null

    private var _audioSessionId: Int = 0
    val audioSessionId get() = _audioSessionId

    fun playerUiStateFlow(uri: String): Flow<PlayerUiState> =
        mediaPlayerListenerUseCase.playerUiStateFlow(uri)

    fun addMediaItem(sourceUrl: String) {
        val mediaItem = MediaItem.fromUri(sourceUrl)
        mediaController?.addMediaItem(mediaItem)
    }

    fun addMediaItems(urls: List<String>) {
        mediaController?.addMediaItems(urls.map {
            MediaItem.fromUri(it)
        })
    }

    fun play(sourceUrl: String) {
        val mediaItem = MediaItem.fromUri(sourceUrl)

        // Set mediaItem if different from the media item currently playing
        if (mediaController?.currentMediaItem?.mediaId != mediaItem.mediaId) {
            mediaController?.setMediaItem(mediaItem)
            mediaController?.prepare()
        }

        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun stop() {
        mediaController?.stop()
        mediaController?.release()
    }

    fun next() {
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
        }
    }

    fun previous() {
        if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
        } else {
            mediaController?.seekToDefaultPosition()
        }
    }

    fun advanceBy() {
        mediaController?.apply {
            seekTo(currentPosition + com.squirtles.mediaservice.SEEK_TO_DURATION)
        }
    }

    fun rewindBy() {
        mediaController?.apply {
            seekTo(currentPosition - com.squirtles.mediaservice.SEEK_TO_DURATION)
        }
    }

    fun onSeekingStarted() {
        mediaController?.seekToDefaultPosition()
    }

    fun onSeekingFinished(time: Long) {
        mediaController?.seekTo(time)
    }
}
