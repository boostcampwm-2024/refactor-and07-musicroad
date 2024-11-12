package com.squirtles.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 앱에서 사용하기 위한 Pick 정보 데이터클래스
 */
data class Pick(
    val id: String,
    val albumTitle: String,
    val artists: List<String>,
    val songTitle: String,
    val location: GeoPoint,
    val comment: String,
    val createdAt: Long,
    val createdBy: String,
    val favoriteCount: Int = 0,
    val imageUrl: String,
    val previewUrl: String,
    val externalUrl: String,
) {
    val formattedDate: String
        get() = formatTimestamp(createdAt)

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
