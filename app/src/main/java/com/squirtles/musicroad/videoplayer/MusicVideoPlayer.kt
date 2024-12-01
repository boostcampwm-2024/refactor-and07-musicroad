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
    val videoSize by videoPlayerViewModel.videoSize.collectAsStateWithLifecycle()
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

    LaunchedEffect(videoSize) {
        videoSize?.let { size ->
            adjustVideoSize(size.width, size.height, textureView.width, textureView.height, textureView)
        }
    }

    AndroidView(
        factory = {
            videoPlayerViewModel.initializePlayer(context, pick)
            textureView.apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        currentSurfaceTexture = surfaceTexture
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

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

fun adjustVideoSize(videoWidth: Int, videoHeight: Int, surfaceWidth: Int, surfaceHeight: Int, textureView: TextureView) {

    // 세로가 더 긴 경우 영상을 화면 크기에 맞게 확대
    if (surfaceHeight > surfaceWidth) {
        val matrix = Matrix()
        val scaleFactor = surfaceHeight.toFloat() / videoHeight.toFloat()
        matrix.setScale(scaleFactor, scaleFactor)

        // 영상 중앙 정렬
        val translateX = (surfaceWidth - surfaceWidth * scaleFactor) / 2f
        matrix.postTranslate(translateX, 0f)

        textureView.setTransform(matrix)
    }
}
