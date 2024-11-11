package com.example.spotifyapitest.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("id") val id: String,
    @SerialName("attributes") val attributes: Attributes,
)
