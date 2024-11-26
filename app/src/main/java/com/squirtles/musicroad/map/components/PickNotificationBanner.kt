package com.squirtles.musicroad.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import com.squirtles.musicroad.pick.PickViewModel.Companion.DEFAULT_PICK
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.MusicRoadTheme
import com.squirtles.musicroad.ui.theme.White

@Composable
fun PickNotificationBanner(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    nearPicks: List<Pick>,
    onClick: () -> Unit,
) {
    val lastPlayedSongId = remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(
                id = if (isPlaying) R.string.map_pick_notification_stop else R.string.map_pick_notification_play,
                nearPicks.count()
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (LocalConfiguration.current.screenHeightDp * 0.08).dp)
                .clip(RoundedCornerShape(30.dp))
                .clickable {
                    onClick()
                }
                .background(White.copy(alpha = 0.8f))
                .padding(vertical = 10.dp, horizontal = 23.dp),
            color = Black,
        )
    }
}

@Preview
@Composable
private fun PickNotificationBannerPreview() {
    MusicRoadTheme {
        PickNotificationBanner(
            nearPicks = listOf(DEFAULT_PICK),
            onClick = { }
        )
    }
}
