package com.squirtles.musicroad.picklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Order
import com.squirtles.domain.model.Pick
import com.squirtles.domain.usecase.favoritepick.FetchFavoritePicksUseCase
import com.squirtles.domain.usecase.local.GetFavoriteListOrderUseCase
import com.squirtles.domain.usecase.local.GetMyListOrderUseCase
import com.squirtles.domain.usecase.local.SaveFavoriteListOrderUseCase
import com.squirtles.domain.usecase.local.SaveMyListOrderUseCase
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
    private val getFavoriteListOrderUseCase: GetFavoriteListOrderUseCase,
    private val getMyListOrderUseCase: GetMyListOrderUseCase,
    private val saveFavoriteListOrderUseCase: SaveFavoriteListOrderUseCase,
    private val saveMyListOrderUseCase: SaveMyListOrderUseCase,
) : ViewModel() {

    private var defaultList: List<Pick>? = null

    private val _pickListUiState = MutableStateFlow<PickListUiState>(PickListUiState.Loading)
    val pickListUiState = _pickListUiState.asStateFlow()

    fun fetchFavoritePicks(userId: String) {
        viewModelScope.launch {
            fetchFavoritePicksUseCase(userId)
                .onSuccess { favoritePicks ->
                    defaultList = favoritePicks
                    setList(getFavoriteListOrderUseCase())
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
                    setList(getMyListOrderUseCase())
                }
                .onFailure {
                    _pickListUiState.emit(PickListUiState.Error)
                }
        }
    }

    fun setListOrder(isFavoritePicks: Boolean, order: Order) {
        viewModelScope.launch {
            if (isFavoritePicks) {
                saveFavoriteListOrderUseCase(order)
            } else {
                saveMyListOrderUseCase(order)
            }
            setList(order)
        }
    }

    private fun setList(order: Order) {
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
