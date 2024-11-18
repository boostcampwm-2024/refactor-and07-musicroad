package com.squirtles.musicroad.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Blue
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.Purple15
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun NaverMap(mapViewModel: MapViewModel) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val naverMap = remember { mutableStateOf<NaverMap?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationSource = remember { FusedLocationSource(context as Activity, LOCATION_PERMISSION_REQUEST_CODE) }
    val locationOverlay = remember { mutableStateOf<LocationOverlay?>(null) }
    val circleOverlay = remember { CircleOverlay() }

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val mapLifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }

        lifecycleOwner.lifecycle.addObserver(mapLifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(mapLifecycleObserver)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                coroutineScope.launch {
                    naverMap.value = suspendCoroutine { continuation ->
                        getMapAsync {
                            continuation.resume(it)
                        }
                    }

                    naverMap.value?.run {
                        initMapSettings()
                        initDeviceLocation(context, circleOverlay, fusedLocationClient)
                        initLocationOverlay(locationSource, locationOverlay)
                        setLocationChangeListener(circleOverlay, mapViewModel)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun NaverMap.initLocationOverlay(
    currentLocationSource: FusedLocationSource,
    currentLocationOverlay: MutableState<LocationOverlay?>
) {
    locationSource = currentLocationSource
    locationTrackingMode = LocationTrackingMode.Follow
    currentLocationOverlay.value = locationOverlay
    currentLocationOverlay.value?.run {
        isVisible = true
        icon = OverlayImage.fromResource(R.drawable.ic_location)
    }
    moveCamera(CameraUpdate.zoomTo(INITIAL_CAMERA_ZOOM))
}

private fun NaverMap.initDeviceLocation(
    context: Context,
    circleOverlay: CircleOverlay,
    fusedLocationClient: FusedLocationProviderClient,
) {
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

private fun NaverMap.initMapSettings() {
    setCameraZoomLimit()
    uiSettings.setNaverMapMapUi()
}

private fun UiSettings.setNaverMapMapUi() {
    isLocationButtonEnabled = true
    isZoomControlEnabled = false
    isTiltGesturesEnabled = false
}

private fun NaverMap.setCameraZoomLimit() {
    minZoom = 6.0
    maxZoom = 18.0
}

private fun checkSelfPermission(context: Context): Boolean {
    return PermissionChecker.checkSelfPermission(context, PERMISSIONS[0]) ==
            PermissionChecker.PERMISSION_GRANTED &&
            PermissionChecker.checkSelfPermission(context, PERMISSIONS[1]) ==
            PermissionChecker.PERMISSION_GRANTED
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val CIRCLE_RADIUS_METER = 100.0
private const val PICK_RADIUS_METER = 5000.0
private const val INITIAL_CAMERA_ZOOM = 16.5
private val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)