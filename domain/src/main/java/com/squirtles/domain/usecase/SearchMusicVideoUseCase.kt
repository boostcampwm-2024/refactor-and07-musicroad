package com.squirtles.domain.usecase

import com.squirtles.domain.model.Song
import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

class GetMusicVideoUrlUseCase @Inject constructor(
    private val appleMusicRepository: AppleMusicRepository
) {
    suspend operator fun invoke(song: Song): String {
        val keyword = song.songName + "-" + song.artistName
        appleMusicRepository.searchMusicVideos(keyword)
        return ""
    }
}
