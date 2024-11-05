package com.squirtles.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyResponse(
    val tracks: Tracks? = null,
)
