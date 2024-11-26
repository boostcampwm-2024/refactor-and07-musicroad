package com.squirtles.musicroad.map

import android.util.Size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.create.HorizontalSpacer
import com.squirtles.musicroad.map.components.BottomNavigation
import com.squirtles.musicroad.map.components.InfoWindow
import com.squirtles.musicroad.map.components.PickNotificationBanner
import com.squirtles.musicroad.musicplayer.PlayerViewModel
import com.squirtles.musicroad.ui.theme.Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit,
    onPickSummaryClick: (String) -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val nearPicks by mapViewModel.nearPicks.collectAsStateWithLifecycle()
    val lastLocation by mapViewModel.lastLocation.collectAsStateWithLifecycle()

    val clickedMarkerState by mapViewModel.clickedMarkerState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var isPlaying: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(nearPicks) {
        if (nearPicks.isNotEmpty()) {
            playerViewModel.readyPlayerSetList(context, nearPicks.map { it.song.previewUrl })
        }
    }

    LaunchedEffect(playerState) {
        isPlaying = playerState.isPlaying
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
                lastLocation = lastLocation
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
                clickedMarkerState.prevClickedMarker?.let {
                    if (clickedMarkerState.curPickId != null) { // 단말 마커 클릭 시
                        showBottomSheet = false
                        mapViewModel.picks[clickedMarkerState.curPickId]?.let { pick ->
                            InfoWindow(
                                pick,
                                navigateToPick = { pickId ->
                                    playerViewModel.pause()
                                    onPickSummaryClick(pickId)
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
                        }
                    } else { // 클러스터 마커 클릭 시
                        showBottomSheet = true
                    }
                }

                Spacer(Modifier.height(16.dp))

                BottomNavigation(
                    modifier = Modifier.padding(bottom = 16.dp),
                    lastLocation = lastLocation,
                    onFavoriteClick = {
                        playerViewModel.pause()
                        onFavoriteClick()
                    },
                    onCenterClick = {
                        playerViewModel.pause()
                        onCenterClick()
                        mapViewModel.saveCurLocationForced()
                    },
                    onSettingClick = {
                        playerViewModel.pause()
                        onSettingClick()
                    }
                )
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                        mapViewModel.resetClickedMarkerState(context)
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(WindowInsets.statusBars.asPaddingValues()),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    clickedMarkerState.clusterPickList?.let { pickList ->
                        LazyColumn {
                            items(
                                items = pickList,
                                key = { it.id }
                            ) { pick ->
                                BottomSheetItem(
                                    song = pick.song,
                                    pickLocation = pick.location,
                                    // TODO: createdBy
                                    // TODO: comment
                                    calculateDistance = { lat, lng ->
                                        mapViewModel.calculateDistance(lat, lng).let { distance ->
                                            when {
                                                distance >= 1000.0 -> "%.1fkm".format(distance / 1000.0)
                                                distance >= 0 -> "%.0fm".format(distance)
                                                else -> ""
                                            }
                                        }
                                    },
                                    navigateToPickDetail = {
                                        playerViewModel.pause()
                                        onPickSummaryClick(pick.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetItem(
    song: Song,
    pickLocation: LocationPoint,
    calculateDistance: (Double, Double) -> String,
    navigateToPickDetail: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigateToPickDetail() }
            .padding(horizontal = DEFAULT_PADDING, vertical = DEFAULT_PADDING / 2)
    ) {
        AsyncImage(
            model = song.getImageUrlWithSize(REQUEST_IMAGE_SIZE),
            contentDescription = stringResource(R.string.map_album_image_description),
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(4.dp)),
            placeholder = ColorPainter(Gray),
            contentScale = ContentScale.Crop,
        )

        HorizontalSpacer(16)

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${song.songName} - ${song.artistName}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = calculateDistance(pickLocation.latitude, pickLocation.longitude),
                    style = MaterialTheme.typography.bodyMedium.copy(Gray)
                )
            }

            // TODO: 나머지 정보들: 누가 등록한 픽인지, 한마디
        }
    }
}

private val DEFAULT_PADDING = 16.dp
private val REQUEST_IMAGE_SIZE = Size(300, 300)
