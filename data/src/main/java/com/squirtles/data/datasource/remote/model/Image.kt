package com.squirtles.data.datasource.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    @SerialName("url") val url: String,
    @SerialName("height") val height: Int?,
    @SerialName("width") val width: Int?
)
