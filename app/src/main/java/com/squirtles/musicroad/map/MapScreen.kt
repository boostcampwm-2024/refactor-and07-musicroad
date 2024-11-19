package com.squirtles.musicroad.map

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
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
    val selectedPick by mapViewModel.selectedPickState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NaverMap(
                mapViewModel = mapViewModel
            )

            if (pickCount > 0) {
                PickNotificationBanner(pickCount)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                pickMarkers[selectedPick.current]?.pick?.let { pick ->
                    InfoWindow(
                        pick,
                        navigateToPick = { pickId ->
                            onInfoWindowClick(pickId)
                        },
                        calculateDistance = { lat, lng ->
                            mapViewModel.calculateDistance(lat = lat, lng = lng).toInt().toString()
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                BottomNavigation(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onFavoriteClick = onFavoriteClick,
                    onCenterClick = {
                        onCenterClick()
                        mapViewModel.onCenterButtonClick()
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

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .size(245.dp, 50.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            // 왼쪽 버튼
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.map_navigation_favorite_icon_description),
                    modifier = Modifier.padding(start = BottomNavigationHorizontalPadding),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 오른쪽 버튼
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSettingClick() },
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.map_navigation_setting_icon_description),
                    modifier = Modifier.padding(end = BottomNavigationHorizontalPadding),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 중앙 버튼
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary)
                .clickable { onCenterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_musical_note_64),
                contentDescription = stringResource(R.string.map_navigation_center_icon_description),
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationLightPreview() {
    MusicRoadTheme {
        BottomNavigation(
            onFavoriteClick = {},
            onCenterClick = {},
            onSettingClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomNavigationDarkPreview() {
    MusicRoadTheme {
        BottomNavigation(
            onFavoriteClick = {},
            onCenterClick = {},
            onSettingClick = {}
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

private val BottomNavigationHorizontalPadding = 32.dp
