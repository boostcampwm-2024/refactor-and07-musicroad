package com.squirtles.musicroad.videoplayer

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.squirtles.domain.model.Pick

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoPlayer(
    pick: Pick,
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val player by videoPlayerViewModel.player.collectAsStateWithLifecycle()

    val textureView = remember { TextureView(context) }
    var currentSurfaceTexture by remember { mutableStateOf<SurfaceTexture?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            player?.pause()
            videoPlayerViewModel.setLastPosition(player?.currentPosition ?: 0)
            player?.release()
            textureView.surfaceTexture?.release()
            textureView.surfaceTextureListener = null
            videoPlayerViewModel.releasePlayer()
        }
    }

    LaunchedEffect(player, currentSurfaceTexture) {
        if (player != null && currentSurfaceTexture != null) {
            val surface = Surface(currentSurfaceTexture)
            player?.setVideoSurface(surface)
        }
    }

    AndroidView(
        factory = {
            videoPlayerViewModel.initializePlayer(context, pick)
            textureView.apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        currentSurfaceTexture = surfaceTexture
                        setVideoSize(width, height, textureView)
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        setVideoSize(width, height, textureView)
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        player?.setVideoSurface(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                        // TODO
                    }
                }
            }
        }
    )
}

private fun setVideoSize(width: Int, height: Int, textureView: TextureView) {
    // 세로가 더 긴 경우 영상을 화면 크기에 맞게 확대
    if (height > width) {
        val matrix = Matrix()
        val scaleFactor = height.toFloat() / width.toFloat()
        matrix.setScale(scaleFactor, 1f)

        // 영상 중앙 정렬
        val translateX = (width - width * scaleFactor) / 2f
        matrix.postTranslate(translateX, 0f)
        textureView.setTransform(matrix)
    }
}
