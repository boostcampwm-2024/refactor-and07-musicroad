package com.squirtles.domain.usecase

import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

class SearchMusicVideoUseCase @Inject constructor(
    private val appleMusicRepository: AppleMusicRepository
) {
    suspend operator fun invoke(songId: String) = appleMusicRepository.searchMusicVideoById(songId)
}
