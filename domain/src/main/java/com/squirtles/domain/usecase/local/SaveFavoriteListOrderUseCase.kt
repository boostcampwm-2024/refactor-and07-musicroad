package com.squirtles.domain.usecase.local

import com.squirtles.domain.model.Order
import com.squirtles.domain.repository.LocalRepository
import javax.inject.Inject

class SaveFavoriteListOrderUseCase @Inject constructor(
    private val localRepository: LocalRepository
) {
    suspend operator fun invoke(order: Order) = localRepository.saveFavoriteListOrder(order)
}
