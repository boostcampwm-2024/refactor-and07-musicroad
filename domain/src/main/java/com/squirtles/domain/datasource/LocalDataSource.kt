package com.squirtles.domain.datasource

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocalDataSource {
    val userId: String?
    val currentLocation: StateFlow<Location?>
    fun saveUserId(userId: String)
    suspend fun saveCurrentLocation(location: Location)
}
