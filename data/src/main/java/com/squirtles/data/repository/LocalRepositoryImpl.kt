package com.squirtles.data.repository

import com.squirtles.domain.datasource.LocalDataSource
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : LocalRepository {

    private val _userId = ""
    override val userId: String get() = _userId

    private val _selectedPick = MutableStateFlow<Pick?>(null)
    override val selectedPick: Flow<Pick?> get() = _selectedPick

    private val _currentLocation = MutableStateFlow(null)
    override val currentLocation: Flow<LocationPoint?> get() = _currentLocation

    override suspend fun saveUserId(userId: String) {
        localDataSource.saveUserId(userId)
    }

    override suspend fun saveSelectedPick(pick: Pick) {
        localDataSource.saveSelectedPick(pick)
    }

    override suspend fun saveCurrentLocation(x: Double, y: Double) {
        localDataSource.saveCurrentLocation(x, y)
    }
}
