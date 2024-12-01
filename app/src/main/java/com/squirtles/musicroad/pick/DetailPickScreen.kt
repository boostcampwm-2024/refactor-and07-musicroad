package com.squirtles.musicroad.pick

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import com.squirtles.musicroad.musicplayer.PlayerViewModel
import com.squirtles.musicroad.pick.PickViewModel.Companion.DEFAULT_PICK
import com.squirtles.musicroad.pick.components.CircleAlbumCover
import com.squirtles.musicroad.pick.components.CommentText
import com.squirtles.musicroad.pick.components.DeletePickDialog
import com.squirtles.musicroad.pick.components.DetailPickTopAppBar
import com.squirtles.musicroad.pick.components.MusicVideoKnob
import com.squirtles.musicroad.pick.components.PickInformation
import com.squirtles.musicroad.pick.components.SongInfo
import com.squirtles.musicroad.pick.components.music.MusicPlayer
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.White
import com.squirtles.musicroad.videoplayer.MusicVideoScreen
import com.squirtles.musicroad.videoplayer.VideoPlayerViewModel
import kotlin.math.absoluteValue

@Composable
fun DetailPickScreen(
    pickId: String,
    onBackClick: () -> Unit,
    onDeleted: (Context) -> Unit,
    pickViewModel: PickViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isFavorite = false
    val uiState by pickViewModel.detailPickUiState.collectAsStateWithLifecycle()
    var showDeletePickDialog by rememberSaveable { mutableStateOf(false) }

    var isMusicVideoAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        pickViewModel.fetchPick(pickId)
    }

    when (uiState) {
        DetailPickUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DetailPickUiState.Success -> {
            val pick = (uiState as DetailPickUiState.Success).pick
            val isCreatedBySelf = pickViewModel.getUserId() == pick.createdBy.userId
            val onActionClick: () -> Unit = {
                when {
                    isCreatedBySelf -> {
                        playerViewModel.pause()
                        showDeletePickDialog = true
                    }

                    isFavorite -> {
                        // TODO: 픽 담기 해제
                    }

                    else -> {
                        // TODO: 픽 담기
                    }
                }
            }

            val pagerState = rememberPagerState(
                pageCount = { if (isMusicVideoAvailable) 2 else 1 }
            )

            // 비디오 플레이어 설정
            LaunchedEffect(pick) {
                isMusicVideoAvailable = pick.musicVideoUrl.isNotEmpty()
            }

            LaunchedEffect(pagerState) {
                pagerState.scrollToPage(page = pickViewModel.currentTab)
            }

            DisposableEffect(Unit) {
                onDispose {
                    pickViewModel.setCurrentTab(pagerState.currentPage)
                }
            }

            HorizontalPager(
                state = pagerState
            ) { page ->
                when (page) {
                    DETAIL_PICK_TAB -> {
                        DetailPick(
                            pick = pick,
                            isCreatedBySelf = isCreatedBySelf,
                            isFavorite = isFavorite, // TODO
                            userName = pick.createdBy.userName,
                            isMusicVideoAvailable = isMusicVideoAvailable,
                            playerViewModel = playerViewModel,
                            onBackClick = onBackClick,
                            onActionClick = onActionClick,
                        )
                    }

                    MUSIC_VIDEO_TAB -> {
                        MusicVideoScreen(
                            pick = pick,
                            modifier = Modifier
                                .background(Black)
                                .graphicsLayer {
                                    val pageOffset = (
                                            (pagerState.currentPage - page) + pagerState
                                                .currentPageOffsetFraction
                                            ).absoluteValue
                                    alpha = lerp(
                                        start = 0.5f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                    )
                                },
                            onBackClick = onBackClick,
                        )
                    }
                }

                // 페이지 전환에 따른 음원과 뮤비 재생 상태
                if (page != DETAIL_PICK_TAB) playerViewModel.pause()
            }
        }

        DetailPickUiState.Deleted -> {
            LaunchedEffect(Unit) {
                onBackClick()
                onDeleted(context)
                Toast.makeText(
                    context,
                    context.getString(R.string.success_delete_pick),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        DetailPickUiState.Error -> {
            // TODO: pick 로딩 실패
        }
    }

    if (showDeletePickDialog) {
        DeletePickDialog(
            onDismissRequest = {
                showDeletePickDialog = false
            },
            onDeletion = {
                showDeletePickDialog = false
                pickViewModel.deletePick(pickId)
            }
        )
    }
}

@Composable
private fun DetailPick(
    pick: Pick,
    isCreatedBySelf: Boolean,
    isFavorite: Boolean,
    userName: String,
    isMusicVideoAvailable: Boolean,
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dynamicBackgroundColor = Color(pick.song.bgColor)
    val onDynamicBackgroundColor = if (dynamicBackgroundColor.luminance() >= 0.5f) Black else White
    val view = LocalView.current
    val context = LocalContext.current

    val audioEffectColor = dynamicBackgroundColor.copy(
        red = (dynamicBackgroundColor.red + 0.2f).coerceAtMost(1.0f),
        green = (dynamicBackgroundColor.green + 0.2f).coerceAtMost(1.0f),
        blue = (dynamicBackgroundColor.blue + 0.2f).coerceAtMost(1.0f),
    )

    // PlayerViewModel Collect
    val audioSessionId by playerViewModel.audioSessionId.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val bufferPercentage by playerViewModel.bufferPercentage.collectAsStateWithLifecycle()
    val duration by playerViewModel.duration.collectAsStateWithLifecycle()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            val isLightStatusBar = dynamicBackgroundColor.luminance() >= 0.5f

            windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
        }
    }

    Scaffold(
        topBar = {
            DetailPickTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                isCreatedBySelf = isCreatedBySelf,
                isFavorite = isFavorite,
                userName = userName,
                onDynamicBackgroundColor = onDynamicBackgroundColor,
                onBackClick = onBackClick,
                onActionClick = { onActionClick() }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to dynamicBackgroundColor,
                            0.47f to Black
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SongInfo(
                    song = pick.song,
                    dynamicOnBackgroundColor = onDynamicBackgroundColor
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircleAlbumCover(
                        modifier = Modifier
                            .size(320.dp)
                            .align(Alignment.Center),
                        song = pick.song,
                        playerState = playerState,
                        duration = duration,
                        audioSessionId = audioSessionId,
                        audioEffectColor = audioEffectColor,
                        onSeekChanged = { timeMs ->
                            playerViewModel.playerSeekTo(timeMs)
                        },
                    )

                    if (isMusicVideoAvailable) {
                        MusicVideoKnob(
                            thumbnail = pick.musicVideoThumbnailUrl,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                PickInformation(formattedDate = pick.createdAt, favoriteCount = pick.favoriteCount)

                CommentText(
                    comment = pick.comment,
                    scrollState = scrollState
                )

                if (pick.song.previewUrl.isBlank().not()) {
                    MusicPlayer(
                        previewUrl = pick.song.previewUrl,
                        playerState = playerState,
                        duration = duration,
                        bufferPercentage = bufferPercentage,
                        readyPlayer = { sourceUrl ->
                            playerViewModel.readyPlayer(context, sourceUrl)
                        },
                        onSeekChanged = { timeMs ->
                            playerViewModel.playerSeekTo(timeMs)
                        },
                        onReplayForwardClick = { replaySec ->
                            playerViewModel.replayForward(replaySec)
                        },
                        onPauseToggle = {
                            playerViewModel.togglePlayPause()
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DetailPickPreview() {
    DetailPick(
        pick = DEFAULT_PICK,
        isCreatedBySelf = false,
        isFavorite = false,
        userName = "짱구",
        isMusicVideoAvailable = true,
        playerViewModel = PlayerViewModel(),
        onBackClick = {},
        onActionClick = {}
    )
}

private const val DETAIL_PICK_TAB = 0
private const val MUSIC_VIDEO_TAB = 1
