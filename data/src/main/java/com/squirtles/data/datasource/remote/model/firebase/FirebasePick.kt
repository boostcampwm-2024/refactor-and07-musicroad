package com.squirtles.data.datasource.remote.model.firebase

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime

/**
 * Firestore에 저장된 pick document를 불러와 변환하기위한 데이터 클래스
 */
data class FirebasePick(
    @PropertyName("id") val id: String,
    @PropertyName("album_title") val albumTitle: String,
    @PropertyName("artists") val artists: List<String>,
    @PropertyName("song_title") val songTitle: String,
    @PropertyName("geo_hash") val geoHash: String, // TODO: Firestore 데이터에도 추가해야함
    @PropertyName("location") val location: GeoPoint,
    @PropertyName("comment") val comment: String,
    @PropertyName("created_at") val createdAt: LocalDateTime,
    @PropertyName("created_by") val createdBy: String,
    @PropertyName("favorite_count") val favoriteCount: Int = 0,
    @PropertyName("image_url") val imageUrl: String,
    @PropertyName("preview_url") val previewUrl: String? = null,
    @PropertyName("external_url") val externalUrl: String,
)
