package com.squirtles.musicroad.videoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.squirtles.domain.model.Pick

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoScreen(
    pick: Pick,
    modifier: Modifier,
    onBackClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        MusicVideoPlayer()
        VideoPlayerOverlay(pick, onBackClick)
    }
}
