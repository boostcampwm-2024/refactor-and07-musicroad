package com.squirtles.data.datasource.local

import android.location.Location
import com.squirtles.domain.datasource.LocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocalDataSourceImpl : LocalDataSource {
    
    private var _userId: String? = null
    private var _currentLocation: MutableStateFlow<Location?> = MutableStateFlow(null)

    override val userId: String?
        get() = _userId

    override val currentLocation: StateFlow<Location?>
        get() = _currentLocation

    override fun saveUserId(userId: String) {
        _userId = userId
    }

    override suspend fun saveCurrentLocation(location: Location) {
        _currentLocation.emit(location)
    }
}
