package com.squirtles.musicroad.pick

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Creator
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.DeletePickUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import com.squirtles.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickViewModel @Inject constructor(
    private val fetchPickUseCase: FetchPickUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val deletePickUseCase: DeletePickUseCase
) : ViewModel() {

    private val _detailPickUiState = MutableStateFlow<DetailPickUiState>(DetailPickUiState.Loading)
    val detailPickUiState = _detailPickUiState.asStateFlow()

    fun getUserId() = getCurrentUserUseCase().userId

    fun fetchPick(pickId: String) {
        viewModelScope.launch {
            fetchPickUseCase(pickId)
                .onSuccess {
                    _detailPickUiState.emit(DetailPickUiState.Success(it))
                }
                .onFailure {
                    _detailPickUiState.emit(DetailPickUiState.Error)
                }
        }
    }

    fun deletePick(pickId: String) {
        viewModelScope.launch {
            _detailPickUiState.emit(DetailPickUiState.Loading)
            deletePickUseCase(pickId)
                .onSuccess {
                    _detailPickUiState.emit(DetailPickUiState.Deleted)
                }
                .onFailure {
                    _detailPickUiState.emit(DetailPickUiState.Error)
                }
        }
    }

    companion object {
        val DEFAULT_PICK =
            Pick(
                id = "",
                song = Song(
                    id = "",
                    songName = "",
                    artistName = "",
                    albumName = "",
                    imageUrl = "",
                    genreNames = listOf(),
                    bgColor = "#000000".toColorInt(),
                    externalUrl = "",
                    previewUrl = ""
                ),
                comment = "",
                createdAt = "",
                createdBy = Creator(userId = "", userName = "짱구"),
                favoriteCount = 0,
                location = LocationPoint(1.0, 1.0),
                musicVideoUrl = "",
            )
    }
}
