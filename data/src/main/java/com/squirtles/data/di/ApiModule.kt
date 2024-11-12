package com.squirtles.data.di

import com.squirtles.data.BuildConfig
import com.squirtles.data.datasource.remote.applemusic.api.AppleMusicApi
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
    private const val BASE_APPLE_MUSIC_URL = "https://api.music.apple.com/"

    @Provides
    @Singleton
    fun provideAppleOkhttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.APPLE_MUSIC_API_TOKEN}")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideConverterFactory(
        json: Json,
    ): Converter.Factory {
        return json.asConverterFactory("application/json".toMediaType())
    }

    @Provides
    @Singleton
    fun provideAppleMusicApi(
        appleOkHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): AppleMusicApi {
        return Retrofit.Builder()
            .baseUrl(BASE_APPLE_MUSIC_URL)
            .addConverterFactory(converterFactory)
            .client(appleOkHttpClient)
            .build()
            .create(AppleMusicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
