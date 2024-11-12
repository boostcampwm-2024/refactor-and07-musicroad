package com.squirtles.data.repository

import com.squirtles.data.exception.AppleMusicException
import com.squirtles.domain.datasource.AppleMusicRemoteDataSource
import com.squirtles.domain.model.MusicVideo
import com.squirtles.domain.model.Song
import com.squirtles.domain.repository.AppleMusicRepository
import javax.inject.Inject

class AppleMusicRepositoryImpl @Inject constructor(
    private val appleMusicDataSource: AppleMusicRemoteDataSource
) : AppleMusicRepository {

    override suspend fun searchSongs(searchText: String): Result<List<Song>> {
        return handleResult(AppleMusicException.NotFoundException()) {
            appleMusicDataSource.searchSongs(searchText).ifEmpty { null }
        }
    }

    override suspend fun searchSongById(songId: String): Result<Song> {
        TODO("Not yet implemented")
    }

    override suspend fun searchMusicVideoById(songId: String): Result<List<MusicVideo>> {
        TODO("Not yet implemented")
    }


    private suspend fun <T> handleResult(
        appleMusicException: AppleMusicException,
        call: suspend () -> T?
    ): Result<T> {
        return runCatching {
            call() ?: throw appleMusicException
        }
    }

    private suspend fun <T> handleResult(
        call: suspend () -> T
    ): Result<T> {
        return runCatching {
            call()
        }
    }
}
