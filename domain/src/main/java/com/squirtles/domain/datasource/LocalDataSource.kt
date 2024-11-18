package com.squirtles.domain.datasource

import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick

interface LocalDataSource {
    val userId: String?
    val selectedPick: Pick?
    val currentLocation: LocationPoint?
    fun saveUserId(userId: String)
    fun saveSelectedPick(pick: Pick)
    fun saveCurrentLocation(x: Double, y: Double)
}
