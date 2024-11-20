package com.squirtles.domain.repository

import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song

interface AppleMusicRepository {
    suspend fun searchSongs(searchText: String): Result<List<Song>>
    suspend fun searchSongById(songId: String): Result<Song>
    suspend fun searchMusicVideos(searchText: String): Result<List<MusicVideo>>
}
