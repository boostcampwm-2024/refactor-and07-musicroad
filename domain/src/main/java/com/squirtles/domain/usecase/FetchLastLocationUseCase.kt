package com.squirtles.domain.usecase

import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class FetchLastLocationUseCase @Inject constructor(
    private val localRepository: LocalRepository
) {
    operator fun invoke() = localRepository.lastLocation
}
