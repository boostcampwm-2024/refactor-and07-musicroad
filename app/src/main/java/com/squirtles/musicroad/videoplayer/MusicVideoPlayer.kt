package com.squirtles.musicroad.videoplayer

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun MusicVideoPlayer(
    videoPlayerViewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val player = remember { videoPlayerViewModel.player }
    val textureView = remember { TextureView(context) }

    AndroidView(
        factory = {
            textureView.apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        val surface = Surface(surfaceTexture)
                        player?.setVideoSurface(surface)
                        setVideoSize(width, height, textureView)
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                        // TODO
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
    // 영상을 화면 크기에 맞게 확대
    val matrix = Matrix()
    val scaleFactor = height.toFloat() / width.toFloat()
    matrix.setScale(scaleFactor, 1f)

    // 영상 중앙 정렬
    val translateX = (width - width * scaleFactor) / 2f
    matrix.postTranslate(translateX, 0f)
    textureView.setTransform(matrix)
}
