package com.squirtles.musicroad.map

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import com.squirtles.musicroad.pick.PickViewModel.Companion.DEFAULT_PICK
import com.squirtles.musicroad.pick.musicplayer.PlayerViewModel
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.MusicRoadTheme
import com.squirtles.musicroad.ui.theme.White

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    playerViewModel: PlayerViewModel,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit,
    onInfoWindowClick: (String) -> Unit
) {
    val nearPicks by mapViewModel.nearPicks.collectAsStateWithLifecycle()
    val pickMarkers by mapViewModel.pickMarkers.collectAsStateWithLifecycle()
    val selectedPickState by mapViewModel.selectedPickState.collectAsStateWithLifecycle()
    val lastLocation by mapViewModel.lastLocation.collectAsStateWithLifecycle()

    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(context)
        Log.d("MapScreen", "initializePlayer")
    }

    LaunchedEffect(nearPicks) {
        if (nearPicks.isNotEmpty()) {
            playerViewModel.readyPlayerSetList(nearPicks.map { it.song.previewUrl })
        }
    }

    LaunchedEffect(playerState) {
        isPlaying = playerState.isPlaying
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
//                Lifecycle.Event.ON_PAUSE -> playerViewModel.pause()
                Lifecycle.Event.ON_STOP -> playerViewModel.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            playerViewModel.stop()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NaverMap(
                mapViewModel = mapViewModel,
                lastLocation = lastLocation,
                pickMarkers = pickMarkers,
                selectedPickState = selectedPickState
            )

            if (nearPicks.isNotEmpty()) {
                PickNotificationBanner(
                    nearPicks = nearPicks,
                    isPlaying = isPlaying,
                    onClick = {
                        playerViewModel.shuffleNextItem()
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                pickMarkers[selectedPickState.current]?.pick?.let { pick ->
                    InfoWindow(
                        pick,
                        navigateToPick = { pickId ->
                            onInfoWindowClick(pickId)
                        },
                        calculateDistance = { lat, lng ->
                            mapViewModel.calculateDistance(lat, lng).let { distance ->
                                when {
                                    distance >= 1000.0 -> "%.1fkm".format(distance / 1000.0)
                                    distance >= 0 -> "%.0fm".format(distance)
                                    else -> ""
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                BottomNavigation(
                    modifier = Modifier.padding(bottom = 16.dp),
                    lastLocation = lastLocation,
                    onFavoriteClick = onFavoriteClick,
                    onCenterClick = {
                        onCenterClick()
                        mapViewModel.saveCurLocationForced()
                    },
                    onSettingClick = onSettingClick
                )
            }
        }
    }
}

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
