package com.squirtles.musicroad.pick

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import com.squirtles.musicroad.musicplayer.PlayerViewModel
import com.squirtles.musicroad.pick.PickViewModel.Companion.DEFAULT_PICK
import com.squirtles.musicroad.pick.components.CircleAlbumCover
import com.squirtles.musicroad.pick.components.CommentText
import com.squirtles.musicroad.pick.components.DeletePickDialog
import com.squirtles.musicroad.pick.components.DetailPickTopAppBar
import com.squirtles.musicroad.pick.components.PickInformation
import com.squirtles.musicroad.pick.components.SongInfo
import com.squirtles.musicroad.pick.components.SwipeUpIcon
import com.squirtles.musicroad.pick.components.music.MusicPlayer
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.White
import com.squirtles.musicroad.videoplayer.MusicVideoScreen
import com.squirtles.musicroad.videoplayer.VideoPlayerViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalWearMaterialApi::class)
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
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeight.toPx() }
    val statusBarHeight = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this) }
    val contentHeightPx = screenHeightPx + statusBarHeight

    val initialOffset by videoPlayerViewModel.swipeState.collectAsStateWithLifecycle()
    val swipeableState =
        rememberSwipeableState(initialValue = if (initialOffset != 0f) contentHeightPx else 0f)
    val anchors = mapOf(contentHeightPx to 0f, 0f to 1f)
    val swipeableModifier = Modifier.swipeable(
        state = swipeableState,
        anchors = anchors,
        thresholds = { _, _ -> FractionalThreshold(0.3f) },
        orientation = Orientation.Vertical
    )

    val uiState by pickViewModel.detailPickUiState.collectAsStateWithLifecycle()
    var showDeletePickDialog by rememberSaveable { mutableStateOf(false) }
    var showProcessIndicator by rememberSaveable { mutableStateOf(false) }

    val swipePlayState by videoPlayerViewModel.swipePlayState.collectAsStateWithLifecycle(false)
    var isMusicVideoAvailable by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            videoPlayerViewModel.updateSwipeState(swipeableState.offset.value)
        }
    }

    LaunchedEffect(Unit) {
        pickViewModel.fetchPick(pickId)
    }

    BackHandler {
        if (showProcessIndicator.not()) {
            onBackClick()
        }
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
            val isFavorite = (uiState as DetailPickUiState.Success).isFavorite

            // 비디오 플레이어 설정
            LaunchedEffect(pick) {
                isMusicVideoAvailable = pick.musicVideoUrl.isNotEmpty()

                if (isMusicVideoAvailable) {
                    videoPlayerViewModel.initializePlayer(context, pick.musicVideoUrl)
                }
            }

            val isCreatedBySelf = pickViewModel.getUserId() == pick.createdBy.userId
            val onActionClick: () -> Unit = {
                when {
                    isCreatedBySelf -> {
                        playerViewModel.pause()
                        showDeletePickDialog = true
                    }

                    isFavorite -> {
                        showProcessIndicator = true
                        pickViewModel.deleteAtFavorite(pickId) {
                            showProcessIndicator = false
                            context.showShortToast(context.getString(R.string.success_delete_at_favorite))
                        }
                    }

                    else -> {
                        showProcessIndicator = true
                        pickViewModel.addToFavorite(pickId) {
                            showProcessIndicator = false
                            context.showShortToast(context.getString(R.string.success_add_to_favorite))
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                DetailPick(
                    pick = pick,
                    isCreatedBySelf = isCreatedBySelf,
                    isFavorite = isFavorite, // TODO
                    userName = pick.createdBy.userName,
                    isMusicVideoAvailable = isMusicVideoAvailable,
                    swipeableModifier = swipeableModifier,
                    playerViewModel = playerViewModel,
                    onBackClick = onBackClick,
                    onActionClick = onActionClick
                )

                // 최초 Swipe 동작 전에 MusicVideoScreen이 생성되지 않도록 함
                if (swipeableState.offset.value != 0.0f && contentHeightPx != swipeableState.offset.value) {
                    videoPlayerViewModel.setShowMusicVideo(true)
                }

                if (isMusicVideoAvailable && videoPlayerViewModel.showMusicVideo) {
                    videoPlayerViewModel.setSwipePlayState(swipeableState.offset.value < contentHeightPx * 0.9f)
                    val alpha =
                        (1 - (swipeableState.offset.value / contentHeightPx)).coerceIn(0f, 1f)

                    MusicVideoScreen(
                        pick = pick,
                        swipePlayState = swipePlayState,
                        modifier = swipeableModifier
                            .fillMaxSize()
                            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                            .graphicsLayer { this.alpha = alpha },
                        onBackClick = onBackClick
                    )

                    LaunchedEffect(swipePlayState) {
                        if (swipePlayState) playerViewModel.pause()
                        else {
                            videoPlayerViewModel.setShowMusicVideo(false)

                            // 비디오 플레이어 초기화
                            videoPlayerViewModel.initializePlayer(context, pick.musicVideoUrl)
                        }
                    }
                }
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
            // TODO: pick 로딩 실패 or 담기 실패, 두 상태를 나눠야할지 고민
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

    if (showProcessIndicator) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black.copy(alpha = 0.5F))
                .clickable( // 클릭 효과 제거 및 클릭 이벤트 무시
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DetailPick(
    pick: Pick,
    isCreatedBySelf: Boolean,
    isFavorite: Boolean,
    userName: String,
    isMusicVideoAvailable: Boolean,
    swipeableModifier: Modifier,
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
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

                if (playerState.isReady) {
                    CircleAlbumCover(
                        modifier = Modifier
                            .size(320.dp)
                            .align(Alignment.CenterHorizontally),
                        song = pick.song,
                        playerState = playerState,
                        duration = duration,
                        audioEffectColor = audioEffectColor,
                        audioSessionId = { playerViewModel.audioSessionId },
                        onSeekChanged = { timeMs ->
                            playerViewModel.playerSeekTo(timeMs)
                        },
                    )
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

            if (isMusicVideoAvailable) {
                SwipeUpIcon(
                    swipeableModifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )
            }
        }
    }
}

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        swipeableModifier = Modifier,
        playerViewModel = PlayerViewModel(),
        onBackClick = {},
        onActionClick = {}
    )
}
