package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Songs(
    @SerialName("next") val next: String? = null,
    @SerialName("data") val data: List<Data>,
)
