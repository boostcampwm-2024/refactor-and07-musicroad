package com.squirtles.domain.model

/**
 * 앱에서 사용하기 위한 Pick 정보 데이터클래스
 */
data class Pick(
    val id: String,
    val song: Song,
    val comment: String,
    val favoriteCount: Int = 0,
    val createdBy: String,
    val createdAt: String,
    val location: PickLocation,
    val musicVideoUrl: String = "",
)

data class PickLocation(
    val latitude: Double,
    val longitude: Double
) {
    /* TODO: Location 변환 함수 */
}
