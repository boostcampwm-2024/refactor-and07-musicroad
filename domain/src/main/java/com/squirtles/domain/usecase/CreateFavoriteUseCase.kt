package com.squirtles.domain.usecase

import com.squirtles.domain.repository.FirebaseRepository
import javax.inject.Inject

class CreateFavoriteUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(pickId: String, userId: String) =
        firebaseRepository.createFavorite(pickId, userId)
}
