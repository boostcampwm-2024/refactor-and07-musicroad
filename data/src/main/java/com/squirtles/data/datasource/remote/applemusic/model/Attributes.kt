package com.squirtles.data.datasource.remote.applemusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attributes(
    @SerialName("name") val songName: String,
    @SerialName("artistName") val artistName: String,
    @SerialName("albumName") val albumName: String? = null,
    @SerialName("releaseDate") val releaseDate: String,
    @SerialName("genreNames") val genreNames: List<String>,
    @SerialName("artwork") val artwork: Artwork,
    @SerialName("url") val externalUrl: String,
    @SerialName("previews") val previews: List<Preview>
)
