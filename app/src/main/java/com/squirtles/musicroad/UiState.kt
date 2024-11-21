package com.squirtles.musicroad

sealed class UiState<out T> {
    data object Init: UiState<Nothing>()
    data class Loading<T>(val prevData: T?): UiState<T>()
    data class Success<T>(val data: T): UiState<T>()
    data object Error : UiState<Nothing>()
}