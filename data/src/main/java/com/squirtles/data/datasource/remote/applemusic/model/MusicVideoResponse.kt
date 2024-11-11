package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.Serializable

@Serializable
data class MusicVideoResponse(
    val data: List<Data>,
)

