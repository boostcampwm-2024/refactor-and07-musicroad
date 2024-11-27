package com.squirtles.musicroad.videoplayer

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getString
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.squirtles.musicroad.R

@Composable
fun MusicVideoPlayer(
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

                        player.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_ENDED) {
                                    Toast.makeText(context, getString(context, R.string.video_player_ended_message), Toast.LENGTH_SHORT).show()
                                    playerPlayState.value = false
                                    player.seekTo(0)
                                }
                            }
                        })
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
