package com.squirtles.musicroad.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {
    private val _pickMap = MutableStateFlow<Map<Pick, Marker>>(emptyMap())
    val pickMap = _pickMap.asStateFlow()

    private val _curLocation = MutableStateFlow<Location?>(null)
    val curLocation = _curLocation.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    private val _selectedPick = MutableStateFlow<Pick?>(null)
    val selectedPick = _selectedPick.asStateFlow()

    private val _selectedMarker = MutableStateFlow<Marker?>(null)
    val selectedMarker = _selectedMarker.asStateFlow()

    fun updateCurLocation(location: Location) {
        viewModelScope.launch {
            _curLocation.value = location
        }
    }

    fun fetchPick(pickId: String) {
        viewModelScope.launch {
            val pick = fetchPickUseCase(pickId)
            pick.onSuccess {
                // TODO: UiState 등 사용할 수 도?
            }
            pick.onFailure {
                // TODO
            }

            Log.d("MapViewModel", pick.toString())
        }
    }

    fun fetchPickInArea(lat: Double, lng: Double, radiusInM: Double) {
        viewModelScope.launch {
            val picks = fetchPickInAreaUseCase(lat, lng, radiusInM)

            picks.onSuccess {
                val newMap = mutableMapOf<Pick, Marker>()
                it.forEach { pick ->
                    if (_pickMap.value.contains(pick)) {
                        newMap[pick] = _pickMap.value[pick]!!
                    } else {
                        newMap[pick] = Marker()
                    }
                }
                _pickMap.value = newMap
            }
            picks.onFailure {
                // TODO
            }

            Log.d("MapViewModel", picks.toString())
        }
    }

    fun setSelectedMarkerAndPick(marker: Marker, pick: Pick) {
        _selectedMarker.value = marker
        _selectedPick.value = pick
    }

    fun resetSelectedMarkerAndPick() {
        _selectedMarker.value = null
        _selectedPick.value = null
    }

    fun setMapToMarker(map: NaverMap) {
        _pickMap.value.forEach { (_, marker) ->
            marker.map = map
        }
        _pickMap.value = _pickMap.value
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
