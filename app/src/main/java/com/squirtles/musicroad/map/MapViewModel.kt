package com.squirtles.musicroad.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.FetchLocationUseCase
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import com.squirtles.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickState(
    val previous: String,
    val current: String
)

@HiltViewModel
class MapViewModel @Inject constructor(
    fetchLocationUseCase: FetchLocationUseCase,
    private val saveLocationUseCase: SaveLocationUseCase,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {

    private val _pickMarkers = MutableStateFlow<Map<String, MusicRoadMarker>>(emptyMap())
    val pickMarkers = _pickMarkers.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    private val _selectedPickState = MutableStateFlow(PickState("", ""))
    val selectedPickState = _selectedPickState.asStateFlow()

    // FIXME : 네이버맵의 LocationChangeListener에서 실시간으로 변하는 위치 정보
    // 등록 버튼 눌렀을때 해당 시점에서의 위치정보를 LocalDataSource에 저장하기 위함 -> 더 나은 방법이 있으면 고쳐주세요
    private var _realTimeLocation: Location? = null
    val realTimeLocation get() = _realTimeLocation

    // LocalDataSource에 저장되는 위치 정보
    // Firestore 데이터 쿼리 작업 최소화 및 위치데이터 공유 용도
    val curLocation: StateFlow<Location> = fetchLocationUseCase()
        .map { it ?: DEFAULT_LOCATION } // 기본값을 설정
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_LOCATION
        )

    fun updateCurLocation(location: Location) {
        _realTimeLocation = location

        if (curLocation.value == DEFAULT_LOCATION) {
            saveCurLocation(location)
        } else {
            if (calculateDistance(location, curLocation.value) > 5.0) {
                saveCurLocation(location)
            }
        }
    }

    private fun saveCurLocation(location: Location) {
        viewModelScope.launch {
            saveLocationUseCase(location)
        }
    }

    fun onCenterButtonClick() {
        if (_realTimeLocation == null) return

        viewModelScope.launch {
            saveCurLocation(_realTimeLocation!!)
        }
    }

    /* 미터 단위 거리 계산 */
    fun calculateDistance(location1: Location, location2: Location): Double =
        location1.distanceTo(location2).toDouble()

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

    companion object {
        val DEFAULT_LOCATION = Location("default").apply { latitude = 0.0; longitude = 0.0 }
    }
}
