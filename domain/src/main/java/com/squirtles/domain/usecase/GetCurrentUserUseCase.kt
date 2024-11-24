package com.squirtles.domain.usecase

import com.squirtles.domain.model.User
import com.squirtles.domain.repository.FirebaseRepository
import com.squirtles.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke(): Flow<User> = flow {
        // userId 확인
        val userId = localRepository.userId.firstOrNull()

        if (userId == null) {
            // userId가 없으면 새 유저 생성
            val createdUser = firebaseRepository.createUser().getOrThrow()

            // 생성된 유저의 userId 저장 후 user 반환
            localRepository.saveUserId(createdUser.userId)
            emit(createdUser)
        } else {
            // userId가 있으면 Firestore에서 유저 가져오기
            val user = firebaseRepository.fetchUser(userId).getOrThrow()
            emit(user)
        }
    }
}