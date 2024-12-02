package com.squirtles.musicroad.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.User
import com.squirtles.domain.usecase.local.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _profileUser = MutableStateFlow<User?>(null)
    val profileUser = _profileUser.asStateFlow()

    val currentUser = getCurrentUserUseCase()

    fun getUserById(userId: String) {
        viewModelScope.launch {
            if (userId == currentUser.userId) {
                _profileUser.emit(currentUser)
            } else {
                // TODO ID로 유저 정보 불러오기
                val tmpUser = User("", "", listOf())
                _profileUser.emit(tmpUser)
            }
        }
    }
}

