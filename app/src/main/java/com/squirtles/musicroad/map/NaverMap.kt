package com.squirtles.musicroad.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.squirtles.musicroad.R
import com.squirtles.musicroad.map.marker.MarkerIconView
import com.squirtles.musicroad.map.marker.MarkerKey
import com.squirtles.musicroad.ui.theme.Blue
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.Purple15
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun NaverMap(
    mapViewModel: MapViewModel,
    lastLocation: Location?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val naverMap = remember { mutableStateOf<NaverMap?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationSource =
        remember { FusedLocationSource(context as Activity, LOCATION_PERMISSION_REQUEST_CODE) }
    val locationOverlay = remember { mutableStateOf<LocationOverlay?>(null) }
    val circleOverlay = remember { CircleOverlay() }
    var clusterer by remember { mutableStateOf<Clusterer<MarkerKey>?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(lastLocation) {
        // 현재 위치와 마지막 위치가 5미터 이상 차이가 날때만 현위치 기준 반경 100m 픽 정보 개수 불러오기
        lastLocation?.let {
            mapViewModel.requestPickNotificationArea(lastLocation, CIRCLE_RADIUS_METER)
        }
    }

    DisposableEffect(Unit) {
        clusterer = Clusterer.ComplexBuilder<MarkerKey>()
            .tagMergeStrategy { cluster ->
                cluster.children.map { it.tag }.joinToString(",")
            }
            .clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
                override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                    super.updateClusterMarker(info, marker)

                    Log.d("test", "updateClustererMarker - info: ${info.tag}")
                    // 클릭된 마커가 클러스터 마커 안에 포함되면 클릭된 마커 해제
                    mapViewModel.clickedMarkerState.value.curPickId?.let { curPickId ->
                        if (info.tag.toString().contains(curPickId)) {
                            mapViewModel.resetClickedMarkerState(context)
                        }
                    }
                    val markerIconView = MarkerIconView(context).apply {
                        setPaintColor(Blue.toArgb())
                    }
                    marker.icon = OverlayImage.fromView(markerIconView)
                    marker.onClickListener = Overlay.OnClickListener {
                        true
                    }
                }
            })
            .leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
                override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                    super.updateLeafMarker(info, marker)

                    Log.d("test", "updateLeafMarker - info: ${info.key}")
                    val pick = (info.key as MarkerKey).pick
                    val markerIconView = MarkerIconView(context).apply {
                        setPaintColor(Blue.toArgb())
                    }
                    markerIconView.setLeafMarkerIcon(pick) {
                        marker.icon = OverlayImage.fromView(markerIconView)
                        marker.setOnClickListener {
                            marker.map?.let { map ->
                                setCameraToMarker(
                                    map = map,
                                    clickedMarkerPosition = marker.position
                                )
                                mapViewModel.setClickedMarkerState(
                                    context = context,
                                    marker = marker,
                                    pickId = pick.id
                                )
                            }
                            Log.d("test", "map: ${marker.map}, marker: ${marker.position}, pick: $pick")
                            true
                        }
                    }
                }
            })
            .build()
        onDispose {
            clusterer?.clear()
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
                        setMapClickListener { mapViewModel.resetClickedMarkerState(context) }
                        setCameraIdleListener { leftTop, rightBottom ->
                            mapViewModel.fetchPicksInBounds(leftTop, rightBottom, clusterer)
                        }
                        clusterer?.map = this
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun setCameraToMarker(
    map: NaverMap,
    clickedMarkerPosition: LatLng
) {
    val cameraUpdate = CameraUpdate
        .scrollTo(clickedMarkerPosition)
        .animate(CameraAnimation.Easing)
    map.moveCamera(cameraUpdate)
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
private const val CIRCLE_RADIUS_METER = 100.0
private const val INITIAL_CAMERA_ZOOM = 16.5
private const val MIN_ZOOM_LEVEL = 6.0
private const val MAX_ZOOM_LEVEL = 18.0
private val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
