package com.squirtles.domain.repository

import android.location.Location
import com.squirtles.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocalRepository {
    val userId: Flow<String?> // 기기에 저장된 userId
    val currentUser: User
    val lastLocation: StateFlow<Location?>

    suspend fun saveUserId(userId: String)
    suspend fun saveCurrentUser(user: User)
    suspend fun saveCurrentLocation(location: Location)
}
