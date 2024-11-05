package com.squirtles.data.datasource.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalUrls(
    @SerialName("spotify") val spotifyUrl: String
)
