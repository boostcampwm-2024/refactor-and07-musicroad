package com.example.spotifyapitest.data

import kotlinx.serialization.Serializable

@Serializable
data class MusicVideoResponse(
    val data: List<Data>,
)

