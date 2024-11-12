package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("results") val results: Results,
)
