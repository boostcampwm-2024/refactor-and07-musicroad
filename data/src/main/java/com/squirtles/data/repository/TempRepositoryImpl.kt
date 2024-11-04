package com.squirtles.data.repository

import com.squirtles.domain.datasource.TempRemoteDataSource
import com.squirtles.domain.model.Temp
import com.squirtles.domain.repository.TempRepository
import javax.inject.Inject

internal class TempRepositoryImpl @Inject constructor(
    private val tempRemoteDataSource: TempRemoteDataSource
) : TempRepository {
    override suspend fun getTemp(): Temp {
        return tempRemoteDataSource.getTemp()
    }
}