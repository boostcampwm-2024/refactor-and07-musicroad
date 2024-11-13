package com.squirtles.musicroad.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {
    private val _centerButtonClick = MutableSharedFlow<Boolean>()
    val centerButtonClick = _centerButtonClick.asSharedFlow()

    private val _curLocation = MutableStateFlow<Location?>(null)
    val curLocation = _curLocation.asStateFlow()

    private val _pickCount = MutableStateFlow(0)
    val pickCount = _pickCount.asStateFlow()

    fun createMarker() {
        viewModelScope.launch {
            _centerButtonClick.emit(true)
        }
    }

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
                // TODO
            }
            picks.onFailure {
                // TODO
            }

            Log.d("MapViewModel", picks.toString())
        }
    }

    fun requestPickNotificationArea(lat: Double, lng: Double, notiRadius: Double) {
        viewModelScope.launch {
            fetchPickInAreaUseCase(lat, lng, notiRadius)
                .onSuccess {
                    _pickCount.emit(it.count())
                }.onFailure {
                    _pickCount.emit(0)
                }
        }
    }
}
