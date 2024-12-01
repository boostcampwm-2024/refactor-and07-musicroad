package com.squirtles.musicroad.picklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.favoritepick.FetchFavoritePicksUseCase
import com.squirtles.domain.usecase.mypick.FetchMyPicksUseCase
import com.squirtles.domain.usecase.local.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickListViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val fetchFavoritePicksUseCase: FetchFavoritePicksUseCase,
    private val fetchMyPicksUseCase: FetchMyPicksUseCase,
) : ViewModel() {

    private val _pickListUiState = MutableStateFlow<PickListUiState>(PickListUiState.Loading)
    val pickListUiState = _pickListUiState.asStateFlow()

    fun fetchFavoritePicks() {
        viewModelScope.launch {
            fetchFavoritePicksUseCase(getCurrentUserUseCase().userId)
                .onSuccess { favoritePicks ->
                    _pickListUiState.emit(PickListUiState.Success(favoritePicks))
                }
                .onFailure {
                    _pickListUiState.emit(PickListUiState.Error)
                }
        }
    }

    fun fetchMyPicks() {
        viewModelScope.launch {
            fetchMyPicksUseCase(getCurrentUserUseCase().userId)
                .onSuccess { myPicks ->
                    _pickListUiState.emit(PickListUiState.Success(myPicks))
                }
                .onFailure {
                    _pickListUiState.emit(PickListUiState.Error)
                }
        }
    }
}
