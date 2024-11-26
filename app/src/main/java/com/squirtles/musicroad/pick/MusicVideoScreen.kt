package com.squirtles.musicroad.pick

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.domain.model.Creator
import com.squirtles.domain.model.LocationPoint
import com.squirtles.domain.model.Pick
import com.squirtles.domain.model.Song
import com.squirtles.musicroad.R
import com.squirtles.musicroad.ui.theme.Black
import com.squirtles.musicroad.ui.theme.Gray
import com.squirtles.musicroad.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoScreen(
    pick: Pick,
    swipePlayState: Boolean,
    modifier: Modifier,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    val playerPlayState = remember { mutableStateOf(true) }

    Box(
        modifier = modifier
    ) {
        MusicVideoPlayer(pick.musicVideoUrl, player, swipePlayState, playerPlayState)
        VideoPlayerOverlay(pick, playerPlayState, onBackClick)
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerOverlay(
    pick: Pick,
    playerPlayState: MutableState<Boolean>,
    onBackClick: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(Black.copy(alpha = alpha.value.coerceAtMost(0.5f)))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        coroutineScope.launch {
                            // Fade In
                            alpha.animateTo(1.0f, animationSpec = tween(durationMillis = 300))

                            // 대기
                            delay(3_000L)

                            // Fade Out
                            alpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
                        }
                    }
                )
            }
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = pick.createdBy.userName + stringResource(id = R.string.pick_app_bar_title_user),
                    color = White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    enabled = alpha.value > 0.5f
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.pick_app_bar_back_description),
                        tint = White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        IconButton(
            onClick = { playerPlayState.value = playerPlayState.value.not() },
            modifier = Modifier
                .align(Alignment.Center)
                .background(Black.copy(0.5f), shape = CircleShape)
                .padding(8.dp),
            enabled = alpha.value > 0.5f
        ) {
            Icon(
                imageVector = if (playerPlayState.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(id = R.string.player_play_pause_description),
                modifier = Modifier.size(30.dp),
                tint = White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = pick.song.songName,
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pick.song.artistName,
                color = Gray,
                fontSize = 20.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = pick.comment,
                color = White,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun MusicVideoPlayer(
    videoUri: String,
    player: ExoPlayer,
    swipePlayState: Boolean,
    playerPlayState: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val textureView = remember { TextureView(context) }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        factory = {
            textureView.apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        val surface = Surface(surfaceTexture)
                        val mediaItem = MediaItem.fromUri(videoUri)
                        player.setVideoSurface(surface)
                        player.setMediaItem(mediaItem)
                        player.prepare()

                        setVideoSize(width, height, textureView)
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        // TODO
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        player.setVideoSurface(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                        // TODO
                    }
                }
            }
        }
    ) {
        if (swipePlayState && playerPlayState.value) player.play()
        else player.pause()
    }
}

private fun setVideoSize(width: Int, height: Int, textureView: TextureView) {
    // 영상을 화면 크기에 맞게 확대
    val matrix = Matrix()
    val scaleFactor = height.toFloat() / width.toFloat()
    matrix.setScale(scaleFactor, 1f)

    // 영상 중앙 정렬
    val translateX = (width - width * scaleFactor) / 2f
    matrix.postTranslate(translateX, 0f)
    textureView.setTransform(matrix)
}

@Preview
@Composable
private fun VideoPlayerOverlayPreview() {
    VideoPlayerOverlay(
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
            createdBy = Creator(userId = "", userName = "짱구"),
            favoriteCount = 100,
            location = LocationPoint(1.0, 1.0),
            musicVideoUrl = "",
        ),
        playerPlayState = remember { mutableStateOf(false) },
        onBackClick = {}
    )
}