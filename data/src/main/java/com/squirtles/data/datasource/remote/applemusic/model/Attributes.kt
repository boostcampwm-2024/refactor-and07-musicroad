package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attributes(
    @SerialName("name") val songName: String,
    val artistName: String,
    val albumName: String? = null,
    val releaseDate: String,
    val genreNames: List<String>,
    val artwork: Artwork,
    @SerialName("url") val externalUrl: String,
    val previews: List<Preview>
)
