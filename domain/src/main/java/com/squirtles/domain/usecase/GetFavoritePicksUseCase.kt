package com.squirtles.domain.usecase

import com.squirtles.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetFavoritePicksUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(userId: String) = firebaseRepository.getFavoritePicks(userId)
}
