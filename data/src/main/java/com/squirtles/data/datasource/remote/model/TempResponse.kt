package com.squirtles.data.datasource.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TempResponse(
    @SerialName("content") val content: String
)