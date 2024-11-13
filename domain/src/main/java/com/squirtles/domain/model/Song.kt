package com.squirtles.domain.model

/**
 * 애플뮤직에서 불러온 노래 정보를 비즈니스 로직에서 사용하기 위해 변환한 클래스
 */
data class Song(
    val id: String,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String,
    val genreNames: List<String>,
    val bgColor: String,
    val externalUrl: String,
    val previewUrl: String,
)

