package com.squirtles.musicroad.profile

import androidx.lifecycle.ViewModel
import com.squirtles.domain.usecase.local.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    fun getUser() = getCurrentUserUseCase()

}

