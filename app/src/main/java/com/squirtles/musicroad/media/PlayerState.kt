package com.squirtles.musicroad.media

data class PlayerState(
    val isReady: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
) {
    companion object {
        val PLAYER_STATE_INITIAL = PlayerState(isReady = false)
        val PLAYER_STATE_STOP = PlayerState(isReady = true, isPlaying = false)
    }
}
