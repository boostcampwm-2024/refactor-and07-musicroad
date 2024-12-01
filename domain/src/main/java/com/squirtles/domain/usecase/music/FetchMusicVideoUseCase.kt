package com.squirtles.domain.usecase.music

import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song
import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

<<<<<<< HEAD:domain/src/main/java/com/squirtles/domain/usecase/GetMusicVideoUrlUseCase.kt
class GetMusicVideoUseCase @Inject constructor(
=======
class FetchMusicVideoUrlUseCase @Inject constructor(
>>>>>>> develop:domain/src/main/java/com/squirtles/domain/usecase/music/FetchMusicVideoUrlUseCase.kt
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
