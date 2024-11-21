package com.squirtles.musicroad.pick.musicplayer

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MusicPlayer(
    context: Context = LocalContext.current,
    previewUrl: String,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val exoPlayer by playerViewModel.playerState.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle(false)
    val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()
    val bufferPercentage by playerViewModel.bufferPercentage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(context, previewUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            playerViewModel.savePlayerState()
            playerViewModel.releasePlayer()
        }
    }

    if (exoPlayer != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayBar(
                duration = if (exoPlayer?.duration!! < 0) 0L else exoPlayer!!.duration,
                currentTime = { currentPosition },
                bufferPercentage = { bufferPercentage },
                onSeekChanged = { timeMs -> playerViewModel.playerSeekTo(timeMs.toLong()) },
                isPlaying = { isPlaying },
                onReplayClick = {
                    playerViewModel.replay(5_000)
                },
                onPauseToggle = {
                    playerViewModel.togglePlayPause()
                },
                onForwardClick = {
                    playerViewModel.forward(5_000)
                },
            )
        }
    }
}
