package com.squirtles.data.repository

import com.squirtles.data.datasource.remote.api.TempApi
import com.squirtles.data.mapper.toTemp
import com.squirtles.domain.model.Temp
import com.squirtles.domain.repository.TempRepository
import javax.inject.Inject

internal class TempRepositoryImpl @Inject constructor(
    private val tempApi: TempApi
) : TempRepository {
    override suspend fun getTemp(): Temp {
        return tempApi.getTemp().toTemp()
    }
}