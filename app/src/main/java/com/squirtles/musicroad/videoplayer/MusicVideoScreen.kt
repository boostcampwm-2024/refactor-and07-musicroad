package com.squirtles.musicroad.videoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.domain.model.Pick

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoScreen(
    pick: Pick,
    swipePlayState: Boolean,
    modifier: Modifier,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    val playerState = remember { mutableStateOf(true) }

    Box(
        modifier = modifier
    ) {
        MusicVideoPlayer(pick.musicVideoUrl, player, swipePlayState, playerState)
        VideoPlayerOverlay(pick, playerState, onBackClick)
    }
}
