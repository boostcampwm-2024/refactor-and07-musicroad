package com.squirtles.musicroad.map

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.MusicRoadTheme
import com.squirtles.musicroad.ui.theme.White

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit,
    onInfoWindowClick: (String) -> Unit
) {
    val pickCount by mapViewModel.pickCount.collectAsStateWithLifecycle()
    val pickMarkers by mapViewModel.pickMarkers.collectAsStateWithLifecycle()
    val selectedPickState by mapViewModel.selectedPickState.collectAsStateWithLifecycle()
    val lastLocation by mapViewModel.lastLocation.collectAsStateWithLifecycle()

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

            if (pickCount > 0) {
                PickNotificationBanner(pickCount)
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
                            val distance = mapViewModel.calculateDistance(lat = lat, lng = lng)
                            if (distance == -1.0) "" else "${distance}km"
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                BottomNavigation(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onFavoriteClick = onFavoriteClick,
                    onCenterClick = {
                        // 마지막으로 저장된 위치값이 없으면 화면전환 X
                        if (lastLocation != null) {
                            onCenterClick()
                            mapViewModel.saveCurLocationForced()
                        }
                    },
                    onSettingClick = onSettingClick
                )
            }
        }
    }
}

@Composable
fun PickNotificationBanner(pickCount: Int) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.map_pick_notification, pickCount),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (LocalConfiguration.current.screenHeightDp * 0.08).dp)
                .clip(RoundedCornerShape(30.dp))
                .background(White.copy(alpha = 0.8f))
                .padding(vertical = 10.dp, horizontal = 23.dp)
        )
    }
}

@Preview
@Composable
private fun PickNotificationBannerPreview() {
    MusicRoadTheme {
        PickNotificationBanner(1)
    }
}
