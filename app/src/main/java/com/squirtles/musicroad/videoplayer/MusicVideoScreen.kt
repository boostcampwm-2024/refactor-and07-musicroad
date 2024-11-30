package com.squirtles.musicroad.videoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoScreen(
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MusicVideoPlayer()
        //  VideoPlayerOverlay(pick, onBackClick)
    }
}
