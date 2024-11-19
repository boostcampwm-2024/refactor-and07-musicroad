package com.squirtles.musicroad.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.squirtles.domain.model.Pick
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
    val previous: Pick?,
    val current: Pick?
)

@HiltViewModel
class MapViewModel @Inject constructor(
    fetchLocationUseCase: FetchLocationUseCase,
    private val saveLocationUseCase: SaveLocationUseCase,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {
    private val _pickMarkers = MutableStateFlow<Map<Pick, Marker>>(emptyMap())
    val pickMarkers = _pickMarkers.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    private val _selectedPickState = MutableStateFlow(PickState(null, null))
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

    fun fetchPick(pickId: String) {
        viewModelScope.launch {
            val pick = fetchPickUseCase(pickId)
            pick.onSuccess {
                // TODO: UiState 등 사용할 수 도?
            }
            pick.onFailure {
                // TODO
            }
        }
    }

    fun fetchPickInArea(lat: Double, lng: Double, radiusInM: Double) {
        viewModelScope.launch {
            val picks = fetchPickInAreaUseCase(lat, lng, radiusInM)

            picks.onSuccess {
                val newMap = mutableMapOf<Pick, Marker>()
                it.forEach { pick ->
                    newMap[pick] = _pickMarkers.value[pick] ?: Marker()
                }
                _pickMarkers.value = newMap
            }
            picks.onFailure {
                // TODO
            }
        }
    }

    fun setSelectedPickState(pick: Pick) {
        viewModelScope.launch {
            val lastSelectedPick = selectedPickState.value.current
            if (lastSelectedPick == pick) return@launch

            _selectedPickState.emit(PickState(lastSelectedPick, pick))
        }
    }

    fun resetSelectedPickState() {
        viewModelScope.launch {
            val lastSelectedPick = selectedPickState.value.current
            _selectedPickState.emit(PickState(lastSelectedPick, null))
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
