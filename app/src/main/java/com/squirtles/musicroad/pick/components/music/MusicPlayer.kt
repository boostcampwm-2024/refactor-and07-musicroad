package com.squirtles.musicroad.pick.components.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squirtles.musicroad.musicplayer.PlayerState

@Composable
fun MusicPlayer(
    previewUrl: String,
    playerState: PlayerState,
    bufferPercentage: Int,
    duration: Long,
    readyPlayer: (String) -> Unit,
    onSeekChanged: (Long) -> Unit,
    onReplayForwardClick: (Long) -> Unit,
    onPauseToggle: () -> Unit,
) {

    LaunchedEffect(Unit) {
        readyPlayer(previewUrl)
    }

    if (playerState.isReady) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayBar(
                duration = duration,
                currentTime = { playerState.currentPosition },
                bufferPercentage = { bufferPercentage },
                onSeekChanged = { timeMs ->
                    onSeekChanged(timeMs.toLong())
                },
                isPlaying = { playerState.isPlaying },
                onReplayClick = {
                    onReplayForwardClick(DEFAULT_REPLAY_SEC)
                },
                onPauseToggle = {
                    onPauseToggle()
                },
                onForwardClick = {
                    onReplayForwardClick(DEFAULT_FORWARD_SEC)
                },
            )
        }
    }
}

private const val DEFAULT_REPLAY_SEC = -5_000L
private const val DEFAULT_FORWARD_SEC = 5_000L
