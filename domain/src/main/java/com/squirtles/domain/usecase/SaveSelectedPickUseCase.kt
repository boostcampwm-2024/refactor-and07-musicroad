package com.squirtles.domain.usecase

import com.squirtles.domain.model.Pick
import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class SaveSelectedPickUseCase @Inject constructor(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(pick: Pick) = localRepository.saveSelectedPick(pick)
}
