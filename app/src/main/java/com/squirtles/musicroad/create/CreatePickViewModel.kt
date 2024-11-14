package com.squirtles.musicroad.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.PickLocation
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.CreatePickUseCase
import com.squirtles.domain.usecase.SearchMusicVideoUseCase
import com.squirtles.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePickViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val searchMusicVideoUseCase: SearchMusicVideoUseCase,
    private val createPickUseCase: CreatePickUseCase
) : ViewModel() {

    private var _selectedSong: Song? = null
    val selectedSong get() = _selectedSong

    private val _comment = MutableStateFlow("")
    val comment get() = _comment

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchResult = MutableStateFlow<List<Song>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    private val _isSearching = MutableSharedFlow<Boolean>()
    val isSearching = _isSearching.asSharedFlow()

    fun searchSongs() {
        viewModelScope.launch {
            val result = searchSongsUseCase(_searchText.value)
            if (result.isSuccess) {
                _searchResult.value = result.getOrElse { emptyList() }
                Log.d("SearchViewModel", _searchResult.value.toString())
            } else {
                _searchResult.value = emptyList()
                Log.d("SearchViewModel", _searchResult.value.toString())
            }
        }
    }

    fun onSongItemClick(song: Song) {
        _selectedSong = song
    }

    fun onCommentChange(text: String) {
        _comment.value = text
    }

    fun resetComment() {
        _comment.value = ""
    }

    fun onSearchTextChange(text: String) {
        viewModelScope.launch {
            _searchText.emit(text)
            _isSearching.emit(text.isNotEmpty())
        }
    }

    fun createPick() {
        if (_selectedSong == null) return
        val song = selectedSong!!

        viewModelScope.launch {
            val musicVideoUrl = searchMusicVideoById(song.id)

            /* 등록 결과 - pick ID 담긴 Result */
            /* FIXME : createdBy, location 임시값 */
            val createResult = createPickUseCase(
                Pick(
                    id = "",
                    song = song,
                    comment = _comment.value,
                    createdAt = "",
                    createdBy = "",
                    location = PickLocation(37.380324, 127.115282),
                    musicVideoUrl = musicVideoUrl
                )
            )

            if (createResult.isSuccess) {
                /* TODO: 성공처리 */
                Log.d("CreatePickViewModel", createResult.toString())
            } else {
                /* TODO: 실패처리 */
                Log.d("CreatePickViewModel", createResult.exceptionOrNull()?.message.toString())
            }
        }
    }

    private suspend fun searchMusicVideoById(songId: String): String {
        val result = searchMusicVideoUseCase(songId)

        val musicVideoUrl = result.getOrElse { emptyList() }
            .sortedBy { it.releaseDate }

        return musicVideoUrl.firstOrNull()?.previewUrl.orEmpty()  // 결과가 없으면 빈 문자열 반환
    }
}
