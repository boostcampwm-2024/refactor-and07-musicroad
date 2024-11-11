package com.example.spotifyapitest.data

import kotlinx.serialization.Serializable

@Serializable
data class Artwork(
    val width: Int,
    val height: Int,
    val url: String,
)
