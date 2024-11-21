package com.squirtles.musicroad.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.Purple15
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun NaverMap(
    mapViewModel: MapViewModel,
    lastLocation: Location?,
    pickMarkers: Map<String, MusicRoadMarker>,
    selectedPickState: PickState,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val naverMap = remember { mutableStateOf<NaverMap?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationSource =
        remember { FusedLocationSource(context as Activity, LOCATION_PERMISSION_REQUEST_CODE) }
    val locationOverlay = remember { mutableStateOf<LocationOverlay?>(null) }
    val circleOverlay = remember { CircleOverlay() }

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedPickState) {
        pickMarkers[selectedPickState.current]?.toggleSizeByClick(context, true)
        pickMarkers[selectedPickState.previous]?.toggleSizeByClick(context, false)
    }

    LaunchedEffect(lastLocation) {
        // 현재 위치와 마지막 위치가 5미터 이상 차이가 날때만 현위치 기준 반경 100m 픽 정보 개수 불러오기
        lastLocation?.let {
            mapViewModel.requestPickNotificationArea(lastLocation, CIRCLE_RADIUS_METER)
        }
    }

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
            naverMap.value?.let {
                mapViewModel.setLastCameraPosition(it.cameraPosition)
            }
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
                        initDeviceLocation(
                            context,
                            circleOverlay,
                            fusedLocationClient,
                            mapViewModel.lastCameraPosition
                        )
                        initLocationOverlay(locationSource, locationOverlay)
                        setLocationChangeListener(circleOverlay, mapViewModel)
                        setMapClickListener { mapViewModel.resetSelectedPickState() }
                        setCameraIdleListener { leftTop, rightBottom ->
                            mapViewModel.fetchPicksInBounds(leftTop, rightBottom)
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        naverMap.value?.let {
            pickMarkers.forEach { (pick, marker) ->
                if (marker.map == null) {
                    it.createMarker(
                        context = context,
                        marker = marker,
                        setSelectedPickState = mapViewModel::setSelectedPickState
                    )
                }
            }
        }
    }
}

private fun NaverMap.createMarker(
    context: Context,
    marker: MusicRoadMarker,
    setSelectedPickState: (String) -> Unit
) {
    marker.loadMarkerImage(context = context, map = this) {
        onMarkerClick(
            clickedMarker = marker,
            setSelectedPickState = setSelectedPickState
        )
    }
}

private fun NaverMap.onMarkerClick(
    clickedMarker: MusicRoadMarker,
    setSelectedPickState: (String) -> Unit
) {
    val cameraUpdate = CameraUpdate
        .scrollTo(clickedMarker.position)
        .animate(CameraAnimation.Easing)
    this.moveCamera(cameraUpdate)

    setSelectedPickState(clickedMarker.pick.id)
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
}

private fun NaverMap.initDeviceLocation(
    context: Context,
    circleOverlay: CircleOverlay,
    fusedLocationClient: FusedLocationProviderClient,
    lastCameraPosition: CameraPosition?
) {
    if (checkSelfPermission(context)) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                locationOverlay.position = LatLng(location)
                setCircleOverlay(circleOverlay, location)
                lastCameraPosition?.let {
                    moveCamera(CameraUpdate.toCameraPosition(it))
                } ?: run {
                    moveCamera(CameraUpdate.scrollTo(LatLng(location)))
                    moveCamera(CameraUpdate.zoomTo(INITIAL_CAMERA_ZOOM))
                }
            }
        }
    }
}

private fun NaverMap.setLocationChangeListener(
    circleOverlay: CircleOverlay,
    mapViewModel: MapViewModel
) {
    addOnLocationChangeListener { location ->
        this.setCircleOverlay(circleOverlay, location)
        mapViewModel.updateCurLocation(location)
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
    minZoom = MIN_ZOOM_LEVEL
    maxZoom = MAX_ZOOM_LEVEL
}

// 지도 클릭 이벤트 설정
private fun NaverMap.setMapClickListener(
    resetSelectedMarkerAndPick: () -> Unit
) {
    this.setOnMapClickListener { _, _ ->
        resetSelectedMarkerAndPick()
    }
}

// 카메라 대기 이벤트 설정
private fun NaverMap.setCameraIdleListener(
    fetchPicksInBounds: (LatLng, LatLng) -> Unit
) {
    addOnCameraIdleListener {
        val leftTop = projection.fromScreenLocation(PointF(0f, 0f))
        val rightBottom =
            projection.fromScreenLocation(PointF(contentWidth.toFloat(), contentHeight.toFloat()))
        fetchPicksInBounds(leftTop, rightBottom)
    }
}

private fun checkSelfPermission(context: Context): Boolean {
    return PermissionChecker.checkSelfPermission(context, PERMISSIONS[0]) ==
            PermissionChecker.PERMISSION_GRANTED &&
            PermissionChecker.checkSelfPermission(context, PERMISSIONS[1]) ==
            PermissionChecker.PERMISSION_GRANTED
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val CIRCLE_RADIUS_METER = 150.0
private const val INITIAL_CAMERA_ZOOM = 16.0
private const val MIN_ZOOM_LEVEL = 6.0
private const val MAX_ZOOM_LEVEL = 18.0
private val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
