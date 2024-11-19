package com.squirtles.musicroad.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickState(
    val previous: Pick?,
    val current: Pick?
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {
    private val _pickMarkers = MutableStateFlow<Map<Pick, Marker>>(emptyMap())
    val pickMarkers = _pickMarkers.asStateFlow()

    private val _curLocation = MutableStateFlow<Location?>(null)
    val curLocation = _curLocation.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    private val _selectedPickState = MutableStateFlow(PickState(null, null))
    val selectedPickState = _selectedPickState.asStateFlow()

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
                    newMap[pick] = _pickMarkers.value[pick] ?: Marker()
                }
                _pickMarkers.value = newMap
            }
            picks.onFailure {
                // TODO
            }

            Log.d("MapViewModel", picks.toString())
        }
    }

    fun setSelectedPickState(pick: Pick) {
        viewModelScope.launch {
            val oldSelectedPick = selectedPickState.value.current
            if (oldSelectedPick == pick) return@launch

            _selectedPickState.emit(PickState(oldSelectedPick, pick))
        }
    }

    fun resetSelectedPickState() {
        viewModelScope.launch {
            val oldSelectedPick = selectedPickState.value.current
            _selectedPickState.emit(PickState(oldSelectedPick, null))
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
