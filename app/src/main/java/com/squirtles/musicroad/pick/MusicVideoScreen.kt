package com.squirtles.musicroad.pick

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.squirtles.musicroad.ui.theme.Purple15

@Composable
fun MusicVideoScreen(
    videoUri: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val playerView = remember { PlayerView(context) }
    val player = remember { ExoPlayer.Builder(context).build() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Purple15),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                playerView.apply {
                    this.player = player
                    val mediaItem = MediaItem.fromUri(videoUri)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
