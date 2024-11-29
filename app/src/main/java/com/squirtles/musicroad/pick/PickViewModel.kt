package com.squirtles.musicroad.pick

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Creator
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.CreateFavoriteUseCase
import com.squirtles.domain.usecase.DeleteFavoriteUseCase
import com.squirtles.domain.usecase.DeletePickUseCase
import com.squirtles.domain.usecase.FetchIsFavoriteUseCase
import com.squirtles.domain.usecase.FetchPickUseCase
import com.squirtles.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickViewModel @Inject constructor(
    private val fetchPickUseCase: FetchPickUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val deletePickUseCase: DeletePickUseCase,
    private val fetchIsFavoriteUseCase: FetchIsFavoriteUseCase,
    private val createFavoriteUseCase: CreateFavoriteUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
) : ViewModel() {

    private val _detailPickUiState = MutableStateFlow<DetailPickUiState>(DetailPickUiState.Loading)
    val detailPickUiState = _detailPickUiState.asStateFlow()

    fun getUserId() = getCurrentUserUseCase().userId

    fun fetchPick(pickId: String) {
        viewModelScope.launch {
            val fetchPick = async {
                fetchPickUseCase(pickId)
            }
            val fetchIsFavorite = async {
                fetchIsFavoriteUseCase(pickId, getUserId())
            }

            val fetchPickResult = fetchPick.await()
            val fetchIsFavoriteResult = fetchIsFavorite.await()

            when {
                fetchPickResult.isSuccess && fetchIsFavoriteResult.isSuccess -> {
                    _detailPickUiState.emit(
                        DetailPickUiState.Success(
                            pick = fetchPickResult.getOrDefault(DEFAULT_PICK),
                            isFavorite = fetchIsFavoriteResult.getOrDefault(false)
                        )
                    )
                }

                else -> {
                    _detailPickUiState.emit(DetailPickUiState.Error)
                }
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

    fun addToFavorite(pickId: String, onAddToFavoriteSuccess: () -> Unit) {
        viewModelScope.launch {
            createFavoriteUseCase(pickId, getUserId())
                .onSuccess {
                    onAddToFavoriteSuccess()
                    val currentUiState = _detailPickUiState.value as? DetailPickUiState.Success
                    currentUiState?.let { successState ->
                        _detailPickUiState.emit(successState.copy(isFavorite = true))
                    }
                }
                .onFailure {
                    _detailPickUiState.emit(DetailPickUiState.Error)
                }
        }
    }

    fun deleteAtFavorite(pickId: String, onDeleteAtFavoriteSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteFavoriteUseCase(pickId, getUserId())
                .onSuccess {
                    onDeleteAtFavoriteSuccess()
                    val currentUiState = _detailPickUiState.value as? DetailPickUiState.Success
                    currentUiState?.let { successState ->
                        _detailPickUiState.emit(successState.copy(isFavorite = false))
                    }
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
