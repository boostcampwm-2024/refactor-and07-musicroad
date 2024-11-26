package com.squirtles.domain.datasource

import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.User

interface FirebaseRemoteDataSource {
    suspend fun createUser(): User?
    suspend fun fetchUser(userId: String): User?

    suspend fun fetchPick(pickID: String): Pick?
    suspend fun fetchPicksInArea(lat: Double, lng: Double, radiusInM: Double): List<Pick>
    suspend fun createPick(pick: Pick): String
    suspend fun deletePick(pick: Pick): Boolean
//    suspend fun updatePick(pick: Pick)
}
