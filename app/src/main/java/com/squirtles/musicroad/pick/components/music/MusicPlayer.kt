package com.squirtles.musicroad.pick.components.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squirtles.musicroad.common.Constants.DEFAULT_PADDING
import com.squirtles.musicroad.musicplayer.PlayerState
import com.squirtles.musicroad.ui.theme.PlayerBackground

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
                .padding(horizontal = 8.dp, vertical = DEFAULT_PADDING)
                .background(
                    color = PlayerBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 30.dp, vertical = DEFAULT_PADDING),
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
                }
            )
        }
    }
}

private const val DEFAULT_REPLAY_SEC = -5_000L
private const val DEFAULT_FORWARD_SEC = 5_000L
