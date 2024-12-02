package com.squirtles.musicroad.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.User
import com.squirtles.domain.usecase.local.GetCurrentUserUseCase
import com.squirtles.domain.usecase.user.FetchUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val fetchUserByIdUseCase: FetchUserByIdUseCase
) : ViewModel() {

    private val _profileUser = MutableStateFlow(DEFAULT_USER)
    val profileUser = _profileUser.asStateFlow()

    val currentUser = getCurrentUserUseCase()

    fun getUserById(userId: String) {
        viewModelScope.launch {
            if (userId == currentUser.userId) {
                _profileUser.emit(currentUser)
            } else {
                val otherProfileUsr = fetchUserByIdUseCase(userId).getOrDefault(DEFAULT_USER)
                _profileUser.emit(otherProfileUsr)
            }
        }
    }
}

val DEFAULT_USER = User("", "", listOf())

