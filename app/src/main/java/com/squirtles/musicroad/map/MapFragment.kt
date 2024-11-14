package com.squirtles.musicroad.map

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.squirtles.musicroad.R
import com.squirtles.musicroad.databinding.FragmentMapBinding
import com.squirtles.musicroad.ui.theme.Blue
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.Purple15
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationOverlay: LocationOverlay
    private val circleOverlay = CircleOverlay()
    private var selectedMarker: Marker? = null

    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG_LOG, mapViewModel.toString())
        val mapView = binding.containerMap.getFragment<MapFragment>()
        mapView.getMapAsync(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        initLocationOverlay()
        setCameraZoomLimit()
        setInitLocation()
        setLocationChangeListener()
//        mapViewModel.fetchPick("1PJY507YTSR8vlX7VH5w")
//        mapViewModel.fetchPick("1aDOLBPkTYqPyZJOvpBy")

        // 테스트 : 네이버커넥트 기준 주변 5km 내 픽 정보 불러오기
        mapViewModel.fetchPickInArea(37.380324, 127.115282, 5.0 * 1000.0)

        // 지도 클릭 이벤트 설정
        naverMap.setOnMapClickListener { _, _ ->
            selectedMarker?.let { marker ->
                marker.width = marker.icon.getIntrinsicWidth(requireContext())
                marker.height = marker.icon.getIntrinsicHeight(requireContext())

                selectedMarker = null
            }
        }

        lifecycleScope.launch {
            mapViewModel.centerButtonClick.collect {
                Log.d(TAG_LOG, "map fragment: center button click collect - $it")
                mapViewModel.curLocation.value?.let { location ->
                    createMarker(location)
                }
            }
        }

        lifecycleScope.launch {
            mapViewModel.curLocation.collect {
                Log.d(TAG_LOG, "map fragment: 위치 업데이트 - $it")
            }
        }
    }

    private fun setInitLocation() {
        if (checkSelfPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    locationOverlay.position = LatLng(location)
                    setCircleOverlay(location)
                    naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(location)))
                }
            }
        }
    }

    private fun setCameraZoomLimit() {
        naverMap.minZoom = 6.0
        naverMap.maxZoom = 18.0
    }

    private fun initLocationOverlay() {
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.uiSettings.isZoomControlEnabled = false
        naverMap.uiSettings.isTiltGesturesEnabled = false
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true
        locationOverlay.icon = OverlayImage.fromResource(R.drawable.ic_location)

        naverMap.moveCamera(CameraUpdate.zoomTo(INITIAL_CAMERA_ZOOM))
    }

    private fun setLocationChangeListener() {
        naverMap.addOnLocationChangeListener { location ->
            setCircleOverlay(location)
            mapViewModel.updateCurLocation(location)
        }
    }

    private fun setCircleOverlay(location: Location) {
        circleOverlay.center = LatLng(location.latitude, location.longitude)
        circleOverlay.color = Purple15.toArgb()
        circleOverlay.outlineColor = Primary.toArgb()
        circleOverlay.outlineWidth = 3
        circleOverlay.radius = CIRCLE_RADIUS_METER
        circleOverlay.map = naverMap
    }

    private fun checkSelfPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(requireContext(), PERMISSIONS[0]) ==
                PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(requireContext(), PERMISSIONS[1]) ==
                PermissionChecker.PERMISSION_GRANTED
    }

    private fun createMarker(location: Location) {
        val marker = Marker()
        val markerIconView = MarkerIconView(requireContext())
        markerIconView.setPaintColor(Blue.toArgb())
        markerIconView.loadImage("https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af") {
//            marker.position = LatLng(37.5670135, 126.9783740)
            marker.position = LatLng(location.latitude, location.longitude)
            marker.icon = OverlayImage.fromView(markerIconView)
            marker.setOnClickListener {
                onMarkerClick(marker)
                true
            }
            marker.map = naverMap
        }

        addMarker(35.1969198, 129.0827011)
        addMarker(35.1989350, 129.08407821)
    }

    // 지도에 테스트 마커 임의 추가를 위한 함수
    private fun addMarker(latitude: Double, longitude: Double) {
        val marker = Marker()
        val markerIconView = MarkerIconView(requireContext())
        markerIconView.setPaintColor(Primary.toArgb())
        markerIconView.loadImage("https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af") {
            marker.position = LatLng(latitude, longitude)
            marker.icon = OverlayImage.fromView(markerIconView)
            marker.setOnClickListener {
                onMarkerClick(marker)
                true
            }
            marker.map = naverMap
        }
    }

    private fun onMarkerClick(marker: Marker) {
        if (selectedMarker == marker) return // 선택된 마커를 다시 클릭하는 경우 -> 아무 동작도 하지 않도록

        val defaultIconWidth = marker.icon.getIntrinsicWidth(requireContext())
        val defaultIconHeight = marker.icon.getIntrinsicHeight(requireContext())

        // 선택된 마커가 있는 경우
        selectedMarker?.let {
            it.width = defaultIconWidth
            it.height = defaultIconHeight
        }

        marker.width = (defaultIconWidth * MARKER_SCALE).toInt()
        marker.height = (defaultIconHeight * MARKER_SCALE).toInt()
        selectedMarker = marker
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val CIRCLE_RADIUS_METER = 100.0
        private const val INITIAL_CAMERA_ZOOM = 16.5
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val MARKER_SCALE = 1.5

        private const val TAG_LOG = "MapFragment"
    }
}
