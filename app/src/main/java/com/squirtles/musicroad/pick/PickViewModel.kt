package com.squirtles.musicroad.pick

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.FetchPickUseCase
import com.squirtles.domain.usecase.FetchSelectedPickIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PickViewModel @Inject constructor(
    private val fetchSelectedPickIdUseCase: FetchSelectedPickIdUseCase,
    private val fetchPickUseCase: FetchPickUseCase
) : ViewModel() {

    val selectedPick: StateFlow<Pick> = fetchSelectedPickIdUseCase()
        .map {
            it ?: DEFAULT_PICK
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DEFAULT_PICK
        )

//    fun fetchPick(pickId: String) {
//        viewModelScope.launch {
//            fetchPickUseCase(pickId)
//                .onSuccess { _pick.emit(it) }
//                .onFailure { _pick.emit(DEFAULT_PICK) }
//        }
//    }

    companion object {
        val DEFAULT_PICK =
            Pick(
                id = "",
                song = Song(
                    id = "",
                    songName = "",
                    artistName = "",
                    albumName = "",
                    imageUrl = "",
                    genreNames = listOf(),
                    bgColor = "#000000".toColorInt(),
                    externalUrl = "",
                    previewUrl = ""
                ),
                comment = "",
                createdAt = "",
                createdBy = "",
                favoriteCount = 0,
                location = LocationPoint(1.0, 1.0),
                musicVideoUrl = "",
            )
    }
}
