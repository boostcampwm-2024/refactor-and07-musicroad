package com.squirtles.data.mapper

import com.squirtles.data.datasource.remote.model.firebase.FirebasePick
import com.squirtles.domain.model.GeoPoint
import com.squirtles.domain.model.Pick

internal fun FirebasePick.toPick(): Pick = Pick(
    id = id,
    albumTitle = albumTitle,
    artists = artists,
    songTitle = songTitle,
    location = GeoPoint(1.0, 1.0), // TODO: geohash -> GeoPoint 변환 정의 후 변경
    comment = comment,
    createdAt = createdAt,
    createdBy = createdBy,
    favoriteCount = favoriteCount,
    imageUrl = imageUrl,
    previewUrl = previewUrl,
    externalUrl = externalUrl,
)