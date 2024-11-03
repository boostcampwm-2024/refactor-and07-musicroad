package com.squirtles.musicroad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.usecase.GetTempUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempViewModel @Inject constructor(
    private val getTempUseCase: GetTempUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            getTempUseCase()
        }
    }
}