package com.squirtles.data.datasource.remote.applemusic

import com.squirtles.data.datasource.remote.applemusic.api.AppleMusicApi
import com.squirtles.data.mapper.toMusicVideo
import com.squirtles.data.mapper.toSong
import com.squirtles.domain.datasource.AppleMusicRemoteDataSource
import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song
import retrofit2.Response
import javax.inject.Inject

class AppleMusicDataSourceImpl @Inject constructor(
    private val appleMusicApi: AppleMusicApi
) : AppleMusicRemoteDataSource {

    /**
     * Apple Music API Search
     */
    override suspend fun searchSongs(searchText: String): List<Song> {
        val queryMap = mapOf(
            "term" to searchText.replace(" ", "+"),
            "types" to "songs",
            "limit" to "10",
            "offset" to "0"
        )

        val searchResult = checkResponse(
            appleMusicApi.searchSongs(
                storefront = DEFAULT_STOREFRONT,
                queryMap = queryMap
            )
        )

        return searchResult.results.songs?.data?.map {
            it.toSong()
        } ?: emptyList()
    }

    override suspend fun searchSongById(songId: String): Song {
        TODO("Not yet implemented")
    }

    override suspend fun searchMusicVideoById(songId: String): List<MusicVideo> {
        val musicVideoResult = checkResponse(
            appleMusicApi.searchMusicVideo(DEFAULT_STOREFRONT, songId)
        )

        return musicVideoResult.data.map {
            it.toMusicVideo()
        }
    }

    private fun <T> checkResponse(response: Response<T>): T {
        if (response.isSuccessful) {
            return requireNotNull(response.body())
        } else {
            val errorBody = requireNotNull(response.errorBody()?.string())
            throw Exception(errorBody)
        }
    }

    companion object {
        const val DEFAULT_STOREFRONT = "kr"
    }
}
