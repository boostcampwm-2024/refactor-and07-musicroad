package com.squirtles.data.mapper

import androidx.core.graphics.toColorInt
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.squirtles.data.datasource.remote.firebase.model.FirebasePick
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.PickLocation
import com.squirtles.domain.model.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * using when get pick from firebase and convert to domain data
 */
internal fun FirebasePick.toPick(): Pick = Pick(
    id = id.toString(),
    song = Song(
        id = songId.toString(),
        songName = songName.toString(),
        artistName = artistName.toString(),
        albumName = albumName.toString(),
        imageUrl = artwork?.get("url") ?: "",
        genreNames = genreNames,
        bgColor = artwork?.get("bgColor")?.let {
            "#${it}".toColorInt()
        } ?: "#000000".toColorInt(),
        externalUrl = externalUrl.toString(),
        previewUrl = previewUrl.toString(),
    ),
    comment = comment.toString(),
    createdAt = createdAt?.toDate()?.formatTimestamp() ?: "",
    createdBy = createdBy.toString(),
    location = PickLocation(
        latitude = location?.latitude ?: 0.0,
        longitude = location?.longitude ?: 0.0
    ),
    favoriteCount = favoriteCount,
)

/**
 * using when create pick in firebase
 */
internal fun Pick.toFirebasePick(): FirebasePick = FirebasePick(
    id = id,
    albumName = song.albumName,
    artistName = song.artistName,
    artwork = mapOf("url" to song.imageUrl, "bgColor" to song.bgColor.toRgbString()),
    comment = comment,
    createdBy = createdBy,
    externalUrl = song.externalUrl,
    favoriteCount = favoriteCount,
    genreNames = song.genreNames,
    geoHash = location.toGeoHash(),
    location = GeoPoint(location.latitude, location.longitude),
    previewUrl = song.previewUrl,
    musicVideoUrl = musicVideoUrl,
    songId = song.id,
    songName = song.songName,
)

private fun Int.toRgbString(): String {
    return String.format("%06X", 0xFFFFFF and this)
}

private fun PickLocation.toGeoHash(): String {
    val geoLocation = GeoLocation(this.latitude, this.longitude)
    return GeoFireUtils.getGeoHashForLocation(geoLocation)
}

private fun Date.formatTimestamp(): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    return dateFormat.format(this)
}

private fun String.toTimeStamp(): Timestamp {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    val date = dateFormat.parse(this) ?: Date()

    val time = FieldValue.serverTimestamp()
    return Timestamp(date)
}
