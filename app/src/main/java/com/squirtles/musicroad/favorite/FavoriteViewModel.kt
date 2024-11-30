package com.squirtles.musicroad.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.GetCurrentUserUseCase
import com.squirtles.domain.usecase.GetFavoritePicksUseCase
import com.squirtles.musicroad.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getFavoritePicksUseCase: GetFavoritePicksUseCase,
) : ViewModel() {

    private val _favoriteUiState = MutableStateFlow<UiState<List<Pick>>>(UiState.Loading)
    val favoriteUiState = _favoriteUiState.asStateFlow()

    fun getFavoritePicks() {
        viewModelScope.launch {
            getFavoritePicksUseCase(getCurrentUserUseCase().userId)
                .onSuccess { favoritePicks ->
                    _favoriteUiState.emit(UiState.Success(favoritePicks))
                }
                .onFailure {
                    _favoriteUiState.emit(UiState.Error)
                }
        }
    }
}
