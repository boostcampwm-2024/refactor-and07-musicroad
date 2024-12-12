package com.squirtles.musicroad.picklist

import com.squirtles.domain.model.Pick

enum class Order {
    LATEST,
    OLDEST,
    FAVORITE_DESC,
}

sealed class PickListUiState {
    data object Loading : PickListUiState()
    data class Success(val pickList: List<Pick>, val order: Order) : PickListUiState()
    data object Error : PickListUiState()
}
