package com.squirtles.musicroad.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.FetchLocationUseCase
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import com.squirtles.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fetchLocationUseCase: FetchLocationUseCase,
    private val saveLocationUseCase: SaveLocationUseCase,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {

    private val _centerButtonClick = MutableSharedFlow<Boolean>()
    val centerButtonClick = _centerButtonClick.asSharedFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    val curLocation: StateFlow<Location> = fetchLocationUseCase()
        .map { it ?: DEFAULT_LOCATION } // 기본값을 설정
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_LOCATION
        )
    
    fun createMarker() {
        viewModelScope.launch {
            _centerButtonClick.emit(true)
        }
    }

    fun updateCurLocation(location: Location) {
        val curLocationPoint = curLocation.value

        if (curLocationPoint.latitude == 0.0 && curLocationPoint.longitude == 0.0) {
            saveCurLocation(location)
        } else {
            if (calculateDistance(location, curLocationPoint) > 5.0) {
                saveCurLocation(location)
            }
        }
    }

    private fun saveCurLocation(location: Location) {
        viewModelScope.launch {
            saveLocationUseCase(location)
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

            Log.d("MapViewModel", pick.toString())
        }
    }

    fun fetchPickInArea(lat: Double, lng: Double, radiusInM: Double) {
        viewModelScope.launch {
            val picks = fetchPickInAreaUseCase(lat, lng, radiusInM)

            picks.onSuccess {
                // TODO
            }
            picks.onFailure {
                // TODO
            }

            Log.d("MapViewModel", picks.toString())
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
        val DEFAULT_LOCATION = Location("manual").apply { latitude = 0.0; longitude = 0.0 }
    }
}
