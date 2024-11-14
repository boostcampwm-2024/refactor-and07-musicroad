package com.squirtles.domain.usecase

import com.squirtles.domain.model.Pick
import com.squirtles.domain.repository.FirebaseRepository
import javax.inject.Inject

class CreatePickUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(pick: Pick): Result<Pick> = firebaseRepository.createPick(pick)
}
