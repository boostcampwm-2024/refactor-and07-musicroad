package com.squirtles.domain.datasource

import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song

interface AppleMusicRemoteDataSource {
    suspend fun searchSongs(searchText: String): List<Song>
    suspend fun searchSongById(songId: String): Song
    suspend fun searchMusicVideoById(songId: String): List<MusicVideo>
}
