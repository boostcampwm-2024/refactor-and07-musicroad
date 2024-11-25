package com.squirtles.musicroad.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squirtles.musicroad.map.components.BottomNavigation
import com.squirtles.musicroad.map.components.InfoWindow
import com.squirtles.musicroad.map.components.PickNotificationBanner
import com.squirtles.musicroad.pick.musicplayer.PlayerViewModel

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit,
    onInfoWindowClick: (String) -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val nearPicks by mapViewModel.nearPicks.collectAsStateWithLifecycle()
    val pickMarkers by mapViewModel.pickMarkers.collectAsStateWithLifecycle()
    val selectedPickState by mapViewModel.selectedPickState.collectAsStateWithLifecycle()
    val lastLocation by mapViewModel.lastLocation.collectAsStateWithLifecycle()

    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(nearPicks) {
        if (nearPicks.isNotEmpty()) {
            playerViewModel.readyPlayerSetList(context, nearPicks.map { it.song.previewUrl })
        }
    }

    LaunchedEffect(playerState) {
        isPlaying = playerState.isPlaying
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        playerViewModel.pause()
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
