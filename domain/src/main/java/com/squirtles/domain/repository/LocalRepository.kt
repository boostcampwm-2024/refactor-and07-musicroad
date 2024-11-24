package com.squirtles.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocalRepository {
    val userId: Flow<String?>
    val lastLocation: StateFlow<Location?>
    suspend fun saveUserId(userId: String)
    suspend fun saveCurrentLocation(location: Location)
}
