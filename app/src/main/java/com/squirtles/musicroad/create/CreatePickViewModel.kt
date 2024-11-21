package com.squirtles.musicroad.create

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.CreatePickUseCase
import com.squirtles.domain.usecase.FetchLastLocationUseCase
import com.squirtles.domain.usecase.GetMusicVideoUrlUseCase
import com.squirtles.domain.usecase.SearchSongsUseCase
import com.squirtles.musicroad.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CreatePickViewModel @Inject constructor(
    fetchLastLocationUseCase: FetchLastLocationUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val getMusicVideoUrlUseCase: GetMusicVideoUrlUseCase,
    private val createPickUseCase: CreatePickUseCase
) : ViewModel() {

    // SearchMusicScreen
    private val _searchUiState = MutableStateFlow<UiState<List<Song>>>(UiState.Init)
    val searchUiState = _searchUiState.asStateFlow()

    private var _selectedSong: Song? = null
    val selectedSong get() = _selectedSong

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private var searchResult: List<Song>? = null
    private var searchJob: Job? = null

    // CreatePickScreen
    private val _comment = MutableStateFlow("")
    val comment get() = _comment

    private var lastLocation: Location? = null

    init {
        // 데이터소스의 위치값을 계속 collect하며 curLocation 변수에 저장
        viewModelScope.launch {
            fetchLastLocationUseCase().collect { location ->
                lastLocation = location
            }
        }

        viewModelScope.launch {
            _searchText
                .debounce(300)
                .collect { searchKeyword ->
                    searchJob?.cancel()
                    if (searchKeyword.isBlank()) {
                        searchResult = null
                        _searchUiState.value = UiState.Init
                    } else {
                        searchJob = launch { searchSongs(searchKeyword) }
                    }
                }
        }
    }

    fun searchSongs() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchSongs(_searchText.value)
        }
    }

    private suspend fun searchSongs(searchKeyword: String) {
        _searchUiState.value = UiState.Loading(searchResult)
        val result = searchSongsUseCase(searchKeyword)

        result.onSuccess {
            searchResult = it
            _searchUiState.value = UiState.Success(it)
        }.onFailure {
            searchResult = null
            _searchUiState.value = UiState.Error // NotFoundException(message=No such resource)
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
        _searchText.value = text
    }

    fun createPick(
        onSuccess: (String) -> Unit
    ) {
        _selectedSong?.let { song ->
            viewModelScope.launch {
                val musicVideoUrl = getMusicVideoUrlUseCase(song)

                if (lastLocation == null) {
                    /* TODO: DEFAULT 인 경우 -> LocalDataSource 위치 데이터 못 불러옴 */
                    return@launch
                }

                /* 등록 결과 - pick ID 담긴 Result */
                /* FIXME : createdBy 임시값 */
                val createResult = createPickUseCase(
                    Pick(
                        id = "",
                        song = song,
                        comment = _comment.value,
                        createdAt = "",
                        createdBy = "",
                        location = LocationPoint(lastLocation!!.latitude, lastLocation!!.longitude),
                        musicVideoUrl = musicVideoUrl
                    )
                )

                createResult.onSuccess { pickId ->
                    onSuccess(pickId)
                }.onFailure {
                    /* TODO: Firestore 등록 실패처리 */
                    Log.d("CreatePickViewModel", createResult.exceptionOrNull()?.message.toString())
                }
            }
        }
    }
}
