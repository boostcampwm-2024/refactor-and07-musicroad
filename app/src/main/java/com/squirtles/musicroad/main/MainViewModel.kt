package com.squirtles.musicroad.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.data.exception.FirebaseException
import com.squirtles.domain.model.User
import com.squirtles.domain.usecase.SetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val setUserUseCase: SetUserUseCase
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)  // 에러 상태를 관리
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                setUserUseCase()
                    .catch { throw it }
                    .collect { currentUser ->
                        Log.d("MainViewModel", "현재 유저 $currentUser")
                        _user.emit(currentUser)
                    }
            } catch (e: Exception) {
                _error.emit(e)
            }
        }
    }
}
