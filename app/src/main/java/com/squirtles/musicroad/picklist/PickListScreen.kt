package com.squirtles.musicroad.picklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import com.squirtles.musicroad.R
import com.squirtles.musicroad.common.Constants.COLOR_STOPS
import com.squirtles.musicroad.common.Constants.DEFAULT_PADDING
import com.squirtles.musicroad.common.DefaultTopAppBar
import com.squirtles.musicroad.common.TotalCountText
import com.squirtles.musicroad.ui.theme.Primary
import com.squirtles.musicroad.ui.theme.White

@Composable
fun PickListScreen(
    isFavoritePicks: Boolean,
    onBackClick: () -> Unit,
    onItemClick: (String) -> Unit,
    pickListViewModel: PickListViewModel = hiltViewModel()
) {
    val uiState by pickListViewModel.pickListUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (isFavoritePicks) {
            pickListViewModel.getFavoritePicks()
        } else {
            // TODO: 내가 등록한 픽 불러오기
        }
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(
                    if (isFavoritePicks) R.string.favorite_picks_top_app_bar_title else R.string.my_picks_top_app_bar_title
                ),
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = COLOR_STOPS))
                .padding(innerPadding)
        ) {
            when (uiState) {
                PickListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        indicatorColor = Primary
                    )
                }

                is PickListUiState.Success -> {
                    val pickList = (uiState as PickListUiState.Success).pickList

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TotalCountText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DEFAULT_PADDING),
                            totalCount = pickList.size,
                            defaultColor = White,
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = pickList,
                                key = { it.id }
                            ) { pick ->
                                PickItem(
                                    song = pick.song,
                                    createUserName = pick.createdBy.userName,
                                    favoriteCount = pick.favoriteCount,
                                    comment = pick.comment,
                                    onItemClick = { onItemClick(pick.id) }
                                )
                            }
                        }
                    }
                }

                PickListUiState.Error -> {
                    Text(
                        text = "일시적인 오류가 발생했습니다.",
                        modifier = Modifier.align(Alignment.Center),
                        color = White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // TODO: 다시하기 버튼 같은 거 만들어서 요청 다시 하게 할 수 있도록 만드는 것도 고려해보기
                }
            }
        }
    }
}
