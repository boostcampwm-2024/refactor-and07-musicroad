package com.squirtles.musicroad.pick

import android.app.Activity
import android.util.Log
import android.util.Size
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.pick.musicplayer.MusicPlayer
import com.squirtles.musicroad.pick.musicplayer.PlayerViewModel
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Dark
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.White

@Composable
fun DetailPickScreen(
    pickId: String,
    onBackClick: () -> Unit,
    pickViewModel: PickViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val userId = ""
    val username = "짱구"
    val isFavorite = false

    val pick by pickViewModel.pick.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        pickViewModel.fetchPick(pickId)
    }

    DetailPickScreen(
        userId = userId,
        userName = username,
        pick = pick,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        playerViewModel = playerViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailPickScreen(
    userId: String,
    userName: String,
    pick: Pick,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    val isMine = userId == pick.createdBy
    val scrollState = rememberScrollState()
    val dynamicBackgroundColor = Color(pick.song.bgColor)
    val dynamicOnBackgroundColor = if (dynamicBackgroundColor.luminance() >= 0.5f) Black else White
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = userName + stringResource(id = R.string.pick_app_bar_title_user),
                        color = dynamicOnBackgroundColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.pick_app_bar_back_description),
                            tint = dynamicOnBackgroundColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        val iconPainter =
                            if (isMine) R.drawable.ic_delete else if (isFavorite) R.drawable.ic_favorite_true else R.drawable.ic_favorite_false
                        val iconDescription =
                            if (isMine) R.string.pick_delete_icon_description else if (isFavorite) R.string.pick_favorite_true_icon_description else R.string.pick_favorite_false_icon_description
                        Icon(
                            painter = painterResource(iconPainter),
                            contentDescription = stringResource(id = iconDescription),
                            tint = dynamicOnBackgroundColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->

        Box(
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SongInfo(
                    song = pick.song,
                    dynamicOnBackgroundColor = dynamicOnBackgroundColor
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
        }
    }
}

@Composable
private fun SongInfo(
    song: Song,
    dynamicOnBackgroundColor: Color
) {
    Text(
        text = song.songName,
        color = dynamicOnBackgroundColor,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    Text(
        text = song.artistName,
        color = dynamicOnBackgroundColor,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun PickInformation(formattedDate: String, favoriteCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (formattedDate.isNotBlank()) {
            Text(text = formattedDate, style = MaterialTheme.typography.titleMedium.copy(Gray))
            Icon(
                painter = painterResource(id = R.drawable.ic_favorite),
                contentDescription = stringResource(R.string.pick_favorite_count_icon_description),
                modifier = Modifier.padding(start = 4.dp),
                tint = Gray
            )
            Text(text = "$favoriteCount", style = MaterialTheme.typography.titleMedium.copy(Gray))
        }
    }
}

@Composable
fun CommentText(
    comment: String,
    scrollState: ScrollState
) {
    Text(
        text = comment,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 30.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Dark)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyLarge.copy(White)
    )
}

@Preview
@Composable
private fun DetailPickScreenPreview() {
    DetailPickScreen(
        userId = "",
        userName = "짱구",
        pick = Pick(
            id = "",
            song = Song(
                id = "",
                songName = "Super Shy",
                artistName = "뉴진스",
                albumName = "NewJeans 'Super Shy' - Single",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af",
                genreNames = listOf("KPop", "R&B", "Rap"),
                bgColor = "#8fc1e2".toColorInt(),
                externalUrl = "",
                previewUrl = ""
            ),
            comment = "강남역 거리는 Super Shy 듣기 좋네요 ^-^!",
            createdAt = "2024.11.02",
            createdBy = "짱구",
            favoriteCount = 100,
            location = LocationPoint(1.0, 1.0),
            musicVideoUrl = "",
        ),
        isFavorite = false,
        onBackClick = {},
        playerViewModel = PlayerViewModel()
    )
}
