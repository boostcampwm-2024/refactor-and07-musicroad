package com.squirtles.data.datasource.local

import com.squirtles.domain.datasource.LocalDataSource
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick

class LocalDataSourceImpl : LocalDataSource {
    private var _userId: String? = null
    private var _selectedPick: Pick? = null
    private var _currentLocation: LocationPoint? = null

    override val selectedPick: Pick?
        get() = _selectedPick

    override val userId: String?
        get() = _userId

    override val currentLocation: LocationPoint?
        get() = _currentLocation

    override fun saveUserId(userId: String) {
        _userId = userId
    }

    override fun saveSelectedPick(pick: Pick) {
        _selectedPick = pick
    }

    override fun saveCurrentLocation(x: Double, y: Double) {
        _currentLocation = LocationPoint(x, y)
    }
}
