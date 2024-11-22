package com.squirtles.musicroad.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.FetchLastLocationUseCase
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.SaveLastLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickState(
    val previous: String? = null,
    val current: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    fetchLastLocationUseCase: FetchLastLocationUseCase,
    private val saveLastLocationUseCase: SaveLastLocationUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {

    private var _lastCameraPosition: CameraPosition? = null
    val lastCameraPosition get() = _lastCameraPosition

    private val _pickMarkers = MutableStateFlow<Map<String, MusicRoadMarker>>(emptyMap())
    val pickMarkers = _pickMarkers.asStateFlow()

    private val _nearPicks = MutableStateFlow<List<Pick>>(emptyList())
    val nearPicks = _nearPicks.asStateFlow()

    private val _selectedPickState = MutableStateFlow(PickState(null, null))
    val selectedPickState = _selectedPickState.asStateFlow()

    // FIXME : 네이버맵의 LocationChangeListener에서 실시간으로 변하는 위치 정보 -> 더 나은 방법이 있으면 고쳐주세요
    private var _currentLocation: Location? = null
    val curLocation get() = _currentLocation

    // LocalDataSource에 저장되는 위치 정보
    // Firestore 데이터 쿼리 작업 최소화 및 위치데이터 공유 용도
    val lastLocation: StateFlow<Location?> = fetchLastLocationUseCase()

    fun setLastCameraPosition(cameraPosition: CameraPosition) {
        _lastCameraPosition = cameraPosition
    }

    fun updateCurLocation(location: Location) {
        _currentLocation = location

        if (lastLocation.value == null
            || calculateDistance(location.latitude, location.longitude) > 5.0
        ) {
            saveCurLocation(location)
        }
    }

    private fun saveCurLocation(location: Location) {
        viewModelScope.launch {
            saveLastLocationUseCase(location)
        }
    }

    fun saveCurLocationForced() {
        _currentLocation?.let { location ->
            saveCurLocation(location)
        }
    }

    fun calculateDistance(
        lat: Double,
        lng: Double,
        from: Location? = lastLocation.value,
    ): Double {
        return from?.let {
            val location = Location("pickLocation").apply {
                latitude = lat
                longitude = lng
            }
            from.distanceTo(location).toDouble()
        } ?: -1.0
    }

    fun setSelectedPickState(pickId: String) {
        viewModelScope.launch {
            val lastSelectedPick = selectedPickState.value.current
            if (lastSelectedPick == pickId) return@launch

            _selectedPickState.emit(PickState(lastSelectedPick, pickId))
        }
    }

    fun resetSelectedPickState() {
        viewModelScope.launch {
            val lastSelectedPick = selectedPickState.value.current
            _selectedPickState.emit(PickState(lastSelectedPick, null))
        }
    }

    fun fetchPicksInBounds(leftTop: LatLng, rightBottom: LatLng) {
        viewModelScope.launch {
            val center = LatLng(
                (leftTop.latitude + rightBottom.latitude) / 2,
                (leftTop.longitude + rightBottom.longitude) / 2
            )
            val radiusInM = leftTop.distanceTo(rightBottom)
            val picks = fetchPickInAreaUseCase(center.latitude, center.longitude, radiusInM)

            picks.onSuccess { pickList ->
                val newMarkerMap = mutableMapOf<String, MusicRoadMarker>()
                Log.d("MapViewModel", "$pickList")
                pickList.forEach { pick ->
                    newMarkerMap[pick.id] =
                        _pickMarkers.value[pick.id] ?: MusicRoadMarker(pick = pick)
                }
                _pickMarkers.value.forEach { (_, marker) ->
                    if (marker.pick !in pickList) {
                        marker.clearMap()
                    }
                }
                _pickMarkers.value = newMarkerMap
            }
            picks.onFailure {
                // TODO: NoSuchPickInRadiusException일 때
            }

            Log.d("MapViewModel", "개수: ${_pickMarkers.value.size}")
            Log.d("MapViewModel", "값: ${_pickMarkers.value}")
        }
    }

    fun requestPickNotificationArea(location: Location, notiRadius: Double) {
        viewModelScope.launch {
            fetchPickInAreaUseCase(location.latitude, location.longitude, notiRadius)
                .onSuccess {
                    _nearPicks.emit(it)
                }.onFailure {
                    _nearPicks.emit(emptyList())
                }
        }
    }
}
