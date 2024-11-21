package com.squirtles.musicroad.pick

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun MusicPlayer(
    context: Context = LocalContext.current,
    previewUrl: String,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val exoPlayer by playerViewModel.playerState.collectAsState()

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(context, previewUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            playerViewModel.savePlayerState()
            playerViewModel.releasePlayer()
        }
    }

    Column {
        PlayerControls(exoPlayer)
        PlayerAndroidView(exoPlayer)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun PlayerAndroidView(exoPlayer: ExoPlayer?) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                Log.d("DetailPickScreen", "previewUrl:")
                showController()
                controllerShowTimeoutMs = -1
                controllerHideOnTouch = false
                useController = true
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
        }
    )
}

@Composable
private fun PlayerControls(player: ExoPlayer?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { player?.playWhenReady = true }) {
            Text("Play")
        }
        Button(onClick = { player?.playWhenReady = false }) {
            Text("Pause")
        }

        Button(onClick = {
            player?.seekTo(player.currentPosition - 5_000) // Seek backward 10 seconds
        }) {
            Text("Seek -5s")
        }
        Button(onClick = {
            player?.seekTo(player.currentPosition + 5_000) // Seek forward 10 seconds
        }) {
            Text("Seek +5s")
        }
    }
}
