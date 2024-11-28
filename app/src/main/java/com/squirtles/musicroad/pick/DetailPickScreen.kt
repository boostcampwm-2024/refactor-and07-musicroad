package com.squirtles.musicroad.pick

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
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
import coil3.compose.AsyncImage
import com.squirtles.domain.model.Pick
import com.squirtles.musicroad.R
import com.squirtles.musicroad.musicplayer.PlayerViewModel
import com.squirtles.musicroad.pick.PickViewModel.Companion.DEFAULT_PICK
import com.squirtles.musicroad.pick.components.CommentText
import com.squirtles.musicroad.pick.components.DeletePickDialog
import com.squirtles.musicroad.pick.components.DetailPickTopAppBar
import com.squirtles.musicroad.pick.components.PickInformation
import com.squirtles.musicroad.pick.components.SongInfo
import com.squirtles.musicroad.pick.components.SwipeUpIcon
import com.squirtles.musicroad.pick.components.music.MusicPlayer
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.White
import kotlin.math.roundToInt

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun DetailPickScreen(
    pickId: String,
    onBackClick: () -> Unit,
    onDeleted: (Context) -> Unit,
    pickViewModel: PickViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeight.toPx() }
    val statusBarHeight = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this) }
    val contentHeightPx = screenHeightPx + statusBarHeight
    val anchors = mapOf(contentHeightPx to 0, 0f to 1)
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val swipeableModifier = Modifier.swipeable(
        state = swipeableState,
        anchors = anchors,
        thresholds = { _, _ -> FractionalThreshold(0.3f) },
        orientation = Orientation.Vertical
    )

    val isFavorite = false
    val uiState by pickViewModel.detailPickUiState.collectAsStateWithLifecycle()
    var isMusicVideoAvailable by remember { mutableStateOf(false) }
    var showMusicVideo by remember { mutableStateOf(false) }
    var showDeletePickDialog by rememberSaveable { mutableStateOf(false) }

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
            isMusicVideoAvailable = pick.musicVideoUrl.isNotEmpty()

            Box(modifier = Modifier.fillMaxSize()) {
                DetailPick(
                    pick = pick,
                    isCreatedBySelf = pickViewModel.getUserId() == pick.createdBy.userId,
                    isFavorite = isFavorite, // TODO
                    userName = pick.createdBy.userName,
                    isMusicVideoAvailable = isMusicVideoAvailable,
                    swipeableModifier = swipeableModifier,
                    playerViewModel = playerViewModel,
                    onBackClick = onBackClick,
                    onActionClick = {
                        playerViewModel.pause()
                        showDeletePickDialog = true
                    }
                )

                // 최초 Swipe 동작 전에 MusicVideoScreen이 생성되지 않도록 함
                if (swipeableState.offset.value != 0.0f && contentHeightPx != swipeableState.offset.value) {
                    showMusicVideo = true
                }

                if (isMusicVideoAvailable && showMusicVideo) {
                    val isPlaying = swipeableState.offset.value < contentHeightPx * 0.8f
                    val alpha =
                        (1 - (swipeableState.offset.value / contentHeightPx)).coerceIn(0f, 1f)

                    MusicVideoScreen(
                        videoUri = pick.musicVideoUrl,
                        isPlaying = isPlaying,
                        modifier = swipeableModifier
                            .fillMaxSize()
                            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                            .graphicsLayer { this.alpha = alpha }
                    )

                    LaunchedEffect(isPlaying) {
                        if (isPlaying) playerViewModel.pause()
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

                AsyncImage(
                    model = pick.song.getImageUrlWithSize(Size(400, 400)),
                    contentDescription = pick.song.albumName + stringResource(id = R.string.pick_album_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                PickInformation(formattedDate = pick.createdAt, favoriteCount = pick.favoriteCount)

                CommentText(
                    comment = pick.comment,
                    scrollState = scrollState
                )

                if (pick.song.previewUrl.isBlank().not()) {
                    Log.d("DetailPickScreen", "Create Android View Player")
                    MusicPlayer(
                        context = context,
                        previewUrl = pick.song.previewUrl,
                        playerViewModel = playerViewModel
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
