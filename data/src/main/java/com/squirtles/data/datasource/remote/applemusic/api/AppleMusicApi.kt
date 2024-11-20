package com.squirtles.data.datasource.remote.applemusic.api

import com.squirtles.data.datasource.remote.applemusic.model.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AppleMusicApi {
    @GET("v1/catalog/{storefront}/search")
    suspend fun searchSongs(
        @Path("storefront") storefront: String,
        @Query("types") types: String,
        @QueryMap queryMap: Map<String, String>
    ): Response<SearchResponse>
}
