package com.squirtles.musicroad.pick.components.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squirtles.domain.model.PlayerUiState
import com.squirtles.domain.model.Song

@Composable
fun MusicPlayer(
    song: Song,
    playerUiState: PlayerUiState,
    onSeekChanged: (Long) -> Unit,
    onReplayForwardClick: (Boolean) -> Unit,
    onPauseToggle: (Song) -> Unit,
) {

//    LaunchedEffect(Unit) {
//        readyPlayer(previewUrl)
//    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlayBar(
            duration = playerUiState.duration,
            currentTime = { playerUiState.currentPosition },
            bufferPercentage = { playerUiState.bufferPercentage },
            onSeekChanged = { timeMs ->
                onSeekChanged(timeMs.toLong())
            },
            isPlaying = { playerUiState.isPlaying },
            onReplayClick = {
                onReplayForwardClick(false)
            },
            onPauseToggle = {
                onPauseToggle(song)
            },
            onForwardClick = {
                onReplayForwardClick(true)
            },
        )
    }
}

private const val DEFAULT_REPLAY_SEC = -5_000L
private const val DEFAULT_FORWARD_SEC = 5_000L
