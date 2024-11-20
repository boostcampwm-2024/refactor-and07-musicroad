package com.squirtles.data.repository

import android.location.Location
import com.squirtles.domain.datasource.LocalDataSource
import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : LocalRepository {

    override val userId get() = localDataSource.userId
    override val lastLocation get() = localDataSource.lastLocation

    override suspend fun saveUserId(userId: String) {
        localDataSource.saveUserId(userId)
    }

    override suspend fun saveCurrentLocation(location: Location) {
        localDataSource.saveCurrentLocation(location)
    }
}
