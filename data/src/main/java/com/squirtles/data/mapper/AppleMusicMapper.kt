package com.squirtles.data.mapper

import androidx.core.graphics.toColorInt
import com.squirtles.data.datasource.remote.applemusic.model.Data
import com.squirtles.domain.model.Song
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Data.toSong(): Song = Song(
    id = id,
    songName = this.attributes.songName,
    artistName = this.attributes.artistName,
    albumName = this.attributes.albumName.toString(),
    artwork = this.attributes.artwork.url,
    releaseDate = this.attributes.releaseDate.toLocalDate(),
    genreNames = this.attributes.genreNames,
    bgColor = "#${this.attributes.artwork.bgColor}".toColorInt(),
    externalUrl = this.attributes.externalUrl,
    previewUrl = this.attributes.previews[0].url.toString(),
)

private fun String.toLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, formatter)
}

