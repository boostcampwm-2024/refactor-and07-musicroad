package com.example.spotifyapitest.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Songs (
    @SerialName("next") val next: String? = null,
    @SerialName("data") val data: List<Data>,
)
