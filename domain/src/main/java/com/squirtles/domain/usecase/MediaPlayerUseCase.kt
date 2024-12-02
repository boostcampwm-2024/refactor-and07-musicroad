package com.squirtles.domain.usecase

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.domain.model.Song
import com.squirtles.mediaservice.MediaControllerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/*  */
class MediaPlayerUseCase @Inject constructor(
    private val mediaPlayerListenerUseCase: MediaPlayerListenerUseCase,
    private val mediaControllerManager: MediaControllerManager,
) {
    private var mediaController: MediaController? = null

    private var _audioSessionId: Int = 0
    val audioSessionId get() = _audioSessionId

//    init {
//        collectMediaController()
//    }
//
//    private fun collectMediaController() {
//        // CoroutineScope를 통해 mediaControllerFlow를 수집
//        CoroutineScope(Dispatchers.Main).launch {
//            mediaController = mediaControllerManager.mediaControllerFlow.first()
//            Log.d("MediaPlayerUseCase", "mediaController: $mediaController")
//        }
//    }

    fun playerUiStateFlow(): Flow<PlayerUiState> =
        mediaPlayerListenerUseCase.playerUiStateFlow()

    fun addMediaItem(sourceUrl: String) {
        val mediaItem = MediaItem.fromUri(sourceUrl)
        mediaController?.addMediaItem(mediaItem)
    }

    fun addMediaItems(urls: List<String>) {
        mediaController?.addMediaItems(urls.map {
            MediaItem.fromUri(it)
        })
    }

    suspend fun setMediaItem(song: Song) {
        val job = CoroutineScope(Dispatchers.Main).async {
            mediaController = mediaControllerManager.mediaControllerFlow.first()
            Log.d("MediaPlayerUseCase", "mediaController: $mediaController")
        }
        job.await()

        mediaController?.setMediaItem(song.toMediaItem())
        mediaController?.prepare()
        Log.d("MediaPlayerUseCase", "setMediaItem:${mediaController?.currentMediaItem}")
        Log.d("MediaPlayerUseCase", "mediaController:${mediaController}")
    }

    fun play(song: Song) {
//        val mediaItem = song.toMediaItem()
//
//        if (mediaController?.currentMediaItem?.mediaId != mediaItem.mediaId) {
//            mediaController?.setMediaItem(mediaItem)
//            mediaController?.prepare()
//        }
        Log.d("MediaPlayerUseCase", "currentMediaItem:${mediaController?.currentMediaItem}")
        Log.d("MediaPlayerUseCase", "mediaController:${mediaController}")

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

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(this.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(songName)
                    .setArtist(artistName)
                    .setAlbumTitle(albumName)
                    .build()
            )
            .setUri(previewUrl)  // previewUrl을 setUri로 설정
            .build()
    }

}
