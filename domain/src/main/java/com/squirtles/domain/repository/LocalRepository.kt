package com.squirtles.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocalRepository {
    val userId: String?
    val currentLocation: StateFlow<Location?>
    suspend fun saveUserId(userId: String)
    suspend fun saveCurrentLocation(location: Location)
}
