package com.squirtles.domain.model

data class PlayerUiState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val hasNext: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 30_000L,
    val bufferPercentage: Int = 0,
)
