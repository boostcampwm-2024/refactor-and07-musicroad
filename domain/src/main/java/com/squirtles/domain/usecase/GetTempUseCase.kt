package com.squirtles.domain.usecase

import com.squirtles.domain.repository.TempRepository
import javax.inject.Inject

class GetTempUseCase @Inject constructor(
    private val tempRepository: TempRepository
) {
    suspend operator fun invoke() =
        tempRepository.getTemp()
}