package com.squirtles.data.di

import com.squirtles.data.datasource.remote.api.SpotifyApi
import com.squirtles.data.datasource.remote.api.TokenApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {

    private const val BASE_URL = ""
    private const val BASE_SPOTIFY_URL = "https://api.spotify.com/"
    private const val BASE_TOKEN_URL = "https://accounts.spotify.com/"

    @Provides
    @Singleton
    fun provideOkhttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideConverterFactory(
        json: Json,
    ): Converter.Factory {
        return json.asConverterFactory("application/json".toMediaType())
    }

    @Provides
    @Singleton
    fun provideTokenApi(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): TokenApi {
        return Retrofit.Builder()
            .baseUrl(BASE_TOKEN_URL)
            .addConverterFactory(converterFactory)
            .client(okHttpClient).build()
            .create(TokenApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyApi(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): SpotifyApi {
        return Retrofit.Builder()
            .baseUrl(BASE_SPOTIFY_URL)
            .addConverterFactory(converterFactory)
            .client(okHttpClient).build()
            .create(SpotifyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}