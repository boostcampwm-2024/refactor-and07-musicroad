package com.squirtles.musicroad.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.FetchLastLocationUseCase
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.SaveLastLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
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

    private val _pickMarkers = MutableStateFlow<Map<String, MusicRoadMarker>>(emptyMap())
    val pickMarkers = _pickMarkers.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    private val _selectedPickState = MutableStateFlow(PickState("", ""))
    val selectedPickState = _selectedPickState.asStateFlow()

    // FIXME : 네이버맵의 LocationChangeListener에서 실시간으로 변하는 위치 정보 -> 더 나은 방법이 있으면 고쳐주세요
    private var _currentLocation: Location? = null
    val curLocation get() = _currentLocation

    // LocalDataSource에 저장되는 위치 정보
    // Firestore 데이터 쿼리 작업 최소화 및 위치데이터 공유 용도
    val lastLocation: StateFlow<Location?> = fetchLastLocationUseCase()

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
            viewModelScope.launch {
                saveCurLocation(location)
            }
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

            val distanceInMeters = from.distanceTo(location).toDouble()
            val distanceInKm = distanceInMeters / 1000.0

            String.format(Locale.getDefault(), "%.1f", distanceInKm).toDouble()
        } ?: -1.0
    }


    fun fetchPickInArea(lat: Double, lng: Double, radiusInM: Double) {
        viewModelScope.launch {
            val fetchPickResult = fetchPickInAreaUseCase(lat, lng, radiusInM)

            fetchPickResult.onSuccess { pickList ->
                val newMarkerMap = mutableMapOf<String, MusicRoadMarker>()
                pickList.forEach { pick ->
                    newMarkerMap[pick.id] =
                        _pickMarkers.value[pick.id] ?: MusicRoadMarker(pick = pick)
                }
                _pickMarkers.value = newMarkerMap
            }
            fetchPickResult.onFailure {
                // TODO
            }
        }
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
            _selectedPickState.emit(PickState(lastSelectedPick, ""))
        }
    }

    fun requestPickNotificationArea(location: Location, notiRadius: Double) {
        viewModelScope.launch {
            fetchPickInAreaUseCase(location.latitude, location.longitude, notiRadius)
                .onSuccess {
                    _pickCount.emit(it.count())
                }.onFailure {
                    _pickCount.emit(0)
                }
        }
    }
}
