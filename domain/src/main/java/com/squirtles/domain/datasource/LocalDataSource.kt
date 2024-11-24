package com.squirtles.domain.datasource

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocalDataSource {
    val lastLocation: StateFlow<Location?>

    fun readUserId(): Flow<String?>
    suspend fun saveUserId(userId: String)
    suspend fun saveCurrentLocation(location: Location)
}
