package com.squirtles.domain.usecase.local

import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class GetMyListOrderUseCase @Inject constructor(
    private val localRepository: LocalRepository
) {
    operator fun invoke() = localRepository.myListOrder
}
