package com.squirtles.data.datasource.remote.applemusic.api

import com.example.spotifyapitest.data.MusicVideoResponse
import com.example.spotifyapitest.data.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AppleMusicApi {
    @GET("v1/catalog/{storefront}/search")
    suspend fun searchSongs(
        @Path("storefront") storefront: String,
        @QueryMap queryMap: Map<String, String>
    ): Response<SearchResponse>

    @GET("v1/catalog/{storefront}/songs/{id}/music-videos")
    suspend fun searchMusicVideo(
        @Path("storefront") storefront: String,
        @Path("id") id: String,
    ): Response<MusicVideoResponse>
}
