package com.squirtles.musicroad.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Blue
import com.squirtles.musicroad.ui.theme.MusicRoadTheme
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.Purple15
import com.squirtles.musicroad.ui.theme.White

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onFavoriteClick: () -> Unit,
    onCenterClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    val pickCount by mapViewModel.pickCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val naverMap = remember { mutableStateOf<NaverMap?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationSource = remember { FusedLocationSource(context as Activity, LOCATION_PERMISSION_REQUEST_CODE) }
    val locationOverlay = remember { mutableStateOf<LocationOverlay?>(null) }
    val circleOverlay = remember { CircleOverlay() }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) {
                mapView.getMapAsync { map ->
                    naverMap.value = map
                    with(map) {
                        initLocationOverlay(locationSource, locationOverlay)
                        setCameraZoomLimit()
                        setInitLocation(context, circleOverlay, fusedLocationClient)
                        setLocationChangeListener(circleOverlay, mapViewModel)
                    }
                }
            }

            if (pickCount > 0) {
                PickNotificationBanner(pickCount)
            }

            BottomNavigation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                onFavoriteClick = onFavoriteClick,
                onCenterClick = onCenterClick,
                onSettingClick = onSettingClick
            )
        }
    }
}

private fun NaverMap.initLocationOverlay(
    currentLocationSource: FusedLocationSource,
    currentLocationOverlay: MutableState<LocationOverlay?>
) {
    locationSource = currentLocationSource
    uiSettings.isLocationButtonEnabled = true
    uiSettings.isZoomControlEnabled = false
    uiSettings.isTiltGesturesEnabled = false
    locationTrackingMode = LocationTrackingMode.Follow

    currentLocationOverlay.value = locationOverlay
    currentLocationOverlay.value?.run {
        isVisible = true
        icon = OverlayImage.fromResource(R.drawable.ic_location)
    }

    moveCamera(CameraUpdate.zoomTo(INITIAL_CAMERA_ZOOM))
}

private fun NaverMap.setCameraZoomLimit() {
    minZoom = 6.0
    maxZoom = 18.0
}

private fun NaverMap.setInitLocation(context: Context, circleOverlay: CircleOverlay, fusedLocationClient: FusedLocationProviderClient) {
    if (checkSelfPermission(context)) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                locationOverlay.position = LatLng(location)
                setCircleOverlay(circleOverlay, location)
                moveCamera(CameraUpdate.scrollTo(LatLng(location)))
            }
        }
    }
}

private fun NaverMap.setLocationChangeListener(circleOverlay: CircleOverlay, mapViewModel: MapViewModel) {
    addOnLocationChangeListener { location ->
        this.setCircleOverlay(circleOverlay, location)
        mapViewModel.updateCurLocation(location)
        mapViewModel.fetchPickInArea(location.latitude, location.longitude, PICK_RADIUS_METER)
        mapViewModel.requestPickNotificationArea(location, CIRCLE_RADIUS_METER)
    }
}

private fun NaverMap.setCircleOverlay(circleOverlay: CircleOverlay, location: Location) {
    circleOverlay.center = LatLng(location.latitude, location.longitude)
    circleOverlay.color = Purple15.toArgb()
    circleOverlay.outlineColor = Primary.toArgb()
    circleOverlay.outlineWidth = 3
    circleOverlay.radius = CIRCLE_RADIUS_METER
    circleOverlay.map = this
}

private fun checkSelfPermission(context: Context): Boolean {
    return PermissionChecker.checkSelfPermission(context, PERMISSIONS[0]) ==
            PermissionChecker.PERMISSION_GRANTED &&
            PermissionChecker.checkSelfPermission(context, PERMISSIONS[1]) ==
            PermissionChecker.PERMISSION_GRANTED
}

private fun NaverMap.createMarker(context: Context, location: Location) {
    val marker = Marker()
    val markerIconView = MarkerIconView(context)
    markerIconView.setPaintColor(Blue.toArgb())
    markerIconView.loadImage("https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af") {
        marker.position = LatLng(location.latitude, location.longitude)
        marker.icon = OverlayImage.fromView(markerIconView)
        marker.map = this
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
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val CIRCLE_RADIUS_METER = 100.0
private const val PICK_RADIUS_METER = 5000.0
private const val INITIAL_CAMERA_ZOOM = 16.5
private val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private const val TAG_LOG = "MapFragment"