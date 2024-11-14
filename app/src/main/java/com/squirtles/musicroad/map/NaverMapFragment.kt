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
import com.naver.maps.map.MapView
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

class NaverMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationOverlay: LocationOverlay
    private val circleOverlay = CircleOverlay()

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

        mapView = binding.navermap
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
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
            marker.map = naverMap
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        Log.d(TAG_LOG, "onResume() $this")
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        Log.d(TAG_LOG, "onPause() $this")
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG_LOG, "onStop() $this")
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val CIRCLE_RADIUS_METER = 100.0
        private const val INITIAL_CAMERA_ZOOM = 16.5
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private const val TAG_LOG = "MapFragment"
    }
}
