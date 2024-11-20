package com.squirtles.domain.usecase

import com.squirtles.domain.repository.FirebaseRepository
import javax.inject.Inject

class FetchPicksInBoundsUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(lat1: Double, lng1: Double, lat2: Double, lng2: Double) =
        firebaseRepository.fetchPicksInBounds(lat1, lng1, lat2, lng2)
}