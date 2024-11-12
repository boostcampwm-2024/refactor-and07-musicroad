package com.squirtles.domain.model

data class MusicVideo(
    val id: String,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val releaseDate: String,
    val previewUrl: String,
)
