package com.squirtles.musicroad.pick

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun MusicVideoScreen(
    videoUri: String,
    isPlaying: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
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
                    @OptIn(UnstableApi::class)
                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        val surface = Surface(surfaceTexture)
                        val mediaItem = MediaItem.fromUri(videoUri)
                        player.setVideoSurface(surface)
                        player.setMediaItem(mediaItem)
                        player.prepare()

                        // 영상을 화면 크기에 맞게 확대
                        val matrix = Matrix()
                        val scaleFactor = height.toFloat() / width.toFloat()
                        matrix.setScale(scaleFactor, 1f)

                        // 영상 중앙 정렬
                        val translateX = (width - width * scaleFactor) / 2f
                        matrix.postTranslate(translateX, 0f)
                        textureView.setTransform(matrix)

                    }

                    override fun onSurfaceTextureSizeChanged(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) = Unit

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        player.setVideoSurface(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                }
            }
        },
        modifier = modifier
    ) {
        if (isPlaying) player.play()
        else player.pause()
    }
}
