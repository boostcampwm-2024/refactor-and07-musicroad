package com.squirtles.domain.usecase

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.squirtles.domain.model.PlayerState
import com.squirtles.domain.model.Song
import com.squirtles.mediaservice.MediaControllerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/*  */
class MediaPlayerUseCase @Inject constructor(
    private val mediaPlayerListenerUseCase: MediaPlayerListenerUseCase,
    private val mediaControllerProvider: MediaControllerProvider,
) {
    private var mediaController: MediaController? = null

    val audioSessionId = flow {
        emit(mediaControllerProvider.audioSessionFlow.first())
    }

    fun playerUiStateFlow(): Flow<PlayerState> =
        mediaPlayerListenerUseCase.playerStateFlow()

//    fun addMediaItem(sourceUrl: String) {
//        val mediaItem = MediaItem.fromUri(sourceUrl)
//        mediaController?.addMediaItem(mediaItem)
//    }

    fun setMediaItems(songs: List<Song>) {
        mediaController?.run {
            setMediaItems(songs.map {
                it.toMediaItem()
            })
            prepare()
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    fun seekToNextMediaItem() {
        mediaController?.run {
            seekToNextMediaItem()
        }
    }

    suspend fun readyPlayer() {
        mediaController = mediaControllerProvider.mediaControllerFlow.first()
    }

    fun setMediaItem(song: Song) {
        mediaController?.pause()
        mediaController?.setMediaItem(song.toMediaItem())
        mediaController?.prepare()
    }

    fun play() {
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
            .setUri(previewUrl)
            .build()
    }
}
