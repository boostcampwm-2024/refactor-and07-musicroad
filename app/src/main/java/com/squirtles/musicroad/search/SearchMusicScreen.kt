package com.squirtles.musicroad.search2

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.White

@Composable
fun SearchMusicScreen() {

    val focusManager = LocalFocusManager.current

    val searchViewModel: SearchViewModel = hiltViewModel()
    val searchText by searchViewModel.searchText.collectAsStateWithLifecycle()
    val isSearching by searchViewModel.isSearching.collectAsStateWithLifecycle(false)

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                SearchBar(
                    keyword = searchText,
                    onValueChange = searchViewModel::onSearchTextChange,
                    active = isSearching,
                    onActiveChange = searchViewModel::searchSongs,
                    focusManager = focusManager
                )
            }
        },
    ) { innerPadding ->
        // 배경색 설정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = colorStops))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .addFocusCleaner(focusManager)
            ) {
                VerticalSpacer(20)

                TextWithColorAndStyle(
                    text = "Result",
                    textColor = White,
                    textStyle = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = DefaultPadding)
                )
                VerticalSpacer(32)

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = DefaultPadding)
                        .padding(bottom = DefaultPadding),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(10) {
                        MusicItem()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    keyword: String,
    onValueChange: (String) -> Unit,
    active: Boolean, // whether the user is searching or not
    onActiveChange: () -> Unit, //the callback to be invoked when this search bar's active state is changed
    focusManager: FocusManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SearchBarHeight)
            .padding(end = DefaultPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { /* TODO: 지도로 돌아가기 */ },
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로 가기 버튼",
                tint = White
            )
        }
        HorizontalSpacer(8)

        OutlinedTextField(
            value = keyword,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("검색")
            },
            trailingIcon = if (active) {
                {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onActiveChange()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_music_search_button_description),
                            tint = White
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = White,
                focusedIndicatorColor = White,
                unfocusedIndicatorColor = White,
                focusedPlaceholderColor = Gray,
                unfocusedPlaceholderColor = White
            )
        )
    }
}

@Composable
private fun MusicItem() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af",
            contentDescription = stringResource(R.string.search_music_album_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(ImageSize)
                .clip(RoundedCornerShape(size = 16.dp))
        )
        HorizontalSpacer(16)
        Column {
            TextWithColorAndStyle(
                text = "Ditto",
                textColor = White,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            TextWithColorAndStyle(
                text = "NewJeans",
                textColor = Gray,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            TextWithColorAndStyle(
                text = "Ditto",
                textColor = Gray,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun HorizontalSpacer(width: Int) = Spacer(Modifier.width(width.dp))

@Composable
fun VerticalSpacer(height: Int) = Spacer(Modifier.height(height.dp))

@Composable
fun TextWithColorAndStyle(
    text: String,
    textColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = textColor,
        style = textStyle
    )
}

fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: () -> Unit = {}): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                doOnClear()
                focusManager.clearFocus()
            }
        )
    }
}


@Preview
@Composable
fun MusicItemPreview() {
    MusicItem()
}

@Preview(apiLevel = 34)
@Composable
fun SearchMusicPreview() {
    SearchMusicScreen()
}

private val colorStops = arrayOf(
    0.0f to Primary,
    0.25f to Black
)
private val SearchBarHeight = 56.dp
private val DefaultPadding = 16.dp
private val ImageSize = 56.dp
