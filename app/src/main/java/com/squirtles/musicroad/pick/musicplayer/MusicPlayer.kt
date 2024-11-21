package com.squirtles.musicroad.pick.musicplayer

import android.content.Context
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MusicPlayer(
    context: Context = LocalContext.current,
    previewUrl: String,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val bufferPercentage by playerViewModel.bufferPercentage.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(context, previewUrl)
    }

    LaunchedEffect(playerState) {
        Log.d("MusicPlayer", "playerState: $playerState")
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> playerViewModel.pause()
                Lifecycle.Event.ON_DESTROY -> playerViewModel.releasePlayer()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            playerViewModel.savePlayerState()
            playerViewModel.releasePlayer()
        }
    }

    if (playerState.isReady) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayBar(
                duration = playerState.duration,
                currentTime = { playerState.currentPosition },
                bufferPercentage = { bufferPercentage },
                onSeekChanged = { timeMs -> playerViewModel.playerSeekTo(timeMs.toLong()) },
                isPlaying = { playerState.isPlaying },
                onReplayClick = {
                    playerViewModel.replayForward(DEFAULT_REPLAY_SEC)
                },
                onPauseToggle = {
                    playerViewModel.togglePlayPause()
                },
                onForwardClick = {
                    playerViewModel.replayForward(DEFAULT_FORWARD_SEC)
                },
            )
        }
    }
}

private const val DEFAULT_REPLAY_SEC = -5_000L
private const val DEFAULT_FORWARD_SEC = 5_000L
