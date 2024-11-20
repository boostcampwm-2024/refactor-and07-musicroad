package com.squirtles.domain.repository

import com.squirtles.domain.model.Pick

interface FirebaseRepository {
    suspend fun fetchPick(pickID: String): Result<Pick>
    suspend fun fetchPicksInArea(lat: Double, lng: Double, radiusInM: Double): Result<List<Pick>>
    suspend fun fetchPicksInBounds(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Result<List<Pick>>
    suspend fun createPick(pick: Pick): Result<String>
    suspend fun deletePick(pick: Pick): Result<Boolean>
}
