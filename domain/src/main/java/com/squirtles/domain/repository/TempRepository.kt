package com.squirtles.domain.repository

import com.squirtles.domain.model.Temp

interface TempRepository {
    suspend fun getTemp(): Temp
}