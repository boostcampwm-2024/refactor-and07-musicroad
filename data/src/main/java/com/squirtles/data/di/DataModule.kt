package com.squirtles.data.di

import com.squirtles.data.datasource.remote.api.TempApi
import com.squirtles.data.repository.TempRepositoryImpl
import com.squirtles.domain.repository.TempRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Singleton
    fun provideTempRepository(tempApi: TempApi): TempRepository =
        TempRepositoryImpl(tempApi)
}