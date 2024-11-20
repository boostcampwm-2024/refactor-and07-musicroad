package com.squirtles.domain.datasource

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocalDataSource {
    val userId: String?
    val lastLocation: StateFlow<Location?>
    fun saveUserId(userId: String)
    suspend fun saveCurrentLocation(location: Location)
}
