package com.squirtles.musicroad

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.FetchPickInAreaUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fetchPickUseCase: FetchPickUseCase,
    private val fetchPickInAreaUseCase: FetchPickInAreaUseCase
) : ViewModel() {

    // 1PJY507YTSR8vlX7VH5w
    fun fetchPick(pickId: String){
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

    fun fetchPickInArea(lat: Double, lng: Double, radiusInM: Double){
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
}