package com.squirtles.domain.usecase

import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class FetchLocationUseCase @Inject constructor(
    private val localRepository: LocalRepository
) {
    fun invoke() = localRepository.currentLocation
}
