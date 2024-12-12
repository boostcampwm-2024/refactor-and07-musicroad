package com.squirtles.musicroad.picklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.favoritepick.FetchFavoritePicksUseCase
import com.squirtles.domain.usecase.mypick.FetchMyPicksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickListViewModel @Inject constructor(
    private val fetchFavoritePicksUseCase: FetchFavoritePicksUseCase,
    private val fetchMyPicksUseCase: FetchMyPicksUseCase,
) : ViewModel() {

    private var defaultList: List<Pick>? = null

    private val _pickListUiState = MutableStateFlow<PickListUiState>(PickListUiState.Loading)
    val pickListUiState = _pickListUiState.asStateFlow()

    fun fetchFavoritePicks(userId: String) {
        viewModelScope.launch {
            fetchFavoritePicksUseCase(userId)
                .onSuccess { favoritePicks ->
                    defaultList = favoritePicks
                    val order = (_pickListUiState.value as? PickListUiState.Success)?.order ?: Order.LATEST
                    setListOrder(order)
                }
                .onFailure {
                    _pickListUiState.emit(PickListUiState.Error)
                }
        }
    }

    fun fetchMyPicks(userId: String) {
        viewModelScope.launch {
            fetchMyPicksUseCase(userId)
                .onSuccess { myPicks ->
                    defaultList = myPicks
                    val order = (_pickListUiState.value as? PickListUiState.Success)?.order ?: Order.LATEST
                    setListOrder(order)
                }
                .onFailure {
                    _pickListUiState.emit(PickListUiState.Error)
                }
        }
    }

    fun setListOrder(order: Order) {
        defaultList?.let { pickList ->
            when (order) {
                Order.LATEST ->
                    _pickListUiState.value = PickListUiState.Success(
                        pickList = pickList,
                        order = order
                    )

                Order.OLDEST ->
                    _pickListUiState.value = PickListUiState.Success(
                        pickList = pickList.reversed(),
                        order = order
                    )

                Order.FAVORITE_DESC ->
                    _pickListUiState.value = PickListUiState.Success(
                        pickList = pickList.sortedByDescending { it.favoriteCount },
                        order = order
                    )
            }
        }
    }
}
