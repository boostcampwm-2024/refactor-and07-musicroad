package com.squirtles.musicroad.pick

import com.squirtles.domain.model.Pick

sealed class DetailPickUiState {
    data object Loading : DetailPickUiState()
    data class Success(val pick: Pick) : DetailPickUiState()
    data object Deleted : DetailPickUiState()
    data object Error : DetailPickUiState()
}
