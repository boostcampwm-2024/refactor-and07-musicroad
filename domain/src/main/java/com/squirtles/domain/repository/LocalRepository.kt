package com.squirtles.domain.repository

import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    val userId: String
    val selectedPick: Flow<Pick?>
    val currentLocation: Flow<LocationPoint?>

    suspend fun saveUserId(userId: String)
    suspend fun saveSelectedPick(pick: Pick)
    suspend fun saveCurrentLocation(x: Double, y: Double)
}
