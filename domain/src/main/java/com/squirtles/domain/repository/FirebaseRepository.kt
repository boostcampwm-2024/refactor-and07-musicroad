package com.squirtles.domain.repository

import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.User

interface FirebaseRepository {
    suspend fun createUser(): Result<User>
    suspend fun fetchUser(userId: String): Result<User>

    suspend fun fetchPick(pickID: String): Result<Pick>
    suspend fun fetchPicksInArea(lat: Double, lng: Double, radiusInM: Double): Result<List<Pick>>
    suspend fun createPick(pick: Pick): Result<String>
    suspend fun deletePick(pick: Pick): Result<Boolean>
}
