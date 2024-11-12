package com.squirtles.domain.usecase

import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val appleMusicRepository: AppleMusicRepository
) {
    suspend operator fun invoke(searchText: String) = appleMusicRepository.searchSongs(searchText)
}
