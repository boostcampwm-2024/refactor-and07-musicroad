package com.squirtles.musicroad.search2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squirtles.domain.model.Song
import com.squirtles.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase
) : ViewModel() {

    private val _searchText = MutableStateFlow<String>("")
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

    fun onSearchTextChange(text: String) {
        viewModelScope.launch {
            _searchText.emit(text)
        }
    }
}
