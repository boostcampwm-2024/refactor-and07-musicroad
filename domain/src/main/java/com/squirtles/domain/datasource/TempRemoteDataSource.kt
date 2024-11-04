package com.squirtles.domain.datasource

import com.squirtles.domain.model.Temp

interface TempRemoteDataSource {
    suspend fun getTemp(): Temp
}