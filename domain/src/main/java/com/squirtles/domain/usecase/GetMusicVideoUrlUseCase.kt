package com.squirtles.domain.usecase

import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song
import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

class GetMusicVideoUseCase @Inject constructor(
    private val appleMusicRepository: AppleMusicRepository
) {
    suspend operator fun invoke(song: Song): MusicVideo? {
        val keyword = "${song.songName}-${song.artistName}"
        appleMusicRepository.searchMusicVideos(keyword).onSuccess { musicVideos ->
            return musicVideos.find {
                it.artistName == song.artistName && it.songName in song.songName
            }
        }
        return null
    }
}
