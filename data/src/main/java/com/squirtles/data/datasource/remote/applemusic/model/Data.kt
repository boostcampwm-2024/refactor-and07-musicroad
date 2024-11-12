package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("id") val id: String,
    @SerialName("attributes") val attributes: Attributes,
)
