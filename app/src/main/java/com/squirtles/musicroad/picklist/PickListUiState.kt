package com.squirtles.musicroad.picklist

import com.squirtles.domain.model.Pick

sealed class PickListUiState {
    data object Loading: PickListUiState()
    data class Success(val pickList: List<Pick>) : PickListUiState()
    data object Error : PickListUiState()
}
