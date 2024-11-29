package com.squirtles.domain.usecase

import com.squirtles.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(userId: String) = firebaseRepository.getFavorites(userId)
}
