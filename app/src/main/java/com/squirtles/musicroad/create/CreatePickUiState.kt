package com.squirtles.musicroad.create

sealed class SearchUiState<out T> {
    data object Init : SearchUiState<Nothing>()
    data class Loading<T>(val prevData: T?) : SearchUiState<T>()
    data class Success<T>(val data: T) : SearchUiState<T>()
    data object Error : SearchUiState<Nothing>()
}

sealed class CreateUiState<out T> {
    data object Default: CreateUiState<Nothing>()
    data class Success<T>(val data: T) : CreateUiState<T>()
    data object Error : CreateUiState<Nothing>()
}
