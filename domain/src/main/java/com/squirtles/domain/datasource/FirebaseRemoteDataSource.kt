package com.squirtles.domain.datasource

import com.squirtles.domain.model.Pick

interface FirebaseRemoteDataSource {
    suspend fun fetchPick(pickID: String): Pick?
    suspend fun fetchPicksInArea(lat: Double, lng: Double, radiusInM: Double): List<Pick>
    suspend fun fetchPicksInBounds(lat1: Double, lng1: Double, lat2: Double, lng2: Double): List<Pick>
    suspend fun createPick(pick: Pick): String
    suspend fun deletePick(pick: Pick): Boolean
//    suspend fun updatePick(pick: Pick)
}
