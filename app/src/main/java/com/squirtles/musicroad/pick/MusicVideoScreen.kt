package com.squirtles.musicroad.pick

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoScreen(
    videoUri: String,
    isPlaying: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    val playerView = remember { PlayerView(context) }
    val player = remember { ExoPlayer.Builder(context).build() }

    AndroidView(
        factory = {
            playerView.apply {
                this.player = player
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                val mediaItem = MediaItem.fromUri(videoUri)
                player.setMediaItem(mediaItem)
                player.prepare()
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        if (isPlaying) player.play()
        else player.pause()
    }
}
