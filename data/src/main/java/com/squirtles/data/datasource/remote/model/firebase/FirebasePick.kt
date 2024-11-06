package com.squirtles.data.datasource.remote.model.firebase

import com.google.firebase.firestore.GeoPoint
import java.time.LocalDateTime

/**
 * Firestore에 저장된 pick document를 불러와 변환하기위한 데이터 클래스
 */
data class FirebasePick(
    val id: String,
    val albumTitle: String,
    val artists: List<String>,
    val songTitle: String,
    val geoHash: String, // TODO: Firestore 데이터에도 추가해야함
    val location: GeoPoint,
    val comment: String,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val favoriteCount: Int = 0,
    val imageUrl: String,
    val previewUrl: String? = null,
    val externalUrl: String,
)
